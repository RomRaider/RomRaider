/*
 * $Id: CompilerConfig.java 1816 2007-04-23 19:57:27Z jponge $
 * IzPack - Copyright 2001-2007 Julien Ponge, All Rights Reserved.
 * 
 * http://izpack.org/
 * http://developer.berlios.de/projects/izpack/
 * 
 * Copyright 2001 Johannes Lehtinen
 * Copyright 2002 Paul Wilkinson
 * Copyright 2004 Gaganis Giorgos
 *
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 *     
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.izforge.izpack.compiler;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;
import java.util.jar.JarInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.tools.ant.DirectoryScanner;

import com.izforge.izpack.CustomData;
import com.izforge.izpack.ExecutableFile;
import com.izforge.izpack.GUIPrefs;
import com.izforge.izpack.Info;
import com.izforge.izpack.PackFile;
import com.izforge.izpack.Panel;
import com.izforge.izpack.ParsableFile;
import com.izforge.izpack.UpdateCheck;
import com.izforge.izpack.compiler.Compiler.CmdlinePackagerListener;
import com.izforge.izpack.event.CompilerListener;
import com.izforge.izpack.util.Debug;
import com.izforge.izpack.util.OsConstraint;
import com.izforge.izpack.util.VariableSubstitutor;

import net.n3.nanoxml.IXMLReader;
import net.n3.nanoxml.NonValidator;
import net.n3.nanoxml.StdXMLBuilder;
import net.n3.nanoxml.StdXMLParser;
import net.n3.nanoxml.StdXMLReader;
import net.n3.nanoxml.XMLElement;

/**
 * A parser for the installer xml configuration. This parses a document
 * conforming to the installation.dtd and populates a Compiler instance to
 * perform the install compilation. 
 * 
 * @author Scott Stark
 * @version $Revision: 1816 $
 */
public class CompilerConfig extends Thread
{
    /** The compiler version. */
    public final static String VERSION = "1.0";

    /** Standard installer. */
    public final static String STANDARD = "standard";

    /** Web installer. */
    public final static String WEB = "web";

    /** Constant for checking attributes. */
    private static boolean YES = true;

    /** Constant for checking attributes. */
    private static boolean NO = false;

    private final static String IZ_TEST_FILE = "ShellLink.dll";

    private final static String IZ_TEST_SUBDIR = "bin" + File.separator + "native" + File.separator
            + "izpack";

    /** The xml install file */
    private String filename;
    /** The xml install configuration text */
    private String installText;
    /** The base directory. */
    protected String basedir;

    /** The installer packager compiler */
    private Compiler compiler;

    /**
     * List of CompilerListeners which should be called at packaging
     */
    protected List compilerListeners = new ArrayList();

    /**
     * Set the IzPack home directory
     * @param izHome - the izpack home directory
     */
    public static void setIzpackHome(String izHome)
    {
        Compiler.setIzpackHome(izHome);
    }

    /**
     * The constructor.
     * 
     * @param filename The XML filename.
     * @param basedir The base directory.
     * @param kind The installer kind.
     * @param output The installer filename.
     * @throws CompilerException
     */
    public CompilerConfig(String filename, String basedir, String kind, String output) throws CompilerException
    {
        this(filename, basedir, kind, output, (PackagerListener) null);
    }
    /**
     * The constructor.
     * 
     * @param filename The XML filename.
     * @param basedir The base directory.
     * @param kind The installer kind.
     * @param output The installer filename.
     * @param listener The PackagerListener.
     * @throws CompilerException
     */
    public CompilerConfig(String filename, String basedir, String kind, String output, PackagerListener listener) 
    throws CompilerException
    {
        this(filename,basedir,kind,output, "default", listener);
    }

    /**
     * @param filename The XML filename.
     * @param kind The installer kind.
     * @param output The installer filename.
     * @param compr_format The compression format to be used for packs.
     * @param listener The PackagerListener.
     * @throws CompilerException
     */
    public CompilerConfig(String filename, String base, String kind, String output, String compr_format, 
            PackagerListener listener) throws CompilerException
    {
        this(filename,base,kind,output, compr_format,listener, (String) null);
    }

    /**
     * 
     * @param basedir The base directory.
     * @param kind The installer kind.
     * @param output The installer filename.
     * @param listener The PackagerListener.
     * @param installText The install xml configuration text
     * @throws CompilerException
     */
    public CompilerConfig(String basedir, String kind, String output, PackagerListener listener,
            String installText) throws CompilerException
    {
        this((String) null, basedir, kind, output, "default", listener, installText);
    }
    /**
     * 
     * @param filename The XML filename.
     * @param basedir The base directory.
     * @param kind The installer kind.
     * @param output The installer filename.
     * @param compr_format The compression format to be used for packs.
     * @param listener The PackagerListener.
     * @param installText The install xml configuration text
     * @throws CompilerException
     */
    public CompilerConfig(String filename, String basedir, String kind, String output, String compr_format, 
            PackagerListener listener, String installText) throws CompilerException
    {
        this( filename, basedir, kind, output, compr_format, -1, listener, installText);
    }

    /**
     * @param filename The XML filename.
     * @param basedir The base directory.
     * @param kind The installer kind.
     * @param output The installer filename.
     * @param compr_format The compression format to be used for packs.
     * @param compr_level Compression level to be used if supported.
     * @param listener The PackagerListener.
     * @param installText The install xml configuration text
    * @throws CompilerException
     */
    public CompilerConfig(String filename, String basedir, String kind, String output, String compr_format, 
            int compr_level, PackagerListener listener, String installText) throws CompilerException
    {
        this.filename = filename;
        this.installText = installText;
        this.basedir = basedir;
        this.compiler = new Compiler(basedir, kind, output, compr_format, compr_level);
        compiler.setPackagerListener(listener);
    }



    /**
     * Add a name value pair to the project property set. It is <i>not</i>
     * replaced it is already in the set of properties.
     * 
     * @param name the name of the property
     * @param value the value to set
     * @return true if the property was not already set
     */
    public boolean addProperty(String name, String value)
    {
        return compiler.addProperty(name, value);
    }

    /**
     * Access the install compiler
     * @return the install compiler
     */
    public Compiler getCompiler()
    {
        return compiler;
    }

    /**
     * Retrieves the packager listener
     */
    public PackagerListener getPackagerListener()
    {
        return compiler.getPackagerListener();
    }

    /** Compile the installation */
    public void compile()
    {
        start();
    }

    /** The run() method. */
    public void run()
    {
        try
        {
            executeCompiler();
        }
        catch (CompilerException ce)
        {
            System.out.println(ce.getMessage() + "\n");
        }
        catch (Exception e)
        {
            if (Debug.stackTracing())
            {
                e.printStackTrace();
            }
            else
            {
                System.out.println("ERROR: " + e.getMessage());
            }
        }
    }

    /**
     * Compiles the installation.
     * 
     * @exception Exception Description of the Exception
     */
    public void executeCompiler() throws Exception
    {
        // normalize and test: TODO: may allow failure if we require write
        // access
        File base = new File(basedir).getAbsoluteFile();
        if (!base.canRead() || !base.isDirectory())
            throw new CompilerException("Invalid base directory: " + base);

        // add izpack built in property
        compiler.setProperty("basedir", base.toString());

        // We get the XML data tree
        XMLElement data = getXMLTree();
        // loads the specified packager
        loadPackagingInformation(data);
        
        // Listeners to various events
        addCustomListeners(data);

        // Read the properties and perform replacement on the rest of the tree
        substituteProperties(data);

        // We add all the information
        addVariables(data);
        addInfo(data);
        addGUIPrefs(data);
        addLangpacks(data);
        addResources(data);
        addNativeLibraries(data);
        addJars(data);
        addPanels(data);
        addPacks(data);

        // We ask the packager to create the installer
        compiler.createInstaller();
    }

    private void loadPackagingInformation(XMLElement data) throws CompilerException
    {
        notifyCompilerListener("loadPackager", CompilerListener.BEGIN, data);
        // Initialisation
        XMLElement root = data.getFirstChildNamed("packaging");
        String packagerclassname = "com.izforge.izpack.compiler.Packager";
        String unpackerclassname = "com.izforge.izpack.installer.Unpacker";
        XMLElement packager = null;
        if (root != null){
            packager = root.getFirstChildNamed("packager");
            
            if (packager != null){
                packagerclassname = requireAttribute(packager, "class");
            }
            
            XMLElement unpacker = root.getFirstChildNamed("unpacker");
            
            if (unpacker != null){
                unpackerclassname = requireAttribute(unpacker, "class");                
            }        
        }
        compiler.initPackager(packagerclassname);  
        if (packager != null){
            XMLElement options = packager.getFirstChildNamed("options");
            if (options != null){
                compiler.getPackager().addConfigurationInformation(options);
            }
        }
        compiler.addProperty("UNPACKER_CLASS", unpackerclassname);
        notifyCompilerListener("loadPackager", CompilerListener.END, data);        
    }

