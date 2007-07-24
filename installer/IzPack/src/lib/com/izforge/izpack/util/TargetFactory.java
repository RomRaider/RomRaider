/*
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

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.StringTokenizer;

/*---------------------------------------------------------------------------*/
/**
 * The <code>TargetFactory</code> serves as a central mechanism to instantiate OS specific class
 * flavors, provide OS specific file extension types, default install directories and similar
 * functionality. In addition it provides services that are related to OS versions and flavors. For
 * a tutorial on using some of the features in this class see the <A
 * HREF=doc-files/TargetFactory.html>TargetFactory Tutorial</A>.
 * 
 * @version 0.0.1 / 1/3/2002
 * @author Elmar Grom
 */
/*---------------------------------------------------------------------------*/
/*
 * $ @design
 * 
 * Reports actually observed on some systems:
 * 
 * OS OS Name Version Architecture Native Report (ver)
 * ----------------------------------------------------------------------------------------------------------
 * Windows 95 Windows 98 Windows 98 4.10 x86 Windows 98 [Version 4.10.1998] Windows-ME Windows Me
 * 4.90 x86 Windows Millennium [Version 4.90.3000] Windows-NT 3.5 Windows-NT 4.0 Windows NT 4.0 x86
 * Windows NT Version 4.0 Windows 2000 Windows 2000 5.0 x86 Microsoft Windows 2000 [Version
 * 5.00.2195] Windows-XP Windows 2000 5.1 x86 Microsoft Windows XP [Version 5.1.2600] Windows-XP
 * Windows XP 5.1 x86 Mac Mac OS-X Linux Linux 2.4.7-10 i386 Linux Linux 2.4.18-4GB i386 Solaris
 * 
 * ---------------------------------------------------------------------------
 */
public class TargetFactory
{

    // ------------------------------------------------------------------------
    // Constant Definitions
    // ------------------------------------------------------------------------

    // Basic operating systems

    /** Identifies Microsoft Windows. */
    public static final int WINDOWS = 0;

    /** Identifies generic UNIX operating systems */
    public static final int UNIX = 2;

    /** Used to report a non specific operating system. */
    public static final int GENERIC = 3;

    // operating system favors

    /** This is the basic flavor for every operating system. */
    public static final int STANDARD = 0;

    /**
     * Used to identify the Windows-NT class of operating systems in terms of an OS flavor. It is
     * reported for Windows-NT, 2000 and XP.
     */
    public static final int NT = 1;

    /** Used to identify the OS X flavor of the Mac OS */
    public static final int X = 2;

    // system architecture

    /** Identifies Intel X86 based processor types. */
    public static final int X86 = 0;

    /** Nonspecific processor architecture, other than X86. */
    public static final int OTHER = 1;

    /**
     * The extensions used for native libraries on various operating systems. The string positions
     * correspond to the basic operating system indexes. The following values are legal to use :
     * <br>
     * <br>
     * <ul>
     * <li>WINDOWS
     * <li>MAC
     * <li>UNIX
     * <li>GENERIC
     * </ul>
     */
    static final String[] LIBRARY_EXTENSION = { "dll", "so", "", ""};

    /**
     * The os specific class prefixes for classes that implement different versions for the various
     * operating systems. The string positions correspond to the basic operating system indexes. The
     * following values are legal to use : <br>
     * <br>
     * <ul>
     * <li>WINDOWS
     * <li>MAC
     * <li>UNIX
     * <li>GENERIC
     * </ul>
     */
    static final String[] CLASS_PREFIX = { "Win_", "Mac_", "Unix_", ""};

    /**
     * The os favor specific class prefixes for classes the implement different versions for various
     * os favors. The string positions correspond to the flavor indexes. The following values are
     * legal to use : <br>
     * <br>
     * <ul>
     * <li>STANDARD
     * <li>NT
     * <li>X
     * </ul>
     */
    static final String[] CLASS_FLAVOR_PREFIX = { "", "NT_", "X_"};

    /**
     * The list of processor architecture specific prefixes. The string positions correspond to the
     * architecture indexes. The following values are leegal to use : <br>
     * <br>
     * <ul>
     * <li>X86
     * <li>OTHER
     * </ul>
     */
    static final String[] CLASS_ARCHITECTURE_PREFIX = { "X86_", // Intel X86
            // architecture
            "U_" // unknown
    };

