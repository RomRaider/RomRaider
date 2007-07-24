/*
 * IzPack - Copyright 2001-2007 Julien Ponge, All Rights Reserved.
 * 
 * http://izpack.org/
 * http://developer.berlios.de/projects/izpack/
 * 
 * Copyright 2004 Klaus Bartz
 * Copyright 2004 Thomas Guenter
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

package com.izforge.izpack.event;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.tools.ant.BuildLogger;
import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.DemuxOutputStream;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Target;
import org.apache.tools.ant.input.DefaultInputHandler;
import org.apache.tools.ant.taskdefs.Ant;
import org.apache.tools.ant.util.JavaEnvUtils;

/**
 * This class contains data and 'perform' logic for ant action listeners.
 * 
 * @author Thomas Guenter
 * @author Klaus Bartz
 * 
 */
public class AntAction extends ActionBase
{

    // --------AntAction specific String constants for ------------
    // --- parsing the XML specification ------------

    private static final long serialVersionUID = 3258131345250005557L;

    public static final String ANTACTIONS = "antactions";

    public static final String ANTACTION = "antaction";

    public static final String ANTCALL = "antcall";

    private boolean quiet = false;

    private boolean verbose = false;

    private Properties properties = null;

    private List targets = null;

    private List uninstallTargets = null;

    private String logFile = null;

    private String buildFile = null;

    private List propertyFiles = null;

    /**
     * Default constructor
     */
    public AntAction()
    {
        super();
        properties = new Properties();
        targets = new ArrayList();
        uninstallTargets = new ArrayList();
        propertyFiles = new ArrayList();
    }

    /**
     * Performs all defined install actions.
     * 
     * Calls {#performAction performAction(false)}.
     * 
     * @throws Exception
     */
    public void performInstallAction() throws Exception
    {
        performAction(false);
    }

    /**
     * Performs all defined uninstall actions.
     * 
     * Calls {#performAction performAction(true)}.
     * 
     * @throws Exception
     */
    public void performUninstallAction() throws Exception
    {
        performAction(true);
    }

    /**
     * Performs all defined actions.
     * 
     * @param uninstall An install/uninstall switch. If this is <tt>true</tt> only the uninstall
     * actions, otherwise only the install actions are being performed.
     * 
     * @see #performInstallAction() for calling all install actions.
     * @see #performUninstallAction() for calling all uninstall actions.
     * 
     * @throws Exception
     */
    public void performAction(boolean uninstall) throws Exception
    {
        if (verbose) System.out.println("Calling ANT with buildfile: " + buildFile);
        SecurityManager oldsm = null;
        if (!JavaEnvUtils.isJavaVersion("1.0") && !JavaEnvUtils.isJavaVersion("1.1"))
            oldsm = System.getSecurityManager();
        PrintStream err = System.err;
        PrintStream out = System.out;
        try
        {
            Project antProj = new Project();
            antProj.setName("antcallproject");
            antProj.addBuildListener(createLogger());
            antProj.setInputHandler(new DefaultInputHandler());
            antProj.setSystemProperties();
            addProperties(antProj, getProperties());
            addPropertiesFromPropertyFiles(antProj);
            // TODO: propertyfiles, logFile
            antProj.fireBuildStarted();
            antProj.init();
            List antcalls = new ArrayList();
            List choosenTargets = (uninstall) ? uninstallTargets : targets;
            if (choosenTargets.size() > 0)
            {
                Ant antcall = null;
                for (int i = 0; i < choosenTargets.size(); i++)
                {
                    antcall = (Ant) antProj.createTask("ant");
                    antcall.setAntfile(getBuildFile());
                    antcall.setTarget((String) choosenTargets.get(i));
                    antcalls.add(antcall);
                }
            }
            Target target = new Target();
            target.setName("calltarget");

            for (int i = 0; i < antcalls.size(); i++)
            {
                target.addTask((Ant) antcalls.get(i));
            }
            antProj.addTarget(target);
            System.setOut(new PrintStream(new DemuxOutputStream(antProj, false)));
            System.setErr(new PrintStream(new DemuxOutputStream(antProj, true)));
            antProj.executeTarget("calltarget");
        }
        finally
        {
            if (oldsm != null) System.setSecurityManager(oldsm);
            System.setOut(out);
            System.setErr(err);
        }
    }

    /**
     * Returns the build file.
     * 
     * @return the build file
     */
    public String getBuildFile()
    {
        return buildFile;
    }

    /**
     * Sets the build file to be used to the given string.
     * 
     * @param buildFile build file path to be used
     */
    public void setBuildFile(String buildFile)
    {
        this.buildFile = buildFile;
    }

    /**
     * Returns the current logfile path as string.
     * 
     * @return current logfile path
     */
    public String getLogFile()
    {
        return logFile;
    }

    /**
     * Sets the logfile path to the given string.
     * 
     * @param logFile to be set
     */
    public void setLogFile(String logFile)
    {
        this.logFile = logFile;
    }