    public boolean wasSuccessful()
    {
        return compiler.wasSuccessful();
    }

    /**
     * Returns the GUIPrefs.
     * 
     * @param data The XML data.
     * @exception CompilerException Description of the Exception
     */
    protected void addGUIPrefs(XMLElement data) throws CompilerException
    {
        notifyCompilerListener("addGUIPrefs", CompilerListener.BEGIN, data);
        // We get the XMLElement & the attributes
        XMLElement gp = data.getFirstChildNamed("guiprefs");
        GUIPrefs prefs = new GUIPrefs();
        if (gp != null)
        {
            prefs.resizable = requireYesNoAttribute(gp, "resizable");
            prefs.width = requireIntAttribute(gp, "width");
            prefs.height = requireIntAttribute(gp, "height");

            // Look and feel mappings
            Iterator it = gp.getChildrenNamed("laf").iterator();
            while (it.hasNext())
            {
                XMLElement laf = (XMLElement) it.next();
                String lafName = requireAttribute(laf, "name");
                requireChildNamed(laf, "os");

                Iterator oit = laf.getChildrenNamed("os").iterator();
                while (oit.hasNext())
                {
                    XMLElement os = (XMLElement) oit.next();
                    String osName = requireAttribute(os, "family");
                    prefs.lookAndFeelMapping.put(osName, lafName);
                }

                Iterator pit = laf.getChildrenNamed("param").iterator();
                Map params = new TreeMap();
                while (pit.hasNext())
                {
                    XMLElement param = (XMLElement) pit.next();
                    String name = requireAttribute(param, "name");
                    String value = requireAttribute(param, "value");
                    params.put(name, value);
                }
                prefs.lookAndFeelParams.put(lafName, params);
            }
            // Load modifier
            it = gp.getChildrenNamed("modifier").iterator();
            while (it.hasNext())
            {
                XMLElement curentModifier = (XMLElement) it.next();
                String key = requireAttribute(curentModifier, "key");
                String value = requireAttribute(curentModifier, "value");
                prefs.modifier.put(key, value);

            }
            // make sure jar contents of each are available in installer
            // map is easier to read/modify than if tree
            HashMap lafMap = new HashMap();
            lafMap.put("liquid", "liquidlnf.jar");
            lafMap.put("kunststoff", "kunststoff.jar");
            lafMap.put("metouia", "metouia.jar");
            lafMap.put("looks", "looks.jar");

            // is this really what we want? a double loop? needed, since above,
            // it's
            // the /last/ lnf for an os which is used, so can't add during
            // initial
            // loop
            Iterator kit = prefs.lookAndFeelMapping.keySet().iterator();
            while (kit.hasNext())
            {
                String lafName = (String) prefs.lookAndFeelMapping.get(kit.next());
                String lafJarName = (String) lafMap.get(lafName);
                if (lafJarName == null) parseError(gp, "Unrecognized Look and Feel: " + lafName);

                URL lafJarURL = findIzPackResource("lib/" + lafJarName, "Look and Feel Jar file",
                        gp);
                compiler.addJarContent(lafJarURL);
            }
        }
        compiler.setGUIPrefs(prefs);
        notifyCompilerListener("addGUIPrefs", CompilerListener.END, data);
    }

    /**
     * Add project specific external jar files to the installer.
     * 
     * @param data The XML data.
     */
    protected void addJars(XMLElement data) throws Exception
    {
        notifyCompilerListener("addJars", CompilerListener.BEGIN, data);
        Iterator iter = data.getChildrenNamed("jar").iterator();
        while (iter.hasNext())
        {
            XMLElement el = (XMLElement) iter.next();
            String src = requireAttribute(el, "src");
            URL url = findProjectResource(src, "Jar file", el);
            compiler.addJarContent(url);
            // Additionals for mark a jar file also used in the uninstaller.
            // The contained files will be copied from the installer into the
            // uninstaller if needed.
            // Therefore the contained files of the jar should be in the
            // installer also
            // they are used only from the uninstaller. This is the reason why
            // the stage
            // wiil be only observed for the uninstaller.
            String stage = el.getAttribute("stage");
            if (stage != null
                    && ("both".equalsIgnoreCase(stage) || "uninstall".equalsIgnoreCase(stage)))
            {
                CustomData ca = new CustomData(null, getContainedFilePaths(url), null,
                        CustomData.UNINSTALLER_JAR);
                compiler.addCustomJar(ca, url);
            }
        }
        notifyCompilerListener("addJars", CompilerListener.END, data);
    }

    /**
     * Add native libraries to the installer.
     * 
     * @param data The XML data.
     */
    protected void addNativeLibraries(XMLElement data) throws Exception
    {
        boolean needAddOns = false;
        notifyCompilerListener("addNativeLibraries", CompilerListener.BEGIN, data);
        Iterator iter = data.getChildrenNamed("native").iterator();
        while (iter.hasNext())
        {
            XMLElement el = (XMLElement) iter.next();
            String type = requireAttribute(el, "type");
            String name = requireAttribute(el, "name");
            String path = "bin/native/" + type + "/" + name;
            URL url = findIzPackResource(path, "Native Library", el);
            compiler.addNativeLibrary(name, url);
            // Additionals for mark a native lib also used in the uninstaller
            // The lib will be copied from the installer into the uninstaller if
            // needed.
            // Therefore the lib should be in the installer also it is used only
            // from
            // the uninstaller. This is the reason why the stage wiil be only
            // observed
            // for the uninstaller.
            String stage = el.getAttribute("stage");
            List constraints = OsConstraint.getOsList(el);
            if (stage != null
                    && ("both".equalsIgnoreCase(stage) || "uninstall".equalsIgnoreCase(stage)))
            {
                ArrayList al = new ArrayList();
                al.add(name);
                CustomData cad = new CustomData(null, al, constraints, CustomData.UNINSTALLER_LIB);
                compiler.addNativeUninstallerLibrary(cad);
                needAddOns = true;
            }

        }
        if (needAddOns)
        {
            // Add the uninstaller extensions as a resource if specified
            XMLElement root = requireChildNamed(data, "info");
            XMLElement uninstallInfo = root.getFirstChildNamed("uninstaller");
            if (validateYesNoAttribute(uninstallInfo, "write", YES))
            {
                URL url = findIzPackResource("lib/uninstaller-ext.jar", "Uninstaller extensions",
                        root);
                compiler.addResource("IzPack.uninstaller-ext", url);
            }

        }
        notifyCompilerListener("addNativeLibraries", CompilerListener.END, data);
    }

