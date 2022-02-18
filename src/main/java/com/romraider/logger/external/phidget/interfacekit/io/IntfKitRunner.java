/*
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2022 RomRaider.com
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

import static com.romraider.util.ThreadUtil.sleep;
import static org.apache.log4j.Logger.getLogger;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.phidgets.InterfaceKitPhidget;
import com.phidgets.PhidgetException;
import com.romraider.logger.external.core.Stoppable;
import com.romraider.logger.external.phidget.interfacekit.plugin.IntfKitDataItem;

/**
 * IntfKitRunner manages the connections to the PhidgetInterfaceKits.  It
 * also responds to the sensor change events to update the appropriate
 * sensor's value.
 */
public final class IntfKitRunner implements Stoppable {
    private static final Logger LOGGER = getLogger(IntfKitRunner.class);
    private final Map<String, IntfKitDataItem> dataItems;
    private Set<InterfaceKitPhidget> connections;
    private boolean stop;

    /**
     * IntfKitRunner interrogates the PhidgetInterfaceKits for data and updates
     * the appropriate sensor result.
     * @param kits - List of serial numbers of the PhidgetInterfaceKits
     * @param dataItems - a Map of PhidgetInterfaceKit data items (sensors)
     */
    public IntfKitRunner(
            final List<Integer> kits,
            final Map<String, IntfKitDataItem> dataItems) {
        this.dataItems = dataItems;
        this.connections = IntfKitConnector.openIkSerial(this, kits);
    }

    /**
     * This method is used to start and stop the reading of the
     * PhidgetInterfaceKits data.  Each data item value is set by the
     * sensor change Listener. When stop is issued the connection is
     * closed to the device.
     * @throws PhidgetException
     * @see IntfKitSensorChangeListener
     */
    @Override
    public void run() {
        try {
            while (!stop) {
                sleep(500L);
            }
            for (InterfaceKitPhidget connector : connections) {
                    connector.close();
            }
        } catch (PhidgetException e) {
               LOGGER.error("InterfaceKit close error: " + e);
        }
    }

    /**
     * This method is used stop the reading of the PhidgetInterfaceKits data
     * and close the connections to the devices.
     */
    @Override
    public void stop() {
        stop = true;
    }

    /**
     * This method is event driven and called by the SensorChangeListner
     * with the sensor ID and the new value to be set.
     * @param serial - serial number of the InterfaceKit reporting the change
     * @param sensor - the sensor number reporting the change
     * @param value  - the new value to set
     */
    public void updateDataItem(final int serial, final int sensor, final int value) {
        if (serial != -1) {
        final String inputName = String.format("%d:%d", serial, sensor);
        dataItems.get(inputName).setData(value);
        if (LOGGER.isTraceEnabled())
            LOGGER.trace(String.format(
                    "Phidget InterfaceKit sensor %s event - raw value: %d",
                    inputName,
                    sensor,
                    value));
        }
        else {
            LOGGER.error("Phidget InterfaceKit dataitem update error");
        }
    }
}