    /**
     * The list of default install path fragments. Depending on the operating system, a path
     * fragment might represent either a part of the default install path or the entire path to use.
     * For MS-Windows it is always only a part of the full install path. The string positions
     * correspond to the basic operating system indexes. The following values are leegal to use :
     * <br>
     * <br>
     * <ul>
     * <li>WINDOWS
     * <li>MAC
     * <li>UNIX
     * <li>GENERIC
     * </ul>
     */
    static final String[] INSTALL_PATH_FRAGMENT = { "Program Files" + File.separator,
            "/Applications" + File.separator, "/usr/local" + File.separator,
            File.separator + "apps" + File.separator};

    /**
     * This is a list of keys to use when looking for resources that define the default install path
     * to use. The list is organized as two dimensional array of <code>String</code>s. To access
     * the array, denote the first dimension with the operating system index and the second
     * dimension with the flavor index. For example to access the key for Windows-NT use
     * <code>INSTALL_PATH_RESOURCE_KEY[WINDOWS][NT]</code> The array uses a sparse population,
     * that is, not all array locations actually contain a key. Only locations for which a real
     * operating system/flavor combination exists are populated. For example, there is no such thing
     * as <code>INSTALL_PATH_RESOURCE_KEY[UNIX][X]</code>
     */
    static final String[][] INSTALL_PATH_RESOURCE_KEY = {
    // Standard NT X
            { "TargetPanel.dir.windows", "TargetPanel.dir.windows", ""}, // Windows
            { "TargetPanel.dir.mac", "", "TargetPanel.dir.macosx"}, // Mac
            { "TargetPanel.dir.unix", "", ""}, // UNIX
            { "TargetPanel.dir", "", ""} // Generic
    };

    /** The delimiter characters used to tokenize version numbers */
    private static final String VERSION_DELIMITER = ".-";

    // ------------------------------------------------------------------------
    // Variable Declarations
    // ------------------------------------------------------------------------
    /**
     * The reference to the single instance of <code>TargetFactory</code>. Used in static methods
     * in place of <code>this</code>.
     */
    private static TargetFactory me = null;

    /** identifies the operating system we are running on */
    private int os = -1;

    /** identifies the operating system favor */
    private int osFlavor = -1;

    /** identifies the hardware architecture we are running on */
    private int architecture = -1;

    /** represents the version number of the target system */
    private String version = "";

    /*--------------------------------------------------------------------------*/
    /**
     * Constructor
     */
    /*--------------------------------------------------------------------------*/
    /*
     * $ @design
     * 
     * Identify the following about the target system: - OS type - architecture - version
     * 
     * and store this information for later use.
     * --------------------------------------------------------------------------
     */
    private TargetFactory()
    {
        version = System.getProperty("os.version");

        // ----------------------------------------------------
        // test for Windows
        // ----------------------------------------------------
        if (OsVersion.IS_WINDOWS)
        {
            os = WINDOWS;
            osFlavor = STANDARD;
            architecture = X86;
            String osName = OsVersion.OS_NAME.toLowerCase();

            if (osName.indexOf("nt") > -1)
            {
                osFlavor = NT;
            }
            else if (osName.indexOf("2000") > -1)
            {
                osFlavor = NT;
            }
            else if (osName.indexOf("xp") > -1)
            {
                osFlavor = NT;
            }
        }
        // ----------------------------------------------------
        // test for Mac OS
        // ----------------------------------------------------
        else if (OsVersion.IS_OSX)
        {
            os = X;
            osFlavor = STANDARD;
            architecture = OTHER;
        }
        // ----------------------------------------------------
        // what's left should be unix
        // ----------------------------------------------------
        else
        {
            os = UNIX;
            osFlavor = STANDARD;
            architecture = OTHER;
            String osName = OsVersion.OS_NAME.toLowerCase();

            if (osName.indexOf("x86") > -1)
            {
                architecture = X86;
            }
        }
    }

    /*--------------------------------------------------------------------------*/
    /**
     * Returns an instance of <code>TargetFactory</code> to use.
     * 
     * @return an instance of <code>TargetFactory</code>.
     */
    /*--------------------------------------------------------------------------*/
    public static TargetFactory getInstance()
    {
        if (me == null)
        {
            me = new TargetFactory();
        }

        return me;
    }

