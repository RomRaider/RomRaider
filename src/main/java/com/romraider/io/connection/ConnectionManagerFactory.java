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

package com.romraider.io.connection;

import static com.romraider.util.Platform.WINDOWS;
import static com.romraider.util.Platform.isPlatform;
import static com.romraider.util.proxy.Proxifier.proxy;
import static org.apache.log4j.Logger.getLogger;

import java.io.File;
import java.util.Set;

import org.apache.log4j.Logger;

import com.romraider.Settings;
import com.romraider.io.j2534.api.J2534DllLocator;
import com.romraider.io.j2534.api.J2534Library;
import com.romraider.io.j2534.api.J2534TransportFactory;
import com.romraider.io.serial.connection.SerialConnectionManager;
import com.romraider.util.SettingsManager;
import com.romraider.util.proxy.TimerWrapper;

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

        Settings settings = SettingsManager.getSettings();

        try {

            if (!isPlatform(WINDOWS)) {
                final String op2Lib = "/usr/local/lib/j2534.so";
                final File libFile = new File(op2Lib);
                if (libFile.exists()) {
                    return J2534TransportFactory.getManager(
                            settings.getTransportProtocol().toUpperCase(),
                            connectionProperties,
                            op2Lib);
                }
                else {
                    throw new RuntimeException(
                            "Linux Openport 2.0 library not found in: " +
                            op2Lib);
                }
            }
            Set<J2534Library> libraries =
                    J2534DllLocator.listLibraries(
                            settings.getTransportProtocol().toUpperCase());

            if (libraries.isEmpty())
                throw new RuntimeException(
                        "No J2534 libraries found that support protocol " +
                                settings.getTransportProtocol());;

                                // if the J2534 device has not been previously defined, search for it
                                // else use the defined device
                                if (settings.getJ2534Device() == null) {
                                    for (J2534Library dll : libraries) {
                                        LOGGER.info(String.format("Trying new J2534/%s connection: %s",
                                                settings.getTransportProtocol(),
                                                dll.getVendor()));
                                        try {
                                            settings.setJ2534Device(dll.getLibrary());
                                            return J2534TransportFactory.getManager(
                                                    settings.getTransportProtocol().toUpperCase(),
                                                    connectionProperties,
                                                    dll.getLibrary());

                                        }
                                        catch (Throwable t) {
                                            settings.setJ2534Device(null);
                                            LOGGER.info(String.format("%s is not available: %s",
                                                    dll.getVendor(), t.getMessage()));
                                        }
                                    }
                                }
                                else {
                                    for (J2534Library dll : libraries) {
                                        if (dll.getLibrary().toLowerCase().contains(
                                                settings.getJ2534Device().toLowerCase())) {

                                            LOGGER.info(String.format(
                                                    "Re-trying previous J2534/%s connection: %s",
                                                    settings.getTransportProtocol(),
                                                    dll.getVendor()));
                                            try {
                                                settings.setJ2534Device(dll.getLibrary());
                                                return J2534TransportFactory.getManager(
                                                        settings.getTransportProtocol().toUpperCase(),
                                                        connectionProperties,
                                                        dll.getLibrary());
                                            }
                                            catch (Throwable t) {
                                                settings.setJ2534Device(null);
                                                LOGGER.info(String.format("%s is not available: %s",
                                                        dll.getVendor(), t.getMessage()));
                                            }
                                        }
                                    }
                                }
                                throw new RuntimeException("J2534 connection not available");
        }
        catch (Throwable t) {
            settings.setJ2534Device(null);
            LOGGER.info(String.format("%s, trying serial connection...",
                    t.getMessage()));
            return new SerialConnectionManager(portName, connectionProperties);
        }
    }
}
