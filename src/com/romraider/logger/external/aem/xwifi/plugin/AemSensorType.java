/*
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2012 RomRaider.com
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
 */

package com.romraider.logger.external.aem.xwifi.plugin;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
*    Enum class representing the AEM X-Wifi sensor types available with methods to
*    translate the mnemonic and numerical values.
*/
public enum AemSensorType {
    UEGO(0),
    EGT1(1),
    EGT2(2),
    UNDEFINED(-1);    // Returned when no match is found for get()

    private static final Map<Integer, AemSensorType> lookup
            = new HashMap<Integer, AemSensorType>();

    static {
        for(AemSensorType s : EnumSet.allOf(AemSensorType.class))
            lookup.put(s.getValue(), s);
    }

    private final int value;

    private AemSensorType(int value) {
        this.value = value;
    }

    /**
    * Get the numeric value associated with the <b>AemSensorType</b>
    * mnemonic string.
    * @return    numeric value.
    */
    public int getValue() {
        return value;
    }

    /**
    * Get the <b>AemSensorType</b> mnemonic mapped to the numeric
    * value or UNDEFINED if value is undefined. 
    * @param    value - numeric value to be translated.
    * @return    the <b>AemSensorType</b> mnemonic mapped to the numeric
    *            value or UNDEFINED if value is undefined.
    */
    public static AemSensorType valueOf(int value) {
        if (lookup.containsKey(value)) {
            return lookup.get(value);
        }
        else return UNDEFINED;
    }
}
