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

package com.izforge.izpack;

import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;

/**
 * This class holds the GUI preferences for an installer.
 * 
 * @author Julien Ponge
 */
public class GUIPrefs implements Serializable
{

    static final long serialVersionUID = -9081878949718963824L;

    /** Specifies wether the window will be resizable. */
    public boolean resizable;

    /** Specifies the starting window width, in pixels. */
    public int width;

    /** Specifies the starting window height, in pixels. */
    public int height;

    /** Specifies the OS Look and Feels mappings. */
    public Map lookAndFeelMapping = new TreeMap();

    /** Specifies the OS Look and Feels optionnal parameters. */
    public Map lookAndFeelParams = new TreeMap();

    /** Specifies the modifier. */
    public Map modifier = new TreeMap();
}
