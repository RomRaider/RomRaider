/*
 * IzPack - Copyright 2001-2007 Julien Ponge, All Rights Reserved.
 * 
 * http://izpack.org/
 * http://developer.berlios.de/projects/izpack/
 *
 * Copyright 2003 Jonathan Halliday
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

import net.n3.nanoxml.XMLElement;

/**
 * Defines the Interface that must be implemented for running Panels in automated (or "silent",
 * "headless") install mode.
 * 
 * Implementing classes MUST NOT link against awt/swing classes. Thus the Panels cannot implement
 * this interface directly, they should use e.g. helper classes instead.
 * 
 * @see AutomatedInstaller
 * @author Jonathan Halliday
 * @author Julien Ponge
 */
public interface PanelAutomation
{

    /**
     * Asks the panel to set its own XML data that can be brought back for an automated installation
     * process. Use it as a blackbox if your panel needs to do something even in automated mode.
     * 
     * @param installData The installation data
     * @param panelRoot The XML root element of the panels blackbox tree.
     */
    public void makeXMLData(AutomatedInstallData installData, XMLElement panelRoot);

    /**
     * Makes the panel work in automated mode. Default is to do nothing, but any panel doing
     * something 'effective' during the installation process should implement this method.
     * 
     * @param installData The installation data
     * @param panelRoot The XML root element of the panels blackbox tree.
     * 
     * @return true if the automated work was performed successful, false if it failed critically.
     */
    public boolean runAutomated(AutomatedInstallData installData, XMLElement panelRoot);
}
