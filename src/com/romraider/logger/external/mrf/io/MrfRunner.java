/*
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2010 RomRaider.com
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

import com.romraider.io.connection.ConnectionProperties;
import com.romraider.logger.external.core.Stoppable;
import com.romraider.logger.external.core.TerminalConnection;
import com.romraider.logger.external.core.TerminalConnectionImpl;
import com.romraider.logger.external.mrf.plugin.MrfDataItem;
import com.romraider.logger.external.mrf.plugin.MrfSensorType;
import static com.romraider.util.ParamChecker.isNullOrEmpty;
import static java.nio.charset.Charset.forName;
import java.nio.charset.Charset;
import java.util.Map;

public final class MrfRunner implements Stoppable {
    private static final ConnectionProperties CONNECTION_PROPS = new MrfConnectionProperties();
    private static final Charset CHARSET_UTF8 = forName("UTF-8");
    private final Map<MrfSensorType, MrfDataItem> dataItems;
    private final TerminalConnection connection;
    private boolean stop;

    public MrfRunner(String port, Map<MrfSensorType, MrfDataItem> dataItems) {
        this.connection = new TerminalConnectionImpl("Mrf Stealth Gauge", port, CONNECTION_PROPS);
        this.dataItems = dataItems;
    }

    public void run() {
        try {
            while (!stop) {
                byte[] bytes = connection.read();
                String response = new String(bytes, CHARSET_UTF8);
                if (isNullOrEmpty(response)) continue;
                String[] values = response.split(",");
                for (int i = 0; i < values.length; i++) {
                    MrfDataItem dataItem = dataItems.get(MrfSensorType.valueOf(i));
                    if (dataItem != null) dataItem.setData(parseDouble(values[i]));
                }
            }
        } finally {
            connection.close();
        }
    }

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