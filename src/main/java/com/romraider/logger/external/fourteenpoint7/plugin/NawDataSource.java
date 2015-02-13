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

package com.romraider.logger.external.fourteenpoint7.plugin;

import static com.romraider.logger.external.core.SensorConversionsLambda.AFR_146;
import static com.romraider.logger.external.core.SensorConversionsLambda.AFR_147;
import static com.romraider.logger.external.core.SensorConversionsLambda.AFR_155;
import static com.romraider.logger.external.core.SensorConversionsLambda.AFR_172;
import static com.romraider.logger.external.core.SensorConversionsLambda.AFR_34;
import static com.romraider.logger.external.core.SensorConversionsLambda.AFR_64;
import static com.romraider.logger.external.core.SensorConversionsLambda.AFR_90;
import static com.romraider.logger.external.core.SensorConversionsLambda.LAMBDA;
import static com.romraider.util.ThreadUtil.runAsDaemon;
import static java.util.Arrays.asList;

import java.util.List;
import java.util.Properties;

import javax.swing.Action;

import com.romraider.logger.ecu.EcuLogger;
import com.romraider.logger.external.core.ExternalDataItem;
import com.romraider.logger.external.core.ExternalDataSource;
import com.romraider.logger.external.fourteenpoint7.io.NawRunner;

public final class NawDataSource implements ExternalDataSource {
    private NawDataItem dataItem = new NawDataItem(LAMBDA, AFR_147, AFR_90, AFR_146, AFR_64, AFR_155, AFR_172, AFR_34);
    private NawRunner runner;
    private String port;

    public String getId() {
        return getClass().getName();
    }

    public String getName() {
        return "14Point7 NAW_7S UEGO";
    }

    public String getVersion() {
        return "0.03";
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
    }

    public void connect() {
        runner = new NawRunner(port, dataItem);
        runAsDaemon(runner);
    }

    public void disconnect() {
        if (runner != null) runner.stop();
    }
}