    /*--------------------------------------------------------------------------*/
    /**
     * This method returns an OS and OS flavor specific instance of the requested class. <br>
     * <br>
     * <b>Class Naming Rules</b><br>
     * Class versions must be named with the OS and OS flavor as prefix. The prefixes are simply
     * concatenated, with the OS prefix first and the flavor prefix second. Use the following OS
     * specific prefixes:<br>
     * <br>
     * <TABLE BORDER=1>
     * <TR>
     * <TH>Operating System</TH>
     * <TH>Prefix</TH>
     * </TR>
     * <TR>
     * <TD>Microsoft Windows</TD>
     * <TD>Win_</TD>
     * </TR>
     * <TR>
     * <TD>Mac OS</TD>
     * <TD>Mac_</TD>
     * </TR>
     * <TR>
     * <TD>UNIX</TD>
     * <TD>UNIX_</TD>
     * </TR>
     * </TABLE><br>
     * For the different OS flavors, use these prefixes:<br>
     * <br>
     * <TABLE BORDER=1>
     * <TR>
     * <TH>OS Flavor</TH>
     * <TH>Prefix</TH>
     * </TR>
     * <TR>
     * <TD>NT</TD>
     * <TD>NT_</TD>
     * </TR>
     * <TR>
     * <TD>Mac OS X</TD>
     * <TD>X_</TD>
     * </TR>
     * </TABLE> <br>
     * <br>
     * <b>Naming Example:</b> <br>
     * <br>
     * For the class <code>MyClass</code>, the specific version for Windows NT must be in the
     * same package as <code>MyClass</code> and the name must be <code>Win_NT_MyClass</code>. A
     * version that should be instantiated for any non-NT flavor would be called
     * <code>Win_MyClass</code>. This would also be the version instantiated on Windows NT if the
     * version <code>Win_NT_MyClass</code> does not exist. <br>
     * <br>
     * <b>The Loading Process</b> <br>
     * <br>
     * The process is completed after the first successful attempt to load a class. <br>
     * <ol>
     * <li>load a version that is OS and OS-Flavor specific
     * <li>load a version that is OS specific
     * <li>load the base version (without OS or OS-Flavor prefix)
     * </ol>
     * <br>
     * See the <A HREF=doc-files/TargetFactory.html>TargetFactory Tutorial</A> for more
     * information.<br>
     * <br>
     * 
     * @param name the fully qualified name of the class to load without the extension.
     * 
     * @return An instance of the requested class. Note that specific initialization that can not be
     * accomplished in the default constructor still needs to be performed before the object can be
     * used.
     * 
     * @exception Exception if all attempts to instantiate class fail
     */
    /*--------------------------------------------------------------------------*/
    public Object makeObject(String name) throws Exception
    {
        int nameStart = name.lastIndexOf('.') + 1;
        String packageName = name.substring(0, nameStart);
        String className = name.substring(nameStart, name.length());
        String actualName;

        try
        {
            actualName = packageName + CLASS_PREFIX[os] + CLASS_FLAVOR_PREFIX[osFlavor] + className;
            Class temp = Class.forName(actualName);
            return temp.newInstance();
        }
        catch (Throwable exception1)
        {
            try
            {
                Class temp = Class.forName(packageName + CLASS_PREFIX[os] + className);
                return temp.newInstance();
            }
            catch (Throwable exception2)
            {
                try
                {
                    actualName = name;
                    Class temp = Class.forName(actualName);
                    return temp.newInstance();
                }
                catch (Throwable exception3)
                {
                    throw new Exception("can not instantiate class " + name);
                }
            }
        }
    }

