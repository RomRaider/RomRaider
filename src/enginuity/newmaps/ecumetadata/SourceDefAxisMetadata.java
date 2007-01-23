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

import static enginuity.newmaps.definition.AttributeParser.stringToStringArray;

import java.io.Serializable;

public class SourceDefAxisMetadata extends AxisMetadata implements Serializable {
    
    private String[] values;
    
    public SourceDefAxisMetadata(String name) {
        super(name);
    }

    public String[] getValues() {
        return values;
    }

    public void setValues(String s, String delim) {
        values = stringToStringArray(s, delim);
    }
    
    public int getSize() {
        return 0;
    }
    
}