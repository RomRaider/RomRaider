/*
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2013 RomRaider.com
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
 * reported from information gathered from the Phidget device or as user defined
 * in the settings.xml file.  An IntfKitSensor is created for each input found
 * on a Phidget device.
 */
public final class IntfKitSensor {
    private int inputNumber;
    private String inputName;
    private String units;
    private float minValue;
    private float maxValue;
    private String expression;
    private String format;
    private float step;

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

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public float getStepValue() {
        return step;
    }

    public void setStepValue(float value) {
        step = value;
    }

    @Override
    public String toString() {
        return String.format("[%s|%s|%s|%s|%s|%s|%s]",
                this.getInputName(),
                this.getExpression(),
                this.getUnits(),
                this.getFormat(),
                this.getMinValue(),
                this.getMaxValue(),
                this.getStepValue());
    }
}
