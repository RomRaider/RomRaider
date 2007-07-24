/*
 * IzPack - Copyright 2001-2007 Julien Ponge, All Rights Reserved.
 * 
 * http://izpack.org/ http://developer.berlios.de/projects/izpack/
 * 
 * Copyright 2007 Dennis Reil
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.izforge.izpack.installer;

import java.io.BufferedOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Stack;
import java.util.TreeSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.regexp.RE;
import org.apache.regexp.RECompiler;
import org.apache.regexp.RESyntaxException;

import com.izforge.izpack.ExecutableFile;
import com.izforge.izpack.LocaleDatabase;
import com.izforge.izpack.Pack;
import com.izforge.izpack.PackFile;
import com.izforge.izpack.ParsableFile;
import com.izforge.izpack.UpdateCheck;
import com.izforge.izpack.XPackFile;
import com.izforge.izpack.event.InstallerListener;
import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.installer.InstallerFrame;
import com.izforge.izpack.installer.IzPanel;
import com.izforge.izpack.installer.ResourceManager;
import com.izforge.izpack.installer.ScriptParser;
import com.izforge.izpack.installer.UninstallData;
import com.izforge.izpack.io.FileSpanningInputStream;
import com.izforge.izpack.io.FileSpanningOutputStream;
import com.izforge.izpack.io.VolumeNotFoundException;
import com.izforge.izpack.panels.NextMediaDialog;
import com.izforge.izpack.util.AbstractUIHandler;
import com.izforge.izpack.util.AbstractUIProgressHandler;
import com.izforge.izpack.util.Debug;
import com.izforge.izpack.util.FileExecutor;
import com.izforge.izpack.util.IoHelper;
import com.izforge.izpack.util.OsConstraint;
import com.izforge.izpack.util.VariableSubstitutor;

/**
 * Unpacker class.
 * 
 * @author Dennis Reil
 */
public class MultiVolumeUnpacker implements IUnpacker
{

    /** The installdata. */
    private AutomatedInstallData idata;

    /** The installer listener. */
    private AbstractUIProgressHandler handler;

    /** The uninstallation data. */
    private UninstallData udata;

    /** The variables substitutor. */
    private VariableSubstitutor vs;

    /** The instances of the unpacker objects. */
    private static HashMap instances = new HashMap();

    /** The absolute path of the installation. (NOT the canonical!) */
    private File absolute_installpath;

    /** The packs locale database. */
    private LocaleDatabase langpack = null;

    /** Interrupt flag if global interrupt is desired. */
    private static boolean interruptDesired = false;

    /** Do not perform a interrupt call. */
    private static boolean discardInterrupt = false;

    /** The name of the XML file that specifies the panel langpack */
    private static final String LANG_FILE_NAME = "packsLang.xml";

    public static final String ALIVE = "alive";

    public static final String INTERRUPT = "doInterrupt";

    public static final String INTERRUPTED = "interruppted";

    /** The result of the operation. */
    private boolean result = true;

    /**
     * The constructor.
     * 
     * @param idata The installation data.
     * @param handler The installation progress handler.
     */
    public MultiVolumeUnpacker(AutomatedInstallData idata, AbstractUIProgressHandler handler)
    {        
        try
        {
            String resource = LANG_FILE_NAME + "_" + idata.localeISO3;
            this.langpack = new LocaleDatabase(ResourceManager.getInstance().getInputStream(
                    resource));
        }
        catch (Throwable exception)
        {}

        this.idata = idata;
        this.handler = handler;

        // Initialize the variable substitutor
        vs = new VariableSubstitutor(idata.getVariables());
    }

    /**
     * Returns a copy of the active unpacker instances.
     * 
     * @return a copy of active unpacker instances
     */
    public static HashMap getRunningInstances()
    {
        synchronized (instances)
        { // Return a shallow copy to prevent a
            // ConcurrentModificationException.
            return (HashMap) (instances.clone());
        }
    }

    /**
     * Adds this to the map of all existent instances of Unpacker.
     */
    private void addToInstances()
    {
        synchronized (instances)
        {
            instances.put(this, ALIVE);
        }
    }

    /**
     * Removes this from the map of all existent instances of Unpacker.
     */
    private void removeFromInstances()
    {
        synchronized (instances)
        {
            instances.remove(this);
        }
    }

    /**
     * Initiate interrupt of all alive Unpacker. This method does not interrupt the Unpacker objects
     * else it sets only the interrupt flag for the Unpacker objects. The dispatching of interrupt
     * will be performed by the Unpacker objects self.
     */
    private static void setInterruptAll()
    {
        synchronized (instances)
        {
            Iterator iter = instances.keySet().iterator();
            while (iter.hasNext())
            {
                Object key = iter.next();
                if (instances.get(key).equals(ALIVE))
                {
                    instances.put(key, INTERRUPT);
                }
            }
            // Set global flag to allow detection of it in other classes.
            // Do not set it to thread because an exec will then be stoped.
            setInterruptDesired(true);
        }
    }

