/*
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2012 RomRaider.com
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

package com.romraider.logger.external.phidget.interfacekit.io;

/**
 * IntfKitSensor contains all the relevant information about a sensor as
 * reported from information gathered from the Phidget device.  An IntfKitSensor
 * is created for each input found on the Phidget device.
 */
public final class IntfKitSensor {
    private int inputNumber;
    private String inputName;
    private String units;
    private float minValue;
    private float maxValue;

    /**
     * Create an IntfKitSensor with all fields set to type default values.
     */
    public IntfKitSensor() {
    }

    public int getInputNumber() {
        return inputNumber;
    }

    public void setInputNumber(int input) {
        inputNumber = input;
    }

    public String getInputName() {
        return inputName;
    }

    public void setInputName(String name) {
        inputName = name;
    }

    public String getUnits() {
        return units;
    }

    public void setUnits(String unit) {
        units = unit;
    }

    public float getMinValue() {
        return minValue;
    }

    public void setMinValue(float value) {
        minValue = value;
    }

    public float getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(float value) {
        maxValue = value;
    }
}
