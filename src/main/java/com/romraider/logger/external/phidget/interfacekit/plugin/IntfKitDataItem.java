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

package com.romraider.logger.external.phidget.interfacekit.plugin;

import com.romraider.logger.ecu.definition.EcuDataConvertor;
import com.romraider.logger.ecu.definition.ExternalDataConvertorImpl;
import com.romraider.logger.ecu.ui.handler.dash.GaugeMinMax;
import com.romraider.logger.external.core.ConvertorManager;
import com.romraider.logger.external.core.DataListener;
import com.romraider.logger.external.core.ExternalDataItem;
import com.romraider.logger.external.phidget.interfacekit.io.IntfKitSensor;

/**
 * IntfKitDataItem contains all the relevant information about a data item as
 * reported from information gathered from the Phidget device.  A data item
 * is created for each input found on the Phidget device.  The conversion formula
 * of each sensor input is set to raw defaults and can be modified by the user
 * using the Phidget InterfaceKit dialog from the Logger Plugins menu item.  
 */
public final class IntfKitDataItem implements
        ExternalDataItem, DataListener, ConvertorManager {

    private final String name;
    private double data;
    private EcuDataConvertor[] convertors;
    
    /**
     * Create a new data item and set its fields according to the supplied
     * IntfKitSensor.  Append a default raw value convertor if the user has
     * defined a custom convertor for the sensor.
     * @param sensor - IntfKitSensor to create a data item for.
     * @see IntfKitSensor
     */
    public IntfKitDataItem(IntfKitSensor sensor) {
        super();
        this.name = sensor.getInputName();
        int convertorCount = 1;
        if (!sensor.getExpression().equals("x") &&
                !sensor.getUnits().equals("raw value")) {
            convertorCount = 2;
        }
        GaugeMinMax gaugeMinMax = new GaugeMinMax(
                sensor.getMinValue(), sensor.getMaxValue(), sensor.getStepValue());
        convertors = new EcuDataConvertor[convertorCount];
        convertors[0] = new ExternalDataConvertorImpl(
                this,
                sensor.getUnits(),
                sensor.getExpression(),
                sensor.getFormat(),
                gaugeMinMax);
        if (convertorCount == 2) {
            gaugeMinMax = new GaugeMinMax(0,1000, 100);
            convertors[1] = new ExternalDataConvertorImpl(
                    this,
                    "raw value",
                    "x",
                    "0",
                    gaugeMinMax);
        }
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return name + " data";
    }

    @Override
    public double getData() {
        return data;
    }

    @Override
    public void setData(double data) {
        this.data = data;
    }

    @Override
    public EcuDataConvertor[] getConvertors() {
        return convertors;
    }

    @Override
    public void setConvertors(EcuDataConvertor[] convertors) {
        this.convertors = convertors;        
    }
}