    /**
     * Add packs and their contents to the installer.
     * 
     * @param data The XML data.
     */
    protected void addPacks(XMLElement data) throws CompilerException
    {
        notifyCompilerListener("addPacks", CompilerListener.BEGIN, data);
        // Initialisation
        XMLElement root = requireChildNamed(data, "packs");

        // at least one pack is required
        Vector packElements = root.getChildrenNamed("pack");
        if (packElements.isEmpty()) parseError(root, "<packs> requires a <pack>");

        File baseDir = new File(basedir);
        
        Iterator packIter = packElements.iterator();
        while (packIter.hasNext())
        {
            XMLElement el = (XMLElement) packIter.next();

            // Trivial initialisations
            String name = requireAttribute(el, "name");
            String id = el.getAttribute("id");
            
            boolean loose = "true".equalsIgnoreCase(el.getAttribute("loose", "false"));
            String description = requireChildNamed(el, "description").getContent();
            boolean required = requireYesNoAttribute(el, "required");
            String group = el.getAttribute("group");
            String installGroups = el.getAttribute("installGroups");
            String excludeGroup = el.getAttribute("excludeGroup");
            
            if(required && excludeGroup != null)
            {
                parseError(el, "Pack, which has excludeGroup can not be required.", 
                    new Exception("Pack, which has excludeGroup can not be required."));
            }
            
            PackInfo pack = new PackInfo(name, id, description, required, loose, excludeGroup);
            pack.setOsConstraints(OsConstraint.getOsList(el)); // TODO:
            
            // unverified
            // if the pack belongs to an excludeGroup it's not preselected by default
            if(excludeGroup == null)
                pack.setPreselected(validateYesNoAttribute(el, "preselected", YES));
            else
                pack.setPreselected(validateYesNoAttribute(el, "preselected", NO));
            
            // Set the pack group if specified
            if (group != null)
                pack.setGroup(group);
            // Set the pack install groups if specified
            if (installGroups != null)
            {
                StringTokenizer st = new StringTokenizer(installGroups, ",");
                while (st.hasMoreTokens())
                {
                    String igroup = st.nextToken();
                    pack.addInstallGroup(igroup);
                }
            }

            // We get the parsables list
            Iterator iter = el.getChildrenNamed("parsable").iterator();
            while (iter.hasNext())
            {
                XMLElement p = (XMLElement) iter.next();
                String target = requireAttribute(p, "targetfile");
                String type = p.getAttribute("type", "plain");
                String encoding = p.getAttribute("encoding", null);
                List osList = OsConstraint.getOsList(p); // TODO: unverified

                pack.addParsable(new ParsableFile(target, type, encoding, osList));
            }

            // We get the executables list
            iter = el.getChildrenNamed("executable").iterator();
            while (iter.hasNext())
            {
                XMLElement e = (XMLElement) iter.next();
                ExecutableFile executable = new ExecutableFile();
                String val; // temp value

                executable.path = requireAttribute(e, "targetfile");

                // when to execute this executable
                val = e.getAttribute("stage", "never");
                if ("postinstall".equalsIgnoreCase(val))
                    executable.executionStage = ExecutableFile.POSTINSTALL;
                else if ("uninstall".equalsIgnoreCase(val))
                    executable.executionStage = ExecutableFile.UNINSTALL;

                // type of this executable
                val = e.getAttribute("type", "bin");
                if ("jar".equalsIgnoreCase(val))
                {
                    executable.type = ExecutableFile.JAR;
                    executable.mainClass = e.getAttribute("class"); // executable
                    // class
                }

                // what to do if execution fails
                val = e.getAttribute("failure", "ask");
                if ("abort".equalsIgnoreCase(val))
                    executable.onFailure = ExecutableFile.ABORT;
                else if ("warn".equalsIgnoreCase(val)) executable.onFailure = ExecutableFile.WARN;

                // whether to keep the executable after executing it
                val = e.getAttribute("keep");
                executable.keepFile = "true".equalsIgnoreCase(val);

                // get arguments for this executable
                XMLElement args = e.getFirstChildNamed("args");
                if (null != args)
                {
                    Iterator argIterator = args.getChildrenNamed("arg").iterator();
                    while (argIterator.hasNext())
                    {
                        XMLElement arg = (XMLElement) argIterator.next();
                        executable.argList.add(requireAttribute(arg, "value"));
                    }
                }

                executable.osList = OsConstraint.getOsList(e); // TODO:
                // unverified

                pack.addExecutable(executable);
            }

            // We get the files list
            iter = el.getChildrenNamed("file").iterator();
            while (iter.hasNext())
            {
                XMLElement f = (XMLElement) iter.next();
                String src = requireAttribute(f, "src");
                String targetdir = requireAttribute(f, "targetdir");
                List osList = OsConstraint.getOsList(f); // TODO: unverified
                int override = getOverrideValue(f);
                Map additionals = getAdditionals(f);
                boolean unpack = src.endsWith(".zip") && "true".equalsIgnoreCase(f.getAttribute("unpack"));

                File file = new File(src);
                if (!file.isAbsolute()) file = new File(basedir, src);

                try
                {
                    if (unpack)
                        addArchiveContent(baseDir, file, targetdir, osList, override, pack, additionals);
                    else
                        addRecursively(baseDir, file, targetdir, osList, override, pack, additionals);
                }
                catch (Exception x)
                {
                    parseError(f, x.getMessage(), x);
                }
            }

            // We get the singlefiles list
            iter = el.getChildrenNamed("singlefile").iterator();
            while (iter.hasNext())
            {
                XMLElement f = (XMLElement) iter.next();
                String src = requireAttribute(f, "src");
                String target = requireAttribute(f, "target");
                List osList = OsConstraint.getOsList(f); // TODO: unverified
                int override = getOverrideValue(f);
                Map additionals = getAdditionals(f);

                File file = new File(src);
                if (!file.isAbsolute()) file = new File(basedir, src);

                try
                {
                    pack.addFile(baseDir, file, target, osList, override, additionals);
                }
                catch (FileNotFoundException x)
                {
                    parseError(f, x.getMessage(), x);
                }
            }

            // We get the fileset list
            iter = el.getChildrenNamed("fileset").iterator();
            while (iter.hasNext())
            {
                XMLElement f = (XMLElement) iter.next();
                String dir_attr = requireAttribute(f, "dir");

                File dir = new File(dir_attr);
                if (!dir.isAbsolute()) dir = new File(basedir, dir_attr);
                if (!dir.isDirectory()) // also tests '.exists()'
                    parseError(f, "Invalid directory 'dir': " + dir_attr);

                boolean casesensitive = validateYesNoAttribute(f, "casesensitive", YES);
                boolean defexcludes = validateYesNoAttribute(f, "defaultexcludes", YES);
                String targetdir = requireAttribute(f, "targetdir");
                List osList = OsConstraint.getOsList(f); // TODO: unverified
                int override = getOverrideValue(f);
                Map additionals = getAdditionals(f);

                // get includes and excludes
                Vector xcludesList = null;
                String[] includes = null;
                xcludesList = f.getChildrenNamed("include");
                if (!xcludesList.isEmpty())
                {
                    includes = new String[xcludesList.size()];
                    for (int j = 0; j < xcludesList.size(); j++)
                    {
                        XMLElement xclude = (XMLElement) xcludesList.get(j);
                        includes[j] = requireAttribute(xclude, "name");
                    }
                }
                String[] excludes = null;
                xcludesList = f.getChildrenNamed("exclude");
                if (!xcludesList.isEmpty())
                {
                    excludes = new String[xcludesList.size()];
                    for (int j = 0; j < xcludesList.size(); j++)
                    {
                        XMLElement xclude = (XMLElement) xcludesList.get(j);
                        excludes[j] = requireAttribute(xclude, "name");
                    }
                }

                // parse additional fileset attributes "includes" and "excludes"
                String[] toDo = new String[] { "includes", "excludes"};
                // use the existing containers filled from include and exclude
                // and add the includes and excludes to it
                String[][] containers = new String[][] { includes, excludes};
                for (int j = 0; j < toDo.length; ++j)
                {
                    String inex = f.getAttribute(toDo[j]);
                    if (inex != null && inex.length() > 0)
                    { // This is the same "splitting" as ant PatternSet do ...
                        StringTokenizer tok = new StringTokenizer(inex, ", ", false);
                        int newSize = tok.countTokens();
                        int k = 0;
                        String[] nCont = null;
                        if (containers[j] != null && containers[j].length > 0)
                        { // old container exist; create a new which can hold
                            // all values
                            // and copy the old stuff to the front
                            newSize += containers[j].length;
                            nCont = new String[newSize];
                            for (; k < containers[j].length; ++k)
                                nCont[k] = containers[j][k];
                        }
                        if (nCont == null) // No container for old values
                            // created,
                            // create a new one.
                            nCont = new String[newSize];
                        for (; k < newSize; ++k)
                            // Fill the new one or expand the existent container
                            nCont[k] = tok.nextToken();
                        containers[j] = nCont;
                    }
                }
                includes = containers[0]; // push the new includes to the
                // local var
                excludes = containers[1]; // push the new excludes to the
                // local var

                // scan and add fileset
                DirectoryScanner ds = new DirectoryScanner();
                ds.setIncludes(includes);
                ds.setExcludes(excludes);
                if (defexcludes) ds.addDefaultExcludes();
                ds.setBasedir(dir);
                ds.setCaseSensitive(casesensitive);
                ds.scan();

                String[] files = ds.getIncludedFiles();
                String[] dirs = ds.getIncludedDirectories();

                // Directory scanner has done recursion, add files and
                // directories
                for (int i = 0; i < files.length; ++i)
                {
                    try
                    {
                        String target = new File(targetdir, files[i]).getPath();
                        pack
                                .addFile(baseDir, new File(dir, files[i]), target, osList, override,
                                        additionals);
                    }
                    catch (FileNotFoundException x)
                    {
                        parseError(f, x.getMessage(), x);
                    }
                }
                for (int i = 0; i < dirs.length; ++i)
                {
                    try
                    {
                        String target = new File(targetdir, dirs[i]).getPath();
                        pack.addFile(baseDir, new File(dir, dirs[i]), target, osList, override, additionals);
                    }
                    catch (FileNotFoundException x)
                    {
                        parseError(f, x.getMessage(), x);
                    }
                }
            }

            // get the updatechecks list
            iter = el.getChildrenNamed("updatecheck").iterator();
            while (iter.hasNext())
            {
                XMLElement f = (XMLElement) iter.next();

                String casesensitive = f.getAttribute("casesensitive");

                // get includes and excludes
                ArrayList includesList = new ArrayList();
                ArrayList excludesList = new ArrayList();

                // get includes and excludes
                Iterator include_it = f.getChildrenNamed("include").iterator();
                while (include_it.hasNext())
                {
                    XMLElement inc_el = (XMLElement) include_it.next();
                    includesList.add(requireAttribute(inc_el, "name"));
                }

                Iterator exclude_it = f.getChildrenNamed("exclude").iterator();
                while (exclude_it.hasNext())
                {
                    XMLElement excl_el = (XMLElement) exclude_it.next();
                    excludesList.add(requireAttribute(excl_el, "name"));
                }

                pack.addUpdateCheck(new UpdateCheck(includesList, excludesList, casesensitive));
            }
            // We get the dependencies
            iter = el.getChildrenNamed("depends").iterator();
            while (iter.hasNext())
            {
                XMLElement dep = (XMLElement) iter.next();
                String depName = requireAttribute(dep, "packname");
                pack.addDependency(depName);

            }
            // We add the pack
            compiler.addPack(pack);
        }
        
        compiler.checkDependencies();
        compiler.checkExcludes();

        notifyCompilerListener("addPacks", CompilerListener.END, data);
    }

