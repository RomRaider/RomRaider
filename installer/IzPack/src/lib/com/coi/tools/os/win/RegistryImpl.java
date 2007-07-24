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

package com.coi.tools.os.win;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * System dependent helper for MS Windows registry handling. This class is only vaild on Windows. It
 * declares naitve methods which are implemented in COIOSHelper.dll. The native methods uses the
 * classes RegDataContainer and AccessControlList as in and output. Do not change the getter and
 * setter methods of them. Do not try to implement a get or setValueACL because it will be nonsense.
 * In the registry only keys have a ACL. not values.
 * 
 * @author Klaus Bartz
 */
public class RegistryImpl implements MSWinConstants
{

    private static final String DEFAULT_PLACEHOLDER = "__#$&DEFAULT_PLACEHODER_VALUE#$?";

    private int currentRoot = HKEY_CURRENT_USER;

    private List logging = new ArrayList();

    private boolean doLogging = false;

    /**
     * Creates a new empty RegistryImpl object.
     */
    public RegistryImpl()
    {
        super();
    }

    /**
     * Returns current root.
     * 
     * @return current root
     */
    public int getRoot()
    {
        return currentRoot;
    }

    /**
     * Sets the root id to the given value.
     * 
     * @param i root id to be set
     */
    public void setRoot(int i)
    {
        currentRoot = i;
    }

    /**
     * Returns the value of the given value name as RegDataContainer.
     * 
     * @param key key of the registry entry
     * @param value value name of the registry entry
     * @return the value of the given value name as RegDataContainer
     * @throws NativeLibException
     */
    public RegDataContainer getValue(String key, String value) throws NativeLibException
    {
        if( key == null)
            key = "";
        return (getValue(currentRoot, key, value));
    }

    /**
     * Returns the value of the given value name as Object. The real type depends to the type of the
     * value.
     * 
     * @param key key of the registry entry
     * @param value value name of the registry entry
     * @return the value of the given value name as RegDataContainer
     * @throws NativeLibException
     */
    public Object getValueAsObject(String key, String value) throws NativeLibException
    {
        if( key == null)
            key = "";
        return (getValue(currentRoot, key, value).getDataAsObject());
    }

    /**
     * Returns all sub keys under the given key which is under the current root.
     * 
     * @param key key for which the sub keys should be detected
     * @return all sub keys under the given key which is under the current root
     * @throws NativeLibException
     */
    public String[] getSubkeys(String key) throws NativeLibException
    {
        if( key == null)
            key = "";
        return (getSubkeyNames(currentRoot, key));
    }

    /**
     * Returns all value names under the given key which is under the current root.
     * 
     * @param key key for which the values should be detected
     * @return all value names under the given key which is under the current root
     * @throws NativeLibException
     */
    public String[] getValueNames(String key) throws NativeLibException
    {
        if( key == null)
            key = "";
        return (getValueNames(currentRoot, key));
    }

    /**
     * Creates the given key under the current root.
     * 
     * @param key key to be created
     * @throws NativeLibException
     */
    public void createKey(String key) throws NativeLibException
    {
        createKey(currentRoot, key);
    }

    /**
     * Creates the given key under the given root. 
     * It is possible to declare keys without a sub path.
     * This is only possible on roots which are no real roots
     * (e.g. HKEY_CURRENT_USER which is a link to
     * HKEY_USERS\GUID of current user). Therefore this method
     * will be raise an exception if root is a real root and
     * key contains no sub path.
     * 
     * @param root to be used
     * @param key key to be created
     * @throws NativeLibException
     */
    public void createKey(int root, String key) throws NativeLibException
    {
        int pathEnd = key.lastIndexOf('\\');
        if( pathEnd > 0 )
        {
            String subkey = key.substring(0, pathEnd);
            if (!exist(root, subkey))
            { // Create missing sub keys
                createKey(root, subkey);
            }
        }
        // Create key
        createKeyN(root, key);
        RegistryLogItem rli = new RegistryLogItem(RegistryLogItem.CREATED_KEY, root, key, null,
                null, null);
        log(rli);

    }

    /**
     * Returns whether the given key under the current root exist or not.
     * 
     * @param key key to be tested
     * @return true if thekey exist, else false
     * @throws NativeLibException
     */
    public boolean keyExist(String key) throws NativeLibException
    {
        return (keyExist(currentRoot, key));
    }