    /**
     * Initiate interrupt of all alive Unpacker and waits until all Unpacker are interrupted or the
     * wait time has arrived. If the doNotInterrupt flag in InstallerListener is set to true, the
     * interrupt will be discarded.
     * 
     * @param waitTime wait time in millisecounds
     * @return true if the interrupt will be performed, false if the interrupt will be discarded
     */
    public static boolean interruptAll(long waitTime)
    {
        long t0 = System.currentTimeMillis();
        if (isDiscardInterrupt()) return (false);
        setInterruptAll();
        while (!isInterruptReady())
        {
            if (System.currentTimeMillis() - t0 > waitTime) return (true);
            try
            {
                Thread.sleep(100);
            }
            catch (InterruptedException e)
            {}
        }
        return (true);
    }

    private static boolean isInterruptReady()
    {
        synchronized (instances)
        {
            Iterator iter = instances.keySet().iterator();
            while (iter.hasNext())
            {
                Object key = iter.next();
                if (!instances.get(key).equals(INTERRUPTED)) return (false);
            }
            return (true);
        }

    }

    /**
     * Sets the interrupt flag for this Unpacker to INTERRUPTED if the previos state was INTERRUPT
     * or INTERRUPTED and returns whether interrupt was initiate or not.
     * 
     * @return whether interrupt was initiate or not
     */
    private boolean performInterrupted()
    {
        synchronized (instances)
        {
            Object doIt = instances.get(this);
            if (doIt != null && (doIt.equals(INTERRUPT) || doIt.equals(INTERRUPTED)))
            {
                instances.put(this, INTERRUPTED);
                this.result = false;
                return (true);
            }
            return (false);
        }
    }

    /**
     * Returns whether interrupt was initiate or not for this Unpacker.
     * 
     * @return whether interrupt was initiate or not
     */
    private boolean shouldInterrupt()
    {
        synchronized (instances)
        {
            Object doIt = instances.get(this);
            if (doIt != null && (doIt.equals(INTERRUPT) || doIt.equals(INTERRUPTED))) { return (true); }
            return (false);
        }

    }

    protected File enterNextMediaMessage(String volumename)
    {
        Debug.trace("Enter next media: " + volumename);
        StackTraceElement[] el = (new Exception()).getStackTrace();
        for (int i = 0; i < el.length; i++)
        {
            StackTraceElement element = el[i];
            Debug.trace(element.toString());
        }

        File nextvolume = new File(volumename);
        NextMediaDialog nmd = null;
        while (!nextvolume.exists())
        {
            if ((this.handler != null) && (this.handler instanceof IzPanel))
            {
                InstallerFrame installframe = ((IzPanel) this.handler).getInstallerFrame();
                nmd = new NextMediaDialog(installframe, idata, volumename);
            }
            else
            {
                nmd = new NextMediaDialog(null, idata, volumename);
            }
            nmd.setVisible(true);
            String nextmediainput = nmd.getNextMedia();
            if (nextmediainput != null)
            {
                nextvolume = new File(nextmediainput);
            }
            else
            {
                Debug.trace("Input from NextMediaDialog was null");
                nextvolume = new File(volumename);
            }
        }
        return nextvolume;
    }