    /**
     * Checks whether the dependencies stated in the configuration file are correct. Specifically it
     * checks that no pack point to a non existent pack and also that there are no circular
     * dependencies in the packs.
     */
    public void checkDependencies(List packs) throws CompilerException
    {
        // Because we use package names in the configuration file we assosiate
        // the names with the objects
        Map names = new HashMap();
        for (int i = 0; i < packs.size(); i++)
        {
            PackInfo pack = (PackInfo) packs.get(i);
            names.put(pack.getPack().name, pack);
        }
        int result = dfs(packs, names);
        // @todo More informative messages to include the source of the error
        if (result == -2)
            parseError("Circular dependency detected");
        else if (result == -1) parseError("A dependency doesn't exist");
    }

    /**
     * We use the dfs graph search algorithm to check whether the graph is acyclic as described in:
     * Thomas H. Cormen, Charles Leiserson, Ronald Rivest and Clifford Stein. Introduction to
     * algorithms 2nd Edition 540-549,MIT Press, 2001
     * 
     * @param packs The graph
     * @param names The name map
     */
    private int dfs(List packs, Map names)
    {
        Map edges = new HashMap();
        for (int i = 0; i < packs.size(); i++)
        {
            PackInfo pack = (PackInfo) packs.get(i);
            if (pack.colour == PackInfo.WHITE)
            {
                if (dfsVisit(pack, names, edges) != 0) return -1;
            }

        }
        return checkBackEdges(edges);
    }

    /**
     * This function checks for the existence of back edges.
     */
    private int checkBackEdges(Map edges)
    {
        Set keys = edges.keySet();
        for (Iterator iterator = keys.iterator(); iterator.hasNext();)
        {
            final Object key = iterator.next();
            int color = ((Integer) edges.get(key)).intValue();
            if (color == PackInfo.GREY) { return -2; }
        }
        return 0;

    }

    /**
     * This class is used for the classification of the edges
     */
    private class Edge
    {

        PackInfo u;

        PackInfo v;

        Edge(PackInfo u, PackInfo v)
        {
            this.u = u;
            this.v = v;
        }
    }

    private int dfsVisit(PackInfo u, Map names, Map edges)
    {
        u.colour = PackInfo.GREY;
        List deps = u.getDependencies();
        if (deps != null)
        {
            for (int i = 0; i < deps.size(); i++)
            {
                String name = (String) deps.get(i);
                PackInfo v = (PackInfo) names.get(name);
                if (v == null)
                {
                    System.out.println("Failed to find dependency: "+name);
                    return -1;
                }
                Edge edge = new Edge(u, v);
                if (edges.get(edge) == null) edges.put(edge, new Integer(v.colour));

                if (v.colour == PackInfo.WHITE)
                {

                    final int result = dfsVisit(v, names, edges);
                    if (result != 0) return result;
                }
            }
        }
        u.colour = PackInfo.BLACK;
        return 0;
    }

    /**
     * Add files in an archive to a pack
     * @param archive the archive file to unpack
     * @parm targetdir the target directory where the content of the archive will be installed
     * @param osList The target OS constraints.
     * @param override Overriding behaviour.
     * @param pack Pack to be packed into
     * @param additionals Map which contains additional data
     */
    protected void addArchiveContent(File baseDir, File archive, String targetdir, List osList, int override, PackInfo pack, Map additionals) throws IOException {
      
      FileInputStream fin = new FileInputStream(archive);
      ZipInputStream zin = new ZipInputStream(fin);
      while (true) {
        ZipEntry zentry = zin.getNextEntry();
        if (zentry==null) break;
        if (zentry.isDirectory()) continue;
        
        try {
            File temp = File.createTempFile("izpack", null);
            temp.deleteOnExit();
            
            FileOutputStream out = new FileOutputStream(temp);
            PackagerHelper.copyStream(zin, out);
            out.close();
        
            pack.addFile(baseDir, temp, targetdir + "/" + zentry.getName(), osList, override, additionals);
        } catch (IOException e) {
            throw new IOException("Couldn't create temporary file for "+zentry.getName()+" in archive "+archive+" ("+e.getMessage()+")");
        }
        
      }
      fin.close();
    }
    
    /**
     * Recursive method to add files in a pack.
     * 
     * @param file The file to add.
     * @param targetdir The relative path to the parent.
     * @param osList The target OS constraints.
     * @param override Overriding behaviour.
     * @param pack Pack to be packed into
     * @param additionals Map which contains additional data
     * @exception FileNotFoundException if the file does not exist
     */
    protected void addRecursively(File baseDir, File file, String targetdir, List osList, int override,
            PackInfo pack, Map additionals) throws IOException
    {
        String targetfile = targetdir + "/" + file.getName();
        if (!file.isDirectory())
            pack.addFile(baseDir, file, targetfile, osList, override, additionals);
        else
        {
            File[] files = file.listFiles();
            if (files.length == 0) // The directory is empty so must be added
                pack.addFile(baseDir, file, targetfile, osList, override, additionals);
            else
            {
                // new targetdir = targetfile;
                for (int i = 0; i < files.length; i++)
                    addRecursively(baseDir, files[i], targetfile, osList, override, pack, additionals);
            }
        }
    }

    /**
     * Parse panels and their paramters, locate the panels resources and add to the Packager.
     * 
     * @param data The XML data.
     * @exception CompilerException Description of the Exception
     */
    protected void addPanels(XMLElement data) throws CompilerException
    {
        notifyCompilerListener("addPanels", CompilerListener.BEGIN, data);
        XMLElement root = requireChildNamed(data, "panels");

        // at least one panel is required
        Vector panels = root.getChildrenNamed("panel");
        if (panels.isEmpty()) parseError(root, "<panels> requires a <panel>");

        // We process each panel markup
        Iterator iter = panels.iterator();
        while (iter.hasNext())
        {
            XMLElement xmlPanel = (XMLElement) iter.next();

            // create the serialized Panel data
            Panel panel = new Panel();
            panel.osConstraints = OsConstraint.getOsList(xmlPanel);
            String className = xmlPanel.getAttribute("classname");
            // add an id
            String panelid = xmlPanel.getAttribute("id");
            panel.setPanelid(panelid);                   
            // Panel files come in jars packaged w/ IzPack
            String jarPath = "bin/panels/" + className + ".jar";
            URL url = findIzPackResource(jarPath, "Panel jar file", xmlPanel);
            String fullClassName = null;
            try
            {
                fullClassName = getFullClassName(url, className);
            }
            catch (IOException e)
            {
            }
            if (fullClassName != null)
                panel.className = fullClassName;
            else
                panel.className = className;
            // insert into the packager
            compiler.addPanelJar(panel, url);
        }
        notifyCompilerListener("addPanels", CompilerListener.END, data);
    }

