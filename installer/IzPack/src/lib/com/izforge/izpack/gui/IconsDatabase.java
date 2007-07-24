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

package com.izforge.izpack.gui;

import java.util.TreeMap;

import javax.swing.ImageIcon;

/**
 * The icons database class.
 * 
 * @author Julien Ponge October 27, 2002
 */
public class IconsDatabase extends TreeMap
{

    private static final long serialVersionUID = 3257567287145083446L;

    /** The constructor. */
    public IconsDatabase()
    {
        super();
    }

    /**
     * Convenience method to retrieve an element.
     * 
     * @param key The icon key.
     * @return The icon as an ImageIcon object.
     */
    public ImageIcon getImageIcon(String key)
    {
        return (ImageIcon) get(key);
    }
}
