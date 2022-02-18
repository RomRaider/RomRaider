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

package com.romraider.logger.external.fourteenpoint7.io;

import com.romraider.io.connection.ConnectionProperties;
import com.romraider.io.serial.connection.SerialConnection;
import com.romraider.io.serial.connection.SerialConnectionImpl;
import com.romraider.logger.ecu.exception.SerialCommunicationException;
import static com.romraider.util.HexUtil.asHex;
import static com.romraider.util.ParamChecker.checkNotNullOrEmpty;
import org.apache.log4j.Logger;
import static org.apache.log4j.Logger.getLogger;

public final class NawConnectionImpl implements NawConnection {
    private static final Logger LOGGER = getLogger(NawConnectionImpl.class);
    private final SerialConnection connection;

    public NawConnectionImpl(String port) {
        checkNotNullOrEmpty(port, "port");
        connection = serialConnection(port);
    }

    @Override
    public byte[] readBytes() {
        try {
            byte[] bytes = new byte[9];
            connection.read(bytes);
            if (LOGGER.isTraceEnabled())
                LOGGER.trace("NAW_7S Response: " + asHex(bytes));
            return bytes;
        } catch (Exception e) {
            close();
            throw new SerialCommunicationException(e);
        }
    }

    @Override
    public void write(byte[] bytes) {
        try {
            connection.write(bytes);
        } catch (Exception e) {
            close();
            throw new SerialCommunicationException(e);
        }
    }

    @Override
    public void close() {
        connection.close();
    }

    private SerialConnectionImpl serialConnection(String port) {
        ConnectionProperties connectionProperties = new NawConnectionProperties();
        return new SerialConnectionImpl(port, connectionProperties);
    }
}
