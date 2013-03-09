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

package com.romraider.io.connection;

import java.util.Set;

import com.romraider.Settings;
import com.romraider.io.j2534.api.J2534ConnectionISO9141;
import com.romraider.io.j2534.api.J2534DllLocator;
import com.romraider.io.j2534.api.J2534Library;
import com.romraider.io.serial.connection.SerialConnectionManager;
import static com.romraider.util.proxy.Proxifier.proxy;
import com.romraider.util.proxy.TimerWrapper;
import org.apache.log4j.Logger;
import static org.apache.log4j.Logger.getLogger;
import static com.romraider.util.Platform.isPlatform;
import static com.romraider.util.Platform.WINDOWS;

public final class ConnectionManagerFactory {
    private static final Logger LOGGER = getLogger(ConnectionManagerFactory.class);
    private static final boolean ENABLE_TIMER = false;

    private ConnectionManagerFactory() {
    }

    public static ConnectionManager getManager(
            String portName,
            ConnectionProperties connectionProperties) {
        
        ConnectionManager manager = manager(portName, connectionProperties);
        if (ENABLE_TIMER) return proxy(manager, TimerWrapper.class);
        return manager;
    }

    private static ConnectionManager manager(
            String portName,
            ConnectionProperties connectionProperties) {
        
        try {
            if (!isPlatform(WINDOWS))
                throw new RuntimeException("J2534 is not support on this platform");
            Set<J2534Library> libraries =
                    J2534DllLocator.listLibraries(
                            Settings.getJ2534Protocol().toUpperCase());
            
            // if the J2534 device has not been previously defined, search for it
            // else use the defined device
            if (Settings.getJ2534Device() == null) {
                for (J2534Library dll : libraries) {
                    LOGGER.info(String.format("Trying new J2534 %s connection: %s",
                            Settings.getJ2534Protocol(),
                            dll.getVendor()));
                    try {
                        Settings.setJ2534Device(dll.getLibrary());
                        return new J2534ConnectionISO9141(
                                connectionProperties,
                                dll.getLibrary());
                    }
                    catch (Throwable t) {
                        Settings.setJ2534Device(null);
                        LOGGER.info(String.format("%s is not available: %s",
                                dll.getVendor(), t.getMessage()));
                    }
                }
            }
            else {
                for (J2534Library dll : libraries) {
                    if (dll.getLibrary().toLowerCase().contains(
                            Settings.getJ2534Device().toLowerCase())) {

                        LOGGER.info(String.format(
                                "Re-trying previous J2534 %s connection: %s",
                                Settings.getJ2534Protocol(),
                                dll.getVendor()));
                        try {
                            Settings.setJ2534Device(dll.getLibrary());
                            return new J2534ConnectionISO9141(
                                    connectionProperties,
                                    dll.getLibrary());
                        }
                        catch (Throwable t) {
                            Settings.setJ2534Device(null);
                            LOGGER.info(String.format("%s is not available: %s",
                                    dll.getVendor(), t.getMessage()));
                        }
                    }
                }
            }
            throw new RuntimeException("J2534 connection not available");
        } catch (Throwable t) {
            Settings.setJ2534Device(null);
            LOGGER.info(String.format("%s, trying serial connection...",
                    t.getMessage()));
            return new SerialConnectionManager(portName, connectionProperties);
        }
    }
}
