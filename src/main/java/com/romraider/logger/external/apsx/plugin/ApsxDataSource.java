/*
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2015 RomRaider.com
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

package com.romraider.logger.external.apsx.plugin;

import static com.romraider.logger.external.core.SensorConversionsAFR.AFR_146;
import static com.romraider.logger.external.core.SensorConversionsAFR.AFR_147;
import static com.romraider.logger.external.core.SensorConversionsAFR.AFR_155;
import static com.romraider.logger.external.core.SensorConversionsAFR.AFR_172;
import static com.romraider.logger.external.core.SensorConversionsAFR.AFR_34;
import static com.romraider.logger.external.core.SensorConversionsAFR.AFR_64;
import static com.romraider.logger.external.core.SensorConversionsAFR.AFR_90;
import static com.romraider.logger.external.core.SensorConversionsAFR.LAMBDA;
import static com.romraider.util.ThreadUtil.runAsDaemon;
import static java.util.Arrays.asList;

import java.util.List;
import java.util.Properties;

import javax.swing.Action;

import com.romraider.io.connection.SerialConnectionProperties;
import com.romraider.logger.ecu.EcuLogger;
import com.romraider.logger.external.apsx.io.ApsxRunner;
import com.romraider.logger.external.core.ExternalDataItem;
import com.romraider.logger.external.core.ExternalDataSource;

public final class ApsxDataSource implements ExternalDataSource {
    private SerialConnectionProperties connectionProperties;
    private ApsxDataItem dataItem = new ApsxDataItem(AFR_147, LAMBDA, AFR_90, AFR_146, AFR_64, AFR_155, AFR_172, AFR_34);
    private ApsxRunner runner;
    private String port;

    public String getId() {
        return getClass().getName();
    }

    public String getName() {
        return "APSX D1|D2 WBO2";
    }

    public String getVersion() {
        return "0.01";
    }

    public List<? extends ExternalDataItem> getDataItems() {
        return asList(dataItem);
    }

    public Action getMenuAction(EcuLogger logger) {
        return null;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getPort() {
        return port;
    }

    public void setProperties(Properties properties) {
        connectionProperties = new SerialConnectionProperties(
                Integer.parseInt(properties.getProperty("datasource.baudrate")),
                Integer.parseInt(properties.getProperty("datasource.databits")),
                Integer.parseInt(properties.getProperty("datasource.stopbits")),
                Integer.parseInt(properties.getProperty("datasource.parity")),
                2000, 500
        );
    }

    public void connect() {
        runner = new ApsxRunner(port, dataItem, connectionProperties);
        runAsDaemon(runner);
    }

    public void disconnect() {
        if (runner != null) runner.stop();
    }
}
