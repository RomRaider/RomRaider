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

package com.izforge.izpack;

import java.io.Serializable;
import java.util.List;

/**
 * Container for serialized custom data.
 * 
 * @author Klaus Bartz
 */
public class CustomData implements Serializable
{

    static final long serialVersionUID = 5504496325961965576L;

    /** Identifier for custom data type "installer listener". */
    public static final int INSTALLER_LISTENER = 0;

    /** Identifier for custom data typ "uninstaller listener". */
    public static final int UNINSTALLER_LISTENER = 1;

    /**
     * Identifier for custom data typ "uninstaller lib". This is used for binary libs (DLLs or SHLs
     * or SOs or ...) which will be needed from the uninstaller.
     */
    public static final int UNINSTALLER_LIB = 2;

    /** Identifier for custom data typ "uninstaller jar files". */
    public static final int UNINSTALLER_JAR = 3;

    /**
     * The contens of the managed custom data. If it is a listener or a uninstaller jar, all
     * contained files are listed with it complete sub path. If it is a uninstaller native library,
     * this value is the path in the installer jar.
     */
    public List contents;

    /**
     * Full qualified name of the managed listener. If type is not a listener, this value is
     * undefined.
     */
    public String listenerName;

    /** The target operation system of this custom action */
    public List osConstraints = null;

    /**
     * Type of this custom action data; possible are INSTALLER_LISTENER, UNINSTALLER_LISTENER,
     * UNINSTALLER_LIB and UNINSTALLER_JAR.
     */
    public int type = 0;

    /**
     * Constructs an CustomData object with the needed values. If a listener will be managed with
     * this object, the full qualified name of the listener self must be set as listener name. If a
     * listener or a jar file for uninstall will be managed, all needed files (class, properties and
     * so on) must be referenced in the contents with the path which they have in the installer jar
     * file.
     * 
     * @param listenerName path of the listener
     * @param contents also needed objects referenced with the path in install.jar
     * @param osConstraints target operation system of this custom action
     * @param type type of this custom data
     */
    public CustomData(String listenerName, List contents, List osConstraints, int type)
    {
        this.listenerName = listenerName;
        this.contents = contents;
        this.osConstraints = osConstraints;
        this.type = type;
    }

}
