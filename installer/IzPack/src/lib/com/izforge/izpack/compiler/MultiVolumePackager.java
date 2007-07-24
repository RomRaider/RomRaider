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
package com.izforge.izpack.compiler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import net.n3.nanoxml.XMLElement;

import com.izforge.izpack.CustomData;
import com.izforge.izpack.GUIPrefs;
import com.izforge.izpack.Info;
import com.izforge.izpack.Pack;
import com.izforge.izpack.PackFile;
import com.izforge.izpack.Panel;
import com.izforge.izpack.XPackFile;
import com.izforge.izpack.compressor.PackCompressor;
import com.izforge.izpack.compressor.PackCompressorFactory;
import com.izforge.izpack.io.FileSpanningInputStream;
import com.izforge.izpack.io.FileSpanningOutputStream;
import com.izforge.izpack.util.Debug;

/**
 * The packager class. The packager is used by the compiler to put files into an installer, and
 * create the actual installer files.
 * 
 * This is a packager, which packs everything into multi volumes.
 * 
 * @author Dennis Reil, <Dennis.Reil@reddot.de>
 */
public class MultiVolumePackager implements IPackager
{

    public static final String INSTALLER_PAK_NAME = "installer";

    /** Path to the skeleton installer. */
    public static final String SKELETON_SUBPATH = "lib/installer.jar";

    /** Base file name of all jar files. This has no ".jar" suffix. */
    private File baseFile = null;

    /** Executable zipped output stream. First to open, last to close. */
    private ZipOutputStream primaryJarStream;

    /** Basic installer info. */
    private Info info = null;

    /** Gui preferences of instatller. */
    private GUIPrefs guiPrefs = null;

    /** The variables used in the project */
    private Properties variables = new Properties();

    /** The ordered panels informations. */
    private List panelList = new ArrayList();

    /** The ordered packs informations (as PackInfo objects). */
    private List packsList = new ArrayList();

    /** The ordered langpack ISO3 names. */
    private List langpackNameList = new ArrayList();

    /** The ordered custom actions informations. */
    private List customDataList = new ArrayList();

    /** The langpack URLs keyed by ISO3 name. */
    private Map installerResourceURLMap = new HashMap();

    /** Jar file URLs who's contents will be copied into the installer. */
    private Set includedJarURLs = new HashSet();

    /** Each pack is created in a separte jar if webDirURL is non-null. */
    private boolean packJarsSeparate = false;

    /** The listeners. */
    private PackagerListener listener;

    /** The compression format to be used for pack compression */
    private PackCompressor compressor;

    /** Files which are always written into the container file */
    private HashMap alreadyWrittenFiles = new HashMap();
    
    private XMLElement configdata = null;

    /**
     * The constructor.
     * 
     * @throws CompilerException
     */
    public MultiVolumePackager() throws CompilerException
    {
        this("default");
    }

    /**
     * Extended constructor.
     * 
     * @param compr_format Compression format to be used for packs compression format (if supported)
     * @throws CompilerException
     */
    public MultiVolumePackager(String compr_format) throws CompilerException
    {
        this(compr_format, -1);
    }

    /**
     * Extended constructor.
     * 
     * @param compr_format Compression format to be used for packs
     * @param compr_level Compression level to be used with the chosen compression format (if
     * supported)
     * @throws CompilerException
     */
    public MultiVolumePackager(String compr_format, int compr_level) throws CompilerException
    {
        setCompressorOptions(compr_format, compr_level);
    }

