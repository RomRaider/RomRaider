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

package com.romraider.logger.innovate.generic.serial.io;

import com.romraider.io.connection.ConnectionProperties;
import com.romraider.io.serial.connection.SerialConnection;
import com.romraider.io.serial.connection.SerialConnectionImpl;
import com.romraider.logger.ecu.exception.SerialCommunicationException;
import static com.romraider.util.ByteUtil.matchOnes;
import static com.romraider.util.ByteUtil.matchZeroes;
import static com.romraider.util.HexUtil.asBytes;
import static com.romraider.util.HexUtil.asHex;
import static com.romraider.util.ParamChecker.checkGreaterThanZero;
import static com.romraider.util.ParamChecker.checkNotNull;
import static com.romraider.util.ParamChecker.checkNotNullOrEmpty;
import static com.romraider.util.ThreadUtil.sleep;
import org.apache.log4j.Logger;
import static org.apache.log4j.Logger.getLogger;
import java.io.IOException;
import static java.lang.Math.min;
import static java.lang.System.arraycopy;
import static java.lang.System.currentTimeMillis;

public final class InnovateConnectionImpl implements InnovateConnection {
    private static final Logger LOGGER = getLogger(InnovateConnectionImpl.class);
    private static final byte[] INNOVATE_HEADER = asBytes("0xB280");
    private final SerialConnection connection;
    private final int responseLength;
    private final long sendTimeout;
    private final String device;

    public InnovateConnectionImpl(String device, String portName, ConnectionProperties connectionProperties, int responseLength) {
        checkNotNullOrEmpty(device, "device");
        checkNotNullOrEmpty(portName, "portName");
        checkNotNull(connectionProperties, "connectionProperties");
        checkGreaterThanZero(responseLength, "responseLength");
        this.device = device;
        this.sendTimeout = connectionProperties.getSendTimeout();
        this.responseLength = responseLength;
        connection = new SerialConnectionImpl(portName, connectionProperties);
    }

    // FIX - YIKES!!
    public byte[] read() {
        try {
            connection.readStaleData();
            byte[] response = new byte[responseLength];
            int bufferLength = responseLength + INNOVATE_HEADER.length - 1;
            long start = currentTimeMillis();
            while (currentTimeMillis() - start <= sendTimeout) {
                sleep(1);
                int available = connection.available();
                if (available < bufferLength) continue;
                byte[] buffer = new byte[bufferLength];
                connection.read(buffer);
                LOGGER.trace(device + " input: " + asHex(buffer));
                int responseBeginIndex = 0;
                int bufferBeginIndex = findHeader(buffer);
                if (bufferBeginIndex < 0) {
                    bufferBeginIndex = findLm1(buffer);
                    if (bufferBeginIndex < 0) continue;
                    LOGGER.trace(device + ": v1 protocol found - appending header...");
                    arraycopy(INNOVATE_HEADER, 0, response, 0, INNOVATE_HEADER.length);
                    responseBeginIndex = INNOVATE_HEADER.length;
                }
                int tailLength = responseLength - responseBeginIndex;
                arraycopy(buffer, bufferBeginIndex, response, responseBeginIndex, min(tailLength, (buffer.length - bufferBeginIndex)));
                int remainderLength = tailLength - (buffer.length - bufferBeginIndex);
                if (remainderLength > 0) {
                    byte[] remainder = remainder(remainderLength, start);
                    if (remainder.length == 0) continue;
                    arraycopy(remainder, 0, response, responseLength - remainderLength, remainderLength);
                }
                LOGGER.trace(device + " Response: " + asHex(response));
                return response;
            }
            LOGGER.warn(device + " Response [read timeout]");
            return new byte[0];
        } catch (Exception e) {
            close();
            throw new SerialCommunicationException(e);
        }
    }

    private byte[] remainder(int remainderLength, long start) throws IOException {
        while (currentTimeMillis() - start <= sendTimeout) {
            sleep(1);
            int available = connection.available();
            if (available >= remainderLength) {
                byte[] remainder = new byte[remainderLength];
                connection.read(remainder);
                return remainder;
            }
        }
        return new byte[0];
    }

    public void close() {
        connection.close();
    }

    private int findHeader(byte[] bytes) {
        for (int i = 0; i < bytes.length - 1; i++) {
            if (matchOnes(bytes[i], 178) && matchOnes(bytes[i + 1], 128)) return i;
        }
        return -1;
    }

    private int findLm1(byte[] bytes) {
        for (int i = 0; i < bytes.length; i++) {
            if (matchOnes(bytes[i], 128) && matchZeroes(bytes[i], 34)) return i;
        }
        return -1;
    }
}
