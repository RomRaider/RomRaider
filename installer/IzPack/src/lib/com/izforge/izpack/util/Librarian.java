/*
 * $Id: Librarian.java 1816 2007-04-23 19:57:27Z jponge $ 
 * IzPack - Copyright 2001-2007 Julien Ponge, All Rights Reserved.
 * 
 * http://izpack.org/
 * http://developer.berlios.de/projects/izpack/
 * 
 * Copyright 2002 Elmar Grom
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

package com.izforge.izpack.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.Vector;

/*---------------------------------------------------------------------------*/
/**
 * This class handles loading of native libraries. There must only be one instance of
 * <code>Librarian</code> per Java runtime, therefore this class is implemented as a 'Singleton'.
 * <br>
 * <br>
 * <code>Librarian</code> is capable of loading native libraries from a variety of different
 * source locations. However, you should place your library files in the 'native' directory. The
 * primary reason for supporting different source locations is to facilitate testing in a
 * development environment, without the need to actually packing the application into a *.jar file.
 * 
 * @version 1.0 / 1/30/02
 * @author Elmar Grom
 */
/*---------------------------------------------------------------------------*/
public class Librarian implements CleanupClient
{

    // ------------------------------------------------------------------------
    // Constant Definitions
    // ------------------------------------------------------------------------

    /** Used to identify jar URL protocols */
    private static final String JAR_PROTOCOL = "jar";

    /** Used to identify file URL protocols */
    private static final String FILE_PROTOCOL = "file";

    /**
     * The key used to retrieve the location of temporary files form the system properties.
     */
    private static final String TEMP_LOCATION_KEY = "java.io.tmpdir";

    /**
     * The extension appended to the client name when searching for it as a resource. Since the
     * client is an object, the extension should always be '.class'
     */
    private static final String CLIENT_EXTENSION = ".class";

    /** The default directory for native library files. */
    private static final String NATIVE = "native";

    /** The block size used for reading and writing data, 4k. */
    private static final int BLOCK_SIZE = 4096;

    /** VM version needed to select clean up method. */
    private static final float JAVA_SPECIFICATION_VERSION = Float.parseFloat(System
            .getProperty("java.specification.version"));

    // ------------------------------------------------------------------------
    // Variable Declarations
    // ------------------------------------------------------------------------

    /**
     * The reference to the single instance of <code>Librarian</code>. Used in static methods in
     * place of <code>this</code>.
     */
    private static Librarian me = null;

    /**
     * A list that is used to track all libraries that have been loaded. This list is used to ensure
     * that each library is loaded only once.
     */
    private Vector trackList = new Vector();

    /**
     * A list of references to clients that use libraries that were extracted from a *.jar file.
     * This is needed because the clients need to be called for freeing their libraries.
     */
    private Vector clients = new Vector();

    /**
     * A list of library names as they appear in the temporary directory. This is needed to free
     * each library through the client. The index of each name corresponds to the index of the
     * respective client in the <code>clients</code> list.
     */
    private Vector libraryNames = new Vector();

    /**
     * A list of fully qualified library names. This is needed to delete the temporary library files
     * after use. The index of each name corresponds to the index of the respective client in the
     * <code>clients</code> list.
     */
    private Vector temporaryFileNames = new Vector();

    /** The extension to use for native libraries. */
    private String extension = "";

    /** The directory that is used to hold all native libraries. */
    private String nativeDirectory = NATIVE;

    /*--------------------------------------------------------------------------*/
    /**
     * This class is implemented as a 'Singleton'. Therefore the constructor is private to prevent
     * instantiation of this class. Use <code>getInstance()</code> to obtain an instance for use.
     * <br>
     * <br>
     * For more information about the 'Singleton' pattern I highly recommend the book Design
     * Patterns by Gamma, Helm, Johnson and Vlissides ISBN 0-201-63361-2.
     */
    /*--------------------------------------------------------------------------*/
    private Librarian()
    {
        Housekeeper.getInstance().registerForCleanup(this);
        extension = '.' + TargetFactory.getInstance().getNativeLibraryExtension();
    }

