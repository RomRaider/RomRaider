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

package com.romraider.logger.external.aem.io;

import com.romraider.io.serial.connection.SerialConnection;
import com.romraider.io.serial.connection.SerialConnectionImpl;
import com.romraider.logger.external.aem.plugin.AemDataItem;
import com.romraider.logger.external.core.Stoppable;
import static com.romraider.util.ParamChecker.isNullOrEmpty;
import org.apache.log4j.Logger;
import static org.apache.log4j.Logger.getLogger;

public final class AemRunner implements Stoppable {
    private static final Logger LOGGER = getLogger(AemRunner.class);
    private static final AemConnectionProperties CONNECTION_PROPS = new AemConnectionProperties();
    private final SerialConnection connection;
    private final AemDataItem dataItem;
    private boolean stop;

    public AemRunner(String port, AemDataItem dataItem) {
        this.connection = new SerialConnectionImpl(port, CONNECTION_PROPS);
        this.dataItem = dataItem;
    }

    @Override
    public void run() {
        try {
            while (!stop) {
                final String response = connection.readLine();
                if (LOGGER.isTraceEnabled())
                    LOGGER.trace("AEM UEGO AFR Response: " + response);
                if (!isNullOrEmpty(response)) dataItem.setData(parseDouble(response));
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
            final String[] substr = value.split("\t");
            final double result = Double.parseDouble(substr[0]);
            return result;
        } catch (Exception e) {
            return 0.0;
        }
    }
}
