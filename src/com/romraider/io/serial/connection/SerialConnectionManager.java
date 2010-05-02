/*
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2009 RomRaider.com
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

package com.romraider.io.serial.connection;

import com.romraider.io.connection.ConnectionManager;
import com.romraider.io.connection.ConnectionProperties;
import static com.romraider.util.HexUtil.asHex;
import static com.romraider.util.ParamChecker.checkNotNull;
import static com.romraider.util.ParamChecker.checkNotNullOrEmpty;
import static com.romraider.util.ThreadUtil.sleep;
import static java.lang.System.currentTimeMillis;
import org.apache.log4j.Logger;
import static org.apache.log4j.Logger.getLogger;

public final class SerialConnectionManager implements ConnectionManager {
    private static final Logger LOGGER = getLogger(SerialConnectionManager.class);
    private final SerialConnection connection;

    public SerialConnectionManager(String portName, ConnectionProperties connectionProperties) {
        checkNotNullOrEmpty(portName, "portName");
        checkNotNull(connectionProperties, "connectionProperties");
        // Use TestSerialConnection for testing!!
        connection = new SerialConnectionImpl(portName, connectionProperties);
//        connection = new TestSerialConnection(portName, connectionProperties);
        LOGGER.info("Serial connection initialised");
    }

    // Send request and wait for response with known length
    public void send(byte[] request, byte[] response, long timeout) {
        checkNotNull(request, "request");
        checkNotNull(request, "response");
        connection.readStaleData();
        connection.write(request);
        while (connection.available() < response.length) {
            sleep(1);
            timeout -= 1;
            if (timeout <= 0) {
                byte[] badBytes = connection.readAvailable();
                LOGGER.debug("Bad response (read timeout): " + asHex(badBytes));
                break;
            }
        }
        connection.read(response);
    }

    // Send request and wait specified time for response with unknown length
    public byte[] send(byte[] bytes, long maxWait) {
        checkNotNull(bytes, "bytes");
        connection.readStaleData();
        connection.write(bytes);
        int available = 0;
        boolean keepLooking = true;
        long lastChange = currentTimeMillis();
        while (keepLooking) {
            sleep(2);
            if (connection.available() != available) {
                available = connection.available();
                lastChange = currentTimeMillis();
            }
            keepLooking = (currentTimeMillis() - lastChange) < maxWait;
        }
        return connection.readAvailable();
    }

    public void close() {
        connection.close();
    }
}