    /**
     * Create the installer, beginning with the specified jar. If the name specified does not end in
     * ".jar", it is appended. If secondary jars are created for packs (if the Info object added has
     * a webDirURL set), they are created in the same directory, named sequentially by inserting
     * ".pack#" (where '#' is the pack number) ".jar" suffix: e.g. "foo.pack1.jar". If any file
     * exists, it is overwritten.
     */
    public void createInstaller(File primaryFile) throws Exception
    {
        // first analyze the configuration
        this.analyzeConfigurationInformation();
        
        // preliminary work
        String baseName = primaryFile.getName();
        if (baseName.endsWith(".jar"))
        {
            baseName = baseName.substring(0, baseName.length() - 4);
            baseFile = new File(primaryFile.getParentFile(), baseName);
        }
        else
            baseFile = primaryFile;

        info.setInstallerBase(baseFile.getName());
        packJarsSeparate = (info.getWebDirURL() != null);

        // primary (possibly only) jar. -1 indicates primary
        primaryJarStream = getJarOutputStream(baseFile.getName() + ".jar");

        sendStart();

        // write the primary jar. MUST be first so manifest is not overwritten
        // by
        // an included jar
        System.out.println("Writing skeleton installer.");
        writeSkeletonInstaller();
        writeInstallerObject("info", info);
        writeInstallerObject("vars", variables);
        writeInstallerObject("GUIPrefs", guiPrefs);
        writeInstallerObject("panelsOrder", panelList);
        writeInstallerObject("customData", customDataList);
        writeInstallerObject("langpacks.info", langpackNameList);
        writeInstallerResources();
        writeIncludedJars();

        // Pack File Data may be written to separate jars
        String packfile = baseFile.getParent() + File.separator + INSTALLER_PAK_NAME;
        writePacks(new File(packfile));

        // Finish up. closeAlways is a hack for pack compressions other than
        // default. Some of it (e.g. BZip2) closes the slave of it also.
        // But this should not be because the jar stream should be open
        // for the next pack. Therefore an own JarOutputStream will be used
        // which close method will be blocked.
        // primaryJarStream.closeAlways();
        primaryJarStream.close();

        sendStop();
    }

    /***********************************************************************************************
     * Listener assistance
     **********************************************************************************************/

    private void analyzeConfigurationInformation()
    {
        String classname = this.getClass().getName();
        String sizeprop = classname + ".volumesize";
        String freespaceprop = classname + ".firstvolumefreespace";        
        if (this.configdata == null){
            // no configdata given, set default values
            this.variables.setProperty(sizeprop, Long.toString(FileSpanningOutputStream.DEFAULT_VOLUME_SIZE));
            this.variables.setProperty(freespaceprop, Long.toString(FileSpanningOutputStream.DEFAULT_ADDITIONAL_FIRST_VOLUME_FREE_SPACE_SIZE));            
        }
        else {            
            // configdata was set
            String volumesize = configdata.getAttribute("volumesize", Long.toString(FileSpanningOutputStream.DEFAULT_VOLUME_SIZE));
            String freespace = configdata.getAttribute("firstvolumefreespace", Long.toString(FileSpanningOutputStream.DEFAULT_ADDITIONAL_FIRST_VOLUME_FREE_SPACE_SIZE));
            this.variables.setProperty(sizeprop, volumesize);
            this.variables.setProperty(freespaceprop, freespace);
        }         
    }

    /**
     * Get the PackagerListener.
     * 
     * @return the current PackagerListener
     */
    public PackagerListener getPackagerListener()
    {
        return listener;
    }

    /**
     * Adds a listener.
     * 
     * @param listener The listener.
     */
    public void setPackagerListener(PackagerListener listener)
    {
        this.listener = listener;
    }

    /**
     * Dispatches a message to the listeners.
     * 
     * @param job The job description.
     */
    private void sendMsg(String job)
    {
        sendMsg(job, PackagerListener.MSG_INFO);
    }

    /**
     * Dispatches a message to the listeners at specified priority.
     * 
     * @param job The job description.
     * @param priority The message priority.
     */
    private void sendMsg(String job, int priority)
    {
        Debug.trace(job);
        if (listener != null) listener.packagerMsg(job, priority);
    }

    /** Dispatches a start event to the listeners. */
    private void sendStart()
    {
        if (listener != null) listener.packagerStart();
    }

    /** Dispatches a stop event to the listeners. */
    private void sendStop()
    {
        if (listener != null) listener.packagerStop();
    }

    /***********************************************************************************************
     * Public methods to add data to the Installer being packed
     **********************************************************************************************/

    /**
     * Sets the informations related to this installation.
     * 
     * @param info The info section.
     * @exception Exception Description of the Exception
     */
    public void setInfo(Info info) throws Exception
    {
        sendMsg("Setting the installer information", PackagerListener.MSG_VERBOSE);
        this.info = info;
        if (!getCompressor().useStandardCompression()
                && getCompressor().getDecoderMapperName() != null)
        {
            this.info.setPackDecoderClassName(getCompressor().getDecoderMapperName());
        }
    }