    /*--------------------------------------------------------------------------*/
    /**
     * Returns an instance of <code>Librarian</code> to use.
     * 
     * @return an instance of <code>Librarian</code>.
     */
    /*--------------------------------------------------------------------------*/
    public static Librarian getInstance()
    {
        if (me == null)
        {
            me = new Librarian();
        }

        return (me);
    }

    /*--------------------------------------------------------------------------*/
    /**
     * Loads the requested library. If the library is already loaded, this method returns
     * immediately, without an attempt to load the library again. <br>
     * <br>
     * <b>Invocation Example:</b> This assumes that the call is made from the class that links with
     * the library. If this is not the case, <code>this</code> must be replaced by the reference
     * of the class that links with the library. <br>
     * <br>
     * <code>
     * Librarian.getInstance ().loadLibrary ("MyLibrary", this);
     * </code> <br>
     * <br>
     * Loading of a native library file works as follows:<br>
     * <ul>
     * <li>If the library is already loaded there is nothing to do.
     * <li>An attempt is made to load the library by its name. If there is no system path set to
     * the library, this attempt will fail.
     * <li>If the client is located on the local file system, an attempt is made to load the
     * library from the local files system as well.
     * <li>If the library is located inside a *.jar file, it is extracted to 'java.io.tmpdir' and
     * an attempt is made to load it from there.
     * </ul>
     * <br>
     * <br>
     * Loading from the local file system and from the *.jar file is attempted for the following
     * potential locations of the library in this order:<br>
     * <ol>
     * <li>The same directory where the client is located
     * <li>The native library directory
     * </ol>
     * 
     * @param name the name of the library. A file extension and path are not needed, in fact if
     * supplied, both is stripped off. A specific extension is appended.
     * @param client the object that made the load request
     * 
     * @see #setNativeDirectory
     * 
     * @exception Exception if all attempts to load the library fail.
     */
    /*--------------------------------------------------------------------------*/
    public synchronized void loadLibrary(String name, NativeLibraryClient client) throws Exception
    {
        String libraryName = strip(name);
        String tempFileName = "";

        // ----------------------------------------------------
        // Return if the library is already loaded
        // ----------------------------------------------------
        if (loaded(libraryName)) { return; }

        
        if( System.getProperty("DLL_PATH") != null )
        {
            String path = System.getProperty("DLL_PATH") + "/" + name + extension;
            path = path.replace('/', File.separatorChar);
            Debug.trace("Try to load library " + path);
            System.load(path);
            return;
            
        }
        // ----------------------------------------------------
        // First try a straight load
        // ----------------------------------------------------
        try
        {
            System.loadLibrary(libraryName);
            return;
        }
        catch (UnsatisfiedLinkError exception)
        {}
        catch (SecurityException exception)
        {}

        // ----------------------------------------------------
        // Next, try to get the protocol for loading the resource.
        // ----------------------------------------------------
        Class clientClass = client.getClass();
        String resourceName = clientClass.getName();
        int nameStart = resourceName.lastIndexOf('.') + 1;
        resourceName = resourceName.substring(nameStart, resourceName.length()) + CLIENT_EXTENSION;
        URL url = clientClass.getResource(resourceName);
        if (url == null) { throw (new Exception("can't identify load protocol for " + libraryName
                + extension)); }
        String protocol = url.getProtocol();

        // ----------------------------------------------------
        // If it's a local file, load it from the current location
        // ----------------------------------------------------
        if (protocol.equalsIgnoreCase(FILE_PROTOCOL))
        {
            try
            {
                System.load(getClientPath(name, url));
            }
            catch (Throwable exception)
            {
                try
                {
                    System.load(getNativePath(name, client));
                }
                catch (Throwable exception2)
                {
                    throw (new Exception("error loading library"));
                }
            }
        }

        // ----------------------------------------------------
        // If it is in a *.jar file, extract it to 'java.io.tmpdir'
        // ----------------------------------------------------

        else if (protocol.equalsIgnoreCase(JAR_PROTOCOL))
        {
            tempFileName = getTempFileName(libraryName);
            try
            {
                extractFromJar(libraryName, tempFileName, client);

                clients.add(client);
                temporaryFileNames.add(tempFileName);
                libraryNames.add(tempFileName.substring((tempFileName
                        .lastIndexOf(File.separatorChar) + 1), tempFileName.length()));

                // --------------------------------------------------
                // Try loading the temporary file from 'java.io.tmpdir'.
                // --------------------------------------------------
                System.load(tempFileName);
            }
            catch (Throwable exception)
            {
                throw (new Exception("error loading library\n" + exception.toString()));
            }
        }
    }

