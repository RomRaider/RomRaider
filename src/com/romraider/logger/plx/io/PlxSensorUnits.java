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

package com.romraider.logger.plx.io;

public enum PlxSensorUnits {
    WIDEBAND_AFR_LAMBDA(0),
    WIDEBAND_AFR_GASOLINE147(1),
    WIDEBAND_AFR_DIESEL146(2),
    WIDEBAND_AFR_METHANOL64(3),
    WIDEBAND_AFR_ETHANOL90(4),
    WIDEBAND_AFR_LPG155(5),
    WIDEBAND_AFR_CNG172(6),
    EXHAUST_GAS_TEMPERATURE_CELSIUS(0),
    EXHAUST_GAS_TEMPERATURE_FAHRENHEIT(1),
    AIR_INTAKE_TEMPERATURE_CELSUIS(0),
    AIR_INTAKE_TEMPERATURE_FAHRENHEIT(1),
    KNOCK(0);

    private final int value;

    private PlxSensorUnits(int value) {
        this.value = value;
    }

    public int v() {
        return value;
    }

    public static PlxSensorUnits valueOf(int value) {
        PlxSensorUnits[] types = values();
        for (PlxSensorUnits type : types) {
            if (type.v() == value) return type;
        }
        throw new IllegalArgumentException("Unknown PLX Sensor Units: " + value);
    }
}
