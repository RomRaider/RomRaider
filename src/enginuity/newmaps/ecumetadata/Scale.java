/*
 *
 * Enginuity Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006 Enginuity.org
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 */

package enginuity.newmaps.ecumetadata;

import enginuity.util.Nameable;

import java.io.Serializable;

public class Scale implements Nameable, Serializable {
    
    public static final int ENDIAN_BIG = 0;
    public static final int ENDIAN_LITTLE = 1;
    
    public static final int STORAGE_TYPE_INT8 = 0;
    public static final int STORAGE_TYPE_UINT8 = 1;
    public static final int STORAGE_TYPE_INT16 = 2;
    public static final int STORAGE_TYPE_UINT16 = 3;
    public static final int STORAGE_TYPE_FLOAT = 4;
    public static final int STORAGE_TYPE_HEX = 5;
    public static final int STORAGE_TYPE_CHAR = 6;;
    public static final int STORAGE_TYPE_INT32 = 7;
    public static final int STORAGE_TYPE_UINT32 = 8;
    
    protected String description;
    protected Unit[] units;    
    protected int storageType;
    protected int endian;
    protected String logParam;
    protected int selectedUnit;
    protected String name;
    
    // Disallow default constructor
    private Scale() { }
    
    public Scale(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getStorageType() {
        return storageType;
    }

    public void setStorageType(int storageType) {
        this.storageType = storageType;
    }

    public int getEndian() {
        return endian;
    }

    public void setEndian(int endian) {
        this.endian = endian;
    }

    public String getLogParam() {
        return logParam;
    }

    public void setLogParam(String logParam) {
        this.logParam = logParam;
    }
  
    public Unit getUnit() {
        return getUnits()[selectedUnit];
    }    

    public Unit[] getUnits() {
        return units;
    }

    public void setUnits(Unit[] units) {
        this.units = units;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
    
    public String toString() {
        String output = "      --- SCALE: " + name + " ---" +
                "\n      - Description: " + description +
                "\n      - Storage Type: " + storageType +
                "\n      - Endian: " + endian + 
                "\n      - Log Param: " + logParam;
        return output;
    }
    
}