    /**
     * Adds the resources.
     * 
     * @param data The XML data.
     * @exception CompilerException Description of the Exception
     */
    protected void addResources(XMLElement data) throws CompilerException
    {
        notifyCompilerListener("addResources", CompilerListener.BEGIN, data);
        XMLElement root = data.getFirstChildNamed("resources");
        if (root == null) return;

        // We process each res markup
        Iterator iter = root.getChildrenNamed("res").iterator();
        while (iter.hasNext())
        {
            XMLElement res = (XMLElement) iter.next();
            String id = requireAttribute(res, "id");
            String src = requireAttribute(res, "src");
            boolean parse = validateYesNoAttribute(res, "parse", NO);

            // basedir is not prepended if src is already an absolute path
            URL url = findProjectResource(src, "Resource", res);

            // substitute variable values in the resource if parsed
            if (parse)
            {
                if (compiler.getVariables().isEmpty())
                {
                    parseWarn(res, "No variables defined. " + url.getPath() + " not parsed.");
                }
                else
                {
                    String type = res.getAttribute("type");
                    String encoding = res.getAttribute("encoding");
                    File parsedFile = null;

                    try
                    {
                        // make the substitutions into a temp file
                        InputStream bin = new BufferedInputStream(url.openStream());

                        parsedFile = File.createTempFile("izpp", null);
                        parsedFile.deleteOnExit();
                        FileOutputStream outFile = new FileOutputStream(parsedFile);
                        BufferedOutputStream bout = new BufferedOutputStream(outFile);

                        VariableSubstitutor vs = new VariableSubstitutor(compiler.getVariables());
                        vs.substitute(bin, bout, type, encoding);
                        bin.close();
                        bout.close();

                        // and specify the substituted file to be added to the
                        // packager
                        url = parsedFile.toURL();
                    }
                    catch (IOException x)
                    {
                        parseError(res, x.getMessage(), x);
                    }
                }
            }

            compiler.addResource(id, url);
        }
        notifyCompilerListener("addResources", CompilerListener.END, data);
    }

    /**
     * Adds the ISO3 codes of the langpacks and associated resources.
     * 
     * @param data The XML data.
     * @exception CompilerException Description of the Exception
     */
    protected void addLangpacks(XMLElement data) throws CompilerException
    {
        notifyCompilerListener("addLangpacks", CompilerListener.BEGIN, data);
        XMLElement root = requireChildNamed(data, "locale");

        // at least one langpack is required
        Vector locals = root.getChildrenNamed("langpack");
        if (locals.isEmpty()) parseError(root, "<locale> requires a <langpack>");

        // We process each langpack markup
        Iterator iter = locals.iterator();
        while (iter.hasNext())
        {
            XMLElement el = (XMLElement) iter.next();
            String iso3 = requireAttribute(el, "iso3");
            String path;

            path = "bin/langpacks/installer/" + iso3 + ".xml";
            URL iso3xmlURL = findIzPackResource(path, "ISO3 file", el);

            path = "bin/langpacks/flags/" + iso3 + ".gif";
            URL iso3FlagURL = findIzPackResource(path, "ISO3 flag image", el);

            compiler.addLangPack(iso3, iso3xmlURL, iso3FlagURL);
        }
        notifyCompilerListener("addLangpacks", CompilerListener.END, data);
    }

    /**
     * Builds the Info class from the XML tree.
     * 
     * @param data The XML data. return The Info.
     * @exception Exception Description of the Exception
     */
    protected void addInfo(XMLElement data) throws Exception
    {
        notifyCompilerListener("addInfo", CompilerListener.BEGIN, data);
        // Initialisation
        XMLElement root = requireChildNamed(data, "info");

        Info info = new Info();
        info.setAppName(requireContent(requireChildNamed(root, "appname")));
        info.setAppVersion(requireContent(requireChildNamed(root, "appversion")));
        // We get the installation subpath
        XMLElement subpath = root.getFirstChildNamed("appsubpath");
        if (subpath != null)
        {
            info.setInstallationSubPath(requireContent(subpath));
        }

        // validate and insert app URL
        final XMLElement URLElem = root.getFirstChildNamed("url");
        if (URLElem != null)
        {
            URL appURL = requireURLContent(URLElem);
            info.setAppURL(appURL.toString());
        }

        // We get the authors list
        XMLElement authors = root.getFirstChildNamed("authors");
        if (authors != null)
        {
            Iterator iter = authors.getChildrenNamed("author").iterator();
            while (iter.hasNext())
            {
                XMLElement author = (XMLElement) iter.next();
                String name = requireAttribute(author, "name");
                String email = requireAttribute(author, "email");
                info.addAuthor(new Info.Author(name, email));
            }
        }

        // We get the java version required
        XMLElement javaVersion = root.getFirstChildNamed("javaversion");
        if (javaVersion != null) info.setJavaVersion(requireContent(javaVersion));

        // validate and insert (and require if -web kind) web dir
        XMLElement webDirURL = root.getFirstChildNamed("webdir");
        if (webDirURL != null) info.setWebDirURL(requireURLContent(webDirURL).toString());
        String kind = compiler.getKind();
        if (kind != null)
        {
            if (kind.equalsIgnoreCase(WEB) && webDirURL == null)
            {
                parseError(root, "<webdir> required when \"WEB\" installer requested");
            }
            else if (kind.equalsIgnoreCase(STANDARD) && webDirURL != null)
            {
                // Need a Warning? parseWarn(webDirURL, "Not creating web
                // installer.");
                info.setWebDirURL(null);
            }
        }

        // Add the uninstaller as a resource if specified
        XMLElement uninstallInfo = root.getFirstChildNamed("uninstaller");
        if (validateYesNoAttribute(uninstallInfo, "write", YES))
        {
            URL url = findIzPackResource("lib/uninstaller.jar", "Uninstaller", root);
            compiler.addResource("IzPack.uninstaller", url);

            if (uninstallInfo != null)
            {
                String uninstallerName = uninstallInfo.getAttribute("name");
                if (uninstallerName != null && uninstallerName.endsWith(".jar")
                        && uninstallerName.length() > ".jar".length())
                    info.setUninstallerName(uninstallerName);
            }
        }
        // Add the path for the summary log file if specified
        XMLElement slfPath = root.getFirstChildNamed("summarylogfilepath");
        if (slfPath != null) info.setSummaryLogFilePath(requireContent(slfPath));

        // look for an unpacker class
        String unpackerclass = compiler.getProperty("UNPACKER_CLASS");
        info.setUnpackerClassName(unpackerclass);
        compiler.setInfo(info);
        notifyCompilerListener("addInfo", CompilerListener.END, data);
    }

    /**
     * Variable declaration is a fragment of the xml file. For example:
     * 
     * <pre>
     * 
     *  
     *   
     *    
     *        &lt;variables&gt;
     *          &lt;variable name=&quot;nom&quot; value=&quot;value&quot;/&gt;
     *          &lt;variable name=&quot;foo&quot; value=&quot;pippo&quot;/&gt;
     *        &lt;/variables&gt;
     *      
     *    
     *   
     *  
     * </pre>
     * 
     * variable declared in this can be referred to in parsable files.
     * 
     * @param data The XML data.
     * @exception CompilerException Description of the Exception
     */
    protected void addVariables(XMLElement data) throws CompilerException
    {
        notifyCompilerListener("addVariables", CompilerListener.BEGIN, data);
        // We get the varible list
        XMLElement root = data.getFirstChildNamed("variables");
        if (root == null) return;

        Properties variables = compiler.getVariables();

        Iterator iter = root.getChildrenNamed("variable").iterator();
        while (iter.hasNext())
        {
            XMLElement var = (XMLElement) iter.next();
            String name = requireAttribute(var, "name");
            String value = requireAttribute(var, "value");
            if (variables.contains(name))
                parseWarn(var, "Variable '" + name + "' being overwritten");
            variables.setProperty(name, value);
        }
        notifyCompilerListener("addVariables", CompilerListener.END, data);
    }

    /**
     * Properties declaration is a fragment of the xml file. For example:
     * 
     * <pre>
     * 
     *  
     *   
     *    
     *        &lt;properties&gt;
     *          &lt;property name=&quot;app.name&quot; value=&quot;Property Laden Installer&quot;/&gt;
     *          &lt;!-- Ant styles 'location' and 'refid' are not yet supported --&gt;
     *          &lt;property file=&quot;filename-relative-to-install?&quot;/&gt;
     *          &lt;property file=&quot;filename-relative-to-install?&quot; prefix=&quot;prefix&quot;/&gt;
     *          &lt;!-- Ant style 'url' and 'resource' are not yet supported --&gt;
     *          &lt;property environment=&quot;prefix&quot;/&gt;
     *        &lt;/properties&gt;
     *      
     *    
     *   
     *  
     * </pre>
     * 
     * variable declared in this can be referred to in parsable files.
     * 
     * @param data The XML data.
     * @exception CompilerException Description of the Exception
     */
    protected void substituteProperties(XMLElement data) throws CompilerException
    {
        notifyCompilerListener("substituteProperties", CompilerListener.BEGIN, data);

        XMLElement root = data.getFirstChildNamed("properties");
        if (root != null)
        {
            // add individual properties
            Iterator iter = root.getChildrenNamed("property").iterator();
            while (iter.hasNext())
            {
                XMLElement prop = (XMLElement) iter.next();
                Property property = new Property(prop, this);
                property.execute();
            }
        }

        // temporarily remove the 'properties' branch, replace all properties in
        // the remaining DOM, and replace properties branch.
        // TODO: enhance XMLElement with an "indexOf(XMLElement)" method
        // and addChild(XMLElement, int) so returns to the same place.
        if (root != null) data.removeChild(root);

        substituteAllProperties(data);
        if (root != null) data.addChild(root);

        notifyCompilerListener("substituteProperties", CompilerListener.END, data);
    }

