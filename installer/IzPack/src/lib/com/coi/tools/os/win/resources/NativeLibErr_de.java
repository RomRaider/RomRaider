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
 * German resource bundle for NativLibException.
 * 
 * @author Klaus Bartz
 * 
 */
public class NativeLibErr_de extends ListResourceBundle
{

    private static final Object[][] contents = {
            { "libInternal.OsErrNumPraefix", " Fehlernummer des Betriebssystems: "},
            { "libInternal.OsErrStringPraefix", " Fehlertext des Betriebssystems: "},
            { "system.outOfMemory", "Out of memory in einer DLL."},
            { "functionFailed.RegOpenKeyEx",
                    "Der Registry-Schl\u00fcssel {0}\\{1} konnte nicht ge\u00f6ffnet werden."},
            { "functionFailed.RegCreateKeyEx",
                    "Der Registry-Schl\u00fcssel {0}\\{1} konnte nicht angelegt werden."},
            { "functionFailed.RegDeleteKey",
                    "Der Registry-Schl\u00fcssel {0}\\{1} konnte nicht gel\u00f6scht werden."},
            {
                    "functionFailed.RegEnumKeyEx",
                    "Zu dem Registry-Schl\u00fcssel {0}\\{1} konnte der angeforderte Unterschl\u00fcssel nicht ermittelt werden."},
            { "functionFailed.RegEnumValue",
                    "Zu dem Registry-Schl\u00fcssel {0}\\{1} konnte der angeforderte Wert nicht ermittelt werden."},
            { "functionFailed.RegSetValueEx",
                    "Der Wert {2} unter dem Registry-Schl\u00fcssel {0}\\{1} konnte nicht gesetzt werden."},
            { "functionFailed.RegDeleteValue",
                    "Der Wert {2} unter dem Registry-Schl\u00fcssel {0}\\{1} konnte nicht gel\u00f6scht werden."},
            {
                    "functionFailed.RegQueryValueEx",
                    "Zu dem Wert {2} unter dem Registry-Schl\u00fcssel {0}\\{1} konnten keine Informationen ermittelt werden."},
            { "functionFailed.RegQueryInfoKey",
                    "Die angeforderten Informationen zum Registry-Key {0}\\{1} konnten nicht ermittelt werden."},
            { "registry.ValueNotFound", "Der angeforderte Registry-Wert wurde nicht gefunden."},
            { "registry.KeyNotFound",
                    "Der angeforderte Registry-Schl\u00fcssel wurde nicht gefunden."},
            { "registry.KeyExist",
                    "Der Registry-Schl\u00fcssel {0}\\{1} konnte nicht angelegt werden, weil er bereits existierte."},
            { "registry.ACLNotSupported",
                    "In dieser Version von COIOSHelper werden Berechtigungen von Registry-Schl\u00fcsseln nicht unterst\u00fctzt."}};

    /**
     * Returns the contents array.
     * 
     * @return contents array
     */
    protected Object[][] getContents()
    {
        return contents;
    }

    /**
     * Default constructor.
     */
    public NativeLibErr_de()
    {
        super();
    }

}
