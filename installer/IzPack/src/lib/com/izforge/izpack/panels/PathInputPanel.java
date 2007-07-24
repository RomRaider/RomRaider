/*
 * IzPack - Copyright 2001-2007 Julien Ponge, All Rights Reserved.
 * 
 * http://izpack.org/
 * http://developer.berlios.de/projects/izpack/
 * 
 * Copyright 2004 Klaus Bartz
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

package com.izforge.izpack.panels;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.izforge.izpack.gui.IzPanelLayout;
import com.izforge.izpack.installer.InstallData;
import com.izforge.izpack.installer.InstallerFrame;
import com.izforge.izpack.installer.IzPanel;
import com.izforge.izpack.installer.ResourceNotFoundException;
import com.izforge.izpack.util.AbstractUIHandler;
import com.izforge.izpack.util.Debug;
import com.izforge.izpack.util.IoHelper;
import com.izforge.izpack.util.OsVersion;
import com.izforge.izpack.util.VariableSubstitutor;

/**
 * Base class for panels which asks for paths.
 * 
 * @author Klaus Bartz
 * 
 */
public class PathInputPanel extends IzPanel implements ActionListener
{

    /**
     * 
     */
    private static final long serialVersionUID = 3257566217698292531L;

    /** Flag whether the choosen path must exist or not */
    protected boolean mustExist = false;

    /** Files which should be exist */
    protected String[] existFiles = null;

    /** The path which was chosen */
    // protected String chosenPath;
    /** The path selection sub panel */
    protected PathSelectionPanel pathSelectionPanel;

    protected String emptyTargetMsg;

    protected String warnMsg;

    protected static String defaultInstallDir = null;

    /**
     * The constructor.
     * 
     * @param parent The parent window.
     * @param idata The installation data.
     */
    public PathInputPanel(InstallerFrame parent, InstallData idata)
    {
        super(parent, idata, new IzPanelLayout());
        // Set default values
        emptyTargetMsg = getI18nStringForClass("empty_target", "TargetPanel");
        warnMsg = getI18nStringForClass("warn", "TargetPanel");
         
        String introText = getI18nStringForClass("extendedIntro", "PathInputPanel");
        if (introText == null || introText.endsWith("extendedIntro")
                || introText.indexOf('$') > -1 )
        {
            introText = getI18nStringForClass("intro", "PathInputPanel");
            if (introText == null || introText.endsWith("intro"))
                introText = "";
        }
        // Intro
        // row 0 column 0
        add(createMultiLineLabel(introText));
        // Label for input
        // row 1 column 0.
        add(createLabel("info", "TargetPanel", "open",
                LEFT, true), NEXT_LINE);
        // Create path selection components and add they to this panel.
        pathSelectionPanel = new PathSelectionPanel(this, idata);
        add(pathSelectionPanel, NEXT_LINE);
        createLayoutBottom();
        getLayoutHelper().completeLayout();
        }
    /**
     * This method does nothing. It is called from ctor of PathInputPanel, to give in a derived
     * class the possibility to add more components under the path input components.
     */
    public void createLayoutBottom()
    {
        // Derived classes implements additional elements.
    }

    /**
     * Actions-handling method.
     * 
     * @param e The event.
     */
    public void actionPerformed(ActionEvent e)
    {
        Object source = e.getSource();
        if (source == pathSelectionPanel.getPathInputField())
        {
            parent.navigateNext();
        }

    }

    /**
     * Indicates wether the panel has been validated or not.
     * 
     * @return Wether the panel has been validated or not.
     */
    public boolean isValidated()
    {
        String chosenPath = pathSelectionPanel.getPath();
        boolean ok = true;

        // We put a warning if the specified target is nameless
        if (chosenPath.length() == 0)
        {
            if (isMustExist())
            {
                emitError(parent.langpack.getString("installer.error"), parent.langpack
                        .getString("PathInputPanel.required"));
                return false;
            }
            ok = emitWarning(parent.langpack.getString("installer.warning"), emptyTargetMsg);
        }
        if (!ok) return ok;

        // Normalize the path
        File path = new File(chosenPath).getAbsoluteFile();
        chosenPath = path.toString();
        pathSelectionPanel.setPath(chosenPath);
        if (isMustExist())
        {
            if (!path.exists())
            {
                emitError(parent.langpack.getString("installer.error"), parent.langpack
                        .getString(getI18nStringForClass("required", "PathInputPanel")));
                return false;
            }
            if (!pathIsValid())
            {
                emitError(parent.langpack.getString("installer.error"), parent.langpack
                        .getString(getI18nStringForClass("notValid", "PathInputPanel")));
                return false;
            }
        }
        else
        {
            // We assume, that we would install something into this dir
            if (!isWriteable())
            {
                emitError(parent.langpack.getString("installer.error"), getI18nStringForClass(
                        "notwritable", "TargetPanel"));
                return false;
            }
            // We put a warning if the directory exists else we warn
            // that it will be created
            if (path.exists())
            {
                int res = askQuestion(parent.langpack.getString("installer.warning"), warnMsg,
                        AbstractUIHandler.CHOICES_YES_NO, AbstractUIHandler.ANSWER_YES);
                ok = res == AbstractUIHandler.ANSWER_YES;
            }
            else
            {
			    ok = this.emitNotificationFeedback(getI18nStringForClass("createdir", "TargetPanel") + "\n"
			            + chosenPath);
			
			}
        }
        return ok;
    }