    /**
     * Sets the GUI preferences.
     * 
     * @param prefs The new gUIPrefs value
     */
    public void setGUIPrefs(GUIPrefs prefs)
    {
        sendMsg("Setting the GUI preferences", PackagerListener.MSG_VERBOSE);
        guiPrefs = prefs;
    }

    /**
     * Allows access to add, remove and update the variables for the project, which are maintained
     * in the packager.
     * 
     * @return map of variable names to values
     */
    public Properties getVariables()
    {
        return variables;
    }

    /**
     * Add a panel, where order is important. Only one copy of the class files neeed are inserted in
     * the installer.
     */
    public void addPanelJar(Panel panel, URL jarURL)
    {
        panelList.add(panel); // serialized to keep order/variables correct
        addJarContent(jarURL); // each included once, no matter how many times
        // added
    }

    /**
     * Add a custom data like custom actions, where order is important. Only one copy of the class
     * files neeed are inserted in the installer.
     * 
     * @param ca custom action object
     * @param url the URL to include once
     */
    public void addCustomJar(CustomData ca, URL url)
    {
        customDataList.add(ca); // serialized to keep order/variables correct
        addJarContent(url); // each included once, no matter how many times
        // added
    }

    /**
     * Adds a pack, order is mostly irrelevant.
     * 
     * @param pack contains all the files and items that go with a pack
     */
    public void addPack(PackInfo pack)
    {
        packsList.add(pack);
    }

    /**
     * Gets the packages list
     */
    public List getPacksList()
    {
        return packsList;
    }

    /**
     * Adds a language pack.
     * 
     * @param iso3 The ISO3 code.
     * @param xmlURL The location of the xml local info
     * @param flagURL The location of the flag image resource
     */
    public void addLangPack(String iso3, URL xmlURL, URL flagURL)
    {
        sendMsg("Adding langpack: " + iso3, PackagerListener.MSG_VERBOSE);
        // put data & flag as entries in installer, and keep array of iso3's
        // names
        langpackNameList.add(iso3);
        addResource("flag." + iso3, flagURL);
        installerResourceURLMap.put("langpacks/" + iso3 + ".xml", xmlURL);
    }

    /**
     * Adds a resource.
     * 
     * @param resId The resource Id.
     * @param url The location of the data
     */
    public void addResource(String resId, URL url)
    {
        sendMsg("Adding resource: " + resId, PackagerListener.MSG_VERBOSE);
        installerResourceURLMap.put("res/" + resId, url);
    }

    /**
     * Adds a native library.
     * 
     * @param name The native library name.
     * @param url The url to get the data from.
     * @exception Exception Description of the Exception
     */
    public void addNativeLibrary(String name, URL url) throws Exception
    {
        sendMsg("Adding native library: " + name, PackagerListener.MSG_VERBOSE);
        installerResourceURLMap.put("native/" + name, url);
    }

    /**
     * Adds a jar file content to the installer. Package structure is maintained. Need mechanism to
     * copy over signed entry information.
     * 
     * @param jarURL The url of the jar to add to the installer. We use a URL so the jar may be
     * nested within another.
     */
    public void addJarContent(URL jarURL)
    {
        addJarContent(jarURL, null);
    }

    /**
     * Adds a jar file content to the installer. Package structure is maintained. Need mechanism to
     * copy over signed entry information.
     * 
     * @param jarURL The url of the jar to add to the installer. We use a URL so the jar may be
     * nested within another.
     */
    public void addJarContent(URL jarURL, List files)
    {
        Object[] cont = { jarURL, files};
        sendMsg("Adding content of jar: " + jarURL.getFile(), PackagerListener.MSG_VERBOSE);
        includedJarURLs.add(cont);
    }

    /**
     * Marks a native library to be added to the uninstaller.
     * 
     * @param data the describing custom action data object
     */
    public void addNativeUninstallerLibrary(CustomData data)
    {
        customDataList.add(data); // serialized to keep order/variables
        // correct

    }

