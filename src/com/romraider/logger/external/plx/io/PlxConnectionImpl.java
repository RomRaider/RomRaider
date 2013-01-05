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

package com.romraider.logger.external.plx.io;

import com.romraider.io.connection.ConnectionProperties;
import com.romraider.io.serial.connection.SerialConnection;
import com.romraider.io.serial.connection.SerialConnectionImpl;
import static com.romraider.util.ParamChecker.checkNotNullOrEmpty;

public final class PlxConnectionImpl implements PlxConnection {
    private final SerialConnection connection;

    public PlxConnectionImpl(String port) {
        checkNotNullOrEmpty(port, "port");
        connection = serialConnection(port);
//        connection = new TestPlxConnection();
    }

    public byte readByte() {
        return (byte) connection.read();
    }

    public void close() {
        connection.close();
    }

    private SerialConnectionImpl serialConnection(String port) {
        ConnectionProperties connectionProperties = new PlxConnectionProperties();
        return new SerialConnectionImpl(port, connectionProperties);
    }
}