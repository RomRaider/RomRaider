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

public class Unit implements Nameable, Serializable {
    
    public static final int SYSTEM_STANDARD = 0;
    public static final int SYSTEM_METRIC = 1;
    public static final int SYSTEM_UNIVERSAL = 2;
    
    private String name;
    private String to_real;
    private String to_byte;
    private String format;
    private int system;
    private float coarseIncrement;
    private float fineIncrement;
    
    private Unit() { }
    
    public Unit(String name) {
        this.setName(name);
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getTo_real() {
        return to_real;
    }
    
    public void setTo_real(String to_real) {
        this.to_real = to_real;
    }
    
    public String getTo_byte() {
        return to_byte;
    }
    
    public void setTo_byte(String to_byte) {
        this.to_byte = to_byte;
    }
    
    public String getFormat() {
        return format;
    }
    
    public void setFormat(String format) {
        this.format = format;
    }
    
    public float getCoarseIncrement() {
        return coarseIncrement;
    }
    
    public void setCoarseIncrement(float coarseIncrement) {
        this.coarseIncrement = coarseIncrement;
    }
    
    public float getFineIncrement() {
        return fineIncrement;
    }
    
    public void setFineIncrement(float fineIncrement) {
        this.fineIncrement = fineIncrement;
    }

    public int getSystem() {
        return system;
    }

    public void setSystem(int system) {
        this.system = system;
    }
    
}
