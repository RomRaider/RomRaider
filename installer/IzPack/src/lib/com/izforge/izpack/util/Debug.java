/*
 * IzPack - Copyright 2001-2007 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://developer.berlios.de/projects/izpack/
 *
 * Copyright 2002 Jan Blok
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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Enumeration;
import java.util.Properties;

import com.izforge.izpack.installer.Installer;

/**
 * This class is for debug purposes. It is highly recommended to use it on critical or experimental
 * code places. To enable the debug mode of IzPack, just start the installer with the java parameter
 * -DTRACE=true or -DSTACKTRACE=true to enable extendend output of the internal status of critical
 * objects. <br>
 * How to use it as IzPack Setup Developer: <br>
 * Just import this class and use one of the methods:
 * 
 * <dl>
 * <dt> Debug.trace( aCriticalObject ) </dt>
 * <dd> - to print the status on console </dd>
 * <dt> Debug.error( aCriticalObject ) </dt>
 * <dd> - to print the status on console and<br>
 * print the stacktrace of a supressed Exception. </dd>
 * <dt> Additionally: </dt>
 * <dd> if -DLOG is given the output will be written in the File see #LOGFILENAME in the users Home
 * directory. </dd>
 * </dl>
 * 
 * 
 * @author Julien Ponge, Klaus Bartz, Marc Eppelmann
 * @version $Revision: 1816 $ ($Id: Debug.java 1816 2007-04-23 19:57:27Z jponge $)
 */
public class Debug
{

    // ~ Static fields/initializers *********************************************************

    /**
     * Parameter for public javacall "java -jar izpack.jar -DLOG" (Class.internal.variable: (DLOG =
     * "LOG"))
     */
    public static final String DLOG = "LOG";

    /**
     * Parameter for public javacall "java -jar izpack.jar -DSTACKTRACE" (Class.internal.variable:
     * (DSTACKTRACE = "STACKTRACE"))
     */
    public static final String DSTACKTRACE = "STACKTRACE";

    /**
     * Parameter for public javacall "java -jar izpack.jar -DTRACE" (Class.internal.variable:
     * (DTRACE = "TRACE"))
     */
    public static final String DTRACE = "TRACE";

    /** System.Property Key: IZPACK_LOGFILE = "izpack.logfile" */
    public static final String IZPACK_LOGFILE = "izpack.logfile";

    /** LOG_WITHOUT_DATE = 0 */
    public static final int LOG_WITHOUT_DATE = 0;

    /** LOG_WITH_DATE = 1 */
    public static final int LOG_WITH_DATE = 1;

    /** LOG_WITH_TIME_STAMP = 2 */
    public static final int LOG_WITH_TIME_STAMP = 2;

    /** LOG_WITH_TIME_AND_DATE= LOG_WITH_DATE | LOG_WITH_TIME_STAMP = 3 */
    public static final int LOG_WITH_TIME_AND_DATE = LOG_WITH_DATE | LOG_WITH_TIME_STAMP;

    /** internally initial unintialized TRACE-flag */
    private static boolean TRACE;

    /** internal initial unintialized STACKTRACE-flag */
    private static boolean STACKTRACE;

    /** internal initial unintialized LOG-flag */
    private static boolean LOG;

    /** LOGFILE_PREFIX = "IzPack_Logfile_at_" */
    public static String LOGFILE_PREFIX = "IzPack_Logfile_at_";

    /** LOGFILE_EXTENSION = ".txt" */
    public static String LOGFILE_EXTENSION = ".txt";

    /** LOGFILENAME = LOGFILE_PREFIX + System.currentTimeMillis() + LOGFILE_EXTENSION */
    public static String LOGFILENAME = LOGFILE_PREFIX + System.currentTimeMillis()
            + LOGFILE_EXTENSION;

