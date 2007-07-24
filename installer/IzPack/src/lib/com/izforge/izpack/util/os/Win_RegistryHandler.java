/*
 * $Id: Win_RegistryHandler.java 1816 2007-04-23 19:57:27Z jponge $
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

package com.izforge.izpack.util.os;

import java.util.List;

import com.coi.tools.os.izpack.Registry;
import com.coi.tools.os.win.NativeLibException;
import com.coi.tools.os.win.RegDataContainer;

/**
 * This is the Microsoft Windows specific implementation of <code>RegistryHandler</code>.
 * 
 * @author bartzkau
 * 
 */
public class Win_RegistryHandler extends RegistryHandler
{

    Registry regWorker = null;

    /**
     * Default constructor.
     */
    public Win_RegistryHandler()
    {
        super("com.coi.tools.os.izpack.Registry");
        if (good()) regWorker = (Registry) worker;
    }

    /**
     * Sets the given contents to the given registry value. If a sub key or the registry value does
     * not exist, it will be created. The return value is a String array which contains the names of
     * the keys and values which are created. REG_SZ is used as registry value type.
     * 
     * @param key the registry key which should be used or created
     * @param value the registry value into which the contents should be set
     * @param contents the contents for the value
     * @throws NativeLibException
     * @throws NativeLibException
     */
    public void setValue(String key, String value, String contents) throws NativeLibException
    {
        if (!good()) return;
        regWorker.setValue(key, value, contents);
    }

    /**
     * Sets the given contents to the given registry value. If a sub key or the registry value does
     * not exist, it will be created. The return value is a String array which contains the names of
     * the keys and values which are created. REG_MULTI_SZ is used as registry value type.
     * 
     * @param key the registry key which should be used or created
     * @param value the registry value into which the contents should be set
     * @param contents the contents for the value
     * @throws NativeLibException
     */
    public void setValue(String key, String value, String[] contents) throws NativeLibException
    {
        if (!good()) return;
        regWorker.setValue(key, value, contents);
    }

    /**
     * Sets the given contents to the given registry value. If a sub key or the registry value does
     * not exist, it will be created. The return value is a String array which contains the names of
     * the keys and values which are created. REG_BINARY is used as registry value type.
     * 
     * @param key the registry key which should be used or created
     * @param value the registry value into which the contents should be set
     * @param contents the contents for the value
     * @throws NativeLibException
     */
    public void setValue(String key, String value, byte[] contents) throws NativeLibException
    {
        if (!good()) return;
        regWorker.setValue(key, value, contents);
    }

    /**
     * Sets the given contents to the given registry value. If a sub key or the registry value does
     * not exist, it will be created. The return value is a String array which contains the names of
     * the keys and values which are created. REG_DWORD is used as registry value type.
     * 
     * @param key the registry key which should be used or created
     * @param value the registry value into which the contents should be set
     * @param contents the contents for the value
     * @throws NativeLibException
     */
    public void setValue(String key, String value, long contents) throws NativeLibException
    {
        if (!good()) return;
        regWorker.setValue(key, value, contents);
    }

    /**
     * Returns the contents of the key/value pair if value exist, else the given default value.
     * 
     * @param key the registry key which should be used
     * @param value the registry value from which the contents should be requested
     * @param defaultVal value to be used if no value exist in the registry
     * @return requested value if exist, else the default value
     * @throws NativeLibException
     */
    public RegDataContainer getValue(String key, String value, RegDataContainer defaultVal) throws NativeLibException
    {
        if (!good()) return (null);
        if (valueExist(key, value)) return (getValue(key, value));
        return (defaultVal);
    }

    /**
     * Returns whether a key exist or not.
     * 
     * @param key key to be evaluated
     * @return whether a key exist or not
     * @throws NativeLibException
     */
    public boolean keyExist(String key) throws NativeLibException
    {
        if (!good()) return (false);
        return (regWorker.keyExist(key));
    }

    /**
     * Returns whether a the given value under the given key exist or not.
     * 
     * @param key key to be used as path for the value
     * @param value value name to be evaluated
     * @return whether a the given value under the given key exist or not
     * @throws NativeLibException
     */
    public boolean valueExist(String key, String value) throws NativeLibException
    {
        if (!good()) return (false);
        return (regWorker.valueExist(key, value));
    }