    /**
     * Returns whether the given key under the given root exist or not.
     * 
     * @param root to be used
     * @param key key to be tested
     * @return true if thekey exist, else false
     * @throws NativeLibException
     */
    public boolean keyExist(int root, String key) throws NativeLibException
    {
        try
        {
            return (exist(root, key));
        }
        catch (NativeLibException ne)
        {
            String em = ne.getLibMessage();
            if ("functionFailed.RegOpenKeyEx".equals(em)) { return (false); }
            throw (ne);
        }
    }

    /**
     * Returns whether the given value exist under the current root or not.
     * 
     * @param key key of the value for which should be tested
     * @param value value to be tested
     * @return true if the value exist, else false
     * @throws NativeLibException
     */
    public boolean valueExist(String key, String value) throws NativeLibException
    {
        if( key == null)
            key = "";
        try
        {
            this.getValue(currentRoot, key, value);
        }
        catch (NativeLibException ne)
        {
            String em = ne.getLibMessage();
            if ("functionFailed.RegOpenKeyEx".equals(em)
                    || "functionFailed.RegQueryValueEx".equals(em)) { return (false); }
            throw (ne);
        }
        return (true);
    }

    /**
     * Sets the given contents to the given registry value. If a sub key or the registry value does
     * not exist, it will be created. REG_SZ is used as registry value type.
     * 
     * @param key the registry key which should be used or created
     * @param value the registry value into which the contents should be set
     * @param contents the contents for the value
     * @throws NativeLibException
     */
    public void setValue(String key, String value, String contents) throws NativeLibException
    {

        setValue(currentRoot, key, value, new RegDataContainer(contents));
    }

    /**
     * Sets the given contents to the given registry value. If a sub key or the registry value does
     * not exist, it will be created. REG_MULTI_SZ is used as registry value type.
     * 
     * @param key the registry key which should be used or created
     * @param value the registry value into which the contents should be set
     * @param contents the contents for the value
     * @throws NativeLibException
     */
    public void setValue(String key, String value, String[] contents) throws NativeLibException
    {

        setValue(currentRoot, key, value, new RegDataContainer(contents));
    }

    /**
     * Sets the given contents to the given registry value. If a sub key or the registry value does
     * not exist, it will be created. REG_BINARY is used as registry value type.
     * 
     * @param key the registry key which should be used or created
     * @param value the registry value into which the contents should be set
     * @param contents the contents for the value
     * @throws NativeLibException
     */
    public void setValue(String key, String value, byte[] contents) throws NativeLibException
    {

        setValue(currentRoot, key, value, new RegDataContainer(contents));
    }

    /**
     * Sets the given contents to the given registry value. If a sub key or the registry value does
     * not exist, it will be created. REG_DWORD is used as registry value type.
     * 
     * @param key the registry key which should be used or created
     * @param value the registry value into which the contents should be set
     * @param contents the contents for the value
     * @throws NativeLibException
     */
    public void setValue(String key, String value, long contents) throws NativeLibException
    {
        setValue(currentRoot, key, value, new RegDataContainer(contents));
    }

    /**
     * Sets the given contents to the given registry value under current root. If a sub key or the
     * registry value does not exist, it will be created. The used registry value type will be
     * determined by the type of the RegDataContainer.
     * 
     * @param key the registry key which should be used or created
     * @param value the registry value into which the contents should be set
     * @param contents the contents for the value
     * @throws NativeLibException
     */
    public void setValue(String key, String value, RegDataContainer contents)
            throws NativeLibException
    {
        setValue(currentRoot, key, value, contents);
    }

    /**
     * Sets the given contents to the given registry value. If a sub key or the registry value does
     * not exist, it will be created. The used registry value type will be determined by the type of
     * the RegDataContainer.
     * 
     * @param root id for the root of the key
     * @param key the registry key which should be used or created
     * @param value the registry value into which the contents should be set
     * @param contents the contents for the value
     * @throws NativeLibException
     */
    public void setValue(int root, String key, String value, RegDataContainer contents)
            throws NativeLibException
    {
        RegDataContainer oldContents = null;
        String localValue = value;
        if( key == null)
            key = "";
        if(value == null)   // We allow it for the default value...
            value = "";
        // May be someone do not like backslashes ...
        key = key.replace('/', '\\');

        synchronized (logging)
        {
            try
            {
                oldContents = getValue(currentRoot, key, value);
            }
            catch (NativeLibException ne)
            {
                String em = ne.getLibMessage();
                if ("functionFailed.RegOpenKeyEx".equals(em)
                        || "functionFailed.RegQueryValueEx".equals(em))
                {
                    setValueR(root, key, value, contents);
                    return;
                }
                throw (ne);
            }
            setValueN(root, key, value, contents);
            // Add value changing to log list
            if (value.length() == 0) // The default value ...
                localValue = DEFAULT_PLACEHOLDER; // Rewind will fail if last
            // token is
            // empty.

            RegistryLogItem rli = new RegistryLogItem(RegistryLogItem.CHANGED_VALUE, root, key,
                    localValue, contents, oldContents);
            log(rli);
        }
    }