    /**
     * The log initializion bloc.
     */
    static
    {
        boolean st = false;

        try
        {
            st = Boolean.getBoolean(DSTACKTRACE);
        }
        catch (Exception ex)
        {
            // ignore
        }

        STACKTRACE = st;

        boolean log = false;

        try
        {
            log = Boolean.getBoolean(DLOG);
        }
        catch (Exception ex)
        {
            // ignore
        }

        LOG = log;

        boolean t = false;

        try
        {
            if (STACKTRACE)
            {
                t = true;
            }
            else
            {
                t = Boolean.getBoolean(DTRACE);
            }
        }
        catch (Exception ex)
        {
            // ignore
        }

        TRACE = t;

        if (LOG)
        {
            System.out.println(DLOG + " enabled.");
            PrintWriter logfile = createLogFile();

            Debug.log(Installer.class.getName() + " LogFile created at ");

            // ** write some runtime system properties into the logfile **
            Debug.log("System.Properties:", LOG_WITH_TIME_STAMP);

            Properties sysProps = System.getProperties();

            Enumeration spe = sysProps.keys();

            while (spe.hasMoreElements())
            {
                String aKey = (String) spe.nextElement();
                Debug.log(aKey + "  =  " + sysProps.getProperty(aKey), LOG_WITHOUT_DATE);
            }
            Debug.log("\n==========================================\n", LOG_WITHOUT_DATE);
            Debug.log("\n " + Installer.class.getName() + " installs on: \n", LOG_WITHOUT_DATE);
            Debug.log(OsVersion.getOsDetails(), LOG_WITHOUT_DATE);
            Debug.log("\n==========================================\n", LOG_WITHOUT_DATE);
        }

        if (TRACE)
        {
            System.out.println(DTRACE + " enabled.");
        }

        if (STACKTRACE)
        {
            System.out.println(DSTACKTRACE + " enabled.");
        }
    }

    // ~ Methods ****************************************************************************

    /**
     * Traces the internal status of the given Object
     * 
     * @param s
     */
    public static void trace(Object s)
    {
        if (TRACE)
        {
            // console.println(s.toString());
            System.out.println(s);

            if (STACKTRACE && (s instanceof Throwable))
            {
                // StringWriter sw = new StringWriter();
                // PrintWriter pw = new PrintWriter(sw);
                // ((Throwable)s).printStackTrace(pw);
                // console.println(sw.toString());
                ((Throwable) s).printStackTrace();
            }

            System.out.flush();
        }
    }

    /**
     * Traces the given object and additional write their status in the LOGFILE.
     * 
     * @param s
     */
    public static void error(Object s)
    {
        trace(s);
        System.err.println(s);
        System.err.flush();
        log(s);
    }

    /**
     * Logs the given Object in the created Logfile if -DLOG=true was given on commandline i.e: java
     * -DLOG=true -jar izpack-installer.jar
     * 
     * @param o The Object to log, can be also an exception.
     */
    public static void log(Object o)
    {
        log(o, LOG_WITH_TIME_AND_DATE);
    }

    /**
     * Logs the given Object in the created Logfile if -DLOG=true was given on commandline i.e: java
     * -DLOG=true -jar izpack-installer.jar
     * 
     * @param o The Object to log
     * @param withWhatFormat if the given MASK is greater than 0, Log with Date/Timestamp
     */
    public static void log(Object o, int withWhatFormat)
    {
        // if LOG was given
        if (LOG)
        {
            PrintWriter logfile;
            if ((logfile = getLogFile()) == null)
            {
                logfile = createLogFile();
            }

            if (logfile != null)
            {
                if (o == null)
                {
                    o = "null";
                }

                StringBuffer entry = new StringBuffer();
                if (logWithTimeStamp(withWhatFormat))
                {
                    entry.append(System.currentTimeMillis());
                    entry.append(';');
                    entry.append(' ');
                }
                if (logWithDate(withWhatFormat))
                {
                    entry.append(new Date());
                    entry.append(';');
                    entry.append(' ');
                }

                entry.append(o);

                logfile.println(entry.toString());

                if (o instanceof Throwable)
                {
                    ((Throwable) o).printStackTrace(logfile);
                }

                logfile.flush();

                // logfile.close();
                // logFile = null;
            }
            else
            {
                System.err.println("Cannot write into logfile: (" + logfile + ") <- '" + o + "'");
            }
        }
    }