    /**
     * Returns whether the chosen path is true or not. If existFiles are not null, the existence of
     * it under the choosen path are detected. This method can be also implemented in derived
     * classes to handle special verification of the path.
     * 
     * @return true if existFiles are exist or not defined, else false
     */
    protected boolean pathIsValid()
    {
        if (existFiles == null) return true;
        for (int i = 0; i < existFiles.length; ++i)
        {
            File path = new File(pathSelectionPanel.getPath(), existFiles[i]).getAbsoluteFile();
            if (!path.exists()) return false;
        }
        return true;
    }

    /**
     * Returns the must exist state.
     * 
     * @return the must exist state
     */
    public boolean isMustExist()
    {
        return mustExist;
    }

    /**
     * Sets the must exist state. If it is true, the path must exist.
     * 
     * @param b must exist state
     */
    public void setMustExist(boolean b)
    {
        mustExist = b;
    }

    /**
     * Returns the array of strings which are described the files which must exist.
     * 
     * @return paths of files which must exist
     */
    public String[] getExistFiles()
    {
        return existFiles;
    }

    /**
     * Sets the paths of files which must exist under the chosen path.
     * 
     * @param strings paths of files which must exist under the chosen path
     */
    public void setExistFiles(String[] strings)
    {
        existFiles = strings;
    }

    /**
     * Loads up the "dir" resource associated with TargetPanel. Acceptable dir resource names:
     * <code>
     *   TargetPanel.dir.macosx
     *   TargetPanel.dir.mac
     *   TargetPanel.dir.windows
     *   TargetPanel.dir.unix
     *   TargetPanel.dir.xxx,
     *     where xxx is the lower case version of System.getProperty("os.name"),
     *     with any spaces replace with underscores
     *   TargetPanel.dir (generic that will be applied if none of above is found)
     *   </code>
     * As with all IzPack resources, each the above ids should be associated with a separate
     * filename, which is set in the install.xml file at compile time.
     */
    public static void loadDefaultInstallDir(InstallerFrame parentFrame, InstallData idata)
    {
        // Load only once ...
        if (getDefaultInstallDir() != null) return;
        BufferedReader br = null;
        try
        {
            InputStream in = null;

            if (OsVersion.IS_WINDOWS)
            {
                try
                {
                in = parentFrame.getResource("TargetPanel.dir.windows");
                }
                catch (ResourceNotFoundException rnfe)
                {}//it's usual, that the resource does not exist
            }
            else if (OsVersion.IS_OSX)
            { 
                try
                {
                in = parentFrame.getResource("TargetPanel.dir.macosx");
                }
                catch (ResourceNotFoundException rnfe)
                {}//it's usual, that the resource does not exist
            }
            else
            {
                String os = System.getProperty("os.name");
                // first try to look up by specific os name
                os = os.replace(' ', '_'); // avoid spaces in file names
                os = os.toLowerCase(); // for consistency among TargetPanel res
                // files
                try
                {
                    in = parentFrame.getResource("TargetPanel.dir.".concat(os));
                }
                catch (ResourceNotFoundException rnfe)
                {}
                // if not specific os, try getting generic 'unix' resource file
                if (in == null)
                {
                    try
                    {
                        in = parentFrame.getResource("TargetPanel.dir.unix");
                    }
                    catch (ResourceNotFoundException eee)
                    {}
                }

            }

            // if all above tests failed, there is no resource file,
            // so use system default
            if (in == null)
            {
                try
                {
                    in = parentFrame.getResource("TargetPanel.dir");
                }
                catch (ResourceNotFoundException eee)
                {}
            }

            if (in != null)
            {
                // now read the file, once we've identified which one to read
                InputStreamReader isr = new InputStreamReader(in);
                br = new BufferedReader(isr);
                String line;
                while ((line = br.readLine()) != null)
                {
                    line = line.trim();
                    // use the first non-blank line
                    if (!"".equals(line)) break;
                }
                defaultInstallDir = line;
                VariableSubstitutor vs = new VariableSubstitutor(idata.getVariables());
                defaultInstallDir = vs.substitute(defaultInstallDir, null);
            }
        }
        catch (Exception e)
        {
            //mar: what's the common way to log an exception ?
            e.printStackTrace();
            defaultInstallDir = null;
            // leave unset to take the system default set by Installer class
        }
        finally
        {
            try
            {
                if (br != null) br.close();
            }
            catch (IOException ignored)
            {}
        }
    }

    /**
     * This method determines whether the chosen dir is writeable or not.
     * 
     * @return whether the chosen dir is writeable or not
     */
    public boolean isWriteable()
    {
        File existParent = IoHelper.existingParent(new File(pathSelectionPanel.getPath()));
        if (existParent == null) return false;
        // On windows we cannot use canWrite because
        // it looks to the dos flags which are not valid
        // on NT or 2k XP or ...
        if (OsVersion.IS_WINDOWS)
        {
            File tmpFile;
            try
            {
                tmpFile = File.createTempFile("izWrTe", ".tmp", existParent);
                tmpFile.deleteOnExit();
            }
            catch (IOException e)
            {
                Debug.trace(e.toString());
                return false;
            }
            return true;
        }
        return existParent.canWrite();
    }

    /**
     * Returns the default for the installation directory.
     * 
     * @return the default for the installation directory
     */
    public static String getDefaultInstallDir()
    {
        return defaultInstallDir;
    }

    /**
     * Sets the default for the installation directory to the given string.
     * 
     * @param string path for default for the installation directory
     */
    public static void setDefaultInstallDir(String string)
    {
        defaultInstallDir = string;
    }

}
