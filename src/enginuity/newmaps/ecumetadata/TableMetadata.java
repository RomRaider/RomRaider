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

public abstract class TableMetadata implements Nameable, Serializable {
       
    protected String name;
    protected Scale scale;
    protected int address;
    protected int userLevel;
    protected String description;
    private boolean isAbstract;
    
    public TableMetadata() { }

    public TableMetadata(String name) {
        this.name = name;
    }
    
    public final void setName(String name) {
        this.name = name;
    }
    
    public abstract int getSize();
    
    public final String getName() {
        return name;
    }
    
    public final void setScale(Scale scale) {
        this.scale = scale;
    }
        
    public final Scale getScale() {
        return scale;
    }
    
    public final void setAddress(int address) {
        this.address = address;
    }
    
    public final int getAddress() {
        return address;
    }
        
    public final void setUserLevel(int level) {
        this.userLevel = level;        
        if (userLevel < 0) userLevel = 0;
        else if (userLevel > 5) userLevel = 5;
    }
    
    public final int getUserLevel() {
        return userLevel;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isIsAbstract() {
        return isAbstract;
    }

    public void setIsAbstract(boolean isAbstract) {
        this.isAbstract = isAbstract;
    }
    
    public String toString() {
        String scaleName = "";
        
        try {
            scaleName = scale.getName();
        } catch (NullPointerException ex) {
            scaleName = "Not found";
        }
        
        String output = "      --- Table: " + name + " ---" +
                "\n      - Description: " + description +
                "\n      - Scale: " + scaleName +
                "\n      - Address: " + address +
                "\n      - Userlevel: " + userLevel;
                
        return output;
    }
    
}