    /**
     * Deletes a key under the current root if exist and it is empty, else throw an exception.
     * 
     * @param key key to be deleted
     * @throws NativeLibException
     */
    public void deleteKey(String key) throws NativeLibException
    {
        deleteKeyL(currentRoot, key);
    }

    /**
     * Deletes a key under the current root if it is empty, else do nothing.
     * 
     * @param key key to be deleted
     * @throws NativeLibException
     */
    public void deleteKeyIfEmpty(String key) throws NativeLibException
    {
        deleteKeyIfEmpty(currentRoot, key);
    }

    /**
     * Deletes a key if it is empty, else do nothing.
     * 
     * @param root id for the root of the key
     * @param key key to be deleted
     * @throws NativeLibException
     */
    public void deleteKeyIfEmpty(int root, String key) throws NativeLibException
    {
        if (keyExist(root, key) && isKeyEmpty(root, key)) deleteKeyL(root, key);

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
        deleteValueL(currentRoot, key, value);
    }

    /**
     * Deletes a key with logging.
     * 
     * @param root id for the root of the key
     * @param key key to be deleted
     * @throws NativeLibException
     */
    private void deleteKeyL(int root, String key) throws NativeLibException
    {
        RegistryLogItem rli = new RegistryLogItem(RegistryLogItem.REMOVED_KEY, root, key, null,
                null, null);
        log(rli);
        deleteKeyN(root, key);
    }

    /**
     * Deletes a value with logging.
     * 
     * @param root id for the root of the key
     * @param key key of the value which should be deleted
     * @param value value name to be deleted
     * @throws NativeLibException
     */
    private void deleteValueL(int root, String key, String value) throws NativeLibException
    {
        if( key == null)
            key = "";
        RegDataContainer oldContents = getValue(currentRoot, key, value);
        RegistryLogItem rli = new RegistryLogItem(RegistryLogItem.REMOVED_VALUE, root, key, value,
                null, oldContents);
        log(rli);
        deleteValueN(currentRoot, key, value);
    }

    /**
     * Rewinds all logged actions.
     * 
     * @throws IllegalArgumentException
     * @throws NativeLibException
     */
    public void rewind() throws IllegalArgumentException, NativeLibException
    {
        synchronized (logging)
        {
            Iterator iter = logging.iterator();
            suspendLogging();

            while (iter.hasNext())
            {
                RegistryLogItem rli = (RegistryLogItem) iter.next();
                String rliValueName = (DEFAULT_PLACEHOLDER.equals(rli.getValueName())) ? "" : rli
                        .getValueName();
                switch (rli.getType())
                {
                case RegistryLogItem.CREATED_KEY:
                    deleteKeyIfEmpty(rli.getRoot(), rli.getKey());
                    break;
                case RegistryLogItem.REMOVED_KEY:
                    createKeyN(rli.getRoot(), rli.getKey());
                    break;
                case RegistryLogItem.CREATED_VALUE:
                    RegDataContainer currentContents = null;
                    // Delete value only if reg entry exists and is equal to the stored value.
                    try
                    {
                        currentContents = getValue(rli.getRoot(), rli.getKey(), rliValueName);
                    }
                    catch (NativeLibException nle)
                    {
                        break;
                    }
                    if (currentContents.equals(rli.getNewValue()))
                    {
                        deleteValueN(rli.getRoot(), rli.getKey(), rliValueName);
                    }
                    // TODO: what todo if value has changed?
                    break;
                case RegistryLogItem.REMOVED_VALUE:
                    // Set old value only if reg entry not exists.
                    try
                    {
                        getValue(rli.getRoot(), rli.getKey(), rliValueName);
                    }
                    catch (NativeLibException nle)
                    {
                        setValueN(rli.getRoot(), rli.getKey(), rliValueName, rli
                                .getOldValue());
                    }
                    break;
                case RegistryLogItem.CHANGED_VALUE:
                    // Change to old value only if reg entry exists and equal to
                    // the
                    // stored value.
                    try
                    {
                        currentContents = getValue(rli.getRoot(), rli.getKey(), rliValueName);
                    }
                    catch (NativeLibException nle)
                    {
                        break;
                    }
                    if (currentContents.equals(rli.getNewValue()))
                    {
                        setValueN(rli.getRoot(), rli.getKey(), rliValueName, rli
                                .getOldValue());
                    }
                    break;
                }
            }
        }

    }