    /** The run method. */
    public void run()
    {
        addToInstances();
        try
        {
            //
            // Initialisations
            FileOutputStream out = null;
            ArrayList parsables = new ArrayList();
            ArrayList executables = new ArrayList();
            ArrayList updatechecks = new ArrayList();
            List packs = idata.selectedPacks;
            int npacks = packs.size();
            Debug.trace("Unpacker starting");
            handler.startAction("Unpacking", npacks);
            udata = UninstallData.getInstance();
            // Custom action listener stuff --- load listeners ----
            List[] customActions = getCustomActions();
            // Custom action listener stuff --- beforePacks ----
            informListeners(customActions, InstallerListener.BEFORE_PACKS, idata, new Integer(
                    npacks), handler);
            // vs = new VariableSubstitutor(idata.getVariables());
            packs = idata.selectedPacks;
            npacks = packs.size();
            if (npacks == 0)
            {
                if (performInterrupted())
                { // Interrupt was initiated; perform it.
                    return;
                }

                // Custom action listener stuff --- afterPacks ----
                informListeners(customActions, InstallerListener.AFTER_PACKS, idata, handler, null);
                if (performInterrupted())
                { // Interrupt was initiated; perform it.
                    return;
                }

                // The end :-)
                handler.stopAction();
                return;
            }
            InputStream in = MultiVolumeUnpacker.class
                    .getResourceAsStream(FileSpanningOutputStream.VOLUMES_INFO);
            // get volumes metadata
            ObjectInputStream metadataobj = new ObjectInputStream(in);
            // TODO: create MetadataObject
            int volumes = metadataobj.readInt();
            String volumename = metadataobj.readUTF();
            Debug.trace("Reading from " + volumes + " volumes with basename " + volumename + " ");
            metadataobj.close();
            String mediadirectory = MultiVolumeInstaller.getMediadirectory();
            if ((mediadirectory == null) || (mediadirectory.length() <= 0))
            {
                Debug.trace("Mediadirectory wasn't set.");
                mediadirectory = System.getProperty("java.io.tmpdir"); // try the temporary
                // directory
            }
            Debug.trace("Using mediadirectory = " + mediadirectory);
            File volume = new File(mediadirectory + File.separator + volumename);
            if (!volume.exists())
            {
                volume = enterNextMediaMessage(volume.getAbsolutePath());
            }
            FileSpanningInputStream fin = new FileSpanningInputStream(volume, volumes);

            // We unpack the selected packs
            for (int i = 0; i < npacks; i++)
            {
                // We get the pack stream
                int n = idata.allPacks.indexOf(packs.get(i));

                in = MultiVolumeUnpacker.class.getResourceAsStream("/packs/pack" + n);

                // Custom action listener stuff --- beforePack ----
                informListeners(customActions, InstallerListener.BEFORE_PACK, packs.get(i),
                        new Integer(npacks), handler);
                // find next Entry
                ObjectInputStream objIn = new ObjectInputStream(in);
                // We unpack the files
                int nfiles = objIn.readInt();

                // We get the internationalized name of the pack
                final Pack pack = ((Pack) packs.get(i));
                String stepname = pack.name;// the message to be passed to the
                // installpanel
                if (langpack != null && !(pack.id == null || "".equals(pack.id)))
                {

                    final String name = langpack.getString(pack.id);
                    if (name != null && !"".equals(name))
                    {
                        stepname = name;
                    }
                }
                handler.nextStep(stepname, i + 1, nfiles);
                for (int j = 0; j < nfiles; j++)
                {
                    // We read the header
                    XPackFile pf = (XPackFile) objIn.readObject();

                    if (OsConstraint.oneMatchesCurrentSystem(pf.osConstraints()))
                    {
                        // We translate & build the path
                        String path = IoHelper.translatePath(pf.getTargetPath(), vs);
                        File pathFile = new File(path);
                        File dest = pathFile;
                        if (!pf.isDirectory()) dest = pathFile.getParentFile();

                        if (!dest.exists())
                        {
                            // If there are custom actions which would be called
                            // at
                            // creating a directory, create it recursively.
                            List fileListeners = customActions[customActions.length - 1];
                            if (fileListeners != null && fileListeners.size() > 0)
                                mkDirsWithEnhancement(dest, pf, customActions);
                            else
                            // Create it in on step.
                            {
                                if (!dest.mkdirs())
                                {
                                    handler.emitError("Error creating directories",
                                            "Could not create directory\n" + dest.getPath());
                                    handler.stopAction();
                                    this.result = false;
                                    return;
                                }
                            }
                        }

                        if (pf.isDirectory()) continue;

                        // Custom action listener stuff --- beforeFile ----
                        informListeners(customActions, InstallerListener.BEFORE_FILE, pathFile, pf,
                                null);
                        // We add the path to the log,
                        udata.addFile(path);

                        handler.progress(j, path);

                        // if this file exists and should not be overwritten,
                        // check
                        // what to do
                        if ((pathFile.exists()) && (pf.override() != PackFile.OVERRIDE_TRUE))
                        {
                            boolean overwritefile = false;

                            // don't overwrite file if the user said so
                            if (pf.override() != PackFile.OVERRIDE_FALSE)
                            {
                                if (pf.override() == PackFile.OVERRIDE_TRUE)
                                {
                                    overwritefile = true;
                                }
                                else if (pf.override() == PackFile.OVERRIDE_UPDATE)
                                {
                                    // check mtime of involved files
                                    // (this is not 100% perfect, because the
                                    // already existing file might
                                    // still be modified but the new installed
                                    // is just a bit newer; we would
                                    // need the creation time of the existing
                                    // file or record with which mtime
                                    // it was installed...)
                                    overwritefile = (pathFile.lastModified() < pf.lastModified());
                                }
                                else
                                {
                                    int def_choice = -1;

                                    if (pf.override() == PackFile.OVERRIDE_ASK_FALSE)
                                        def_choice = AbstractUIHandler.ANSWER_NO;
                                    if (pf.override() == PackFile.OVERRIDE_ASK_TRUE)
                                        def_choice = AbstractUIHandler.ANSWER_YES;

                                    int answer = handler.askQuestion(idata.langpack
                                            .getString("InstallPanel.overwrite.title")
                                            + " - " + pathFile.getName(), idata.langpack
                                            .getString("InstallPanel.overwrite.question")
                                            + pathFile.getAbsolutePath(),
                                            AbstractUIHandler.CHOICES_YES_NO, def_choice);

                                    overwritefile = (answer == AbstractUIHandler.ANSWER_YES);
                                }

                            }

                            if (!overwritefile)
                            {
                                if (!pf.isBackReference() && !((Pack) packs.get(i)).loose)
                                {
                                    // objIn.skip(pf.length());
                                }
                                continue;
                            }

                        }

                        // We copy the file
                        out = new FileOutputStream(pathFile);
                        byte[] buffer = new byte[5120];
                        long bytesCopied = 0;
                        // InputStream pis = objIn;
                        InputStream pis = fin;

                        if (((Pack) packs.get(i)).loose)
                        {
                            pis = new FileInputStream(pf.sourcePath);
                        }

                        // read in the position of this file
                        // long fileposition = objIn.readLong();
                        long fileposition = pf.getArchivefileposition();

                        while (fin.getFilepointer() < fileposition)
                        {
                            // we have to skip some bytes
                            Debug.trace("Skipping bytes to get to file " + pathFile.getName()
                                    + " (" + fin.getFilepointer() + "<" + fileposition
                                    + ") target is: " + (fileposition - fin.getFilepointer()));
                            try
                            {
                                fin.skip(fileposition - fin.getFilepointer());
                                break;
                            }
                            catch (VolumeNotFoundException vnfe)
                            {
                                File nextmedia = enterNextMediaMessage(vnfe.getVolumename());
                                fin.setVolumename(nextmedia.getAbsolutePath());
                            }
                        }

                        if (fin.getFilepointer() > fileposition)
                        {
                            Debug.trace("Error, can't access file in pack.");
                        }

                        while (bytesCopied < pf.length())
                        {
                            try
                            {
                                if (performInterrupted())
                                { // Interrupt was initiated; perform it.
                                    out.close();
                                    if (pis != objIn) pis.close();
                                    return;
                                }
                                int maxBytes = (int) Math.min(pf.length() - bytesCopied,
                                        buffer.length);

                                int bytesInBuffer = pis.read(buffer, 0, maxBytes);
                                if (bytesInBuffer == -1)
                                {
                                    Debug.trace("Unexpected end of stream (installer corrupted?)");
                                    throw new IOException(
                                            "Unexpected end of stream (installer corrupted?)");
                                }

                                out.write(buffer, 0, bytesInBuffer);

                                bytesCopied += bytesInBuffer;
                            }
                            catch (VolumeNotFoundException vnfe)
                            {
                                File nextmedia = enterNextMediaMessage(vnfe.getVolumename());
                                fin.setVolumename(nextmedia.getAbsolutePath());
                            }
                        }
                        // Cleanings
                        out.close();
                        // if (pis != objIn) pis.close();

                        // Set file modification time if specified
                        if (pf.lastModified() >= 0) pathFile.setLastModified(pf.lastModified());
                        // Custom action listener stuff --- afterFile ----
                        informListeners(customActions, InstallerListener.AFTER_FILE, pathFile, pf,
                                null);
                    }
                    else
                    {
                        if (!pf.isBackReference())
                        {
                            // objIn.skip(pf.length());
                        }
                    }
                }
                // Load information about parsable files
                int numParsables = objIn.readInt();
                Debug.trace("Looking for parsables");
                for (int k = 0; k < numParsables; k++)
                {
                    ParsableFile pf = null;
                    while (true)
                    {
                        try
                        {
                            pf = (ParsableFile) objIn.readObject();
                            break;
                        }
                        catch (VolumeNotFoundException vnfe)
                        {
                            File nextmedia = enterNextMediaMessage(vnfe.getVolumename());
                            fin.setVolumename(nextmedia.getAbsolutePath());
                        }
                        catch (EOFException eofe)
                        {
                            File nextmedia = enterNextMediaMessage("");
                            fin.setVolumename(nextmedia.getAbsolutePath());
                        }
                    }
                    pf.path = IoHelper.translatePath(pf.path, vs);
                    Debug.trace("Found parsable: " + pf.path);
                    parsables.add(pf);
                }

                // Load information about executable files
                int numExecutables = objIn.readInt();
                Debug.trace("Looking for executables...");
                for (int k = 0; k < numExecutables; k++)
                {
                    ExecutableFile ef = (ExecutableFile) objIn.readObject();

                    ef.path = IoHelper.translatePath(ef.path, vs);
                    if (null != ef.argList && !ef.argList.isEmpty())
                    {
                        String arg = null;
                        for (int j = 0; j < ef.argList.size(); j++)
                        {
                            arg = (String) ef.argList.get(j);
                            arg = IoHelper.translatePath(arg, vs);
                            ef.argList.set(j, arg);
                        }
                    }
                    Debug.trace("Found executable: " + ef.path);
                    executables.add(ef);
                    if (ef.executionStage == ExecutableFile.UNINSTALL)
                    {
                        udata.addExecutable(ef);
                    }
                }
                // Custom action listener stuff --- uninstall data ----
                handleAdditionalUninstallData(udata, customActions);

                // Load information about updatechecks
                int numUpdateChecks = objIn.readInt();
                Debug.trace("Looking for updatechecks");
                for (int k = 0; k < numUpdateChecks; k++)
                {
                    UpdateCheck uc = (UpdateCheck) objIn.readObject();
                    Debug.trace("found updatecheck");
                    updatechecks.add(uc);
                }

                // objIn.close();

                if (performInterrupted())
                { // Interrupt was initiated; perform it.
                    return;
                }

                // Custom action listener stuff --- afterPack ----
                informListeners(customActions, InstallerListener.AFTER_PACK, packs.get(i),
                        new Integer(i), handler);
            }
            Debug.trace("Trying to parse files");
            // We use the scripts parser
            ScriptParser parser = new ScriptParser(parsables, vs);
            parser.parseFiles();
            Debug.trace("parsed files");
            if (performInterrupted())
            { // Interrupt was initiated; perform it.
                return;
            }
            Debug.trace("Trying to execute files");
            // We use the file executor
            FileExecutor executor = new FileExecutor(executables);
            if (executor.executeFiles(ExecutableFile.POSTINSTALL, handler) != 0)
            {
                handler.emitError("File execution failed", "The installation was not completed");
                this.result = false;
                Debug.trace("File execution failed");
            }

            if (performInterrupted())
            { // Interrupt was initiated; perform it.
                return;
            }
            Debug.trace("Create uninstaller");
            // We put the uninstaller (it's not yet complete...)
            putUninstaller();
            Debug.trace("Uninstaller created");
            // update checks _after_ uninstaller was put, so we don't delete it
            Debug.trace("Perform updateChecks");
            performUpdateChecks(updatechecks);
            Debug.trace("updatechecks performed.");
            if (performInterrupted())
            { // Interrupt was initiated; perform it.
                return;
            }

            // Custom action listener stuff --- afterPacks ----
            informListeners(customActions, InstallerListener.AFTER_PACKS, idata, handler, null);
            if (performInterrupted())
            { // Interrupt was initiated; perform it.
                return;
            }
            this.writeConfigInformation();
            // The end :-)
            handler.stopAction();
            Debug.trace("Installation complete");
        }
        catch (Exception err)
        {
            // TODO: finer grained error handling with useful error messages
            handler.stopAction();
            handler.emitError("An error occured", err.toString());
            err.printStackTrace();
            Debug.trace("Error while installing: " + err.toString());
            this.result = false;
        }
        finally
        {
            removeFromInstances();
        }
    }

