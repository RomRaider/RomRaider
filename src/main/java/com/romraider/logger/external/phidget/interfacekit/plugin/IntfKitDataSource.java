/*
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2015 RomRaider.com
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.swing.Action;

import com.romraider.logger.ecu.EcuLogger;
import com.romraider.logger.external.core.ExternalDataItem;
import com.romraider.logger.external.core.ExternalDataSource;
import com.romraider.logger.external.phidget.interfacekit.io.IntfKitManager;
import com.romraider.logger.external.phidget.interfacekit.io.IntfKitRunner;
import com.romraider.logger.external.phidget.interfacekit.io.IntfKitSensor;
import com.romraider.util.ThreadUtil;

/**
 * The IntfKitDataSource class is called when the Logger starts up and the 
 * call to load the external plug-ins is made.  This class with its helpers
 * will open each PhidgetInterfaceKit and find all available inputs.  It will
 * interrogate the inputs then dynamically build a list of inputs found based
 * on the serial number and input number.
 * @see ExternalDataSource
 */
public final class IntfKitDataSource implements ExternalDataSource {
    private final Map<String, IntfKitDataItem> dataItems =
            new HashMap<String, IntfKitDataItem>();
    private IntfKitRunner runner;
    private List<Integer> kits;

    {
        kits = IntfKitManager.findIntfkits();
        if (kits.size() > 0) {
            IntfKitManager.loadIk();
            for (Iterator<Integer> kitIt = kits.iterator(); kitIt.hasNext();) {
                final int serial = kitIt.next();
                final Set<IntfKitSensor> sensors =
                        IntfKitManager.getSensors(serial);
                for (Iterator<IntfKitSensor> sensorIt = sensors.iterator();
                        sensorIt.hasNext();) {

                    final IntfKitSensor sensor = sensorIt.next();
                    final String inputName = String.format(
                            "%d:%d",
                            serial,
                            sensor.getInputNumber());
                    dataItems.put(inputName, new IntfKitDataItem(sensor));
                }
            }
        }
    }

    public String getId() {
        return getClass().getName();
    }

    public String getName() {
        return "Phidget InterfaceKit";
    }

    public String getVersion() {
        return "0.02";
    }

    public List<? extends ExternalDataItem> getDataItems() {
        return Collections.unmodifiableList(
                new ArrayList<IntfKitDataItem>(dataItems.values()));
    }

    public Action getMenuAction(final EcuLogger logger) {
        return new IntfKitPluginMenuAction(logger);
    }

    public void setProperties(Properties properties) {
    }

    public void connect() {
        runner = new IntfKitRunner(kits, dataItems);
        ThreadUtil.runAsDaemon(runner);
    }

    public void disconnect() {
        if (runner != null) runner.stop();
    }

    public void setPort(final String port) {
    }

    public String getPort() {
        return "HID USB";
    }
}