    /***********************************************************************************************
     * Private methods used when writing out the installer to jar files.
     **********************************************************************************************/

    /**
     * Write skeleton installer to primary jar. It is just an included jar, except that we copy the
     * META-INF as well.
     */
    private void writeSkeletonInstaller() throws IOException
    {
        sendMsg("Copying the skeleton installer", PackagerListener.MSG_VERBOSE);
        
        
        InputStream is = MultiVolumePackager.class.getResourceAsStream("/" + SKELETON_SUBPATH);
        if (is == null)
        {
            File skeleton = new File(Compiler.IZPACK_HOME, SKELETON_SUBPATH);
            is = new FileInputStream(skeleton);
        }
        ZipInputStream inJarStream = new ZipInputStream(is);
        
        // copy anything except the manifest.mf
        List excludes = new ArrayList();
        excludes.add("META-INF.MANIFEST.MF");
        copyZipWithoutExcludes(inJarStream, primaryJarStream,excludes);

        // ugly code to modify the manifest-file to set MultiVolumeInstaller as main class
        // reopen Stream
        is = MultiVolumePackager.class.getResourceAsStream("/" + SKELETON_SUBPATH);
        if (is == null)
        {
            File skeleton = new File(Compiler.IZPACK_HOME, SKELETON_SUBPATH);
            is = new FileInputStream(skeleton);
        }
        inJarStream = new ZipInputStream(is);                
        boolean found = false;
        ZipEntry ze = null;
        String modifiedmanifest = null;
        while (((ze = inJarStream.getNextEntry()) != null) && !found){            
            if ("META-INF/MANIFEST.MF".equals(ze.getName())){
                long size = ze.getSize();
                byte[] buffer = new byte[4096];
                int readbytes = 0;
                int totalreadbytes = 0;
                StringBuffer manifest = new StringBuffer();
                while (((readbytes = inJarStream.read(buffer)) > 0) && (totalreadbytes < size)){
                    totalreadbytes += readbytes;
                    String tmp = new String(buffer,0,readbytes,"utf-8");
                    manifest.append(tmp);
                }
                
                
                StringReader stringreader = new StringReader(manifest.toString());
                BufferedReader reader = new BufferedReader(stringreader);
                String line = null;
                StringBuffer modified = new StringBuffer();
                while ((line = reader.readLine()) != null){
                    if (line.startsWith("Main-Class:")){
                        line = "Main-Class: com.izforge.izpack.installer.MultiVolumeInstaller";
                    }
                    modified.append(line);
                    modified.append("\r\n");
                }                
                reader.close();
                modifiedmanifest = modified.toString();
                /*
                System.out.println("Manifest:");
                System.out.println(manifest.toString());
                System.out.println("Modified Manifest:");
                System.out.println(modified.toString());
                */
                break;
            }
        }
        
        primaryJarStream.putNextEntry(new ZipEntry("META-INF/MANIFEST.MF"));        
        primaryJarStream.write(modifiedmanifest.getBytes());
        primaryJarStream.closeEntry();
    }

    /**
     * Write an arbitrary object to primary jar.
     */
    private void writeInstallerObject(String entryName, Object object) throws IOException
    {
        primaryJarStream.putNextEntry(new ZipEntry(entryName));
        ObjectOutputStream out = new ObjectOutputStream(primaryJarStream);
        out.writeObject(object);
        out.flush();
        primaryJarStream.closeEntry();
    }

    /** Write the data referenced by URL to primary jar. */
    private void writeInstallerResources() throws IOException
    {
        sendMsg("Copying " + installerResourceURLMap.size() + " files into installer");

        Iterator i = installerResourceURLMap.keySet().iterator();
        while (i.hasNext())
        {
            String name = (String) i.next();
            InputStream in = ((URL) installerResourceURLMap.get(name)).openStream();
            primaryJarStream.putNextEntry(new ZipEntry(name));
            copyStream(in, primaryJarStream);
            primaryJarStream.closeEntry();
            in.close();
        }
    }