    protected void writeConfigInformation()
    {
        // save the variables
        Properties installerproperties = idata.getVariables();
        Enumeration installerpropertieskeys = installerproperties.keys();
        try
        {
            String installpath = idata.getVariable("INSTALL_PATH");
            PrintWriter pw = new PrintWriter(new FileOutputStream(installpath + File.separator
                    + "installer.properties"));
            pw.println("# Installer properties, written by MultiVolumeUnpacker.");
            while (installerpropertieskeys.hasMoreElements())
            {
                String key = (String) installerpropertieskeys.nextElement();
                if (key.startsWith("SYSTEM_"))
                {
                    // skip
                    continue;
                }
                else if (key.startsWith("password_"))
                {
                    // skip
                    continue;
                }
                pw.println(key + "=" + installerproperties.getProperty(key));
            }
            pw.flush();
            pw.close();
        }
        catch (Exception e)
        {
            Debug.trace("Error while writing config information in MultiVolumeUnpacker: "
                    + e.getMessage());
        }
    }

    /**
     * Return the state of the operation.
     * 
     * @return true if the operation was successful, false otherwise.
     */
    public boolean getResult()
    {
        return this.result;
    }

    /**
     * @param updatechecks
     */
    private void performUpdateChecks(ArrayList updatechecks)
    {
        ArrayList include_patterns = new ArrayList();
        ArrayList exclude_patterns = new ArrayList();

        RECompiler recompiler = new RECompiler();

        this.absolute_installpath = new File(idata.getInstallPath()).getAbsoluteFile();

        // at first, collect all patterns
        for (Iterator iter = updatechecks.iterator(); iter.hasNext();)
        {
            UpdateCheck uc = (UpdateCheck) iter.next();

            if (uc.includesList != null)
                include_patterns.addAll(preparePatterns(uc.includesList, recompiler));

            if (uc.excludesList != null)
                exclude_patterns.addAll(preparePatterns(uc.excludesList, recompiler));
        }

        // do nothing if no update checks were specified
        if (include_patterns.size() == 0) return;

        // now collect all files in the installation directory and figure
        // out files to check for deletion

        // use a treeset for fast access
        TreeSet installed_files = new TreeSet();

        for (Iterator if_it = this.udata.getFilesList().iterator(); if_it.hasNext();)
        {
            String fname = (String) if_it.next();

            File f = new File(fname);

            if (!f.isAbsolute())
            {
                f = new File(this.absolute_installpath, fname);
            }

            installed_files.add(f.getAbsolutePath());
        }

        // now scan installation directory (breadth first), contains Files of
        // directories to scan
        // (note: we'll recurse infinitely if there are circular links or
        // similar nasty things)
        Stack scanstack = new Stack();

        // contains File objects determined for deletion
        ArrayList files_to_delete = new ArrayList();

        try
        {
            scanstack.add(absolute_installpath);

            while (!scanstack.empty())
            {
                File f = (File) scanstack.pop();

                File[] files = f.listFiles();

                if (files == null) { throw new IOException(f.getPath() + "is not a directory!"); }

                for (int i = 0; i < files.length; i++)
                {
                    File newf = files[i];

                    String newfname = newf.getPath();

                    // skip files we just installed
                    if (installed_files.contains(newfname)) continue;

                    if (fileMatchesOnePattern(newfname, include_patterns)
                            && (!fileMatchesOnePattern(newfname, exclude_patterns)))
                    {
                        files_to_delete.add(newf);
                    }

                    if (newf.isDirectory())
                    {
                        scanstack.push(newf);
                    }

                }
            }
        }
        catch (IOException e)
        {
            this.handler.emitError("error while performing update checks", e.toString());
        }

        for (Iterator f_it = files_to_delete.iterator(); f_it.hasNext();)
        {
            File f = (File) f_it.next();

            if (!f.isDirectory())
            // skip directories - they cannot be removed safely yet
            {
                this.handler.emitNotification("deleting " + f.getPath());
                f.delete();
            }

        }

    }

