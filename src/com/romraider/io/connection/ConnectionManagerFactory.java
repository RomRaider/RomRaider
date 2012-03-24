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

import com.romraider.io.j2534.api.J2534ConnectionManager;
import com.romraider.io.serial.connection.SerialConnectionManager;
import static com.romraider.util.proxy.Proxifier.proxy;
import com.romraider.util.proxy.TimerWrapper;
import org.apache.log4j.Logger;
import static org.apache.log4j.Logger.getLogger;

public final class ConnectionManagerFactory {
    private static final Logger LOGGER = getLogger(ConnectionManagerFactory.class);
    private static final boolean ENABLE_TIMER = false;

    private ConnectionManagerFactory() {
    }

    public static ConnectionManager getManager(String portName, ConnectionProperties connectionProperties) {
        ConnectionManager manager = manager(portName, connectionProperties);
        if (ENABLE_TIMER) return proxy(manager, TimerWrapper.class);
        return manager;
    }

    private static ConnectionManager manager(String portName, ConnectionProperties connectionProperties) {
        try {
            LOGGER.info("Trying J2534 connection...");
            return new J2534ConnectionManager(connectionProperties);
        } catch (Throwable t) {
            LOGGER.info("J2534 connection not available [" + t.getClass().getName() + ": " + t.getMessage() + "], trying serial connection...");
            return new SerialConnectionManager(portName, connectionProperties);
        }
    }
}