    /**
     * Sets the given contents to the given registry value. If a sub key or the registry value does
     * not exist, it will be created. The used registry value type will be determined by the type of
     * the RegDataContainer.
     * 
     * @param root id for the root of the key
     * @param key the registry key which should be used or created
     * @param value the registry value into which the contents should be set
     * @param contents the contents for the value
     * @throws NativeLibException
     */
    private void setValueR(int root, String key, String value, RegDataContainer contents)
            throws NativeLibException
    {
        String localValue = value;
        if (!exist(root, key))
        { // Create missing sub keys
            createKey(root, key);
        }
        // Create value
        setValueN(root, key, value, contents);
        // Add value creation to log list
        if (value.length() == 0) // The default value ...
            localValue = DEFAULT_PLACEHOLDER; // Rewind will fail if last token
        // is
        // empty.
        StringBuffer sb = new StringBuffer();
        sb.append("SetValue;").append(Integer.toString(root)).append(";").append(key).append(";")
                .append(localValue);
        RegistryLogItem rli = new RegistryLogItem(RegistryLogItem.CREATED_VALUE, root, key,
                localValue, contents, null);
        log(rli);
    }

    private native boolean exist(int root, String key) throws NativeLibException;

    private native void createKeyN(int root, String key) throws NativeLibException;

    private native void setValueN(int root, String key, String value, RegDataContainer contents)
            throws NativeLibException;

    private native RegDataContainer getValue(int root, String key, String value)
            throws NativeLibException;

    private native void deleteValueN(int root, String key, String value) throws NativeLibException;

    private native void deleteKeyN(int root, String key) throws NativeLibException;

    private native boolean isKeyEmpty(int root, String key) throws NativeLibException;

    private native String[] getSubkeyNames(int root, String key) throws NativeLibException;

    private native String[] getValueNames(int root, String key) throws NativeLibException;

    // Methods which are implemented in the native part but not used yet. To suppress warnings
    // in Eclipse this methods are commented out. Comment in if needed.
    // private native int getValueType(int root, String key, String value) throws
    // NativeLibException;
    //
    // private native int getSubkeyCount(int root, String key) throws NativeLibException;
    //
    // private native int getValueCount(int root, String key) throws NativeLibException;
    //
    // private native String getSubkeyName(int root, String key, int index) throws
    // NativeLibException;
    //
    // private native String getValueName(int root, String key, int index) throws
    // NativeLibException;
    //
    // private native void modifyKeyACL(int root, String key, AccessControlList acl)
    // throws NativeLibException;
    //
    // private native AccessControlList getKeyACL(int root, String key) throws NativeLibException;

    /**
     * Creates a new (empty) logging list and activates logging.
     */
    public void resetLogging()
    {
        logging = new ArrayList();
        activateLogging();
    }

    /**
     * Suspends logging.
     */
    public void suspendLogging()
    {
        doLogging = false;
    }

    /**
     * Activates logging.
     */
    public void activateLogging()
    {
        doLogging = true;
    }

    /**
     * Returns a copy of the colected logging informations.
     * 
     * @return a copy of the colected logging informations
     */
    public List getLoggingInfo()
    {
        ArrayList retval = new ArrayList(logging.size());
        Iterator iter = logging.iterator();
        while (iter.hasNext())
            try
            {
                retval.add(((RegistryLogItem) iter.next()).clone());
            }
            catch (CloneNotSupportedException e)
            { // Should never be...
                e.printStackTrace();
            }
        return (retval);
    }

    /**
     * Copies the contents of the given list of RegistryLogItems to a newly created internal logging
     * list.
     * 
     * @param info list containing RegistryLogItems to be used for logging
     */
    public void setLoggingInfo(List info)
    {
        resetLogging();
        addLoggingInfo(info);
    }

    /**
     * Adds copies of the contents of the given list of RegistryLogItems to the existent internal
     * 
     * @param info list containing RegistryLogItems to be used for logging logging list.
     */
    public void addLoggingInfo(List info)
    {
        Iterator iter = info.iterator();
        while (iter.hasNext())
            try
            {
                logging.add(((RegistryLogItem) iter.next()).clone());
            }
            catch (CloneNotSupportedException e)
            { // Should never be
                e.printStackTrace();
            }

    }

    /**
     * Adds the given item to the beginning of the logging list if logging is enabled, else do
     * nothing.
     * 
     * @param item
     */
    private void log(RegistryLogItem item)
    {
        if (doLogging && logging != null) logging.add(0, item);
    }

}