    /** Copy included jars to primary jar. */
    private void writeIncludedJars() throws IOException
    {
        sendMsg("Merging " + includedJarURLs.size() + " jars into installer");

        Iterator i = includedJarURLs.iterator();
        while (i.hasNext())
        {
            Object[] current = (Object[]) i.next();
            InputStream is = ((URL) current[0]).openStream();
            ZipInputStream inJarStream = new ZipInputStream(is);
            copyZip(inJarStream, primaryJarStream, (List) current[1]);
        }
    }

    /**
     * Write Packs to primary jar or each to a separate jar.
     */
    private void writePacks(File primaryfile) throws Exception
    {

        final int num = packsList.size();
        sendMsg("Writing " + num + " Pack" + (num > 1 ? "s" : "") + " into installer");
        Debug.trace("Writing " + num + " Pack" + (num > 1 ? "s" : "") + " into installer");
        // Map to remember pack number and bytes offsets of back references
        Map storedFiles = new HashMap();

        // First write the serialized files and file metadata data for each pack
        // while counting bytes.

        String classname = this.getClass().getName();
        String volumesize = this.getVariables().getProperty(classname + ".volumesize");
        String extraspace = this.getVariables().getProperty(classname + ".firstvolumefreespace");

        long volumesizel = FileSpanningOutputStream.DEFAULT_VOLUME_SIZE;
        long extraspacel = FileSpanningOutputStream.DEFAULT_ADDITIONAL_FIRST_VOLUME_FREE_SPACE_SIZE;

        if (volumesize != null)
        {
            volumesizel = Long.parseLong(volumesize);
        }
        if (extraspace != null)
        {
            extraspacel = Long.parseLong(extraspace);
        }
        Debug.trace("Volumesize: " + volumesizel);
        Debug.trace("Extra space on first volume: " + extraspacel);
        FileSpanningOutputStream fout = new FileSpanningOutputStream(primaryfile.getParent()
                + File.separator + primaryfile.getName() + ".pak", volumesizel);
        fout.setFirstvolumefreespacesize(extraspacel);

        int packNumber = 0;
        Iterator packIter = packsList.iterator();
        while (packIter.hasNext())
        {
            PackInfo packInfo = (PackInfo) packIter.next();
            Pack pack = packInfo.getPack();
            pack.nbytes = 0;

            sendMsg("Writing Pack " + packNumber + ": " + pack.name, PackagerListener.MSG_VERBOSE);
            Debug.trace("Writing Pack " + packNumber + ": " + pack.name);
            ZipEntry entry = new ZipEntry("packs/pack" + packNumber);
            // write the metadata as uncompressed object stream to primaryJarStream
            // ByteCountingOutputStream dos = new
            // ByteCountingOutputStream(comprStream);
            // ByteCountingOutputStream dos = new
            // ByteCountingOutputStream(primaryJarStream);
            // first write a packs entry

            primaryJarStream.putNextEntry(entry);
            ObjectOutputStream objOut = new ObjectOutputStream(primaryJarStream);

            // We write the actual pack files
            objOut.writeInt(packInfo.getPackFiles().size());

            Iterator iter = packInfo.getPackFiles().iterator();
            while (iter.hasNext())
            {
                boolean addFile = !pack.loose;
                XPackFile pf = new XPackFile((PackFile) iter.next());
                File file = packInfo.getFile(pf.getPackfile());
                Debug.trace("Next file: " + file.getAbsolutePath());
                // use a back reference if file was in previous pack, and in
                // same jar
                long[] info = (long[]) storedFiles.get(file);
                if (info != null && !packJarsSeparate)
                {
                    Debug.trace("File already included in other pack");
                    pf.setPreviousPackFileRef((int) info[0], info[1]);
                    addFile = false;
                }

                if (addFile && !pf.isDirectory())
                {
                    long pos = fout.getFilepointer();

                    pf.setArchivefileposition(pos);

                    // write out the filepointer
                    int volumecountbeforewrite = fout.getVolumeCount();

                    FileInputStream inStream = new FileInputStream(file);
                    long bytesWritten = copyStream(inStream, fout);
                    fout.flush();

                    long posafterwrite = fout.getFilepointer();
                    Debug.trace("File (" + pf.sourcePath + ") " + pos + " <-> " + posafterwrite);

                    if (fout.getFilepointer() != (pos + bytesWritten))
                    {
                        Debug.trace("file: " + file.getName());
                        Debug.trace("(Filepos/BytesWritten/ExpectedNewFilePos/NewFilePointer) ("
                                        + pos + "/" + bytesWritten + "/" + (pos + bytesWritten)
                                        + "/" + fout.getFilepointer() + ")");
                        Debug.trace("Volumecount (before/after) ("
                                + volumecountbeforewrite + "/" + fout.getVolumeCount() + ")");
                        throw new IOException("Error new filepointer is illegal");
                    }

                    if (bytesWritten != pf.length()) { throw new IOException(
                            "File size mismatch when reading " + file); }
                    inStream.close();
                    // keine backreferences möglich
                    // storedFiles.put(file, new long[] { packNumber, pos});
                }

                objOut.writeObject(pf); // base info
                objOut.flush(); // make sure it is written
                // even if not written, it counts towards pack size
                pack.nbytes += pf.length();
            }
            // Write out information about parsable files
            objOut.writeInt(packInfo.getParsables().size());
            iter = packInfo.getParsables().iterator();
            while (iter.hasNext())
                objOut.writeObject(iter.next());

            // Write out information about executable files
            objOut.writeInt(packInfo.getExecutables().size());
            iter = packInfo.getExecutables().iterator();
            while (iter.hasNext())
                objOut.writeObject(iter.next());

            // Write out information about updatecheck files
            objOut.writeInt(packInfo.getUpdateChecks().size());
            iter = packInfo.getUpdateChecks().iterator();
            while (iter.hasNext())
                objOut.writeObject(iter.next());

            // Cleanup
            objOut.flush();
            packNumber++;
        }

        // write metadata for reading in volumes
        int volumes = fout.getVolumeCount();
        Debug.trace("Written " + volumes + " volumes");
        String volumename = primaryfile.getName() + ".pak";

        fout.flush();
        fout.close();

        primaryJarStream.putNextEntry(new ZipEntry("volumes.info"));
        ObjectOutputStream out = new ObjectOutputStream(primaryJarStream);
        out.writeInt(volumes);
        out.writeUTF(volumename);
        out.flush();
        primaryJarStream.closeEntry();

        // Now that we know sizes, write pack metadata to primary jar.
        primaryJarStream.putNextEntry(new ZipEntry("packs.info"));
        out = new ObjectOutputStream(primaryJarStream);
        out.writeInt(packsList.size());

        Iterator i = packsList.iterator();
        while (i.hasNext())
        {
            PackInfo pack = (PackInfo) i.next();
            out.writeObject(pack.getPack());
        }
        out.flush();
        primaryJarStream.closeEntry();
    }

