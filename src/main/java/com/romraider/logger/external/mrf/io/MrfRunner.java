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

package com.romraider.logger.external.mrf.io;

import static com.romraider.Settings.COMMA;
import com.romraider.io.connection.ConnectionProperties;
import com.romraider.io.serial.connection.SerialConnection;
import com.romraider.io.serial.connection.SerialConnectionImpl;
import com.romraider.logger.external.core.Stoppable;
import com.romraider.logger.external.mrf.plugin.MrfDataItem;
import com.romraider.logger.external.mrf.plugin.MrfSensorType;
import static com.romraider.util.ParamChecker.isNullOrEmpty;
import org.apache.log4j.Logger;
import static org.apache.log4j.Logger.getLogger;
import java.util.Map;

public final class MrfRunner implements Stoppable {
    private static final Logger LOGGER = getLogger(MrfRunner.class);
    private static final ConnectionProperties CONNECTION_PROPS = new MrfConnectionProperties();
    private final Map<MrfSensorType, MrfDataItem> dataItems;
    private final SerialConnection connection;
    private boolean stop;

    public MrfRunner(String port, Map<MrfSensorType, MrfDataItem> dataItems) {
        this.connection = new SerialConnectionImpl(port, CONNECTION_PROPS);
        this.dataItems = dataItems;
    }

    @Override
    public void run() {
        try {
            while (!stop) {
                String response = connection.readLine();
                if (isNullOrEmpty(response)) continue;
                if (LOGGER.isTraceEnabled())
                    LOGGER.trace("MRF Stealth Gauge Response: " + response);
                String[] values = response.split(COMMA);
                for (int i = 0; i < values.length; i++) {
                    MrfDataItem dataItem = dataItems.get(MrfSensorType.valueOf(i));
                    if (dataItem != null) dataItem.setData(parseDouble(values[i]));
                }
            }
            connection.close();
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

    private double parseDouble(String value) {
        try {
            return Double.parseDouble(value);
        } catch (Exception e) {
            return 0.0;
        }
    }
}