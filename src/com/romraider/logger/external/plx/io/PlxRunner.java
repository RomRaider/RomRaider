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

import com.romraider.logger.external.core.Stoppable;

import static com.romraider.logger.external.plx.plugin.PlxSensorType.UNKNOWN;

import com.romraider.logger.external.plx.plugin.PlxDataItem;
import com.romraider.logger.external.plx.plugin.PlxSensorType;

import org.apache.log4j.Logger;
import static org.apache.log4j.Logger.getLogger;
import java.util.Map;

public final class PlxRunner implements Stoppable {
    private static final Logger LOGGER = getLogger(PlxRunner.class);
    private final Map<PlxSensorType, PlxDataItem> dataItems;
    private final PlxConnection connection;
    private boolean stop;

    public PlxRunner(String port, Map<PlxSensorType, PlxDataItem> dataItems) {
        this.connection = new PlxConnectionImpl(port);
        this.dataItems = dataItems;
    }

    public void run() {
        try {
            PlxParser parser = new PlxParserImpl();
            while (!stop) {
                byte b = connection.readByte();
                PlxResponse response = parser.pushByte(b);
                if (!isValid(response)) continue;
                PlxDataItem item = dataItems.get(response.sensor);
                if (item != null && (response.instance == item.getInstance())) {
                    item.setRaw(response.value);
                }
            }
            connection.close();
        } catch (Throwable t) {
            LOGGER.error("Error occurred", t);
        } finally {
            connection.close();
        }
    }

    public void stop() {
        stop = true;
    }

    private boolean isValid(PlxResponse response) {
        if (response == null) return false;
        return response.sensor != UNKNOWN;
    }
}