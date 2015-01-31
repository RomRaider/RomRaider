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

import static com.romraider.logger.ecu.comms.learning.parameter.DS2Parameter.E217;
import static com.romraider.logger.ecu.comms.learning.parameter.DS2Parameter.E23;
import static com.romraider.logger.ecu.comms.learning.parameter.DS2Parameter.E24;
import static com.romraider.logger.ecu.comms.learning.parameter.DS2Parameter.P11;
import static com.romraider.logger.ecu.comms.learning.parameter.DS2Parameter.P17;
import static com.romraider.logger.ecu.comms.learning.parameter.DS2Parameter.P2;

import java.util.HashMap;
import java.util.Map;

/**
 * A Map of Parameter and value specific to the Vehicle Information Table. 
 */
public class DS2ParameterCrossReference {
    final Map<DS2Parameter, String> map;

    public DS2ParameterCrossReference() {
        map = new HashMap<DS2Parameter, String>();
        map.put(P17,  "Battery");
        map.put(P11,  "IAT");
        map.put(P2,   "ECT");
        map.put(E23,  "TPS AD");
        map.put(E24,  "Knk Gbl");
        map.put(E217, "Knk Cur");
    }

/**
 * Retrieve the string value associated with the supplied Parameter.
 * @param parameter - Parameter to lookup value for.
 * @return the value of the Parameter.
 */
    public final String getValue(DS2Parameter parameter) {
        return map.get(parameter);
    }
}
