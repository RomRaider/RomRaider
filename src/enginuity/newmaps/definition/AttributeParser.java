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

package enginuity.newmaps.definition;

import enginuity.newmaps.ecumetadata.Scale;
import enginuity.newmaps.ecumetadata.Unit;

import java.util.StringTokenizer;
import java.util.Vector;

public final class AttributeParser {
    
    public static int parseEndian(String s) {        
        if (s.equalsIgnoreCase("big")) return Scale.ENDIAN_BIG;
        else return Scale.ENDIAN_LITTLE;
    }
    
    public static int parseStorageType(String s) {
        if (s.equalsIgnoreCase("int8")) {
            return Scale.STORAGE_TYPE_INT8;
        } else if (s.equalsIgnoreCase("uint8")) {
            return Scale.STORAGE_TYPE_UINT8;
        } else if (s.equalsIgnoreCase("int16")) {
            return Scale.STORAGE_TYPE_INT16;
        } else if (s.equalsIgnoreCase("uint16")) {
            return Scale.STORAGE_TYPE_UINT16;
        } else if (s.equalsIgnoreCase("float")) {
            return Scale.STORAGE_TYPE_FLOAT;
        } else if (s.equalsIgnoreCase("hex")) {
            return Scale.STORAGE_TYPE_HEX;
        } else {
            return Scale.STORAGE_TYPE_CHAR;
        }
    }
    
    
    public static String[] stringToStringArray(String s, String delim) {
        StringTokenizer t = new StringTokenizer(s, delim);
        Vector<String> values = new Vector<String>();
        while (t.hasMoreTokens()) {
            values.add(t.nextToken());
        }
        return (String[])values.toArray();
    }   
    
    
    public static byte[] stringToByteArray(String s, String delim) {
        StringTokenizer t = new StringTokenizer(s, delim);
        Vector<Byte> values = new Vector<Byte>();
        
        while (t.hasMoreTokens()) {
            values.add(Byte.parseByte(t.nextToken()));
        }
        
        byte[] bytes = new byte[values.size()];
     
        for (int i = 0; i < values.size(); i++) {            
            bytes[i] = values.get(i);
        }
        
        return bytes;
    }   
    
    
    public static int parseUnitSystem(String s) {
        if (s.equalsIgnoreCase("metric")) return Unit.SYSTEM_METRIC;
        else if (s.equalsIgnoreCase("standard")) return Unit.SYSTEM_STANDARD;
        else return Unit.SYSTEM_UNIVERSAL;
    }

}