    /**
     * @param filename
     * @param patterns
     * 
     * @return true if the file matched one pattern, false if it did not
     */
    private boolean fileMatchesOnePattern(String filename, ArrayList patterns)
    {
        // first check whether any include matches
        for (Iterator inc_it = patterns.iterator(); inc_it.hasNext();)
        {
            RE pattern = (RE) inc_it.next();

            if (pattern.match(filename)) { return true; }
        }

        return false;
    }

    /**
     * @param list A list of file name patterns (in ant fileset syntax)
     * @param recompiler The regular expression compiler (used to speed up RE compiling).
     * 
     * @return List of org.apache.regexp.RE
     */
    private List preparePatterns(ArrayList list, RECompiler recompiler)
    {
        ArrayList result = new ArrayList();

        for (Iterator iter = list.iterator(); iter.hasNext();)
        {
            String element = (String) iter.next();

            if ((element != null) && (element.length() > 0))
            {
                // substitute variables in the pattern
                element = this.vs.substitute(element, "plain");

                // check whether the pattern is absolute or relative
                File f = new File(element);

                // if it is relative, make it absolute and prepend the
                // installation path
                // (this is a bit dangerous...)
                if (!f.isAbsolute())
                {
                    element = new File(this.absolute_installpath, element).toString();
                }

                // now parse the element and construct a regular expression from
                // it
                // (we have to parse it one character after the next because
                // every
                // character should only be processed once - it's not possible
                // to get this
                // correct using regular expression replacing)
                StringBuffer element_re = new StringBuffer();

                int lookahead = -1;

                int pos = 0;

                while (pos < element.length())
                {
                    char c;

                    if (lookahead != -1)
                    {
                        c = (char) lookahead;
                        lookahead = -1;
                    }
                    else
                        c = element.charAt(pos++);

                    switch (c)
                    {
                    case '/': {
                        element_re.append(File.separator);
                        break;
                    }
                        // escape backslash and dot
                    case '\\':
                    case '.': {
                        element_re.append("\\");
                        element_re.append(c);
                        break;
                    }
                    case '*': {
                        if (pos == element.length())
                        {
                            element_re.append("[^").append(File.separator).append("]*");
                            break;
                        }

                        lookahead = element.charAt(pos++);

                        // check for "**"
                        if (lookahead == '*')
                        {
                            element_re.append(".*");
                            // consume second star
                            lookahead = -1;
                        }
                        else
                        {
                            element_re.append("[^").append(File.separator).append("]*");
                            // lookahead stays there
                        }
                        break;
                    }
                    default: {
                        element_re.append(c);
                        break;
                    }
                    } // switch

                }

                // make sure that the whole expression is matched
                element_re.append('$');

                // replace \ by \\ and create a RE from the result
                try
                {
                    result.add(new RE(recompiler.compile(element_re.toString())));
                }
                catch (RESyntaxException e)
                {
                    this.handler.emitNotification("internal error: pattern \"" + element
                            + "\" produced invalid RE \"" + f.getPath() + "\"");
                }

            }
        }

        return result;
    }

