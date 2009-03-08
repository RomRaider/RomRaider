/*
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
 */

package com.romraider.logger.aem.io;

import com.romraider.io.connection.ConnectionProperties;
import com.romraider.io.serial.connection.SerialConnection;
import com.romraider.io.serial.connection.SerialConnectionImpl;
import com.romraider.logger.ecu.exception.SerialCommunicationException;
import static com.romraider.util.HexUtil.asHex;
import static com.romraider.util.ParamChecker.checkNotNull;
import static com.romraider.util.ParamChecker.checkNotNullOrEmpty;
import static com.romraider.util.ThreadUtil.sleep;
import org.apache.log4j.Logger;
import static org.apache.log4j.Logger.getLogger;
import static java.lang.System.currentTimeMillis;
import java.util.ArrayList;
import java.util.List;

public final class AemConnectionImpl implements AemConnection {
    private static final Logger LOGGER = getLogger(AemConnectionImpl.class);
    private final SerialConnection connection;
    private final long sendTimeout;

    public AemConnectionImpl(String portName, ConnectionProperties connectionProperties) {
        checkNotNull(connectionProperties, "connectionProperties");
        checkNotNullOrEmpty(portName, "portName");
        this.sendTimeout = connectionProperties.getSendTimeout();
        connection = new SerialConnectionImpl(portName, connectionProperties);
    }

    //TODO: This a guess!!...untested!!
    public byte[] read() {
        try {
            connection.readStaleData();
            long start = currentTimeMillis();
            while (currentTimeMillis() - start <= sendTimeout) {
                if (connection.available() > 10) {
                    byte[] bytes = connection.readAvailable();
                    LOGGER.trace("AEM UEGO input: " + asHex(bytes));
                    int startIndex = findStart(bytes);
                    LOGGER.trace("AEM UEGO start index: " + startIndex);
                    if (startIndex < 0 || startIndex >= bytes.length) continue;
                    List<Byte> buffer = new ArrayList<Byte>();
                    for (int i = startIndex; i < bytes.length; i++) {
                        byte b = bytes[i];
                        if (b == (byte) 0x0D) {
                            byte[] response = toArray(buffer);
                            LOGGER.trace("AEM UEGO Response: " + asHex(response));
                            return response;
                        } else {
                            buffer.add(b);
                        }
                    }
                }
                sleep(1);
            }
            LOGGER.warn("AEM UEGO Response [read timeout]");
            return new byte[0];
        } catch (Exception e) {
            close();
            throw new SerialCommunicationException(e);
        }
    }

    private int findStart(byte[] bytes) {
        for (int i = 0; i < bytes.length; i++) {
            if (bytes[i] == (byte) 0x0D) return i + 1;
        }
        return -1;
    }

    public void close() {
        connection.close();
    }

    private byte[] toArray(List<Byte> buffer) {
        byte[] result = new byte[buffer.size()];
        for (int j = 0; j < buffer.size(); j++) {
            result[j] = buffer.get(j);
        }
        return result;
    }
}
