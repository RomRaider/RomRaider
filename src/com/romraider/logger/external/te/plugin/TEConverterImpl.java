/*
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2010 RomRaider.com
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

package com.romraider.logger.external.te.plugin;

import static com.romraider.logger.external.te.plugin.TESensorUnits.WIDEBAND_AFR_LAMBDA;
import static com.romraider.logger.external.te.plugin.TESensorUnits.WIDEBAND_AFR_GASOLINE147;

public final class TEConverterImpl implements TEConverter {
    public double convert(TESensorType sensorType, TESensorUnits sensorUnits, int... raw) {
        switch (sensorType) {
            case Lambda:
                // Lambda = 0.5 to 5.0 (normal usage range of unleaded AFR = 7.35 to AFR = 73.5)
            	if (sensorUnits == WIDEBAND_AFR_LAMBDA) return ((((raw[0] * 256d) + raw[1]) / 8192) + 0.5 );
            	if (sensorUnits == WIDEBAND_AFR_GASOLINE147) return ((((raw[0] * 256d) + raw[1]) / 8192) + 0.5 ) * 14.7;
            case USR1:
                // inputs are sampled at 10 bit accuracy, the result is multiplied by 8 giving 1024 steps
                return ((raw[0] * 256d) + raw[1]) * 0.000610948;
            case USR2:
                // inputs are sampled at 10 bit accuracy, the result is multiplied by 8 giving 1024 steps
                return ((raw[0] * 256d) + raw[1]) * 0.000610948;
            case USR3:
                // inputs are sampled at 10 bit accuracy, the result is multiplied by 8 giving 1024 steps
                return ((raw[0] * 256d) + raw[1]) * 0.000610948;
            case TC1:
                // inputs are sampled at 10 bit accuracy, the result is not multiplied by any value
                return ((raw[0] * 256d) + raw[1]) * 0.004887586;
            case TC2:
                // inputs are sampled at 10 bit accuracy, the result is not multiplied by any value
                return ((raw[0] * 256d) + raw[1]) * 0.004887586;
            case TC3:
                // inputs are sampled at 10 bit accuracy, the result is not multiplied by any value
                return ((raw[0] * 256d) + raw[1]) * 0.004887586;
            case TorVss:
                // raw Thermocouple value or VSS counts @ 100 uSec periods
                return ((raw[0] * 256d) + raw[1]);
            case RPM:
                // RPM = count of 5 uSec periods between sparks for 4 cyl engine
                return (60 / (((raw[0] * 256d) + raw[1]) * 0.00001));
            default:
                throw new UnsupportedOperationException("Calculation for this particular Tech Edge sensor type is not supported:" + sensorType);
        }
    }
}