    /**
     * Puts the uninstaller.
     * 
     * @exception Exception Description of the Exception
     */
    private void putUninstaller() throws Exception
    {
        // get the uninstaller base, returning if not found so that
        // idata.uninstallOutJar remains null
        InputStream[] in = new InputStream[2];
        in[0] = MultiVolumeUnpacker.class.getResourceAsStream("/res/IzPack.uninstaller");
        if (in[0] == null) return;
        // The uninstaller extension is facultative; it will be exist only
        // if a native library was marked for uninstallation.
        in[1] = MultiVolumeUnpacker.class.getResourceAsStream("/res/IzPack.uninstaller-ext");

        // Me make the .uninstaller directory
        String dest = IoHelper.translatePath("$INSTALL_PATH", vs) + File.separator + "Uninstaller";
        String jar = dest + File.separator + idata.info.getUninstallerName();
        File pathMaker = new File(dest);
        pathMaker.mkdirs();

        // We log the uninstaller deletion information
        udata.setUninstallerJarFilename(jar);
        udata.setUninstallerPath(dest);

        // We open our final jar file
        FileOutputStream out = new FileOutputStream(jar);
        // Intersect a buffer else byte for byte will be written to the file.
        BufferedOutputStream bos = new BufferedOutputStream(out);
        ZipOutputStream outJar = new ZipOutputStream(bos);
        idata.uninstallOutJar = outJar;
        outJar.setLevel(9);
        udata.addFile(jar);

        // We copy the uninstallers
        HashSet doubles = new HashSet();

        for (int i = 0; i < in.length; ++i)
        {
            if (in[i] == null) continue;
            ZipInputStream inRes = new ZipInputStream(in[i]);
            ZipEntry zentry = inRes.getNextEntry();
            while (zentry != null)
            {
                // Puts a new entry, but not twice like META-INF
                if (!doubles.contains(zentry.getName()))
                {
                    doubles.add(zentry.getName());
                    outJar.putNextEntry(new ZipEntry(zentry.getName()));

                    // Byte to byte copy
                    int unc = inRes.read();
                    while (unc != -1)
                    {
                        outJar.write(unc);
                        unc = inRes.read();
                    }

                    // Next one please
                    inRes.closeEntry();
                    outJar.closeEntry();
                }
                zentry = inRes.getNextEntry();
            }
            inRes.close();
        }

        // We put the langpack
        InputStream in2 = MultiVolumeUnpacker.class.getResourceAsStream("/langpacks/"
                + idata.localeISO3 + ".xml");
        outJar.putNextEntry(new ZipEntry("langpack.xml"));
        int read = in2.read();
        while (read != -1)
        {
            outJar.write(read);
            read = in2.read();
        }
        outJar.closeEntry();
    }

