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
package com.izforge.izpack.compiler;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Properties;

import net.n3.nanoxml.XMLElement;

import com.izforge.izpack.CustomData;
import com.izforge.izpack.GUIPrefs;
import com.izforge.izpack.Info;
import com.izforge.izpack.Panel;
import com.izforge.izpack.compressor.PackCompressor;

/**
 * Interface for all packager implementations
 * 
 * @author Dennis Reil, <Dennis.Reil@reddot.de>
 */
public interface IPackager
{

    /**
     * Create the installer, beginning with the specified jar. If the name specified does not end in
     * ".jar", it is appended. If secondary jars are created for packs (if the Info object added has
     * a webDirURL set), they are created in the same directory, named sequentially by inserting
     * ".pack#" (where '#' is the pack number) ".jar" suffix: e.g. "foo.pack1.jar". If any file
     * exists, it is overwritten.
     */
    public abstract void createInstaller(File primaryFile) throws Exception;

    /**
     * Get the PackagerListener.
     * @return the current PackagerListener
     */
    public abstract PackagerListener getPackagerListener();

    /**
     * Adds a listener.
     * 
     * @param listener The listener.
     */
    public abstract void setPackagerListener(PackagerListener listener);

    /**
     * Sets the informations related to this installation.
     * 
     * @param info The info section.
     * @exception Exception Description of the Exception
     */
    public abstract void setInfo(Info info) throws Exception;

    /**
     * Sets the GUI preferences.
     * 
     * @param prefs The new gUIPrefs value
     */
    public abstract void setGUIPrefs(GUIPrefs prefs);

    /**
     * Allows access to add, remove and update the variables for the project, which are maintained
     * in the packager.
     * 
     * @return map of variable names to values
     */
    public abstract Properties getVariables();

    /**
     * Add a panel, where order is important. Only one copy of the class files neeed are inserted in
     * the installer.
     */
    public abstract void addPanelJar(Panel panel, URL jarURL);

    /**
     * Add a custom data like custom actions, where order is important. Only one copy of the class
     * files neeed are inserted in the installer.
     * 
     * @param ca custom action object
     * @param url the URL to include once
     */
    public abstract void addCustomJar(CustomData ca, URL url);

    /**
     * Adds a pack, order is mostly irrelevant.
     * 
     * @param pack contains all the files and items that go with a pack
     */
    public abstract void addPack(PackInfo pack);

    /**
     * Gets the packages list
     */
    public abstract List getPacksList();

    /**
     * Adds a language pack.
     * 
     * @param iso3 The ISO3 code.
     * @param xmlURL The location of the xml local info
     * @param flagURL The location of the flag image resource
     */
    public abstract void addLangPack(String iso3, URL xmlURL, URL flagURL);

    /**
     * Adds a resource.
     * 
     * @param resId The resource Id.
     * @param url The location of the data
     */
    public abstract void addResource(String resId, URL url);

    /**
     * Adds a native library.
     * 
     * @param name The native library name.
     * @param url The url to get the data from.
     * @exception Exception Description of the Exception
     */
    public abstract void addNativeLibrary(String name, URL url) throws Exception;

    /**
     * Adds a jar file content to the installer. Package structure is maintained. Need mechanism to
     * copy over signed entry information.
     * 
     * @param jarURL The url of the jar to add to the installer. We use a URL so the jar may be
     * nested within another.
     */
    public abstract void addJarContent(URL jarURL);

    /**
     * Adds a jar file content to the installer. Package structure is maintained. Need mechanism to
     * copy over signed entry information. If the given file list is null the hole contents of the
     * jar file will be copied else only the listed.
     * 
     * @param jarURL The url of the jar to add to the installer. We use a URL so the jar may be
     * nested within another.
     * @param files to be copied
     */
    public abstract void addJarContent(URL jarURL, List files);

    /**
     * Marks a native library to be added to the uninstaller.
     * 
     * @param data the describing custom action data object
     */
    public abstract void addNativeUninstallerLibrary(CustomData data);

    /**
     * Returns the current pack compressor
     * @return Returns the current pack compressor.
     */
    public abstract PackCompressor getCompressor();     
    
    /**
     * Initializes a pack compressor if supported by the packager
     * @param compr_format
     * @param compr_level
     */
    public abstract void initPackCompressor(String compr_format, int compr_level) throws CompilerException;
    
    /**
     * Adds configuration information to the packager.
     * @param data - the xml-element packaging from the install.xml
     */
    public abstract void addConfigurationInformation(XMLElement data);
}
