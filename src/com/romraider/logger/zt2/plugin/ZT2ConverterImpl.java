/*
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2009 RomRaider.com
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

package com.romraider.logger.zt2.plugin;

import com.romraider.logger.zt2.io.ZT2SensorType;
import static java.lang.Math.round;

public final class ZT2ConverterImpl implements ZT2Converter {
    public double convert(ZT2SensorType sensorType, int... raw) {
        switch (sensorType) {
            case AFR:
                // AFR / 10
                return raw[0] / 10d;
            case EGT:
                // (EGT(low) + EGT(high)) * 256
                return (raw[0] + raw[1]) * 256d;
            case RPM:
                // Cm = (Number of Cylinders in the Engine) / 2;
                // round([(1000000/(RPM(low)+(RPM(high)*256)))*4.59]/Cm)
                return round(((1000000d / (raw[0] + (raw[1] * 256d))) * 4.59d) / 2d);
            case MAP:
                // special handling on high byte - if 8th bit is 1 (means that it's negative)
                if ((raw[1] & 128) == 128) {
                    // We are supposed to clear the 8 bit, calc, then restore the sign.
                    return 0 - ((raw[0] + (raw[1] & ~(1 << 7)) * 256d) / 10d);
                } else {
                    return (raw[0] + raw[1] * 256d) / 10d;
                }
            case TPS:
                return raw[0];
            default:
                throw new UnsupportedOperationException("Calculation for this particular ZTSensorType is not supported");
        }
    }
}