    /*--------------------------------------------------------------------------*/
    /**
     * Returns true if the version in the parameter string is higher than the version of the target
     * os.
     * 
     * @param version the version number to compare to
     * 
     * @return <code>false</code> if the version of the target system is higher, otherwise
     * <code>true</code>
     */
    /*--------------------------------------------------------------------------*/
    /*
     * $ @design
     * 
     * Version numbers are assumed to be constructed as follows: - a list of one or more numbers,
     * separated by periods as in X.X.X. ... or periods and dashes as in X.X.X-Y. ... - the numbers
     * follow the decimal number system - the left most number is of highest significance
     * 
     * The process compares each set of numbers, beginning at the most significant and working down
     * the ranks (this is working left to right). The process is stopped as soon as the pair of
     * numbers compaired is not equal. If the numer for the target system is higher, flase is
     * returned, otherwise true.
     * --------------------------------------------------------------------------
     */
    public boolean versionIsHigher(String version) throws Exception
    {
        StringTokenizer targetVersion = new StringTokenizer(this.version, VERSION_DELIMITER);
        StringTokenizer compareVersion = new StringTokenizer(version, VERSION_DELIMITER);

        int target;
        int compare;

        while (targetVersion.hasMoreTokens() && compareVersion.hasMoreTokens())
        {
            try
            {
                target = Integer.parseInt(targetVersion.nextToken());
                compare = Integer.parseInt(compareVersion.nextToken());
            }
            catch (Throwable exception)
            {
                throw new Exception("error in version string");
            }

            if (compare > target)
            {
                return true;
            }
            else if (target > compare) { return false; }
        }

        return false;
    }

    /*--------------------------------------------------------------------------*/
    /**
     * Returns the index number for the target operating system that was detected.
     * 
     * @return an index number for the OS
     * 
     * @see #WINDOWS
     * @see #UNIX
     * @see #GENERIC
     */
    /*--------------------------------------------------------------------------*/
    public int getOS()
    {
        return os;
    }

    /*--------------------------------------------------------------------------*/
    /**
     * Returns the index number for the operating system flavor that was detected on the target
     * system.
     * 
     * @return an index for the OS flavor
     * 
     * @see #STANDARD
     * @see #NT
     * @see #X
     */
    /*--------------------------------------------------------------------------*/
    public int getOSFlavor()
    {
        return osFlavor;
    }

    /*--------------------------------------------------------------------------*/
    /**
     * Returns an index number that identified the processor architecture of the target system.
     * 
     * @return an index for the processor architecture
     * 
     * @see #X86
     * @see #OTHER
     */
    /*--------------------------------------------------------------------------*/
    public int getArchitecture()
    {
        return architecture;
    }

    /*--------------------------------------------------------------------------*/
    /**
     * Returns the file extension customarily used on the target OS for dynamically loadable
     * libraries.
     * 
     * @return a <code>String</code> containing the customary library extension for the target OS.
     * Note that the string might be empty if there no such specific extension for the target OS.
     */
    /*--------------------------------------------------------------------------*/
    public String getNativeLibraryExtension()
    {
        return LIBRARY_EXTENSION[os];
    }

