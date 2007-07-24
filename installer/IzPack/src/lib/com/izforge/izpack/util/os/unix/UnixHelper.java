/*
 * IzPack - Copyright 2001-2007 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/ http://developer.berlios.de/projects/izpack/
 *
 * Copyright 2006 Marc Eppelmann
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
package com.izforge.izpack.util.os.unix;

import com.izforge.izpack.util.FileExecutor;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.StringReader;

import java.util.ArrayList;

/**
 * Helper Methods for unix-systems and derived.
 * 
 * @author marc.eppelmann&#064;reddot.de
 * @version $Revision: 1816 $
 */
public class UnixHelper
{

    // ~ Static fields/initializers *********************************************************

    /** whichCommand = "/usr/bin/which" or so */
    public static String whichCommand = FileExecutor.getExecOutput(
            new String[] { "/usr/bin/env", "which", "which"}, false).trim();

    public final static String VERSION = "$Revision: 1816 $";

    // ~ Methods ****************************************************************************

    /**
     * get the lines from /etc/passwd as Array
     * 
     * @return the /etc/passwd as String ArrayList
     */
    public static ArrayList getEtcPasswdArray()
    {
        ArrayList result = new ArrayList();

        String line = "";
        BufferedReader reader = null;

        try
        {
            reader = new BufferedReader(new FileReader(UnixConstants.etcPasswd));

            while ((line = reader.readLine()) != null)
            {
                result.add(line);
            }
        }
        catch (Exception e)
        {
            // ignore - there are maybe no users
        }

        return result;
    }

    /**
     * get the lines from /etc/passwd as Array
     * 
     * @return the /etc/passwd as String ArrayList
     */
    public static ArrayList getYpPasswdArray()
    {
        ArrayList result = new ArrayList();

        String line = "";
        BufferedReader reader = null;

        try
        {
            reader = new BufferedReader(new StringReader(FileExecutor.getExecOutput(new String[] {
                    getYpCatCommand(), "passwd"})));

            while ((line = reader.readLine()) != null)
            {
                result.add(line);
            }
        }
        catch (Exception e)
        {
            // ignore - there are maybe no users
        }

        return result;
    }

    /**
     * Test if KDE is installed. This is done by $>/usr/bin/env konqueror --version This assumes
     * that the konqueror as a main-app of kde is already installed. If this returns with 0 konqeror
     * and resp. kde means to be installed,
     * 
     * @return true if kde is installed otherwise false.
     */
    public static boolean kdeIsInstalled()
    {
        FileExecutor fe = new FileExecutor();

        String[] execOut = new String[2];

        int execResult = fe.executeCommand(
                new String[] { "/usr/bin/env", "konqueror", "--version"}, execOut);

        return execResult == 0;
    }

    /**
     * Gets the absolute Pathe to the cp (Copy) Command. This is necessary, because the command is
     * located at /bin on linux but in /usr/bin on Sun Solaris.
     * 
     * @return /bin/cp on linux /usr/bin/cp on solaris
     */
    public static String getWhichCommand()
    {
        return whichCommand;
    }

    /**
     * Gets the absolute Pathe to the cp (Copy) Command. This is necessary, because the command is
     * located at /bin on linux but in /usr/bin on Sun Solaris.
     * 
     * @return /bin/cp on linux /usr/bin/cp on solaris
     */
    public static String getCpCommand()
    {
        return FileExecutor.getExecOutput(new String[] { getWhichCommand(), "cp"}).trim();
    }

    /**
     * Gets the absolute Pathe to the su (SuperUser) Command. This is necessary, because the command
     * is located at /bin on linux but in /usr/bin on Sun Solaris.
     * 
     * @return /bin/su on linux /usr/bin/su on solaris
     */
    public static String getSuCommand()
    {
        return FileExecutor.getExecOutput(new String[] { getWhichCommand(), "su"}).trim();
    }

    /**
     * Gets the absolute Pathe to the rm (Remove) Command. This is necessary, because the command is
     * located at /bin on linux but in /usr/bin on Sun Solaris.
     * 
     * @return /bin/rm on linux /usr/bin/rm on solaris
     */
    public static String getRmCommand()
    {
        return FileExecutor.getExecOutput(new String[] { whichCommand, "rm"}).trim();
    }

    /**
     * Gets the absolute Pathe to the ypcat (YellowPage/NIS Cat) Command. This is necessary, because
     * the command is located at /bin on linux but in /usr/bin on Sun Solaris.
     * 
     * @return /bin/ypcat on linux /usr/bin/ypcat on solaris
     */
    public static String getYpCatCommand()
    {
        return FileExecutor.getExecOutput(new String[] { whichCommand, "ypcat"}).trim();
    }

    /**
     * Gets the absolute Pathe to the ypcat (YellowPage/NIS Cat) Command. This is necessary, because
     * the command is located at /bin on linux but in /usr/bin on Sun Solaris.
     * 
     * @param aCommand a Custom Command
     * 
     * @return /bin/ypcat on linux /usr/bin/ypcat on solaris
     */
    public static String getCustomCommand(String aCommand)
    {
        return FileExecutor.getExecOutput(new String[] { whichCommand, aCommand}).trim();
    }

    /**
     * Standalone Test Main Method call with : &gt; java -cp
     * ../bin/panels/UserInputPanel.jar:../_dist/IzPack-install-3.9.0-preview1.jar
     * com.izforge.izpack.util.os.unix.UnixHelper
     * 
     * @param args commandline args
     */
    public static void main(String[] args)
    {
        System.out.println("Hallo from " + UnixHelper.class.getName() + VERSION);

        // System.out.println( StringTool.stringArrayListToString(UnixUsers.getUsersAsArrayList())
        // );

        //System.out.println("Kde is" + (kdeIsInstalled() ? " " : " not ") + "installed");

        System.out.println("WhichCommand: '" + getWhichCommand() + "'");
        System.out.println("SuCommand: " + getSuCommand());
        System.out.println("RmCommand: " + getRmCommand());
        System.out.println("CopyCommand: " + getCpCommand());
        System.out.println("YpCommand: " + getYpCatCommand());

        System.out.println("CustomCommand: " + getCustomCommand("cat"));

        File tempFile = null;

        try
        {
            tempFile = File.createTempFile(UnixHelper.class.getName(), Long.toString(System
                    .currentTimeMillis())
                    + ".tmp");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        System.out.println("Tempfile: " + tempFile.toString());

        // This does not work :-(
        /*
         * FileExecutor.getExecOutput(new String[] { getCustomCommand("echo"), "Hallo", ">",
         * tempFile.toString()});
         */

        // so try:
        try
        {
            BufferedWriter w = new BufferedWriter(  new FileWriter(tempFile) );
            w.write("Hallo");
            w.flush();
            w.close();
            if( tempFile.exists() )
              System.out.println("Wrote: " + tempFile + ">>Hallo");
            else
                System.out.println("Could not Wrote: " + tempFile + "Hallo");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        // tempFile

        String destfilename = "/home/marc.eppelmann" + File.separator + "Desktop" ;

        System.out.println("Copy: " + tempFile.toString() + " to " + destfilename);

        String result = FileExecutor.getExecOutput(new String[] { getSuCommand(), "marc.eppelmann", "-c",
                "\"" + getCpCommand() + " " + tempFile.toString() + " " + destfilename + "\""});
        
        System.out.println("Wrote: " + tempFile.toString() + " to " + destfilename + " > " + result);

        // getYpPasswdArray();
        // System.out.println("/bin/bash".endsWith("sh"));
    }
}
