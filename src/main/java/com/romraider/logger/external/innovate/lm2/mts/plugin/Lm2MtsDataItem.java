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

package com.romraider.logger.external.innovate.lm2.mts.plugin;

import com.romraider.logger.ecu.definition.EcuDataConvertor;
import com.romraider.logger.ecu.definition.ExternalDataConvertorImpl;
import com.romraider.logger.ecu.ui.handler.dash.GaugeMinMax;
import com.romraider.logger.external.core.DataListener;
import com.romraider.logger.external.core.ExternalDataItem;

public final class Lm2MtsDataItem implements ExternalDataItem, DataListener {
    private final String name;
    private final GaugeMinMax gaugeMinMax;
    private int channel;
    private double data;
    private String units;
    private float minValue;
    private float maxValue;
    private float multiplier;

    public Lm2MtsDataItem(String name, int channel, String units, float minValue, float maxValue, float multiplier) {
        super();
        this.name = name;
        this.channel = channel;
        this.units = units;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.multiplier = multiplier;
        float step = (Math.abs(maxValue) + Math.abs(minValue)) / 10f;
        if (step > 0.5f) {
            step = (float) Math.round(step);
        }
        else {
            step = 0.5f;
        }
        gaugeMinMax = new GaugeMinMax(minValue, maxValue, step);
    }

    public String getName() {
        return "Innovate MTS " + name + " CH" + channel;
    }

    public String getDescription() {
        return "Innovate MTS " + name + " CH" + channel + " data";
    }

    public int getChannel() {
        return channel;
    }
    
    public double getData() {
        return data;
    }

    public String getUnits() {
        return units;
    }

    public float getMinValue() {
        return minValue;
    }

    public float getMaxValue() {
        return maxValue;
    }

    public float getMultiplier() {
        return multiplier;
    }

    public void setData(double data) {
        this.data = data;
    }

    public EcuDataConvertor[] getConvertors() {
        EcuDataConvertor[] convertors = {
                new ExternalDataConvertorImpl(
                        this,
                        units,
                        "x",
                        "0.##",
                        gaugeMinMax)};
        return convertors;
    }
}