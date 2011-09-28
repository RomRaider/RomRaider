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
	//SENSOR_NAME(InputNumber)
    LC_1_0(0),
    TC_4_1(17),
    TC_4_2(18),
    TC_4_3(19),
    TC_4_4(20);

    private final int inputNumber;

    private Lm2SensorType(int inputNumber) {
        this.inputNumber = inputNumber;
    }

    public int getInputNumber() {
        return inputNumber;
    }

   public static Lm2SensorType valueOf(int inputNumber) {
        for (Lm2SensorType type : values()) {
            	if (type.getInputNumber() == inputNumber)
            			return type;
        }
        return null;
    }
}
