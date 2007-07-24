/*
 * IzPack - Copyright 2001-2007 Julien Ponge, All Rights Reserved.
 * 
 * http://izpack.org/ http://developer.berlios.de/projects/izpack/
 * 
 * Copyright 2004 Hani Suleiman
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
package com.izforge.izpack.util;

import java.io.File;
import java.io.IOException;

/**
 * This is a convienient class, which helps you to detect / identify the running OS/Distribution
 * 
 * Created at: Date: Nov 9, 2004 Time: 8:53:22 PM
 * 
 * @author hani, Marc.Eppelmann&#064;reddot.de
 */
public final class OsVersion implements OsVersionConstants, StringConstants
{

    //~ Static fields/initializers
    // *******************************************************************************************************************************

    /** OS_NAME = System.getProperty( "os.name" ) */
    public static final String OS_NAME = System.getProperty( OSNAME );

    /** True if this is FreeBSD. */
    public static final boolean IS_FREEBSD = StringTool.startsWithIgnoreCase(OS_NAME, FREEBSD );

    /** True if this is Linux. */
    public static final boolean IS_LINUX = StringTool.startsWithIgnoreCase(OS_NAME, LINUX );

    /** True if this is HP-UX. */
    public static final boolean IS_HPUX = StringTool.startsWithIgnoreCase(OS_NAME, HP_UX );

    /** True if this is AIX. */
    public static final boolean IS_AIX = StringTool.startsWithIgnoreCase(OS_NAME, AIX );

    /** True if this is SunOS. */
    public static final boolean IS_SUNOS = StringTool.startsWithIgnoreCase(OS_NAME, SUNOS );

    /** True if this is OS/2. */
    public static final boolean IS_OS2 = StringTool.startsWith(OS_NAME, OS_2 );

    /** True is this is Mac OS */
    public static final boolean IS_MAC = StringTool.startsWith(OS_NAME, MAC );
    
    /** True if this is the Mac OS X. */
    public static final boolean IS_OSX = StringTool.startsWithIgnoreCase(OS_NAME, MACOSX);

    /** True if this is Windows. */
    public static final boolean IS_WINDOWS = StringTool.startsWith(OS_NAME, WINDOWS );

    /** True if this is some variant of Unix (OSX, Linux, Solaris, FreeBSD, etc). */
    public static final boolean IS_UNIX = !IS_OS2 && !IS_WINDOWS;

    /** True if RedHat Linux was detected */
    public static final boolean IS_REDHAT_LINUX = IS_LINUX
            && ( ( FileUtil.fileContains(getReleaseFileName(), REDHAT ) || FileUtil.fileContains(getReleaseFileName() ,
                    RED_HAT ) ) );

    /** True if Fedora Linux was detected */
    public static final boolean IS_FEDORA_LINUX = IS_LINUX
            && FileUtil.fileContains(getReleaseFileName(), FEDORA );

    /** True if Mandriva(Mandrake) Linux was detected */
    public static final boolean IS_MANDRAKE_LINUX = IS_LINUX
             && FileUtil.fileContains( getReleaseFileName(), MANDRAKE );
    
    /** True if Mandrake/Mandriva Linux was detected */
    public static final boolean IS_MANDRIVA_LINUX = ( IS_LINUX
            && FileUtil.fileContains( getReleaseFileName(), MANDRIVA ) ) || IS_MANDRAKE_LINUX;

    /** True if SuSE Linux was detected */
    public static final boolean IS_SUSE_LINUX = IS_LINUX
            && FileUtil.fileContains( getReleaseFileName(), SUSE, true );  /*  caseInsensitive , since 'SUSE' 10 */

    /** True if Debian Linux or derived was detected */
    public static final boolean IS_DEBIAN_LINUX = (IS_LINUX
            && FileUtil.fileContains(PROC_VERSION, DEBIAN )) || ( IS_LINUX && new File( "/etc/debian_version" ).exists() );