    /**
     * Perform recursive substitution on all properties
     */
    protected void substituteAllProperties(XMLElement element) throws CompilerException
    {
        Enumeration attributes = element.enumerateAttributeNames();
        while (attributes.hasMoreElements())
        {
            String name = (String) attributes.nextElement();
            String value = compiler.replaceProperties(element.getAttribute(name));
            element.setAttribute(name, value);
        }

        String content = element.getContent();
        if (content != null)
        {
            element.setContent(compiler.replaceProperties(content));
        }

        Enumeration children = element.enumerateChildren();
        while (children.hasMoreElements())
        {
            XMLElement child = (XMLElement) children.nextElement();
            substituteAllProperties(child);
        }
    }

    /**
     * Returns the XMLElement representing the installation XML file.
     * 
     * @return The XML tree.
     * @exception CompilerException For problems with the installation file
     * @exception IOException for errors reading the installation file
     */
    protected XMLElement getXMLTree() throws CompilerException, IOException
    {
        // Initialises the parser
        IXMLReader reader = null;
        if( filename != null )
        {
            File file = new File(filename).getAbsoluteFile();
            if (!file.canRead()) throw new CompilerException("Invalid file: " + file);
            reader = new StdXMLReader(new FileInputStream(filename));
            // add izpack built in property
            compiler.setProperty("izpack.file", file.toString());
        }
        else if( installText != null )
        {
            reader = StdXMLReader.stringReader(installText);
        }
        else
        {
            throw new CompilerException("Neither install file or text specified");
        }

        StdXMLParser parser = new StdXMLParser();
        parser.setBuilder(new StdXMLBuilder());
        parser.setReader(reader);
        parser.setValidator(new NonValidator());

        // We get it
        XMLElement data = null;
        try
        {
            data = (XMLElement) parser.parse();
        }
        catch (Exception x)
        {
            throw new CompilerException("Error parsing installation file", x);
        }

        // We check it
        if (!"installation".equalsIgnoreCase(data.getName()))
            parseError(data, "this is not an IzPack XML installation file");
        if (!requireAttribute(data, "version").equalsIgnoreCase(VERSION))
            parseError(data, "the file version is different from the compiler version");

        // We finally return the tree
        return data;
    }

    protected int getOverrideValue(XMLElement f) throws CompilerException
    {
        int override = PackFile.OVERRIDE_UPDATE;

        String override_val = f.getAttribute("override");
        if (override_val != null)
        {
            if ("true".equalsIgnoreCase(override_val))
            {
                override = PackFile.OVERRIDE_TRUE;
            }
            else if ("false".equalsIgnoreCase(override_val))
            {
                override = PackFile.OVERRIDE_FALSE;
            }
            else if ("asktrue".equalsIgnoreCase(override_val))
            {
                override = PackFile.OVERRIDE_ASK_TRUE;
            }
            else if ("askfalse".equalsIgnoreCase(override_val))
            {
                override = PackFile.OVERRIDE_ASK_FALSE;
            }
            else if ("update".equalsIgnoreCase(override_val))
            {
                override = PackFile.OVERRIDE_UPDATE;
            }
            else
                parseError(f, "invalid value for attribute \"override\"");
        }

        return override;
    }

    /**
     * Look for a project specified resources, which, if not absolute, are sought relative to the
     * projects basedir. The path should use '/' as the fileSeparator. If the resource is not found,
     * a CompilerException is thrown indicating fault in the parent element.
     * 
     * @param path the relative path (using '/' as separator) to the resource.
     * @param desc the description of the resource used to report errors
     * @param parent the XMLElement the resource is specified in, used to report errors
     * @return a URL to the resource.
     */
    private URL findProjectResource(String path, String desc, XMLElement parent)
            throws CompilerException
    {
        URL url = null;
        File resource = new File(path);
        if (!resource.isAbsolute()) resource = new File(basedir, path);

        if (!resource.exists()) // fatal
            parseError(parent, desc + " not found: " + resource);

        try
        {
            url = resource.toURL();
        }
        catch (MalformedURLException how)
        {
            parseError(parent, desc + "(" + resource + ")", how);
        }

        return url;
    }

    /**
     * Look for an IzPack resource either in the compiler jar, or within IZPACK_HOME. The path must
     * not be absolute. The path must use '/' as the fileSeparator (it's used to access the jar
     * file). If the resource is not found, a CompilerException is thrown indicating fault in the
     * parent element.
     * 
     * @param path the relative path (using '/' as separator) to the resource.
     * @param desc the description of the resource used to report errors
     * @param parent the XMLElement the resource is specified in, used to report errors
     * @return a URL to the resource.
     */
    private URL findIzPackResource(String path, String desc, XMLElement parent)
            throws CompilerException
    {
        URL url = getClass().getResource("/" + path);
        if (url == null)
        {
            File resource = new File(path);

            if (!resource.isAbsolute()) resource = new File(Compiler.IZPACK_HOME, path);

            if (!resource.exists()) // fatal
                parseError(parent, desc + " not found: " + resource);

            try
            {
                url = resource.toURL();
            }
            catch (MalformedURLException how)
            {
                parseError(parent, desc + "(" + resource + ")", how);
            }
        }

        return url;
    }

    /**
     * Create parse error with consistent messages. Includes file name. For use When parent is
     * unknown.
     * 
     * @param message Brief message explaining error
     */
    protected void parseError(String message) throws CompilerException
    {
        throw new CompilerException(filename + ":" + message);
    }

    /**
     * Create parse error with consistent messages. Includes file name and line # of parent. It is
     * an error for 'parent' to be null.
     * 
     * @param parent The element in which the error occured
     * @param message Brief message explaining error
     */
    protected void parseError(XMLElement parent, String message) throws CompilerException
    {
        throw new CompilerException(filename + ":" + parent.getLineNr() + ": " + message);
    }

    /**
     * Create a chained parse error with consistent messages. Includes file name and line # of
     * parent. It is an error for 'parent' to be null.
     * 
     * @param parent The element in which the error occured
     * @param message Brief message explaining error
     */
    protected void parseError(XMLElement parent, String message, Throwable cause)
            throws CompilerException
    {
        throw new CompilerException(filename + ":" + parent.getLineNr() + ": " + message, cause);
    }

    /**
     * Create a parse warning with consistent messages. Includes file name and line # of parent. It
     * is an error for 'parent' to be null.
     * 
     * @param parent The element in which the warning occured
     * @param message Warning message
     */
    protected void parseWarn(XMLElement parent, String message)
    {
        System.out.println(filename + ":" + parent.getLineNr() + ": " + message);
    }

    /**
     * Call getFirstChildNamed on the parent, producing a meaningful error message on failure. It is
     * an error for 'parent' to be null.
     * 
     * @param parent The element to search for a child
     * @param name Name of the child element to get
     */
    protected XMLElement requireChildNamed(XMLElement parent, String name) throws CompilerException
    {
        XMLElement child = parent.getFirstChildNamed(name);
        if (child == null)
            parseError(parent, "<" + parent.getName() + "> requires child <" + name + ">");
        return child;
    }

    /**
     * Call getContent on an element, producing a meaningful error message if not present, or empty,
     * or a valid URL. It is an error for 'element' to be null.
     * 
     * @param element The element to get content of
     */
    protected URL requireURLContent(XMLElement element) throws CompilerException
    {
        URL url = null;
        try
        {
            url = new URL(requireContent(element));
        }
        catch (MalformedURLException x)
        {
            parseError(element, "<" + element.getName() + "> requires valid URL", x);
        }
        return url;
    }

    /**
     * Call getContent on an element, producing a meaningful error message if not present, or empty.
     * It is an error for 'element' to be null.
     * 
     * @param element The element to get content of
     */
    protected String requireContent(XMLElement element) throws CompilerException
    {
        String content = element.getContent();
        if (content == null || content.length() == 0)
            parseError(element, "<" + element.getName() + "> requires content");
        return content;
    }

