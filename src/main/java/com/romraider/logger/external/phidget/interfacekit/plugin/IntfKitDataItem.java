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

package com.romraider.logger.external.phidget.interfacekit.plugin;

import com.romraider.logger.ecu.definition.EcuDataConvertor;
import com.romraider.logger.ecu.definition.ExternalDataConvertorImpl;
import com.romraider.logger.ecu.ui.handler.dash.GaugeMinMax;
import com.romraider.logger.external.core.DataListener;
import com.romraider.logger.external.core.ExternalDataItem;

/**
 * IntfKitDataItem contains all the relevant information about a data item as
 * reported from information gathered from the Phidget device.  A data item
 * is created for each input found on the Phidget device.  Only raw data values
 * are recorded for these sensors as we don't know the conversion formula of
 * the analog device attached to the sensor input.  Data will need to be post-
 * processed in a spreadsheet application with the formula of the device.
 */
public final class IntfKitDataItem implements ExternalDataItem, DataListener {
    private final String name;
    private final GaugeMinMax gaugeMinMax;
    private double data;
    private String units;

    /**
     * Create a new data item and set its fields according to the supplied
     * parameters.
     * @param name - unique name of the data item
     * @param units - the units of the data
     * @param minValue - the minimum value expected for the data item
     * @param maxValue - the maximum value expected for the data item
     */
    public IntfKitDataItem(String name, String units, float minValue, float maxValue) {
        super();
        this.name = name;
        this.units = units;
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
        return "Phidget IK " + name;
    }

    public String getDescription() {
        return "Phidget IK " + name + " data";
    }

    public double getData() {
        return data;
    }

    public String getUnits() {
        return units;
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
