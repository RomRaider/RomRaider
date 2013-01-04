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

package com.romraider.ramtune.test.command.executor;

import com.romraider.io.connection.ConnectionManager;
import com.romraider.io.connection.ConnectionProperties;

import static com.romraider.io.connection.ConnectionManagerFactory.getManager;
import static com.romraider.util.ParamChecker.checkNotNull;
import static com.romraider.util.ParamChecker.checkNotNullOrEmpty;

public final class CommandExecutorImpl implements CommandExecutor {
    private final ConnectionProperties connectionProperties;
    private final String port;

    public CommandExecutorImpl(ConnectionProperties connectionProperties, String port) {
        checkNotNull(connectionProperties);
        checkNotNullOrEmpty(port, "port");
        this.connectionProperties = connectionProperties;
        this.port = port;
    }

    public byte[] executeCommand(byte[] command) {
        ConnectionManager connectionManager = getManager(port, connectionProperties);
        try {
            return connectionManager.send(command);
        } finally {
            connectionManager.close();
        }
    }
}
