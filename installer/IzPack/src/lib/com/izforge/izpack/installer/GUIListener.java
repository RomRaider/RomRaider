/*
 * $Id:$
 * IzPack - Copyright 2001-2007 Julien Ponge, All Rights Reserved.
 * 
 * http://izpack.org/ http://developer.berlios.de/projects/izpack/
 * 
 * Copyright 2006 Klaus Bartz
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
package com.izforge.izpack.installer;


/**
 * Interface for a gui listener. This interface can be used to modify the installer frame of IzPack.
 * 
 * @author Klaus Bartz
 * 
 */
public interface GUIListener
{

    /**
     * Constant to indicate that method buildGUI has called
     */
    static final int GUI_BUILDED = 0;

    /**
     * Constant to indicate that method blockGUI has called
     */
    static final int GUI_BLOCKED = 1;

    /**
     * Constant to indicate that method releaseGUI has called
     */
    static final int GUI_RELEASED = 2;

    /**
     * Constant to indicate that method switchPanel has called
     */
    static final int PANEL_SWITCHED = 3;

    /**
     * This method will be called from the installer frame at end of the methods buildGUI, blockGUI,
     * releaseGUI and switchPanel.<br>
     * 
     * The param what indicates from what method this listener was called.<br>
     * If buildGUI is the calling method, the navigation panel will be set as param. At other
     * calling methods param will be null.
     * 
     * @param what identifier for the calling method
     * @param param
     */
    void guiActionPerformed(int what, Object param);

}
