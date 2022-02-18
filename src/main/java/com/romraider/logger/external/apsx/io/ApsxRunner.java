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

package com.romraider.logger.external.apsx.io;

import static org.apache.log4j.Logger.getLogger;

import org.apache.log4j.Logger;

import com.romraider.io.connection.ConnectionProperties;
import com.romraider.io.serial.connection.SerialConnection;
import com.romraider.io.serial.connection.SerialConnectionImpl;
import com.romraider.logger.external.apsx.plugin.ApsxDataItem;
import com.romraider.logger.external.core.Stoppable;

public final class ApsxRunner implements Stoppable {
    private static final Logger LOGGER = getLogger(ApsxRunner.class);
    private final SerialConnection connection;
    private final ApsxDataItem dataItem;
    private boolean stop;

    public ApsxRunner(String port, ApsxDataItem dataItem, ConnectionProperties properties) {
        this.connection = new SerialConnectionImpl(port, properties);
        this.dataItem = dataItem;
    }

    @Override
    public void run() {
        try {
            while (!stop) {
                final int response = connection.read();
                if (LOGGER.isTraceEnabled())
                    LOGGER.trace("APSX AFR Response: " + response);
                if (response != -1) dataItem.setData(response / 10.0);
            }
        } catch (Throwable t) {
            LOGGER.error("Error occurred", t);
        } finally {
            connection.close();
        }
    }

    @Override
    public void stop() {
        stop = true;
    }
}