    /***********************************************************************************************
     * Stream utilites for creation of the installer.
     **********************************************************************************************/

    /** Return a stream for the next jar. */
    private ZipOutputStream getJarOutputStream(String name) throws IOException
    {
        File file = new File(baseFile.getParentFile(), name);
        sendMsg("Building installer jar: " + file.getAbsolutePath());
        Debug.trace("Building installer jar: " + file.getAbsolutePath());
        ZipOutputStream jar = new ZipOutputStream(new FileOutputStream(file));
        jar.setLevel(Deflater.BEST_COMPRESSION);
        // jar.setPreventClose(true); // Needed at using FilterOutputStreams which
        // calls close
        // of the slave at finalizing.

        return jar;
    }

    /**
     * Copies contents of one jar to another.
     * 
     * <p>
     * TODO: it would be useful to be able to keep signature information from signed jar files, can
     * we combine manifests and still have their content signed?
     * 
     * @see #copyStream(InputStream, OutputStream)
     */
    private void copyZip(ZipInputStream zin, ZipOutputStream out) throws IOException
    {
        copyZip(zin, out, null);
    }

    /**
     * Copies specified contents of one jar to another.
     * 
     * <p>
     * TODO: it would be useful to be able to keep signature information from signed jar files, can
     * we combine manifests and still have their content signed?
     * 
     * @see #copyStream(InputStream, OutputStream)
     */
    private void copyZip(ZipInputStream zin, ZipOutputStream out, List files) throws IOException
    {
        java.util.zip.ZipEntry zentry;
        if (!alreadyWrittenFiles.containsKey(out)) alreadyWrittenFiles.put(out, new HashSet());
        HashSet currentSet = (HashSet) alreadyWrittenFiles.get(out);
        while ((zentry = zin.getNextEntry()) != null)
        {
            String currentName = zentry.getName();
            String testName = currentName.replace('/', '.');
            testName = testName.replace('\\', '.');
            if (files != null)
            {
                Iterator i = files.iterator();
                boolean founded = false;
                while (i.hasNext())
                { // Make "includes" self to support regex.
                    String doInclude = (String) i.next();
                    if (testName.matches(doInclude))
                    {
                        founded = true;
                        break;
                    }
                }
                if (!founded) continue;
            }
            if (currentSet.contains(currentName)) continue;
            try
            {
                out.putNextEntry(new ZipEntry(currentName));
                copyStream(zin, out);
                out.closeEntry();
                zin.closeEntry();
                currentSet.add(currentName);
            }
            catch (ZipException x)
            {
                // This avoids any problem that can occur with duplicate
                // directories. for instance all META-INF data in jars
                // unfortunately this do not work with the apache ZipOutputStream...
            }
        }
    }
    