    // CUSTOM ACTION STUFF -------------- start -----------------

    /**
     * Informs all listeners which would be informed at the given action type.
     * 
     * @param customActions array of lists with the custom action objects
     * @param action identifier for which callback should be called
     * @param firstParam first parameter for the call
     * @param secondParam second parameter for the call
     * @param thirdParam third parameter for the call
     */
    private void informListeners(List[] customActions, int action, Object firstParam,
            Object secondParam, Object thirdParam) throws Exception
    {
        List listener = null;
        // select the right action list.
        switch (action)
        {
        case InstallerListener.BEFORE_FILE:
        case InstallerListener.AFTER_FILE:
        case InstallerListener.BEFORE_DIR:
        case InstallerListener.AFTER_DIR:
            listener = customActions[customActions.length - 1];
            break;
        default:
            listener = customActions[0];
            break;
        }
        if (listener == null) return;
        // Iterate the action list.
        Iterator iter = listener.iterator();
        while (iter.hasNext())
        {
            if (shouldInterrupt()) return;
            InstallerListener il = (InstallerListener) iter.next();
            switch (action)
            {
            case InstallerListener.BEFORE_FILE:
                il.beforeFile((File) firstParam, (PackFile) secondParam);
                break;
            case InstallerListener.AFTER_FILE:
                il.afterFile((File) firstParam, (PackFile) secondParam);
                break;
            case InstallerListener.BEFORE_DIR:
                il.beforeDir((File) firstParam, (PackFile) secondParam);
                break;
            case InstallerListener.AFTER_DIR:
                il.afterDir((File) firstParam, (PackFile) secondParam);
                break;
            case InstallerListener.BEFORE_PACK:
                il.beforePack((Pack) firstParam, (Integer) secondParam,
                        (AbstractUIProgressHandler) thirdParam);
                break;
            case InstallerListener.AFTER_PACK:
                il.afterPack((Pack) firstParam, (Integer) secondParam,
                        (AbstractUIProgressHandler) thirdParam);
                break;
            case InstallerListener.BEFORE_PACKS:
                il.beforePacks((AutomatedInstallData) firstParam, (Integer) secondParam,
                        (AbstractUIProgressHandler) thirdParam);
                break;
            case InstallerListener.AFTER_PACKS:
                il.afterPacks((AutomatedInstallData) firstParam,
                        (AbstractUIProgressHandler) secondParam);
                break;

            }
        }
    }

