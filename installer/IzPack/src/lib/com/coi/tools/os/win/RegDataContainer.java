/*
 * IzPack - Copyright 2001-2007 Julien Ponge, All Rights Reserved.
 * 
 * http://izpack.org/
 * http://developer.berlios.de/projects/izpack/
 * 
 * Copyright 2005-2006 Klaus Bartz
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
 * <p>
 * Data container for Windows registry values. Windows registry values can contain different data
 * types. It is not possible to map they all to one Java type. Therefore this class contains the
 * different container types. DO NOT CHANGE METHODE SIGNATURES etc. without addapt the native method
 * RegistryImpl.setValueN and RegistryImpl.getValue.
 * </p>
 * 
 * @author Klaus Bartz
 * 
 */
public class RegDataContainer implements Cloneable, Serializable, MSWinConstants
{

    private static final long serialVersionUID = 3979265850388066865L;

    private static final int[] VALID_TYPES = { 0, 1, 2, 3, 4, 6, 7};

    private long dwordData = 0;

    private String stringData = null;

    private String[] multiStringData = null;

    private byte[] binData = null;

    private int type = 0;

    /**
     * Default constructor.
     */
    public RegDataContainer()
    {
        super();
    }

    /**
     * Creates a RegDataContainer for a special type The data self is not set. Valid types are
     * 
     * @param type
     * @throws IllegalArgumentException if the type is not valid
     */
    public RegDataContainer(int type) throws IllegalArgumentException
    {
        super();
        if (!isValidType(type)) throw new IllegalArgumentException("Type is not valid");

        this.type = type;
    }

    /**
     * Creates a RegDataContainer for type REG_DWORD with the given data
     * 
     * @param data data which should be used with this object
     */
    public RegDataContainer(long data)
    {
        super();
        type = REG_DWORD;
        dwordData = data;
    }

    /**
     * Creates a RegDataContainer for type REG_SZ with the given data
     * 
     * @param data data which should be used with this object
     */
    public RegDataContainer(String data)
    {
        super();
        type = REG_SZ;
        stringData = data;
    }

    /**
     * Creates a RegDataContainer for type REG_MULTI_SZ with the given data
     * 
     * @param data data which should be used with this object
     */
    public RegDataContainer(String[] data)
    {
        super();
        type = REG_MULTI_SZ;
        multiStringData = data;
    }

    /**
     * Creates a RegDataContainer for type REG_BINARY with the given data
     * 
     * @param data data which should be used with this object
     */
    public RegDataContainer(byte[] data)
    {
        super();
        type = REG_BINARY;
        binData = data;
    }

    /**
     * Returns the binary data of this container. It will be contain only data, if the type of this
     * object is REG_BINARY.
     * 
     * @return binary data
     */
    public byte[] getBinData()
    {
        return binData;
    }

    /**
     * Returns the dword data of this container. It will be contain only data, if the type of this
     * object is REG_DWORD.
     * 
     * @return the dword data
     */
    public long getDwordData()
    {
        return dwordData;
    }

    /**
     * Returns the multi string data as string array of this container. It will be contain only
     * data, if the type of this object is REG_REG_MULTI_SZ.
     * 
     * @return the multi string data
     */
    public String[] getMultiStringData()
    {
        return multiStringData;
    }

    /**
     * Returns the string data of this container. It will be contain only data, if the type of this
     * object is REG_REG_SZ.
     * 
     * @return the string data
     */
    public String getStringData()
    {
        return stringData;
    }

    /**
     * Returns the data type handled by this object.
     * 
     * @return the data type handled by this object
     */
    public int getType()
    {
        return type;
    }

    /**
     * Sets the binary data to the given byte array.
     * 
     * @param bytes data to be set
     */
    public void setBinData(byte[] bytes)
    {
        binData = bytes;
    }

    /**
     * Sets the dword data to the given value.
     * 
     * @param i data to be set
     */
    public void setDwordData(long i)
    {
        dwordData = i;
    }

    /**
     * Sets the multi string data to the given string array.
     * 
     * @param strings data to be set
     */
    public void setMultiStringData(String[] strings)
    {
        multiStringData = strings;
    }

    /**
     * Sets the string data to the given value.
     * 
     * @param string data to be set
     */
    public void setStringData(String string)
    {
        stringData = string;
    }

    /**
     * Sets the type.
     * 
     * @param i type to be set
     */
    public void setType(int i)
    {
        type = i;
    }

    /**
     * Verifies whether the given int represents a valid type or not.
     * 
     * @param type0 value to be verified
     * @return whether the given int represents a valid type or not
     */
    public boolean isValidType(int type0)
    {
        for (int i = 0; i < VALID_TYPES.length; ++i)
            if (type0 == VALID_TYPES[i]) return (true);
        return (false);

    }

    /**
     * Returns the contained data depending to the type. Dword data are transformed from long to
     * Long.
     * 
     * @return the contained data
     */
    public Object getDataAsObject()
    {
        switch (type)
        {
        case REG_SZ:
        case REG_EXPAND_SZ:
            return (getStringData());
        case REG_BINARY:
            return (getBinData());
        case REG_DWORD:
            return (new Long(getDwordData()));
        case REG_MULTI_SZ:
            return (getMultiStringData());
        default:
            return (null);
        }
    }

    public Object clone() throws CloneNotSupportedException
    {
        RegDataContainer retval = (RegDataContainer) super.clone();
        if (multiStringData != null)
        {
            retval.multiStringData = new String[multiStringData.length];
            System.arraycopy(multiStringData, 0, retval.multiStringData, 0, multiStringData.length);
        }
        if (binData != null)
        {
            retval.binData = new byte[binData.length];
            System.arraycopy(binData, 0, retval.binData, 0, binData.length);
        }
        return (retval);
    }

    public boolean equals(Object anObject)
    {
        if (this == anObject) return (true);
        if (anObject instanceof RegDataContainer)
        {
            RegDataContainer other = (RegDataContainer) anObject;
            if (other.type != type) return (false);
            switch (type)
            {
            case REG_DWORD:
                return (other.dwordData == dwordData);
            case REG_SZ:
            case REG_EXPAND_SZ:
                if (stringData == null) return (other.stringData == null);
                return (stringData.equals(other.stringData));
            case REG_BINARY:
                if (binData == null) return (other.binData == null);
                if (other.binData != null && binData.length == other.binData.length)
                {
                    for (int i = 0; i < binData.length; ++i)
                    {
                        if (binData[i] != other.binData[i]) return (false);
                    }
                    return (true);
                }
                return (false);
            case REG_MULTI_SZ:
                if (multiStringData == null) return (other.multiStringData == null);
                if (other.multiStringData != null
                        && multiStringData.length == other.multiStringData.length)
                {
                    for (int i = 0; i < multiStringData.length; ++i)
                    {
                        if (multiStringData[i] != null)
                        {
                            if (!multiStringData[i].equals(other.multiStringData[i]))
                                return (false);
                        }
                        else if (other.multiStringData[i] == null) return (false);
                    }
                    return (true);
                }
                return (false);
            }
        }
        return (false);
    }

    public int hashCode()
    {
        int result;
        result = (int) (dwordData ^ (dwordData >>> 32));
        result = 29 * result + (stringData != null ? stringData.hashCode() : 0);
        result = 29 * result + type;
        return result;
    }

}
