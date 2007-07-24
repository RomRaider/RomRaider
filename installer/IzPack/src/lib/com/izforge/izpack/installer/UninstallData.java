/*
 * IzPack - Copyright 2001-2007 Julien Ponge, All Rights Reserved.
 * 
 * http://izpack.org/
 * http://developer.berlios.de/projects/izpack/
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

package com.izforge.izpack.installer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;


import com.izforge.izpack.ExecutableFile;
import com.izforge.izpack.util.os.unix.UnixUser;

/**
 * Holds uninstallation data. Implemented as a singleton.
 * 
 * @author Julien Ponge created October 27, 2002
 */
public class UninstallData
{
    /** The uninstall data object. */
    private static UninstallData instance = null;

    /** The files list. */
    private List filesList;

    /** The executables list. */
    private List executablesList;

    /** The uninstaller jar filename. */
    private String uninstallerJarFilename;

    /** The uninstaller path. */
    private String uninstallerPath;

    /** Additional uninstall data like uninstaller listener list. */
    private Map additionalData;
    
    /** Filesmap which should removed by the root user for another user */
    private String rootScript;

    /** The constructor. */
    private UninstallData()
    {
        filesList = new ArrayList();
        executablesList = new ArrayList();
        additionalData = new HashMap();
        rootScript = new String();
    }
    
    /** Constant RootFiles = "rootfiles" */
    public final static String ROOTSCRIPT = "rootscript";

    /**
     * Returns the instance (it is a singleton).
     * 
     * @return The instance.
     */
    public synchronized static UninstallData getInstance()
    {
        if (instance == null) instance = new UninstallData();
        return instance;
    }

    /**
     * Adds a file to the data.
     * 
     * @param path The file to add.
     */
    public synchronized void addFile(String path)
    {
        if(path != null)
           filesList.add(path);
    }

    /**
     * Returns the files list.
     * 
     * @return The files list.
     */
    public List getFilesList()
    {
        return filesList;
    }

    /**
     * Adds an executable to the data.
     * 
     * @param file The executable file.
     */
    public synchronized void addExecutable(ExecutableFile file)
    {
        executablesList.add(file);
    }

    /**
     * Returns the executables list.
     * 
     * @return The executables list.
     */
    public List getExecutablesList()
    {
        return executablesList;
    }

    /**
     * Returns the uninstaller jar filename.
     * 
     * @return The uninstaller jar filename.
     */
    public synchronized String getUninstallerJarFilename()
    {
        return uninstallerJarFilename;
    }

    /**
     * Sets the uninstaller jar filename.
     * 
     * @param name The uninstaller jar filename.
     */
    public synchronized void setUninstallerJarFilename(String name)
    {
        uninstallerJarFilename = name;
    }

    /**
     * Returns the path to the uninstaller.
     * 
     * @return The uninstaller filename path.
     */
    public String getUninstallerPath()
    {
        return uninstallerPath;
    }

    /**
     * Sets the uninstaller path.
     * 
     * @param path The uninstaller path.
     */
    public void setUninstallerPath(String path)
    {
        uninstallerPath = path;
    }

    /**
     * Returns additional uninstall data like uninstaller listener list.
     * 
     * @return additional uninstall data
     */
    public Map getAdditionalData()
    {
        return additionalData;
    }

    /**
     * Sets additional uninstall data like uninstaller listener list.
     * 
     * @param name key for the additional uninstall data
     * @param value the additional uninstall data
     */
    public void addAdditionalData(String name, Object value)
    {
        additionalData.put(name, value);
    }

    /**
     * Adds the given File to delete several Shortcuts as Root for the given Users.
     * 
     * @param aRootUninstallScript The Script to exec as Root at uninstall.
     */
    public void addRootUninstallScript( String aRootUninstallScript )
    {    
        rootScript = new String( aRootUninstallScript==null?"":aRootUninstallScript );
    }
    
    /**
     * Returns the root data.
     * 
     * @return root data
     */
    public String getRootScript()
    {
        return rootScript;
    }
    
    

}