    /**
     * Returns the property file paths as list of strings.
     * 
     * @return the property file paths
     */
    public List getPropertyFiles()
    {
        return propertyFiles;
    }

    /**
     * Adds one property file path to the internal list of property file paths.
     * 
     * @param propertyFile to be added
     */
    public void addPropertyFile(String propertyFile)
    {
        this.propertyFiles.add(propertyFile);
    }

    /**
     * Sets the property file path list to the given list. Old settings will be lost.
     * 
     * @param propertyFiles list of property file paths to be set
     */
    public void setPropertyFiles(List propertyFiles)
    {
        this.propertyFiles = propertyFiles;
    }

    /**
     * Returns the properties.
     * 
     * @return the properties
     */
    public Properties getProperties()
    {
        return properties;
    }

    /**
     * Sets the internal properties to the given properties. Old settings will be lost.
     * 
     * @param properties properties to be set
     */
    public void setProperties(Properties properties)
    {
        this.properties = properties;
    }

    /**
     * Sets the given value to the property identified by the given name.
     * 
     * @param name key of the property
     * @param value value to be used for the property
     */
    public void setProperty(String name, String value)
    {
        this.properties.put(name, value);
    }

    /**
     * Returns the value for the property identified by the given name.
     * 
     * @param name name of the property
     * @return value of the property
     */
    public String getProperty(String name)
    {
        return this.properties.getProperty(name);
    }

    /**
     * Returns the quiet state.
     * 
     * @return quiet state
     */
    public boolean isQuiet()
    {
        return quiet;
    }

    /**
     * Sets whether the associated ant task should be performed quiet or not.
     * 
     * @param quiet quiet state to set
     */
    public void setQuiet(boolean quiet)
    {
        this.quiet = quiet;
    }

    /**
     * Returns the targets.
     * 
     * @return the targets
     */
    public List getTargets()
    {
        return targets;
    }

    /**
     * Sets the targets which should be performed at installation time. Old settings are lost.
     * 
     * @param targets list of targets
     */
    public void setTargets(ArrayList targets)
    {
        this.targets = targets;
    }

    /**
     * Adds the given target to the target list which should be performed at installation time.
     * 
     * @param target target to be add
     */
    public void addTarget(String target)
    {
        this.targets.add(target);
    }

    /**
     * Returns the uninstaller targets.
     * 
     * @return the uninstaller targets
     */
    public List getUninstallTargets()
    {
        return uninstallTargets;
    }

    /**
     * Sets the targets which should be performed at uninstallation time. Old settings are lost.
     * 
     * @param targets list of targets
     */
    public void setUninstallTargets(ArrayList targets)
    {
        this.uninstallTargets = targets;
    }

    /**
     * Adds the given target to the target list which should be performed at uninstallation time.
     * 
     * @param target target to be add
     */
    public void addUninstallTarget(String target)
    {
        this.uninstallTargets.add(target);
    }

    /**
     * Returns the verbose state.
     * 
     * @return verbose state
     */
    public boolean isVerbose()
    {
        return verbose;
    }

    /**
     * Sets the verbose state.
     * 
     * @param verbose state to be set
     */
    public void setVerbose(boolean verbose)
    {
        this.verbose = verbose;
    }

    private BuildLogger createLogger()
    {
        int msgOutputLevel = 2;
        if (verbose)
            msgOutputLevel = 4;
        else if (quiet) msgOutputLevel = 1;
        BuildLogger logger = new DefaultLogger();
        logger.setMessageOutputLevel(msgOutputLevel);
        if (logFile != null)
        {
            PrintStream printStream;
            try
            {
                printStream = new PrintStream(new FileOutputStream(logFile));
                logger.setOutputPrintStream(printStream);
                logger.setErrorPrintStream(printStream);
            }
            catch (FileNotFoundException e)
            {
                logger.setOutputPrintStream(System.out);
                logger.setErrorPrintStream(System.err);
            }
        }
        else
        {
            logger.setOutputPrintStream(System.out);
            logger.setErrorPrintStream(System.err);
        }
        return logger;
    }

    private void addProperties(Project proj, Properties props)
    {
        if (proj == null) return;
        if (props.size() > 0)
        {
            Iterator iter = props.keySet().iterator();
            String key = null;
            while (iter.hasNext())
            {
                key = (String) iter.next();
                proj.setProperty(key, props.getProperty(key));
            }
        }
    }

    private void addPropertiesFromPropertyFiles(Project proj) throws Exception
    {
        if (proj == null) return;
        Properties props = new Properties();
        File pf = null;
        FileInputStream fis = null;
        try
        {
            for (int i = 0; i < propertyFiles.size(); i++)
            {
                pf = new File((String) propertyFiles.get(i));
                if (pf.exists())
                {
                    fis = new FileInputStream(pf);
                    props.load(fis);
                    fis.close();
                }
                else
                {
                    throw new Exception("Required propertyfile " + pf
                            + " for antcall doesn't exist.");
                }
            }
        }
        finally
        {
            if (fis != null) fis.close();
        }
        addProperties(proj, props);
    }

}
