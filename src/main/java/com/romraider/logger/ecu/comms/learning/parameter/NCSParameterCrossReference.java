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

import static com.romraider.logger.ecu.comms.learning.parameter.NCSParameter.P4390;
import static com.romraider.logger.ecu.comms.learning.parameter.NCSParameter.P4389;
import static com.romraider.logger.ecu.comms.learning.parameter.NCSParameter.P11;
import static com.romraider.logger.ecu.comms.learning.parameter.NCSParameter.P17;
import static com.romraider.logger.ecu.comms.learning.parameter.NCSParameter.P2;

import java.util.HashMap;
import java.util.Map;

/**
 * A Map of Parameter and value specific to the Vehicle Information Table. 
 */
public class NCSParameterCrossReference {
    final Map<NCSParameter, String> map;

    public NCSParameterCrossReference() {
        map = new HashMap<NCSParameter, String>();
        map.put(P4390, "LFTB2");
        map.put(P4389, "LFTB1");
        map.put(P17,   "Battery");
        map.put(P11,   "IAT");
        map.put(P2,    "ECT");
    }

/**
 * Retrieve the string value associated with the supplied Parameter.
 * @param parameter - Parameter to lookup value for.
 * @return the value of the Parameter.
 */
    public final String getValue(NCSParameter parameter) {
        return map.get(parameter);
    }
}
