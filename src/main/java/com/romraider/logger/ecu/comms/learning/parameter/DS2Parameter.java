/*
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2015 RomRaider.com
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
public enum DS2Parameter {
    P2("P2"),       // ECT
    P11("P11"),     // IAT
    P17("P17"),     // Battery Volts
    E19("E19"),     // Lambda Additive Adaptation - Bank 1
    E20("E20"),     // Lambda Additive Adaptation - Bank 2
    E21("E21"),     // Lambda Multiplicative Adaptation - Bank 1
    E22("E22"),     // Lambda Multiplicative Adaptation - Bank 2
    E23("E23"),     // Throttle Position Adaptation
    E24("E24"),     // Knock Retard - Global
    E99("E99"),     // Knock Adaption Table 1 Index**
    E217("E217");   // Knock Retard - Current

    private static final Map<String, DS2Parameter> lookup
            = new HashMap<String, DS2Parameter>();

    static {
        for(DS2Parameter s : EnumSet.allOf(DS2Parameter.class))
        lookup.put(s.toString(), s);
    }

    private DS2Parameter(final String text) {
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
    public static DS2Parameter fromValue(String value) {
        DS2Parameter result = null;
        if (lookup.containsKey(value)) {
            result = lookup.get(value);
        }
        return result;
    }
}