    /**
     * Call getAttribute on an element, producing a meaningful error message if not present, or
     * empty. It is an error for 'element' or 'attribute' to be null.
     * 
     * @param element The element to get the attribute value of
     * @param attribute The name of the attribute to get
     */
    protected String requireAttribute(XMLElement element, String attribute)
            throws CompilerException
    {
        String value = element.getAttribute(attribute);
        if (value == null)
            parseError(element, "<" + element.getName() + "> requires attribute '" + attribute
                    + "'");
        return value;
    }

    /**
     * Get a required attribute of an element, ensuring it is an integer. A meaningful error message
     * is generated as a CompilerException if not present or parseable as an int. It is an error for
     * 'element' or 'attribute' to be null.
     * 
     * @param element The element to get the attribute value of
     * @param attribute The name of the attribute to get
     */
    protected int requireIntAttribute(XMLElement element, String attribute)
            throws CompilerException
    {
        String value = element.getAttribute(attribute);
        if (value == null || value.length() == 0)
            parseError(element, "<" + element.getName() + "> requires attribute '" + attribute
                    + "'");
        try
        {
            return Integer.parseInt(value);
        }
        catch (NumberFormatException x)
        {
            parseError(element, "'" + attribute + "' must be an integer");
        }
        return 0; // never happens
    }

    /**
     * Call getAttribute on an element, producing a meaningful error message if not present, or one
     * of "yes" or "no". It is an error for 'element' or 'attribute' to be null.
     * 
     * @param element The element to get the attribute value of
     * @param attribute The name of the attribute to get
     */
    protected boolean requireYesNoAttribute(XMLElement element, String attribute)
            throws CompilerException
    {
        String value = requireAttribute(element, attribute);
        if ("yes".equalsIgnoreCase(value)) return true;
        if ("no".equalsIgnoreCase(value)) return false;

        parseError(element, "<" + element.getName() + "> invalid attribute '" + attribute
                + "': Expected (yes|no)");

        return false; // never happens
    }

    /**
     * Call getAttribute on an element, producing a meaningful warning if not "yes" or "no". If the
     * 'element' or 'attribute' are null, the default value is returned.
     * 
     * @param element The element to get the attribute value of
     * @param attribute The name of the attribute to get
     * @param defaultValue Value returned if attribute not present or invalid
     */
    protected boolean validateYesNoAttribute(XMLElement element, String attribute,
            boolean defaultValue)
    {
        if (element == null) return defaultValue;

        String value = element.getAttribute(attribute, (defaultValue ? "yes" : "no"));
        if ("yes".equalsIgnoreCase(value)) return true;
        if ("no".equalsIgnoreCase(value)) return false;

        // TODO: should this be an error if it's present but "none of the
        // above"?
        parseWarn(element, "<" + element.getName() + "> invalid attribute '" + attribute
                + "': Expected (yes|no) if present");

        return defaultValue;
    }

    /**
     * The main method if the compiler is invoked by a command-line call.
     * 
     * @param args The arguments passed on the command-line.
     */
    public static void main(String[] args)
    {
        // Outputs some informations
        System.out.println("");
        System.out.println(".::  IzPack - Version " + Compiler.IZPACK_VERSION + " ::.");
        System.out.println("");
        System.out.println("< compiler specifications version: " + VERSION + " >");
        System.out.println("");
        System.out.println("- Copyright (C) 2001-2007 Julien Ponge");
        System.out.println("- Visit http://izpack.org/ for the latest releases");
        System.out.println("- Released under the terms of the Apache Software License version 2.0.");
        System.out.println("");

        // exit code 1 means: error
        int exitCode = 1;
        String home = ".";
        // We get the IzPack home directory 
        String izHome = System.getProperty("IZPACK_HOME");
        if (izHome != null) home = izHome;
                    
        // We analyse the command line parameters
        try
        {
            // Our arguments
            String filename;
            String base = ".";
            String kind = "standard";
            String output;
            String compr_format = "default";
            int compr_level = -1;

            // First check
            int nArgs = args.length;
            if (nArgs < 1) throw new Exception("no arguments given");

            // The users wants to know the command line parameters
            if ("-?".equalsIgnoreCase(args[0]))
            {
                System.out.println("-> Command line parameters are : (xml file) [args]");
                System.out.println("   (xml file): the xml file describing the installation");
                System.out.println("   -h (IzPack home) : the root path of IzPack. This will be needed");
                System.out.println("               if the compiler is not called in the root directory  of IzPack.");
                System.out.println("               Do not forget quotations if there are blanks in the path.");
                         System.out
                          .println("   -b (base) : indicates the base path that the compiler will use for filenames");
                System.out.println("               of sources. Default is the current path. Attend to -h.");
                System.out.println("   -k (kind) : indicates the kind of installer to generate");
                System.out.println("               default is standard");
                System.out.println("   -o (out)  : indicates the output file name");
                System.out.println("               default is the xml file name\n");
                System.out
                        .println("   -c (compression)  : indicates the compression format to be used for packs");
                System.out.println("               default is the internal deflate compression\n");
                System.out
                        .println("   -l (compression-level)  : indicates the level for the used compression format");
                System.out.println("                if supported. Only integer are valid\n");

                System.out
                        .println("   When using vm option -DSTACKTRACE=true there is all kind of debug info ");
                System.out.println("");
                exitCode = 0;
            }
            else
            {
                // We can parse the other parameters & try to compile the
                // installation

                // We get the input file name and we initialize the output file
                // name
                filename = args[0];
                // default jar files names are based on input file name
                output = filename.substring(0, filename.length() - 3) + "jar";

                // We parse the other ones
                int pos = 1;
                while (pos < nArgs)
                    if ((args[pos].startsWith("-")) && (args[pos].length() == 2))
                    {
                        switch (args[pos].toLowerCase().charAt(1))
                        {
                        case 'b':
                            if ((pos + 1) < nArgs)
                            {
                                pos++;
                                base = args[pos];
                            }
                            else
                                throw new Exception("base argument missing");
                            break;
                        case 'k':
                            if ((pos + 1) < nArgs)
                            {
                                pos++;
                                kind = args[pos];
                            }
                            else
                                throw new Exception("kind argument missing");
                            break;
                        case 'o':
                            if ((pos + 1) < nArgs)
                            {
                                pos++;
                                output = args[pos];
                            }
                            else
                                throw new Exception("output argument missing");
                            break;
                        case 'c':
                            if ((pos + 1) < nArgs)
                            {
                                pos++;
                                compr_format = args[pos];
                            }
                            else
                                throw new Exception("compression format argument missing");
                            break;
                        case 'l':
                            if ((pos + 1) < nArgs)
                            {
                                pos++;
                                compr_level = Integer.parseInt(args[pos]);
                            }
                            else
                                throw new Exception("compression level argument missing");
                            break;
                        case 'h':
                            if ((pos + 1) < nArgs)
                            {
                                pos++;
                                home = args[pos];
                            }
                            else
                                throw new Exception("IzPack home path argument missing");
                            break;
                        default:
                            throw new Exception("unknown argument");
                        }
                        pos++;
                    }
                    else
                        throw new Exception("bad argument");

                home = resolveIzPackHome(home);
                // Outputs what we are going to do
                System.out.println("-> Processing  : " + filename);
                System.out.println("-> Output      : " + output);
                System.out.println("-> Base path   : " + base);
                System.out.println("-> Kind        : " + kind);
                System.out.println("-> Compression : " + compr_format);
                System.out.println("-> Compr. level: " + compr_level);
                System.out.println("-> IzPack home : " + home);
                System.out.println("");

                
                Compiler.setIzpackHome(home);


                // Calls the compiler
                CmdlinePackagerListener listener = new CmdlinePackagerListener();
                CompilerConfig compiler = new CompilerConfig(filename, base, kind, output, 
                        compr_format, compr_level, listener, (String) null);
                compiler.executeCompiler();

                // Waits
                while (compiler.isAlive())
                    Thread.sleep(100);

                if (compiler.wasSuccessful()) exitCode = 0;

                System.out.println("Build time: " + new Date());
            }
        }
        catch (Exception err)
        {
            // Something bad has happened
            System.err.println("-> Fatal error :");
            System.err.println("   " + err.getMessage());
            err.printStackTrace();
            System.err.println("");
            System.err.println("(tip : use -? to get the commmand line parameters)");
        }

        // Closes the JVM
        System.exit(exitCode);
    }