    /**
     * Returns all keys which are defined under the given key.
     * 
     * @param key key to be used as path for the sub keys
     * @return all keys which are defined under the given key
     * @throws NativeLibException
     */
    public String[] getSubkeys(String key) throws NativeLibException
    {
        if (!good()) return (null);
        return (regWorker.getSubkeys(key));
    }

    /**
     * Returns all value names which are defined under the given key.
     * 
     * @param key key to be used as path for the value names
     * @return all value names which are defined under the given key
     * @throws NativeLibException
     */
    public String[] getValueNames(String key) throws NativeLibException
    {
        if (!good()) return (null);
        return (regWorker.getValueNames(key));
    }

    /**
     * Returns the contents of the key/value pair if value exist, else an exception is raised.
     * 
     * @param key the registry key which should be used
     * @param value the registry value from which the contents should be requested
     * @return requested value if exist, else an exception
     * @throws NativeLibException
     */
    public RegDataContainer getValue(String key, String value) throws NativeLibException
    {
        if (!good()) return (null);
        return (regWorker.getValue(key, value));
    }

    /**
     * Creates the given key in the registry.
     * 
     * @param key key to be created
     * @throws NativeLibException
     */
    public void createKey(String key) throws NativeLibException
    {
        if (!good()) return;
        regWorker.createKey(key);
    }

    /**
     * Deletes the given key if exist, else throws an exception.
     * @param key key to be deleted
     * @throws NativeLibException
     */
    public void deleteKey( String key) throws NativeLibException
    {
        if (!good()) return;
        regWorker.deleteKey(key);
    }
    
    /**
     * Deletes a key under the current root if it is empty, else do nothing.
     * 
     * @param key key to be deleted
     * @throws NativeLibException
     */
    public void deleteKeyIfEmpty(String key) throws NativeLibException
    {
        if (!good()) return;
        regWorker.deleteKeyIfEmpty(key);
    }
    
    /**
     * Deletes a value.
     * 
     * @param key key of the value which should be deleted
     * @param value value name to be deleted
     * @throws NativeLibException
     */
    public void deleteValue(String key, String value) throws NativeLibException
    {
        if (!good()) return;
        regWorker.deleteValue(key, value);
    }

    /**
     * Sets the root for the next registry access.
     * 
     * @param i an integer which refers to a HKEY
     * @throws NativeLibException
     */
    public void setRoot(int i) throws NativeLibException
    {
        if (!good()) return;
        regWorker.setRoot(i);
    }

    /**
     * Return the root as integer (HKEY_xxx).
     * 
     * @return the root as integer
     * @throws NativeLibException
     */
    public int getRoot() throws NativeLibException
    {
        if (!good()) return (0);
        return (regWorker.getRoot());
    }

    /**
     * Activates logging of registry changes.
     * 
     * @throws NativeLibException
     */
    public void activateLogging() throws NativeLibException
    {
        if (!good()) return;
        regWorker.activateLogging();
    }

    /**
     * Suspends logging of registry changes.
     * 
     * @throws NativeLibException
     */
    public void suspendLogging() throws NativeLibException
    {
        if (!good()) return;
        regWorker.suspendLogging();
    }

    /**
     * Resets logging of registry changes.
     * 
     * @throws NativeLibException
     */
    public void resetLogging() throws NativeLibException
    {
        if (!good()) return;
        regWorker.resetLogging();
    }

    public List getLoggingInfo() throws NativeLibException
    {
        if (!good()) return (null);
        return (regWorker.getLoggingInfo());
    }

    public void setLoggingInfo(List info) throws NativeLibException
    {
        if (!good()) return;
        regWorker.setLoggingInfo(info);
    }

    public void addLoggingInfo(List info) throws NativeLibException
    {
        if (!good()) return;
        regWorker.addLoggingInfo(info);
    }

    public void rewind() throws NativeLibException
    {
        if (!good()) return;
        regWorker.rewind();
    }

}
