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

import java.io.Serializable;

/**
 * Data container for Windows registry logging. This container is used to hold old and new created
 * registry data used at rewinding the registry changes.
 * 
 * @author Klaus Bartz
 * 
 */
public class RegistryLogItem implements Cloneable, Serializable
{

    /**
     * 
     */
    private static final long serialVersionUID = 3618134559108444211L;

    /** Types of log items */
    
    /** Identifier for removed key */
    public static final int REMOVED_KEY = 1;

    /** Identifier for created key */
    public static final int CREATED_KEY = 2;

    /** Identifier for removed value */
    public static final int REMOVED_VALUE = 3;

    /** Identifier for created value */
    public static final int CREATED_VALUE = 4;

    /** Identifier for changed value */
    public static final int CHANGED_VALUE = 5;

    private int type;

    private int root;

    private String key;

    private String valueName;

    private RegDataContainer newValue = null;

    private RegDataContainer oldValue = null;

    /**
     * Default constructor.
     */
    private RegistryLogItem()
    {
        super();
    }

    /**
     * Constructor with settings.
     * 
     * @param type type of loging item. Possible are REMOVED_KEY, CREATED_KEY, REMOVED_VALUE,
     * CREATED_VALUE and CHANGED_VALUE
     * @param root id for the registry root
     * @param key key name of the item which should be logged
     * @param valueName name of the value of the item which should be logged if it is a value type,
     * else null
     * @param newValue new value of the registry entry if it is a value type, else null
     * @param oldValue old value of the registry entry if it is a value type, else null
     */
    public RegistryLogItem(int type, int root, String key, String valueName,
            RegDataContainer newValue, RegDataContainer oldValue)
    {
        this.type = type;
        this.root = root;
        this.key = key;
        this.valueName = valueName;
        this.newValue = newValue;
        this.oldValue = oldValue;
    }

    /**
     * Returns the key name of this logging item.
     * 
     * @return the key name of this logging item
     */
    public String getKey()
    {
        return key;
    }

    /**
     * Returns the new value of this logging item.
     * 
     * @return the new value of this logging item
     */
    public RegDataContainer getNewValue()
    {
        return newValue;
    }

    /**
     * Returns the old value of this logging item.
     * 
     * @return the old value of this logging item
     */
    public RegDataContainer getOldValue()
    {
        return oldValue;
    }

    /**
     * Returns the root id of this logging item.
     * 
     * @return the root id of this logging item
     */
    public int getRoot()
    {
        return root;
    }

    /**
     * Returns the type id of this logging item.
     * 
     * @return the type id of this logging item
     */
    public int getType()
    {
        return type;
    }

    /**
     * Returns the value name of this logging item.
     * 
     * @return the value name of this logging item
     */
    public String getValueName()
    {
        return valueName;
    }

    /**
     * Sets the key name to the given string
     * 
     * @param string to be used as key name
     */
    public void setKey(String string)
    {
        key = string;
    }

    /**
     * Sets the new value to the given RegDataContainer.
     * 
     * @param container to be used as new value
     */
    public void setNewValue(RegDataContainer container)
    {
        newValue = container;
    }

    /**
     * Sets the old value to the given RegDataContainer.
     * 
     * @param container to be used as old value
     */
    public void setOldValue(RegDataContainer container)
    {
        oldValue = container;
    }

    /**
     * Sets the root id for this logging item.
     * 
     * @param i root id to be used for this logging item
     */
    public void setRoot(int i)
    {
        root = i;
    }

    /**
     * Sets the type id for this logging item.
     * 
     * @param i type id to be used for this logging item
     */
    public void setType(int i)
    {
        type = i;
    }

    /**
     * Sets the value name to the given string
     * 
     * @param string to be used as value name
     */
    public void setValueName(String string)
    {
        valueName = string;
    }

    public Object clone() throws CloneNotSupportedException
    {
        RegistryLogItem retval = (RegistryLogItem) super.clone();
        if (newValue != null) retval.newValue = (RegDataContainer) newValue.clone();
        if (oldValue != null) retval.oldValue = (RegDataContainer) oldValue.clone();
        return (retval);

    }
}
