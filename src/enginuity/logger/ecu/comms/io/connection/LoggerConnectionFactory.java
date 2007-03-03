/*
 *
 * Enginuity Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006 Enginuity.org
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

package enginuity.logger.ecu.comms.io.connection;

import enginuity.io.connection.ConnectionProperties;
import enginuity.logger.ecu.exception.UnsupportedProtocolException;

public final class LoggerConnectionFactory {
    private static final LoggerConnectionFactory INSTANCE = new LoggerConnectionFactory();

    public static LoggerConnectionFactory getInstance() {
        return INSTANCE;
    }

    private LoggerConnectionFactory() {
    }

    public LoggerConnection getLoggerConnection(String protocolName, String portName, ConnectionProperties connectionProperties) {
        try {
            Class<?> cls = Class.forName(this.getClass().getPackage().getName() + "." + protocolName + "LoggerConnection");
            return (LoggerConnection) cls.getConstructor(String.class, ConnectionProperties.class).newInstance(portName, connectionProperties);
        } catch (Exception e) {
            throw new UnsupportedProtocolException("'" + protocolName + "' is not a supported protocol", e);
        }
    }
}
