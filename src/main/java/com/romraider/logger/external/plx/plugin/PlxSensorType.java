/*
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2014 RomRaider.com
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

package com.romraider.logger.external.plx.plugin;

public enum PlxSensorType {
    WIDEBAND(0),
    EXHAUST_GAS_TEMPERATURE(1),
    FLUID_TEMPERATURE(2),
    VACUUM(3),
    BOOST(4),
    AIR_INTAKE_TEMPERATURE(5),
    ENGINE_SPEED(6),
    VEHICLE_SPEED(7),
    THROTTLE_POSITION(8),
    ENGINE_LOAD(9),
    FLUID_PRESSURE(10),
    TIMING(11),
    MANIFOLD_ABSOLUTE_PRESSURE(12),
    MASS_AIR_FLOW(13),
    SHORT_TERM_FUEL_TRIM(14),
    LONG_TERM_FUEL_TRIM(15),
    NARROWBAND_AFR(16),
    FUEL_LEVEL(17),
    VOLTAGE(18),
    KNOCK(19),
    DUTY_CYCLE(20),
    WIDEBAND_HEALTH(25),
    WIDEBAND_REACTION(26),
    UNKNOWN(-1);

    private final int value;

    private PlxSensorType(int value) {
        this.value = value;
    }

    public int v() {
        return value;
    }

    public static PlxSensorType valueOf(int value) {
        for (PlxSensorType type : values()) {
            if (type.v() == value) return type;
        }
        return UNKNOWN;
    }
}
