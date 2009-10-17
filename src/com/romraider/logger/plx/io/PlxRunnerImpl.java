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

package com.romraider.logger.plx.io;

import static com.romraider.logger.plx.io.PlxSensorType.UNKNOWN;
import com.romraider.logger.plx.plugin.PlxDataItem;
import com.romraider.logger.plx.plugin.PlxSettings;
import java.util.Map;

public final class PlxRunnerImpl implements PlxRunner {
    private final Map<PlxSensorType, PlxDataItem> dataItems;
    private final PlxConnection connection;
    private boolean stop;

    public PlxRunnerImpl(PlxSettings plxSettings, Map<PlxSensorType, PlxDataItem> dataItems) {
        connection = new PlxConnectionImpl(plxSettings.getPort(), new PlxConnectionProperties());
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
                item.setRaw(response.value);
            }
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