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

import static java.lang.System.currentTimeMillis;
import static org.apache.log4j.Logger.getLogger;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.phidgets.InterfaceKitPhidget;
import com.phidgets.PhidgetException;

/**
 * IntfKitConnector will open a connection to each serial number provided
 * and return those connections in a Set for use by the runner.
 */
public final class IntfKitConnector {
    private static final Logger LOGGER = getLogger(IntfKitConnector.class);

    /**
     * Open a connection to each serial number provided and
     * return those connections in a Set for use by the runner.
     * @param ikr     - the instance of IntfKitRunner calling this class
     * @param serials - List of serial numbers to open
     * @return a Set of InterfaceKitPhidget connections
     * @throws InterruptedException
     * @throws PhidgetException
     * @see IntfKitRunner
     */
    public static Set<InterfaceKitPhidget> openIkSerial(
            final IntfKitRunner ikr,
            final List<Integer> serials) {

        final Set<InterfaceKitPhidget> kits = new HashSet<InterfaceKitPhidget>();
        try {
            for (int serial : serials) {
                final InterfaceKitPhidget ik = new InterfaceKitPhidget();
                final IntfKitSensorChangeListener scl = new IntfKitSensorChangeListener(ikr);
                ik.addSensorChangeListener(scl);
                ik.open(serial);
                final long timeout = currentTimeMillis() + 500L;
                do {
                    Thread.sleep(50);
                } while (!ik.isAttached() && (currentTimeMillis() < timeout));
                final int inputCount = ik.getSensorCount();
                for (int i = 0; i < inputCount; i++) {
                    ik.setSensorChangeTrigger(i, 1);
                }
                kits.add(ik);
            }
           }
           catch (PhidgetException e) {
               LOGGER.error("InterfaceKit open error: " + e);
           } catch (InterruptedException e) {
            LOGGER.info("Sleep interrupted " + e);
        }
        return kits;
    }
}
