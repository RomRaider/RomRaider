/*
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2010 RomRaider.com
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

package com.romraider.logger.external.core;

import com.romraider.io.connection.ConnectionProperties;
import com.romraider.io.serial.connection.SerialConnection;
import com.romraider.io.serial.connection.SerialConnectionImpl;
import com.romraider.logger.ecu.exception.SerialCommunicationException;
import static com.romraider.util.ParamChecker.checkNotNull;
import static com.romraider.util.ParamChecker.checkNotNullOrEmpty;
import static java.nio.charset.Charset.forName;
import org.apache.log4j.Logger;
import static org.apache.log4j.Logger.getLogger;
import java.nio.charset.Charset;

public final class TerminalConnectionImpl implements TerminalConnection {
    private static final Logger LOGGER = getLogger(TerminalConnectionImpl.class);
    private static final Charset CHARSET_UTF8 = forName("UTF-8");
    private final SerialConnection connection;
    private final String deviceName;

    public TerminalConnectionImpl(String deviceName, String port, ConnectionProperties connectionProperties) {
        checkNotNullOrEmpty(deviceName, "deviceName");
        checkNotNullOrEmpty(port, "port");
        checkNotNull(connectionProperties, "connectionProperties");
        this.connection = new SerialConnectionImpl(port, connectionProperties);
        this.deviceName = deviceName;
    }

    public byte[] read() {
        try {
            String s = connection.readLine();
            LOGGER.trace(deviceName + " Response: " + s);
            return s.getBytes(CHARSET_UTF8);
        } catch (Exception e) {
            close();
            throw new SerialCommunicationException(e);
        }
    }

    public void close() {
        connection.close();
    }
}
