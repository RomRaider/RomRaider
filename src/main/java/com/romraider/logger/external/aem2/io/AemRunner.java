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

package com.romraider.logger.external.aem2.io;

import com.romraider.io.serial.connection.SerialConnection;
import com.romraider.io.serial.connection.SerialConnectionImpl;
import com.romraider.logger.external.aem2.plugin.AemDataItem;
import com.romraider.logger.external.core.Stoppable;
import static com.romraider.util.ParamChecker.isNullOrEmpty;
import org.apache.log4j.Logger;
import static org.apache.log4j.Logger.getLogger;

public final class AemRunner implements Stoppable {
    private static final Logger LOGGER = getLogger(AemRunner.class);
    private static final AemConnectionProperties CONNECTION_PROPS = new AemConnectionProperties();
    private static final String TAB = "\t";
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
                    LOGGER.trace("AEM UEGO Lambda Response: " + response);
                if (!isNullOrEmpty(response)) dataItem.setData(parseString(response));
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

    /*
     * http://www.romraider.com/forum/viewtopic.php?f=14&t=7207
     * The new version uses a baud rate of 19200 and outputs data
     * in the form "1.000\tReady\tNo-errors\r"
     * where 1.000 is the lambda value and the two other strings
     * are status values. The values are separated by a tab and
     * terminated with a carriage return. I believe the old version
     * terminated with a carriage return and line feed.  This should
     * work for either case.
     */
    private double parseString(String value){
        try{
            final String[] substr = value.split(TAB);
            final double result = Double.parseDouble(substr[0]);
            return result;
        }
        catch (Exception e){
            return 0.0;
        }
    }
}