    /**
     * Copies specified contents of one jar to another without the specified files
     * 
     * <p>
     * TODO: it would be useful to be able to keep signature information from signed jar files, can
     * we combine manifests and still have their content signed?
     * 
     * @see #copyStream(InputStream, OutputStream)
     */
    private void copyZipWithoutExcludes(ZipInputStream zin, ZipOutputStream out, List excludes) throws IOException
    {
        java.util.zip.ZipEntry zentry;
        if (!alreadyWrittenFiles.containsKey(out)) alreadyWrittenFiles.put(out, new HashSet());
        HashSet currentSet = (HashSet) alreadyWrittenFiles.get(out);
        while ((zentry = zin.getNextEntry()) != null)
        {
            String currentName = zentry.getName();
            String testName = currentName.replace('/', '.');
            testName = testName.replace('\\', '.');
            if (excludes != null)
            {
                Iterator i = excludes.iterator();
                boolean skip = false;
                while (i.hasNext())
                { 
                    // Make "excludes" self to support regex.
                    String doExclude = (String) i.next();                    
                    if (testName.matches(doExclude))
                    {                        
                        skip = true;
                        break;
                    }
                }           
                if (skip){
                    continue;
                }
            }
            if (currentSet.contains(currentName)) continue;
            try
            {
                out.putNextEntry(new ZipEntry(currentName));
                copyStream(zin, out);
                out.closeEntry();
                zin.closeEntry();
                currentSet.add(currentName);
            }
            catch (ZipException x)
            {
                // This avoids any problem that can occur with duplicate
                // directories. for instance all META-INF data in jars
                // unfortunately this do not work with the apache ZipOutputStream...
            }
        }
    }

    /**
     * Copies all the data from the specified input stream to the specified output stream.
     * 
     * @param in the input stream to read
     * @param out the output stream to write
     * @return the total number of bytes copied
     * @exception IOException if an I/O error occurs
     */
    private long copyStream(InputStream in, OutputStream out) throws IOException
    {
        byte[] buffer = new byte[5120];
        long bytesCopied = 0;
        int bytesInBuffer;
        while ((bytesInBuffer = in.read(buffer)) != -1)
        {
            out.write(buffer, 0, bytesInBuffer);
            bytesCopied += bytesInBuffer;
        }
        return bytesCopied;
    }

    /**
     * Returns the current pack compressor
     * 
     * @return Returns the current pack compressor.
     */
    public PackCompressor getCompressor()
    {
        return compressor;
    }

    public void setCompressorOptions(String compr_format, int compr_level) throws CompilerException
    {
        compressor = PackCompressorFactory.get(compr_format);
        compressor.setCompressionLevel(compr_level);
    }

    public void addConfigurationInformation(XMLElement data)
    {
       this.configdata = data;        
    }

    public void initPackCompressor(String compr_format, int compr_level) throws CompilerException
    {       
        this.setCompressorOptions(compr_format, compr_level);        
    }
}