    /*--------------------------------------------------------------------------*/
    /**
     * Verifies if the library has already been loaded and keeps track of all libraries that are
     * verified.
     * 
     * @param name name of the library to verify
     * 
     * @return <code>true</code> if the library had already been loaded, otherwise
     * <code>false</code>.
     */
    /*--------------------------------------------------------------------------*/
    private boolean loaded(String name)
    {
        if (trackList.contains(name))
        {
            return (true);
        }
        else
        {
            trackList.add(name);
            return (false);
        }
    }

    /*--------------------------------------------------------------------------*/
    /**
     * Strips the extension of the library name, if it has one.
     * 
     * @param name the name of the library
     * 
     * @return the name without an extension
     */
    /*--------------------------------------------------------------------------*/
    private String strip(String name)
    {
        int extensionStart = name.lastIndexOf('.');
        int nameStart = name.lastIndexOf('/');
        if (nameStart < 0)
        {
            nameStart = name.lastIndexOf('\\');
        }
        nameStart++;

        String shortName;

        if (extensionStart > 0)
        {
            shortName = name.substring(nameStart, extensionStart);
        }
        else
        {
            shortName = name.substring(nameStart, name.length());
        }

        return (shortName);
    }

    /*--------------------------------------------------------------------------*/
    /**
     * Makes an attempt to extract the named library from the jar file and to store it on the local
     * file system for temporary use. If the attempt is successful, the fully qualified file name of
     * the library on the local file system is returned.
     * 
     * @param name the simple name of the library
     * @param destination the fully qualified name of the destination file.
     * @param client the class that made the load request.
     * 
     * @exception Exception if the library can not be extracted from the *.jar file.
     * @exception FileNotFoundException if the *.jar file does not exist. The way things operate
     * here, this should actually never happen.
     */
    /*--------------------------------------------------------------------------*/
    private void extractFromJar(String name, String destination, NativeLibraryClient client)
            throws Exception
    {
        int bytesRead = 0;
        OutputStream output = null;

        // ----------------------------------------------------
        // open an input stream for the library file
        // ----------------------------------------------------
        InputStream input = openInputStream(name, client);

        // ----------------------------------------------------
        // open an output stream for the temporary file
        // ----------------------------------------------------
        try
        {
            output = new FileOutputStream(destination);
        }
        catch (FileNotFoundException exception)
        {
            input.close();
            throw (new Exception("can't create destination file"));
        }
        catch (SecurityException exception)
        {
            input.close();
            throw (new Exception("creation of destination file denied"));
        }
        catch (Throwable exception)
        {
            input.close();
            throw (new Exception("unknown problem creating destination file\n"
                    + exception.toString()));
        }

        // ----------------------------------------------------
        // pump the data
        // ----------------------------------------------------
        byte[] buffer = new byte[BLOCK_SIZE];
        try
        {
            do
            {
                bytesRead = input.read(buffer);
                if (bytesRead > 0)
                {
                    output.write(buffer, 0, bytesRead);
                }
            }
            while (bytesRead > 0);
        }
        catch (Throwable exception)
        {
            throw (new Exception("error writing to destination file\n" + exception.toString()));
        }

        // ----------------------------------------------------
        // flush the data and close both streams
        // ----------------------------------------------------
        finally
        {
            input.close();
            output.flush();
            output.close();
        }
    }

    /*--------------------------------------------------------------------------*/
    /**
     * Returns the complete path (including file name) for the native library, assuming the native
     * library is located in the same directory from which the client was loaded.
     * 
     * @param name the simple name of the library
     * @param clientURL a URL that points to the client class
     * 
     * @return the path to the client
     */
    /*--------------------------------------------------------------------------*/
    private String getClientPath(String name, URL clientURL)
    {
        String path = clientURL.getFile();

        int nameStart = path.lastIndexOf('/') + 1;

        path = path.substring(0, nameStart);
        path = path + name + extension;
        path = path.replace('/', File.separatorChar);
        // Revise the URI-path to a file path; needed in uninstaller because it
        // writes the jar contents into a sandbox; may be with blanks in the
        // path.
        path = revisePath(path);

        return (path);
    }