    private static String resolveIzPackHome(String home)
    {
        File test = new File(home, IZ_TEST_SUBDIR + File.separator + IZ_TEST_FILE);
        if( test != null && test.exists())
            return( home);
        // Try to resolve the path using compiler.jar which also should be under
        // IZPACK_HOME.
        String self = Compiler.class.getName();
        self = self.replace('.', '/');
        self = "/" + self + ".class";
        URL url = Compiler.class.getResource(self);
        String np = url.getFile();
        int start = np.indexOf(self);
        np = np.substring(0, start);
        if( np.endsWith("!"))
        {   // Where shut IZPACK_HOME at the standalone-compiler be??
            // No idea.
            if(np.endsWith("standalone-compiler.jar!"))
                return(".");
            np = np.substring(0, np.length() - 1);
        }
        File root = null;
        if( URI.create(np).isAbsolute())
            root = new File(URI.create(np));
        else 
            root = new File(np);
        while(true)
        {
            if( root == null )
                throw new IllegalArgumentException("No valid IzPack home directory found");
            test =  new File(root, IZ_TEST_SUBDIR + File.separator + IZ_TEST_FILE);
            if( test.exists())
                return(root.getAbsolutePath());
            root = root.getParentFile();
        }
    }

    // -------------------------------------------------------------------------
    // ------------- Listener stuff ------------------------- START ------------

    /**
     * This method parses install.xml for defined listeners and put them in the right position. If
     * posible, the listeners will be validated. Listener declaration is a fragmention in
     * install.xml like : <listeners> <listener compiler="PermissionCompilerListener"
     * installer="PermissionInstallerListener"/> </<listeners>
     * 
     * @param data the XML data
     * @exception Exception Description of the Exception
     */
    private void addCustomListeners(XMLElement data) throws Exception
    {
        // We get the listeners
        XMLElement root = data.getFirstChildNamed("listeners");
        if (root == null) return;
        Iterator iter = root.getChildrenNamed("listener").iterator();
        while (iter.hasNext())
        {
            XMLElement xmlAction = (XMLElement) iter.next();
            Object[] listener = getCompilerListenerInstance(xmlAction);
            if (listener != null)
                addCompilerListener((CompilerListener) listener[0]);
            String[] typeNames = new String[] { "installer", "uninstaller"};
            int[] types = new int[] { CustomData.INSTALLER_LISTENER,
                    CustomData.UNINSTALLER_LISTENER};
            for (int i = 0; i < typeNames.length; ++i)
            {
                String className = xmlAction.getAttribute(typeNames[i]);
                if (className != null)
                {
                    // Check for a jar attribute on the listener
                    String jarPath = xmlAction.getAttribute("jar");
                    jarPath = compiler.replaceProperties(jarPath);
                    if( jarPath == null )
                        jarPath = "bin/customActions/" + className + ".jar";
                    List constraints = OsConstraint.getOsList(xmlAction);
                    compiler.addCustomListener(types[i], className, jarPath, constraints);
                }
            }
        }

    }

    /**
     * Returns a list which contains the pathes of all files which are included in the given url.
     * This method expects as the url param a jar.
     * 
     * @param url url of the jar file
     * @return full qualified paths of the contained files
     * @throws Exception
     */
    private List getContainedFilePaths(URL url) throws Exception
    {
        JarInputStream jis = new JarInputStream(url.openStream());
        ZipEntry zentry = null;
        ArrayList fullNames = new ArrayList();
        while ((zentry = jis.getNextEntry()) != null)
        {
            String name = zentry.getName();
            // Add only files, no directory entries.
            if (!zentry.isDirectory()) fullNames.add(name);
        }
        jis.close();
        return (fullNames);
    }

    /**
     * Returns the qualified class name for the given class. This method expects as the url param a
     * jar file which contains the given class. It scans the zip entries of the jar file.
     * 
     * @param url url of the jar file which contains the class
     * @param className short name of the class for which the full name should be resolved
     * @return full qualified class name
     * @throws IOException 
     */
    private String getFullClassName(URL url, String className) throws IOException //throws Exception
    {
        JarInputStream jis = new JarInputStream(url.openStream());
        ZipEntry zentry = null;
        while ((zentry = jis.getNextEntry()) != null)
        {
            String name = zentry.getName();
            int lastPos = name.lastIndexOf(".class");
            if (lastPos < 0)
            {
                continue; // No class file.
            }
            name = name.replace('/', '.');
            int pos = -1;
            int nonCasePos = -1;
            if (className != null)
            {
                pos = name.indexOf(className);
                nonCasePos = name.toLowerCase().indexOf(className.toLowerCase());
            }
            if (pos != -1 && name.length() == pos + className.length() + 6) // "Main" class found
            {
                jis.close();
                return (name.substring(0, lastPos));
            }
            
            if (nonCasePos != -1 && name.length() == nonCasePos + className.length() + 6)
                // "Main" class with different case found
                throw new IllegalArgumentException("Fatal error! The declared panel name in the xml file ("
                        + className + ") differs in case to the founded class file (" + name + ").");
        }
        jis.close();
        return (null);
    }

    /**
     * Returns the compiler listener which is defined in the xml element. As
     * xml element a "listner" node will be expected. Additional it is expected,
     * that either "findIzPackResource" returns an url based on
     * "bin/customActions/[className].jar", or that the listener element has
     * a jar attribute specifying the listener jar path. The class will be
     * loaded via an URLClassLoader.
     * 
     * @param var the xml element of the "listener" node
     * @return instance of the defined compiler listener
     * @throws Exception
     */
    private Object[] getCompilerListenerInstance(XMLElement var) throws Exception
    {
        String className = var.getAttribute("compiler");
        Class listener = null;
        Object instance = null;
        if (className == null) return (null);

        // CustomAction files come in jars packaged IzPack, or they can be
        // specified via a jar attribute on the listener
        String jarPath = var.getAttribute("jar");
        jarPath = compiler.replaceProperties(jarPath);
        if( jarPath == null )
            jarPath = "bin/customActions/" + className + ".jar";
        URL url = findIzPackResource(jarPath, "CustomAction jar file", var);
        String fullName = getFullClassName(url, className);
        if (fullName == null){
            // class not found
            return null;
        }
        if (url != null)
        {
            if (getClass().getResource("/" + jarPath) != null)
            { // Oops, standalone, URLClassLoader will not work ...
                // Write the jar to a temp file.
                InputStream in = null;
                FileOutputStream outFile = null;
                byte[] buffer = new byte[5120];
                File tf = null;
                try
                {
                    tf = File.createTempFile("izpj", ".jar");
                    tf.deleteOnExit();
                    outFile = new FileOutputStream(tf);
                    in = getClass().getResourceAsStream("/" + jarPath);
                    long bytesCopied = 0;
                    int bytesInBuffer;
                    while ((bytesInBuffer = in.read(buffer)) != -1)
                    {
                        outFile.write(buffer, 0, bytesInBuffer);
                        bytesCopied += bytesInBuffer;
                    }
                }
                finally
                {
                    if (in != null) in.close();
                    if (outFile != null) outFile.close();
                }
                url = tf.toURL();

            }
            // Use the class loader of the interface as parent, else
            // compile will fail at using it via an Ant task.
            URLClassLoader ucl = new URLClassLoader(new URL[] { url}, CompilerListener.class
                    .getClassLoader());
            listener = ucl.loadClass(fullName);
        }
        if (listener != null)
            instance = listener.newInstance();
        else
            parseError(var, "Cannot find defined compiler listener " + className);
        if (!CompilerListener.class.isInstance(instance))
            parseError(var, "'" + className + "' must be implemented "
                    + CompilerListener.class.toString());
        List constraints = OsConstraint.getOsList(var);
        return (new Object[] { instance, className, constraints});
    }

    /**
     * Add a CompilerListener. A registered CompilerListener will be called at every enhancmend
     * point of compiling.
     * 
     * @param pe CompilerListener which should be added
     */
    private void addCompilerListener(CompilerListener pe)
    {
        compilerListeners.add(pe);
    }

    /**
     * Calls all defined compile listeners notify method with the given data
     * 
     * @param callerName name of the calling method as string
     * @param state CompileListener.BEGIN or END
     * @param data current install data
     * @throws CompilerException
     */
    private void notifyCompilerListener(String callerName, int state, XMLElement data)
            throws CompilerException
    {
        Iterator i = compilerListeners.iterator();
        IPackager packager = compiler.getPackager();
        while (i != null && i.hasNext())
        {
            CompilerListener listener = (CompilerListener) i.next();
            listener.notify(callerName, state, data, packager);
        }

    }

    /**
     * Calls the reviseAdditionalDataMap method of all registered CompilerListener's.
     * 
     * @param f file releated XML node
     * @return a map with the additional attributes
     */
    private Map getAdditionals(XMLElement f) throws CompilerException
    {
        Iterator i = compilerListeners.iterator();
        Map retval = null;
        try
        {
            while (i != null && i.hasNext())
            {
                retval = ((CompilerListener) i.next()).reviseAdditionalDataMap(retval, f);
            }
        }
        catch (CompilerException ce)
        {
            parseError(f, ce.getMessage());
        }
        return (retval);
    }
}
