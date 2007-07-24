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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import java.util.Date;

/**
 * A Generator, Wrapper and Executor for Unix ShellScripts
 * 
 * @author marc.eppelmann&#064;reddot.de
 */
public class ShellScript
{

    // ~ Static fields/initializers *********************************************************

    // ~ Static fields/initializers *********************************************************
    /** Author = "marc.eppelmann_at_gmx.de" */
    private final static String Author = "Author: marc.eppelmann_at_gmx.de";

    /** Generator = "Generator: " + ShellScript.class.getName() */
    private final static String Generator = "Generator: " + ShellScript.class.getName();

    /** internal SourceCode Management ( currently 'svn') ID :: 'SCM_ID = "$Id$"' */
    private final static String SCM_ID = "$Id$";

    /** internal Revision = "$Revision$" */
    private final static String Revision = "$Revision$";

    /** internal comment prefix; makes a line as comment:-) :: 'CommentPre = "# "' */
    private final static String CommentPre = "# ";

    /** H = CommentPre */
    private final static String H = CommentPre;

    /** the linefeed: lf = "\n" */
    private final static String lf = "\n";

    /** lh = lf + H = "\n#" */
    private final static String lh = lf + H;

    /** the explanation header for this generated script */
    private final static String explanation = lh + "This is an automatically generated Script."
            + lh + "Usually this can be removed if the Generator " + lh
            + "was unable to remove the script after execution." + lf;

    /** "Generated at: " + new Date().toString() */
    private static String currentDateMsg = "Generated at: " + new Date().toString();

    /** the header of this ShellScript */
    private final static String header = lf + explanation + lf + H + Generator + lf + H + SCM_ID
            + lf + H + Author + lf + H + Revision + lf + H + currentDateMsg + lf + lf;

    // ~ Instance fields ********************************************************************

    // ~ Instance fields ********************************************************************
    /** Internal ContentBuffer of this ShellScript */
    private StringBuffer content = new StringBuffer();

    /** internal field: where to write via write( itsLocation ) this shellscript. */
    private String itsLocation;

    // ~ Constructors ***********************************************************************

    // ~ Constructors ***********************************************************************
    /**
     * Creates and initializes the ShellScript for running on the given shell.
     * 
     * @param aShell "sh", "bash", "ksh", "csh" and so an...
     */
    public ShellScript(String aShell)
    {
        content.append("#!/usr/bin/env " + aShell);
        content.append(header);
    }

    /**
     * Creates and initializes the ShellScript for running on the bourne shell: "sh".
     */
    public ShellScript()
    {
        this("sh");
    }

    // ~ Methods ****************************************************************************

    // ~ Methods ****************************************************************************
    /**
     * Appends an Object or String to this ShellScript.
     * 
     * @param anObject the Object to append
     */
    public void append(Object anObject)
    {
        content.append(anObject);
    }

    /**
     * Appends a Char to this ShellScript.
     * 
     * @param aChar a char to append
     */
    public void append(char aChar)
    {
        content.append(aChar);
    }

    /**
     * Appends an Object or String to this ShellScript with unix linefeed ("\n").
     * 
     * @param anObject the Object to append
     */
    public void appendln(Object anObject)
    {
        append(anObject);
        append(lf);
    }

    /**
     * Appends a Char Object or String to this ShellScript with unix linefeed ("\n").
     * 
     * @param aChar a char to append
     */
    public void appendln(char aChar)
    {
        append(aChar);
        append(lf);
    }

    /**
     * Appends an Object or String to this ShellScript with unix linefeed ("\n").
     */
    public void appendln()
    {
        append(lf);
    }

    /**
     * gets the Content of this Script.
     * 
     * @return the Content
     */
    public StringBuffer getContent()
    {
        return content;
    }

    /**
     * Gets the Content of this Script as String 
     * 
     * @return the script as String
     */
    public String getContentAsString()
    {
        return content.toString();
    }

    /**
     * Dumps the ShellScript Content, and Location.
     * Use getContentAsString() to get this ShellScripts Content 
     * 
     * @return The ShellScript as Object dump.
     */
    public String toString()
    {
        StringBuffer result = new StringBuffer();
        result.append(getClass().getName());
        result.append('\n');
        result.append(itsLocation);
        result.append('\n');
        result.append(content);

        return result.toString();
    }