    /*--------------------------------------------------------------------------*/
    /**
     * Returns the complete path (including file name) for the native library, assuming the native
     * library is located in a directory where native libraries are ordinarily expected.
     * 
     * @param name the simple name of the library
     * @param client the class that made the load request.
     * 
     * @return the path to the location of the native libraries.
     */
    /*--------------------------------------------------------------------------*/
    private String getNativePath(String name, NativeLibraryClient client)
    {
        ProtectionDomain domain = client.getClass().getProtectionDomain();
        CodeSource codeSource = domain.getCodeSource();
        URL url = codeSource.getLocation();
        String path = url.getPath();
        path = path + nativeDirectory + '/' + name + extension;
        path = path.replace('/', File.separatorChar);
        // Revise the URI-path to a file path; needed in uninstaller because it
        // writes the jar contents into a sandbox; may be with blanks in the
        // path.
        path = revisePath(path);

        return (path);
    }

    /*--------------------------------------------------------------------------*/
    /**
     * Revises the given path to a file compatible path. In fact this method replaces URI-like
     * entries with it chars (e.g. %20 with a space).
     * 
     * @param in path to be revised
     * @return revised path
     */
    /*--------------------------------------------------------------------------*/
    private String revisePath(String in)
    {
        // This was "stolen" from com.izforge.izpack.util.SelfModifier

        StringBuffer sb = new StringBuffer();
        CharacterIterator iter = new StringCharacterIterator(in);
        for (char c = iter.first(); c != CharacterIterator.DONE; c = iter.next())
        {
            if (c == '%')
            {
                char c1 = iter.next();
                if (c1 != CharacterIterator.DONE)
                {
                    int i1 = Character.digit(c1, 16);
                    char c2 = iter.next();
                    if (c2 != CharacterIterator.DONE)
                    {
                        int i2 = Character.digit(c2, 16);
                        sb.append((char) ((i1 << 4) + i2));
                    }
                }
            }
            else
            {
                sb.append(c);
            }
        }
        String path = sb.toString();
        return path;
    }

    /*--------------------------------------------------------------------------*/
    /**
     * Opens an <code>InputStream</code> to the native library.
     * 
     * @param name the simple name of the library
     * @param client the class that made the load request.
     * 
     * @return an <code>InputStream</code> from which the library can be read.
     * 
     * @exception Exception if the library can not be located.
     */
    /*--------------------------------------------------------------------------*/
    private InputStream openInputStream(String name, NativeLibraryClient client) throws Exception
    {
        Class clientClass = client.getClass();
        // ----------------------------------------------------
        // try to open an input stream, assuming the library
        // is located with the client
        // ----------------------------------------------------
        InputStream input = clientClass.getResourceAsStream(name + extension);

        // ----------------------------------------------------
        // if this is not successful, try to load from the
        // location where all native libraries are supposed
        // to be located.
        // ----------------------------------------------------
        if (input == null)
        {
            input = clientClass.getResourceAsStream('/' + nativeDirectory + '/' + name + extension);
        }

        // ----------------------------------------------------
        // if this fails as well, throw an exception
        // ----------------------------------------------------
        if (input == null)
        {
            throw (new Exception("can't locate library"));
        }
        else
        {
            return (input);
        }
    }

    /*--------------------------------------------------------------------------*/
    /**
     * Builds a temporary file name for the native library.
     * 
     * @param name the file name of the library
     * 
     * @return a fully qualified file name that can be used to store the file on the local file
     * system.
     */
    /*--------------------------------------------------------------------------*/
    /*
     * $ @design
     * 
     * Avoid overwriting any existing files on the user's system. If by some remote chance a file by
     * the same name should exist on the user's system, modify the temporary file name until a
     * version is found that is unique on the system and thus won't interfere.
     * --------------------------------------------------------------------------
     */
    private String getTempFileName(String name)
    {
        StringBuffer fileName = new StringBuffer();
        String path = System.getProperty(TEMP_LOCATION_KEY);
        if (path.charAt(path.length() - 1) == File.separatorChar)
        {
            path = path.substring(0, (path.length() - 1));
        }
        String modifier = "";
        int counter = 0;
        File file = null;

        do
        {
            fileName.delete(0, fileName.length());
            fileName.append(path);
            fileName.append(File.separatorChar);
            fileName.append(name);
            fileName.append(modifier);
            fileName.append(extension);

            modifier = Integer.toString(counter);
            counter++;

            file = new File(fileName.toString());
        }
        while (file.exists());

        return (fileName.toString());
    }

