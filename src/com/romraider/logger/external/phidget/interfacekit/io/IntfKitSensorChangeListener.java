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

import com.phidgets.PhidgetException;
import com.phidgets.event.SensorChangeListener;
import com.phidgets.event.SensorChangeEvent;

/**
 * IntfKitSensorChangeListener responds to Sensor changes.  It filters
 * the exact sensor to be updated and provides the update to the 
 * IntfKitRunner's data items.
 */
public class IntfKitSensorChangeListener implements SensorChangeListener {
    private final IntfKitRunner ikr;

    /**
     * Creates a new instance of IntfKitSensorChangeListener to be
     * registered against each PhidgetInterfaceKit opened.
     * @param ikr - the instance of the InterfaceKitRunner
     */
    public IntfKitSensorChangeListener(IntfKitRunner ikr) {
        this.ikr = ikr;
    }

    /**
     * Handles the sensor change, isolates the serial number, sensor
     * number and value then calls to the InterfaceKitRunner to 
     * update the matching data item.
     * @param sensorChangeEvent - the event from the Phidget device
     */
    public void sensorChanged(final SensorChangeEvent sensorChangeEvent) {
        try {
            ikr.updateDataItem(
                    sensorChangeEvent.getSource().getSerialNumber(),
                    sensorChangeEvent.getIndex(),
                    sensorChangeEvent.getValue());
        }
        catch (PhidgetException e) {
            ikr.updateDataItem(-1, -1, -1);
        }
    }
}