    /**
     * write this to the given Destination FileName
     * 
     * @param aDestination a destination filename
     */
    public void write(String aDestination)
    {
        itsLocation = aDestination;

        try
        {
            BufferedWriter writer = new BufferedWriter(new FileWriter(aDestination));
            writer.write(content.toString());
            writer.write(lh + aDestination + lf);
            writer.flush();
            writer.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Executes thsi ShellScript with the given Params.<br>
     * NOTE: the params cannot be contain whitespaces.<br>
     * This (su -c &lt;br&gt;"cp from to"&lt;/br&gt;) would not work:<br>
     * because the underlaying java.runtime.exec("command") does not handle balanced or unbalanced
     * (") correctly.<br>
     * else just whitespace separate tokens.<br>
     * This means for the sample. runtime.exec() would ever execute such as: su "-c" "\"cp"
     * "fromFile" "toFile\""<br>
     * But this his hidden in Sun's native code ;-(<br>
     * This was the reason to write thsi class to have a Workaround :-)
     * 
     * @param itsParams
     * 
     * @return the output from stdout of the execution.
     */
    public String exec(String itsParams)
    {
        FileExecutor.getExecOutput(new String[] { UnixHelper.getCustomCommand("chmod"), "+x",
                itsLocation});

        if (itsParams != null)
        {
            return FileExecutor.getExecOutput(new String[] { itsLocation, itsParams});
        }
        else
        {
            return FileExecutor.getExecOutput(new String[] { itsLocation});
        }
    }

    /**
     * Execute this ShellScript.
     * 
     * @return the output from stdout of the execution.
     */
    public String exec()
    {
        return exec(null);
    }

    /**
     * Execs ths given lines in the creted shell stored on location.
     * 
     * @param aShell A Shell which will be eexecute the script.
     * @param lines The content of the script.
     * @param aLocation The location where to store.
     * @param itsParams Th eoptional params of the script.
     * 
     * @return the exec result
     */
    public static String execute(String aShell, StringBuffer lines, String aLocation,
            String itsParams)
    {
        ShellScript s = new ShellScript((aShell == null) ? "sh" : aShell);
        s.append(lines);
        s.write(aLocation);

        return (itsParams == null) ? s.exec() : s.exec(itsParams);
    }

    /**
     * Executes ths given lines in the created default shell (sh) stored on location.
     * 
     * @param lines the lines of the script to exec.s
     * @param aLocation where to store
     * 
     * @return the stdout of the script.
     */
    public static String execute(StringBuffer lines, String aLocation)
    {
        return ShellScript.execute(null, lines, aLocation, null);
    }

    /**
     * Executes and removes the script.<br>
     * The Lines be also written in python or perl,<br>
     * In this case, the Shell must be python or perl or so.
     * 
     * @param aShell The Shell which should exec the script. Can be also be python or perl, if the
     * shellcontent is given in this language.
     * @param lines of the script.
     * @param aLocation where to store.
     * @param itsParams which should be pass to the script.
     * 
     * @return the stdout.
     */
    public static String execAndDelete(String aShell, StringBuffer lines, String aLocation,
            String itsParams)
    {
        String result = execute(aShell, lines, aLocation, itsParams);
        File location = new File(aLocation);

        try
        {
            location.delete();
        }
        catch (Exception e)
        {
            location.deleteOnExit();
        }

        return result;
    }

    /**
     * Executes and removes the script.
     * 
     * @param lines of the script.
     * @param aLocation where to store.
     * 
     * @return the sdtout.
     */
    public static String execAndDelete(StringBuffer lines, String aLocation)
    {
        return execAndDelete(null, lines, aLocation, null);
    }

    /**
     * Test Main Method Run test with: java -cp .jar com.izforge.izpack.util.os.unix.ShellScript
     * 
     * @param args Arguments from Commandline
     */
    public static void main(String[] args)
    {
        /*
         * ShellScript s = new ShellScript( ); s.append( "ls $HOME" ); s.write( System.getProperty(
         * "user.home", "." ) + File.separator + "test.sh" );
         */

        /*
         * System.out.println(ShellScript.execute(new StringBuffer("ls $HOME"), System.getProperty(
         * "user.home", ".") + File.separator + Long.toString(System.currentTimeMillis()) +
         * "test.sh"));
         */
    }
}