    /*--------------------------------------------------------------------------*/
    /**
     * Sets the directory where <code>Librarian</code> will search for native files. Directories
     * are denoted relative to the root, where the root is the same location where the top level
     * Java package directory is located (usually called <code>com</code>). The default directory
     * is <code>native</code>.
     * 
     * @param directory the directory where native files are located.
     */
    /*--------------------------------------------------------------------------*/
    public void setNativeDirectory(String directory)
    {
        if (directory == null)
        {
            nativeDirectory = "";
        }
        else
        {
            nativeDirectory = directory;
        }
    }

    /*--------------------------------------------------------------------------*/
    /**
     * This method attempts to remove all native libraries that have been temporarily created from
     * the system.
     * The used method for clean up depends on the VM version.
     * If the ersion is 1.5.x or higher this process should be exit in one second, else
     * the native libraries will be not deleted.
     * Tests with the different methods produces hinds that the
     * FreeLibraryAndExitThread (handle, 0) call in the dlls are the 
     * reason for VM crashes (version 1.5.x). May be this is a bug in the VM.
     * But never seen a docu that this behavior is compatible with a VM.
     * Since more than a year all 1.5 versions produce this crash. Therfore we make
     * now a work around for it.
     * But the idea to exit the thread for removing the file locking to give the
     * possibility to delete the dlls are really nice. Therefore we use it with
     * VMs which are compatible with it.  (Klaus Bartz 2006.06.20)
     */
    /*--------------------------------------------------------------------------*/
    public void cleanUp()
    {
        if (JAVA_SPECIFICATION_VERSION < 1.5)
            oldCleanUp();
        else
            newCleanUp();

    }

    /*--------------------------------------------------------------------------*/
    /**
     * This method attempts to remove all native libraries that have been temporarily created from
     * the system.
     * This method will be invoked if the VM has version 1.4.x or less. Version 1.5.x or higher
     * uses newCleanUp.
     * This method starts a new thread which calls a method in the dll which should unload the
     * dll. The thread never returns. 
     */
    /*--------------------------------------------------------------------------*/
    private void oldCleanUp()
    {
        for (int i = 0; i < clients.size(); i++)
        {
            // --------------------------------------------------
            // free the library
            // --------------------------------------------------
            NativeLibraryClient client = (NativeLibraryClient) clients.elementAt(i);
            String libraryName = (String) libraryNames.elementAt(i);

            FreeThread free = new FreeThread(libraryName, client);
            free.start();
            try
            {
                // give the thread some time to get the library
                // freed before attempting to delete it.
                free.join(50);
            }
            catch (Throwable exception)
            {} // nothing I can do

            // --------------------------------------------------
            // delete the library
            // --------------------------------------------------
            String tempFileName = (String) temporaryFileNames.elementAt(i);
            try
            {
                File file = new File(tempFileName);
                file.delete();
            }
            catch (Throwable exception)
            {} // nothing I can do
        }
    }

    /*--------------------------------------------------------------------------*/
    /**
     * This method attempts to remove all native libraries that have been temporarily created from
     * the system. This method will be invoked if the VM has version 1.5.x or higher. Version 1.4.x
     * or less uses oldCleanUp. This method calls LibraryRemover which starts a new process which
     * waits a little bit for exit of this process and tries than to delete the given files.
     */
    /*--------------------------------------------------------------------------*/
    private void newCleanUp()
    {
        // This method will be used the SelfModifier stuff of uninstall 
        // instead of killing the thread in the dlls which provokes a 
        // segmentation violation with a 1.5 (also known as 5.0) VM.

        try
        {
            LibraryRemover.invoke(temporaryFileNames);
        }
        catch (IOException e1)
        {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

    }
}
/*---------------------------------------------------------------------------*/