    /**
     * Returns the defined custom actions split into types including a constructed type for the file
     * related installer listeners.
     * 
     * @return array of lists of custom action data like listeners
     */
    private List[] getCustomActions()
    {
        String[] listenerNames = AutomatedInstallData.CUSTOM_ACTION_TYPES;
        List[] retval = new List[listenerNames.length + 1];
        int i;
        for (i = 0; i < listenerNames.length; ++i)
        {
            retval[i] = (List) idata.customData.get(listenerNames[i]);
            if (retval[i] == null)
            // Make a dummy list, then iterator is ever callable.
                retval[i] = new ArrayList();
        }
        if (retval[AutomatedInstallData.INSTALLER_LISTENER_INDEX].size() > 0)
        { // Installer listeners exist
            // Create file related installer listener list in the last
            // element of custom action array.
            i = retval.length - 1; // Should be so, but safe is safe ...
            retval[i] = new ArrayList();
            Iterator iter = ((List) retval[AutomatedInstallData.INSTALLER_LISTENER_INDEX])
                    .iterator();
            while (iter.hasNext())
            {
                // If we get a class cast exception many is wrong and
                // we must fix it.
                InstallerListener li = (InstallerListener) iter.next();
                if (li.isFileListener()) retval[i].add(li);
            }

        }
        return (retval);
    }

    /**
     * Adds additional unistall data to the uninstall data object.
     * 
     * @param udata unistall data
     * @param customData array of lists of custom action data like uninstaller listeners
     */
    private void handleAdditionalUninstallData(UninstallData udata, List[] customData)
    {
        // Handle uninstall libs
        udata.addAdditionalData("__uninstallLibs__",
                customData[AutomatedInstallData.UNINSTALLER_LIBS_INDEX]);
        // Handle uninstaller listeners
        udata.addAdditionalData("uninstallerListeners",
                customData[AutomatedInstallData.UNINSTALLER_LISTENER_INDEX]);
        // Handle uninstaller jars
        udata.addAdditionalData("uninstallerJars",
                customData[AutomatedInstallData.UNINSTALLER_JARS_INDEX]);
    }

    // This method is only used if a file related custom action exist.
    /**
     * Creates the given directory recursive and calls the method "afterDir" of each listener with
     * the current file object and the pack file object. On error an exception is raised.
     * 
     * @param dest the directory which should be created
     * @param pf current pack file object
     * @param customActions all defined custom actions
     * @return false on error, true else
     * @throws Exception
     */

    private boolean mkDirsWithEnhancement(File dest, PackFile pf, List[] customActions)
            throws Exception
    {
        String path = "unknown";
        if (dest != null) path = dest.getAbsolutePath();
        if (dest != null && !dest.exists() && dest.getParentFile() != null)
        {
            if (dest.getParentFile().exists())
                informListeners(customActions, InstallerListener.BEFORE_DIR, dest, pf, null);
            if (!dest.mkdir())
            {
                mkDirsWithEnhancement(dest.getParentFile(), pf, customActions);
                if (!dest.mkdir()) dest = null;
            }
            informListeners(customActions, InstallerListener.AFTER_DIR, dest, pf, null);
        }
        if (dest == null)
        {
            handler.emitError("Error creating directories", "Could not create directory\n" + path);
            handler.stopAction();
            return (false);
        }
        return (true);
    }

    // CUSTOM ACTION STUFF -------------- end -----------------

    /**
     * Returns whether an interrupt request should be discarded or not.
     * 
     * @return Returns the discard interrupt flag
     */
    public static synchronized boolean isDiscardInterrupt()
    {
        return discardInterrupt;
    }

    /**
     * Sets the discard interrupt flag.
     * 
     * @param di the discard interrupt flag to set
     */
    public static synchronized void setDiscardInterrupt(boolean di)
    {
        discardInterrupt = di;
        setInterruptDesired(false);
    }

    /**
     * Returns the interrupt desired state.
     * 
     * @return the interrupt desired state
     */
    public static boolean isInterruptDesired()
    {
        return interruptDesired;
    }

    /**
     * @param interruptDesired The interrupt desired flag to set
     */
    private static void setInterruptDesired(boolean interruptDesired)
    {
        MultiVolumeUnpacker.interruptDesired = interruptDesired;
    }
}
