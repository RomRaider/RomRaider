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

import static com.phidgets.Phidget.PHIDCLASS_INTERFACEKIT;
import static java.lang.System.currentTimeMillis;
import static org.apache.log4j.Logger.getLogger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.phidgets.InterfaceKitPhidget;
import com.phidgets.Manager;
import com.phidgets.Phidget;
import com.phidgets.PhidgetException;
import com.romraider.util.SettingsManager;

/**
 * IntfKitManager is used to discover all the attached PhidgetInterfaceKits
 * by serial number, load the Phidgets library and get a list of all sensors
 * on all PhidgetInterfaceKits.
 */
public final class IntfKitManager {
    private static final Logger LOGGER = getLogger(IntfKitManager.class);
    private static InterfaceKitPhidget ik;

    /**
     * Using the Phidgets Manager find all of the attached PhidgetInterfaceKits.
     * @return an array of serial numbers
     * @throws PhidgetException
     * @throws InterruptedException
     */
    public static List<Integer> findIntfkits() {
        final List<Integer> serials = new ArrayList<Integer>();
        try {
            final Manager fm = new Manager();
            fm.open();
            Thread.sleep(100);
            @SuppressWarnings("unchecked")
            final List<Phidget> phidgets = fm.getPhidgets();
            for (Phidget phidget : phidgets) {
                if (phidget.getDeviceClass() == PHIDCLASS_INTERFACEKIT) {
                    serials.add(phidget.getSerialNumber());
                }
            }
            fm.close();
        }
        catch (PhidgetException e) {
            LOGGER.error("Phidget Manager error: " + e);
        }
        catch (InterruptedException e) {
            LOGGER.info("Sleep interrupted " + e);
        }
        return serials;
    }

    /**
     * Initialise the Phidgets Library and report the library version in the
     * RomRaider system log file.
     * @throws PhidgetException
     */
    public static void loadIk() {
        try {
            ik = new InterfaceKitPhidget();
            LOGGER.info("Plugin found: " + Phidget.getLibraryVersion());
        }
        catch (PhidgetException e) {
            LOGGER.error("InterfaceKit error: " + e);
        }
    }

    /**
     * For the serial number provided report the name of the
     * associated PhidgetInterfaceKit.
     * @param serial - the serial number previously discovered to be opened
     * @return a format string of the name and serial number
     * @throws PhidgetException
     * @throws InterruptedException
     */
    public static String getIkName(final int serial) {
        String result = null;
        try {
            ik.open(serial);
            waitForAttached();
            try {
                if (ik.getDeviceClass() == PHIDCLASS_INTERFACEKIT) {
                    result = String.format(
                            "%s serial: %d",
                            ik.getDeviceName(),
                            serial);
                }
            }
            catch (PhidgetException e) {
                LOGGER.error("InterfaceKit read device error: " + e);
            }
            finally {
                ik.close();
            }
        }
        catch (PhidgetException e) {
            LOGGER.error("InterfaceKit open serial error: " + e);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * For the serial number provided create a Set of sensors found on the
     * associated PhidgetInterfaceKit.
     * @param serial - the serial number previously discovered to open
     * @return a Set of <b>IntfKitSensor</b>
     * @throws PhidgetException
     * @throws InterruptedException
     */
    public static Set<IntfKitSensor> getSensors(final int serial) {
        Set<IntfKitSensor> sensors = new HashSet<IntfKitSensor>();
        try {
            ik.open(serial);
            waitForAttached();
            try {
                if (ik.isAttached()) {
                    if (ik.getDeviceClass() == PHIDCLASS_INTERFACEKIT) {
                        final String result = String.format(
                                "Plugin found: %s Serial: %d",
                                ik.getDeviceName(),
                                serial);
                        LOGGER.info(result);
                        Map<String, IntfKitSensor> phidgets =
                                SettingsManager.getSettings().getPhidgetSensors();
                        if (phidgets == null) {
                            phidgets = new HashMap<String, IntfKitSensor>();
                        }
                        final int inputCount = ik.getSensorCount();
                        for (int i = 0; i < inputCount; i++) {
                            final String key = String.format("%d:%d", serial, i);
                            if (phidgets.containsKey(key)) {
                                sensors.add(phidgets.get(key));
                                final String stored = String.format(
                                        "Plugin applying user settings for: %s",
                                        phidgets.get(key).toString());
                                LOGGER.info(stored);
                            }
                            else {
                                final IntfKitSensor sensor = new IntfKitSensor();
                                final String inputName = String.format(
                                        "Phidget IK Sensor %d:%d",
                                        serial,
                                        i);
                                sensor.setInputNumber(i);
                                sensor.setInputName(inputName);
                                sensor.setUnits("raw value");
                                sensor.setExpression("x");
                                sensor.setFormat("0");
                                sensor.setMinValue(0);
                                sensor.setMaxValue(1000);
                                sensor.setStepValue(100);
                                sensors.add(sensor);
                                phidgets.put(key, sensor);
                            }
                        }
                        SettingsManager.getSettings().setPhidgetSensors(phidgets);
                    }
                    else {
                        LOGGER.info("No InterfaceKits attached");
                    }
                }
                else {
                    LOGGER.info("No Phidget devices attached");
                }
            }
            catch (PhidgetException e) {
                LOGGER.error("InterfaceKit read error: " + e);
            }
            finally {
                ik.close();
            }
        }
        catch (PhidgetException e) {
            LOGGER.error("InterfaceKit open error: " + e);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
        return sensors;
    }

    /**
     * Wait for the Attach signal after opening the PhidgetInterfaceKit
     * or a maximum timeout of 500msec.
     * @throws PhidgetException
     * @throws InterruptedException
     */
    private static void waitForAttached()
            throws InterruptedException, PhidgetException {
        final long timeout = currentTimeMillis() + 500L;
        do {
            Thread.sleep(50);
        } while (!ik.isAttached() && (currentTimeMillis() < timeout));
    }
}