    /*--------------------------------------------------------------------------*/
    /**
     * Returns the system dependent default install path. This is typically used to suggest an
     * istall path to the end user, when performing an installation. The default install path is
     * assembled form the OS specific path fragment specified in <code>INSTALL_PATH_FRAGMENT</code>,
     * possibly a drive letter and the application name. The user the option to define resources
     * that define default paths which differ from the path fragments defined here. The following
     * resource names will be recognized by this method: <br>
     * <br>
     * <ul>
     * <li><code>TargetPanel.dir.windows</code>
     * <li><code>TargetPanel.dir.macosx</code>
     * <li><code>TargetPanel.dir.unix</code>
     * <li><code>TargetPanel.dir</code> plus the all lower case version of
     * <code>System.getProperty ("os.name")</code>, with all spaces replaced by an underscore
     * ('_').
     * <li><code>TargetPanel.dir</code>
     * </ul>
     * 
     * @param appName the name of the application to install. If no specific resource has been set,
     * then this name will be appended to the OS specific default path fragment.
     * 
     * @return the default install path for the target system
     */
    /*--------------------------------------------------------------------------*/
    /*
     * $ @design
     * 
     * First try to read a path string from a resource file. This approach allows the user to
     * customize the default install path that is suggested to the end user by IzPack. There are a
     * number of choices for the naming of this resource, so we need to go through a few steps in
     * order to exhaust the different possibilities. If this was not successful we use the default
     * install path that is defined for the operating system we are running on. This path should be
     * expanded by the application name to form the full path that to returne.
     * --------------------------------------------------------------------------
     */
    public String getDefaultInstallPath(String appName)
    {
        String path = null;
        InputStream input;
        String keyFragment = "/res/" + INSTALL_PATH_RESOURCE_KEY[GENERIC][STANDARD];

        // ----------------------------------------------------
        // attempt to get an input stream through a resource
        // based on a key which is specific to the target OS
        // ----------------------------------------------------
        input = getClass().getResourceAsStream("/res/" + INSTALL_PATH_RESOURCE_KEY[os][osFlavor]);

        // ----------------------------------------------------
        // attempt to get an input stream through a resource
        // based on a key which is made specific to the target
        // OS by using the string returned by
        // System.getProperty ("os.name").toLowerCase ()
        // ----------------------------------------------------
        if (input == null)
        {
            String key = OsVersion.OS_NAME.toLowerCase().replace(' ', '_'); // avoid
            // spaces
            // in
            // file
            // names
            key = keyFragment + key.toLowerCase(); // for consistency among
            // TargetPanel res files
            input = TargetFactory.class.getResourceAsStream(key);
        }

        // ----------------------------------------------------
        // attempt to get an input stream through a resource
        // based on a key which is not specific to any target OS
        // ----------------------------------------------------
        if (input == null)
        {
            input = TargetFactory.class.getResourceAsStream(keyFragment);
        }

        // ----------------------------------------------------
        // If we got an input stream try to read the path
        // from the file
        // ----------------------------------------------------
        if (input != null)
        {
            InputStreamReader streamReader;
            BufferedReader reader = null;
            String line;

            try
            {
                streamReader = new InputStreamReader(input);
                reader = new BufferedReader(streamReader);
                line = reader.readLine();

                while (line != null)
                {
                    line = line.trim();
                    if (!"".equals(line))
                    {
                        break;
                    }
                    line = reader.readLine();
                }
                path = line;
            }
            catch (Throwable exception)
            {}
            finally
            {
                try
                {
                    if (reader != null) reader.close();
                }
                catch (Throwable exception)
                {}
            }
        }

        // ----------------------------------------------------
        // if we were unable to obtain a path from a resource,
        // use the default for the traget operating system.
        // ----------------------------------------------------
        if (path == null || "".equals(path))
        {
            path = "";

            // --------------------------------------------------
            // if we run on windows, we need a valid drive letter
            // to put in front of the path. The drive that
            // contains the user's home directory is usually the
            // drive that also contains the install directory,
            // so this seems the best choice here.
            // --------------------------------------------------
            if (os == WINDOWS)
            {
                String home = System.getProperty("user.home");
                // take everything up to and including the first '\'
                path = home.substring(0, home.indexOf(File.separatorChar) + 1);
            }

            path = path + INSTALL_PATH_FRAGMENT[os] + appName;
        }

        return path;
    }

    /**
     * Gets a prefix alias for the current platform. "Win_" on Windows Systems "Win_NT_" on WinNT4,
     * 2000, XP Mac on Mac Mac_X on macosx and Unix_
     * 
     * @return a prefix alias for the current platform
     */

    public static String getCurrentOSPrefix()
    {
        String OSName = System.getProperty("os.name").toLowerCase();
        String OSArch = System.getProperty("os.arch").toLowerCase();
        int OS = 0;
        int OSFlavor = 0;
        int OSarchitecture = 0;
        // ----------------------------------------------------
        // test for Windows
        // ----------------------------------------------------
        if (OSName.indexOf("windows") > -1)
        {
            OS = WINDOWS;
            OSFlavor = STANDARD;
            OSarchitecture = X86;

            if (OSName.indexOf("nt") > -1)
            {
                OSFlavor = NT;
            }
            else if (OSName.indexOf("2000") > -1)
            {
                OSFlavor = NT;
            }
            else if (OSName.indexOf("xp") > -1)
            {
                OSFlavor = NT;
            }
        }
        // ----------------------------------------------------
        // test for Mac OS
        // ----------------------------------------------------
        else if (OSName.indexOf("mac") > -1)
        {
            OS = GENERIC;
            OSFlavor = STANDARD;
            OSarchitecture = OTHER;

            if (OSName.indexOf("macosx") > -1)
            {
                OSFlavor = X;
            }
        }
        // ----------------------------------------------------
        // what's left should be unix
        // ----------------------------------------------------
        else
        {
            OS = UNIX;
            OSFlavor = STANDARD;
            OSarchitecture = OTHER;

            if (OSArch.indexOf("86") > -1)
            {
                OSarchitecture = X86;
            }
        }

        return (CLASS_PREFIX[OS] + CLASS_FLAVOR_PREFIX[OSFlavor]);
    }

}
/*---------------------------------------------------------------------------*/
