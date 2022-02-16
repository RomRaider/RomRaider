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

package com.romraider.io.connection;

import static com.romraider.util.ParamChecker.isNullOrEmpty;
import static com.romraider.util.proxy.Proxifier.proxy;
import static org.apache.log4j.Logger.getLogger;

import org.apache.log4j.Logger;

import com.romraider.Settings;
import com.romraider.io.elm327.ElmConnectionManager;
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
            final String portName,
            final ConnectionProperties connectionProperties) {

        final Settings settings = SettingsManager.getSettings();
        ConnectionManager manager = null;

        // Try a serial connection
        if (isNullOrEmpty(settings.getJ2534Device())) {

            if(SettingsManager.getSettings().getElm327Enabled()) {
                LOGGER.info("Trying to connect to ELM327...");
                manager = new ElmConnectionManager(portName, connectionProperties);
            }
            else {
                LOGGER.info("Trying serial connection...");
                manager = new SerialConnectionManager(portName, connectionProperties);
            }
        }
        else {
            // Try a J2534 connection
            manager = J2534TransportFactory.getManager(
                    settings.getTransportProtocol().toUpperCase(),
                    connectionProperties,
                    settings.getJ2534Device());
        }
        if (ENABLE_TIMER) return proxy(manager, TimerWrapper.class);
        return manager;
    }
}
