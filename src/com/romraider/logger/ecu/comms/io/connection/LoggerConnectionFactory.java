/*
 *
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2008 RomRaider.com
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
 *
 */

package com.romraider.logger.ecu.comms.io.connection;

import com.romraider.io.connection.ConnectionManager;
import com.romraider.io.connection.ConnectionProperties;
import com.romraider.io.j2534.api.J2534ConnectionManager;
import com.romraider.io.serial.connection.SerialConnectionManager;
import com.romraider.logger.ecu.exception.UnsupportedProtocolException;
import org.apache.log4j.Logger;
import static org.apache.log4j.Logger.getLogger;

public final class LoggerConnectionFactory {
    private static final Logger LOGGER = getLogger(LoggerConnectionFactory.class);

    private LoggerConnectionFactory() {
    }

    public static LoggerConnection getConnection(String protocolName, String portName, ConnectionProperties connectionProperties) {
        ConnectionManager manager = getManager(portName, connectionProperties);
        return instantiateConnection(protocolName, manager);
    }

    private static ConnectionManager getManager(String portName, ConnectionProperties connectionProperties) {
        try {
            return new J2534ConnectionManager(connectionProperties);
        } catch (Exception e) {
            LOGGER.info("J2534 connection not available [" + e.getMessage() + "], trying serial connection...");
            return new SerialConnectionManager(portName, connectionProperties);
        }
    }

    private static LoggerConnection instantiateConnection(String protocolName, ConnectionManager manager) {
        try {
            Class<?> cls = Class.forName(LoggerConnectionFactory.class.getPackage().getName() + "." + protocolName + "LoggerConnection");
            return (LoggerConnection) cls.getConstructor(ConnectionManager.class).newInstance(manager);
        } catch (Exception e) {
            throw new UnsupportedProtocolException("'" + protocolName + "' is not a supported protocol", e);
        }
    }
}
