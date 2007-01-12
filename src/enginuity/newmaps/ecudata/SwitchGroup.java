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

package enginuity.newmaps.ecudata;

import enginuity.util.NamedSet;
import enginuity.util.exception.NameableNotFoundException;

import java.util.Iterator;

public class SwitchGroup extends ECUData {
    
    public static final int DEFAULT_ON = 0;
    public static final int DEFAULT_OFF = 1;
    public static final int DEFAULT_NONE = 2;
    
    private int defaultValue = DEFAULT_NONE;
    private boolean hidden = false;
    
    NamedSet<Switch> switches = new NamedSet<Switch>();    
    
    public SwitchGroup(String name) {
        super(name);
    }
    
    public void setDefaultValue(int defaultVal) {
        this.defaultValue = defaultValue;
    }
    
    public int getDefaultValue() {
        return defaultValue;
    }
    
    public void setDefaultValue(String value) {
        if (value.equalsIgnoreCase("on")) defaultValue = DEFAULT_ON;
        else if (value.equalsIgnoreCase("off")) defaultValue = DEFAULT_OFF;
        else defaultValue = DEFAULT_NONE;       
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }
    
    public Switch get(String name) throws NameableNotFoundException {
        return (Switch)switches.get(name);
    }
    
    public void add(Switch input) {
        switches.add(input);
    }
    
    public String toString() {
        StringBuffer output = new StringBuffer();
        output.append("      --- Table: " + name + " ---" +
                "\n      - Description: " + description +
                "\n      - Userlevel: " + userLevel);
        
        Iterator it = switches.iterator();
        while (it.hasNext()) {
            output.append("\n" + it.next());
        }
                
        return output+"";
    }
    
}