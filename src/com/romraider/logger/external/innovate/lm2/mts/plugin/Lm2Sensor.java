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

public class Lm2Sensor {
    private int inputNumber = 0;
    private String inputName = "";
    private String deviceName = "";
    private int deviceChannel = 0;
    private String units = "";
    private float minValue = 0f;
    private float maxValue = 0f;

    public Lm2Sensor() {
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

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String name) {
    	deviceName = name;
    }

    public int getDeviceChannel() {
        return deviceChannel;
    }

    public void setDeviceChannel(int channel) {
    	deviceChannel = channel;
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
