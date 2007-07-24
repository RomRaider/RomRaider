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

import java.awt.Color;
import java.io.Serializable;

import com.izforge.izpack.GUIPrefs;
import com.izforge.izpack.Panel;

/**
 * Encloses information about the install process. This class is implemented as a singleton which
 * can be easily accessed by different components of the installer. However, this implementation is
 * not thread safe.
 * 
 * @author Julien Ponge <julien@izforge.com>
 * @author Johannes Lehtinen <johannes.lehtinen@iki.fi>
 */
public class InstallData extends AutomatedInstallData implements Serializable
{

    private static final long serialVersionUID = 4048793450990024505L;

    /** The GUI preferences. */
    public GUIPrefs guiPrefs;
    
    /** Contains at IzPanel constructor call the related Panel object. This is a hack
     *  to allow usage of the meta data stored in the Panel object during construction of
     *  the IzPanel. Do not use this member at an other place.
     */
    public Panel currentPanel;

    /** The buttons highlighting color. */
    public Color buttonsHColor = new Color(230, 230, 230);

    /** Constructs a new instance of this class. */
    protected InstallData()
    {
        super();
    }
}