    /**
     * Indicates that to log with Date.
     * 
     * @param withWhatFormat The whished Format
     * @return true if to log with Date
     */
    private static boolean logWithDate(int withWhatFormat)
    {

        return (withWhatFormat & LOG_WITH_DATE) == LOG_WITH_DATE;
    }

    /**
     * Indicates that to log with Timestamp.
     * 
     * @param withWhatFormat The whished Format
     * @return true if to log with Timestamp
     */
    private static boolean logWithTimeStamp(int withWhatFormat)
    {

        return (withWhatFormat & LOG_WITH_DATE) == LOG_WITH_DATE;
    }

    /**
     * Creates the logfile to write log-infos into.
     * 
     * @return The writer object instance
     */
    private static PrintWriter createLogFile()
    {
        String tempDir = System.getProperty("java.io.tmpdir");

        File tempDirFile = new File(tempDir);

        try
        {
            tempDirFile.mkdirs();
        }
        catch (RuntimeException e1)
        {
            e1.printStackTrace();
        }

        String logfilename = LOGFILENAME;
        System.out.println("creating Logfile: '" + logfilename + "' in: '" + tempDir + "'");

        File out = new File(tempDir, logfilename);

        PrintWriter logfile;
        if (tempDirFile.canWrite())
        {
            try
            {
                BufferedWriter fw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
                        out), "UTF-8"));
                logfile = setLogFile(new PrintWriter(fw));
            }
            catch (Exception e)
            {
                logfile = null;
                e.printStackTrace();
            }
        }
        else
        {
            logfile = null;
            System.err.println("Fatal: cannot write File: '" + logfilename + "' into: "
                    + tempDirFile);
        }

        return logfile;
    }

    /**
     * Indicates if debug is tracing
     * 
     * @return true if tracing otherwise false
     */
    public static boolean tracing()
    {
        return TRACE;
    }

    /**
     * Indicates if debug is stacktracing
     * 
     * @return true if stacktracing otherwise false
     */
    public static boolean stackTracing()
    {
        return STACKTRACE;
    }

    /**
     * Returns the LOG flag.
     * 
     * @return Returns the LOG flag.
     */
    public static boolean isLOG()
    {
        return LOG;
    }

    /**
     * Sets The LOG like the given value
     * 
     * @param aFlag The LOG status to set to or not.
     */
    public static void setLOG(boolean aFlag)
    {
        System.out.println(DLOG + " = " + aFlag);
        LOG = aFlag;
    }

    /**
     * Returns the current STACKTRACE flag
     * 
     * @return Returns the STACKTRACE.
     */
    public static boolean isSTACKTRACE()
    {
        return STACKTRACE;
    }

    /**
     * Sets the STACKTRACE like the given value
     * 
     * @param aFlag The STACKTRACE to set / unset.
     */
    public static void setSTACKTRACE(boolean aFlag)
    {
        System.out.println(DSTACKTRACE + " = " + aFlag);
        STACKTRACE = aFlag;
    }

    /**
     * Gets the current TRACE flag
     * 
     * @return Returns the TRACE.
     */
    public static boolean isTRACE()
    {
        return TRACE;
    }

    /**
     * Sets the TRACE flag like the given value
     * 
     * @param aFlag The TRACE to set / unset.
     */
    public static void setTRACE(boolean aFlag)
    {
        System.out.println(DTRACE + " = " + aFlag);
        TRACE = aFlag;
    }

    /**
     * Get the Logfile
     * 
     * @return Returns the logFile.
     */
    public static PrintWriter getLogFile()
    {
        PrintWriter logfile = (PrintWriter) System.getProperties().get(IZPACK_LOGFILE);

        return logfile;
    }

    /**
     * Sets the Logfile
     * 
     * @param aLogFile The logFile to set. *
     * @return The logfile to write into
     */
    public static synchronized PrintWriter setLogFile(PrintWriter aLogFile)
    {
        System.getProperties().put(IZPACK_LOGFILE, aLogFile);

        PrintWriter logfile = (PrintWriter) System.getProperties().get(IZPACK_LOGFILE);

        if (logfile == null)
        {
            System.err.println("Set::logfile == null");
        }

        return logfile;
    }
}
