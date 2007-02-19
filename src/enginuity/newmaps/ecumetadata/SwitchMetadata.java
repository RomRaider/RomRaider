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

import static enginuity.newmaps.definition.AttributeParser.stringToByteArray;

import java.io.Serializable;

public class SwitchMetadata extends TableMetadata implements Serializable {
    
    protected byte[] stateOn = new byte[1];
    protected byte[] stateOff = new byte[1];
    protected int size;
    
    private int defaultValue = SwitchGroupMetadata.DEFAULT_NONE;
    boolean hidden = false;
    
    public SwitchMetadata(String name) {
        super(name);
    } 
    
    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }  

    public byte[] getStateOn() {
        return stateOn;
    }

    public void setStateOn(String values) {
        this.stateOn = stringToByteArray(values, " ");
    }

    public byte[] getStateOff() {
        return stateOff;
    }

    public void setStateOff(String values) {
        this.stateOff = stringToByteArray(values, " ");
    }
    
    public void setDefaultValue(int defaultValue) {
        this.defaultValue = defaultValue;
    }
    
    public int getDefaultValue() {
        return defaultValue;
    }
    
    public void setDefaultValue(String value) {
        if (value.equalsIgnoreCase("on")) defaultValue = SwitchGroupMetadata.DEFAULT_ON;
        else if (value.equalsIgnoreCase("off")) defaultValue = SwitchGroupMetadata.DEFAULT_OFF;
        else defaultValue = SwitchGroupMetadata.DEFAULT_NONE;       
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }    
    
    public String toString() { 
        String scaleName = "";
        
        try {
            scaleName = scale.getName();
        } catch (NullPointerException ex) {
            scaleName = "Not found";
        }
        
        String output = "      --- Switch: " + name + " ---" +
                "\n      - Description: " + description +
                "\n      - Address: " + address +
                "\n      - Userlevel: " + userLevel;
                
        
        return output;
                       
    }    
}
