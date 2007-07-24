/*
 * IzPack - Copyright 2001-2007 Julien Ponge, All Rights Reserved.
 * 
 * http://izpack.org/
 * http://developer.berlios.de/projects/izpack/
 * 
 * Copyright 2005 Klaus Bartz
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

package com.coi.tools.os.win.resources;

import java.util.ListResourceBundle;

/**
 * "Global" (English) resource bundle for NativLibException.
 * 
 * @author Klaus Bartz
 * 
 */
public class NativeLibErr extends ListResourceBundle
{

    private static final Object[][] contents = {
            { "libInternal.OsErrNumPraefix", " System error number is: "},
            { "libInternal.OsErrStringPraefix", " System error text is: "},
            { "system.outOfMemory", "Out of memory in the native part."},

            { "functionFailed.RegOpenKeyEx", "Cannot open registry key {0}\\{1}."},
            { "functionFailed.RegCreateKeyEx", "Cannot create registry key {0}\\{1}."},
            { "functionFailed.RegDeleteKey", "Cannot delete registry key {0}\\{1}."},
            { "functionFailed.RegEnumKeyEx", "Not possible to determine sub keys for key {0}\\{1}."},
            { "functionFailed.RegEnumValue", "Not possible to determine value under key {0}\\{1}."},
            { "functionFailed.RegSetValueEx",
                    "Cannot create value {2} under registry key {0}\\{1}."},
            { "functionFailed.RegDeleteValue",
                    "Cannot delete value {2} under registry key {0}\\{1}."},
            { "functionFailed.RegQueryValueEx",
                    "No informations available for value {2} of registry key {0}\\{1}."},
            { "functionFailed.RegQueryInfoKey",
                    "No informations available for registry key {0}\\{1}."},

            { "registry.ValueNotFound", "Registry value not found."},
            { "registry.KeyNotFound", "Registry key not found."},
            { "registry.KeyExist", "Cannot create registry key {0}\\{1} because key exist already."},
            { "registry.ACLNotSupported", "In this version of COIOSHelper permission of registry keys are not supported."}};

    /**
     * Default constructor.
     */
    public NativeLibErr()
    {
        super();
    }

    /**
     * Returns the contents array.
     * 
     * @return contents array
     */
    protected Object[][] getContents()
    {
        return contents;
    }

}
