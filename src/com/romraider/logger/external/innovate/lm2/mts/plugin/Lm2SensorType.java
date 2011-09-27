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

package com.romraider.logger.external.innovate.lm2.mts.plugin;

public enum Lm2SensorType {
	//<sensor#>(function, channel)
    WIDEBAND0(0, 0),
    THERMALCOUPLE1(9, 1),
    THERMALCOUPLE2(9, 2),
    THERMALCOUPLE3(9, 3),
    THERMALCOUPLE4(9, 4);

    private final int value;
    private final int channel;

    private Lm2SensorType(int value, int channel) {
        this.value = value;
        this.channel = channel;
    }

    public int getValue() {
        return value;
    }

    public int getChannel() {
        return channel;
    }

    public static Lm2SensorType valueOf(int value, int channel) {
        for (Lm2SensorType type : values()) {
            	if (type.getValue() == value &&
            		type.getChannel() == channel)
            			return type;
        }
        return null;
    }
}