    // TODO detect the newcomer (K)Ubuntu */
    //~ Methods
    // **************************************************************************************************************************************************

    /**
     * Gets the etc Release Filename
     * 
     * @return name of the file the release info is stored in for Linux distributions
     */
    private static String getReleaseFileName()
    {
        String result = "";

        File[] etcList = new File("/etc").listFiles();
        
        if( etcList != null )
        for (int idx = 0; idx < etcList.length; idx++)
        {
            File etcEntry = etcList[idx];

            if (etcEntry.isFile())
            {
                if (etcEntry.getName().endsWith("-release"))
                {
                    //match :-)
                    return result = etcEntry.toString();
                }
            }
        }

        return result;
    }

    /**
     * Gets the Details of a Linux Distribution
     * 
     * @return description string of the Linux distribution
     */
    private static String getLinuxDistribution()
    {
        String result = null;

        if (IS_SUSE_LINUX)
        {
            try
            {
                result = SUSE + SP + LINUX + NL + StringTool.stringArrayListToString(FileUtil.getFileContent(getReleaseFileName()));
            }
            catch (IOException e)
            {
                // TODO ignore
            }
        }
        else if (IS_REDHAT_LINUX)
        {
            try
            {
                result = REDHAT + SP + LINUX + NL + StringTool.stringArrayListToString(FileUtil.getFileContent(getReleaseFileName()));
            }
            catch (IOException e)
            {
                // TODO ignore
            }
        }

        else if (IS_FEDORA_LINUX)
        {
            try
            {
                result = FEDORA + SP + LINUX + NL
                        + StringTool.stringArrayListToString(FileUtil.getFileContent(getReleaseFileName()));
            }
            catch (IOException e)
            {
                // TODO ignore
            }
        }
        else if (IS_MANDRAKE_LINUX)
        {
            try
            {
                result = MANDRAKE + SP + LINUX + NL
                        + StringTool.stringArrayListToString(FileUtil.getFileContent(getReleaseFileName()));
            }
            catch (IOException e)
            {
                // TODO ignore
            }
        }
        else if (IS_MANDRIVA_LINUX)
        {
            try
            {
                result = MANDRIVA + SP +  LINUX + NL
                        + StringTool.stringArrayListToString(FileUtil.getFileContent(getReleaseFileName()));
            }
            catch (IOException e)
            {
                // TODO ignore
            }
        }
        else if (IS_DEBIAN_LINUX)
        {
            try
            {
                result = DEBIAN + SP + LINUX + NL
                        + StringTool.stringArrayListToString(FileUtil.getFileContent("/etc/debian_version"));
            }
            catch (IOException e)
            {
                // TODO ignore
            }
        }
        else
        {
            try
            {
                result = "Unknown Linux Distribution\n"
                        + StringTool.stringArrayListToString(FileUtil.getFileContent(getReleaseFileName()));
            }
            catch (IOException e)
            {
                // TODO ignore
            }
        }

        return result;
    }

    /**
     * returns a String which contains details of known OSs
     * @return the details
     */
    public static String getOsDetails()
    {
        StringBuffer result = new StringBuffer();
        result.append("OS_NAME=").append(OS_NAME).append(NL);

        if( IS_UNIX )
        {
            if( IS_LINUX )
            {
                result.append(getLinuxDistribution()).append(NL);
            }
            else
            {
                try
                {
                    result.append(FileUtil.getFileContent(getReleaseFileName())).append(NL);
                }
                catch (IOException e)
                {
                    // TODO handle or ignore
                }
            }
        }

        if( IS_WINDOWS )
        {
            result.append(System.getProperty(OSNAME)).append(SP).append(System.getProperty("sun.os.patch.level", "")).append(NL);
        }
        return result.toString();
    }
    
    /**
     * Testmain
     * 
     * @param args Commandline Args
     */
    public static void main(String[] args)
    {
      System.out.println( getOsDetails() );
    }    
}
