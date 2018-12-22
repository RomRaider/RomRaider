/*
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2018 RomRaider.com
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

package com.romraider.logger.ecu.comms.learning.parameter;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * This Emun defines all the possible parameters used to query the Learning
 * Table Values of an ECU. 
 */
public enum NCSParameter {
    P2("P2"),       // ECT
    P11("P11"),     // IAT
    P17("P17"),     // Battery Volts
    E173("E173"),   // mixed ratio learning map trim
    E174("E174"),   // mixed ratio learning map count
    P4389("P4389"), // Learned Fuel Trim B1
    P4390("P4390"); // Learned Fuel Trim B2


    private static final Map<String, NCSParameter> lookup
            = new HashMap<String, NCSParameter>();

    static {
        for(NCSParameter s : EnumSet.allOf(NCSParameter.class))
        lookup.put(s.toString(), s);
    }

    private NCSParameter(final String text) {
        this.text = text;
    }

    private final String text;

    @Override
    public final String toString() {
        return text;
    }

    /**
     * Retrieve the Parameter that has the given value. 
     * @param value - the value of the Parameter in String format
     * @return the Parameter that has the given value or null if undefined. 
     */
    public static NCSParameter fromValue(String value) {
        NCSParameter result = null;
        if (lookup.containsKey(value)) {
            result = lookup.get(value);
        }
        return result;
    }
}
