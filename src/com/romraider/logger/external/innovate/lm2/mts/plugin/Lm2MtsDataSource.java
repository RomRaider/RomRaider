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

package com.romraider.logger.external.innovate.lm2.mts.plugin;

import static com.romraider.logger.external.core.SensorConversionsLambda.AFR_146;
import static com.romraider.logger.external.core.SensorConversionsLambda.AFR_147;
import static com.romraider.logger.external.core.SensorConversionsLambda.AFR_155;
import static com.romraider.logger.external.core.SensorConversionsLambda.AFR_172;
import static com.romraider.logger.external.core.SensorConversionsLambda.AFR_34;
import static com.romraider.logger.external.core.SensorConversionsLambda.AFR_64;
import static com.romraider.logger.external.core.SensorConversionsLambda.AFR_90;
import static com.romraider.logger.external.core.SensorConversionsLambda.LAMBDA;
import static com.romraider.logger.external.core.SensorConversionsOther.VOLTS_DC;
import static com.romraider.logger.external.innovate.lm2.mts.plugin.Lm2SensorType.THERMALCOUPLE1;
import static com.romraider.logger.external.innovate.lm2.mts.plugin.Lm2SensorType.THERMALCOUPLE2;
import static com.romraider.logger.external.innovate.lm2.mts.plugin.Lm2SensorType.THERMALCOUPLE3;
import static com.romraider.logger.external.innovate.lm2.mts.plugin.Lm2SensorType.THERMALCOUPLE4;
import static com.romraider.logger.external.innovate.lm2.mts.plugin.Lm2SensorType.WIDEBAND0;
import static com.romraider.util.ThreadUtil.runAsDaemon;
import static java.lang.Integer.parseInt;
import static java.util.Collections.unmodifiableList;
import static org.apache.log4j.Logger.getLogger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Action;

import org.apache.log4j.Logger;

import com.romraider.logger.ecu.EcuLogger;
import com.romraider.logger.external.core.ExternalDataItem;
import com.romraider.logger.external.core.ExternalDataSource;
import com.romraider.logger.external.innovate.generic.mts.io.MTSRunner;

public final class Lm2MtsDataSource implements ExternalDataSource {
    private static final Logger LOGGER = getLogger(Lm2MtsDataSource.class);
    private final Map<Lm2SensorType, Lm2MtsDataItem> dataItems = new HashMap<Lm2SensorType, Lm2MtsDataItem>();
    private MTSRunner runner;
    private int mtsPort = -1;

    {
        dataItems.put(WIDEBAND0, new Lm2MtsDataItem("LM-2", 0, LAMBDA, AFR_147, AFR_90, AFR_146, AFR_64, AFR_155, AFR_172, AFR_34));
        dataItems.put(THERMALCOUPLE1, new Lm2MtsDataItem("TC-4", 1, VOLTS_DC));
        dataItems.put(THERMALCOUPLE2, new Lm2MtsDataItem("TC-4", 2, VOLTS_DC));
        dataItems.put(THERMALCOUPLE3, new Lm2MtsDataItem("TC-4", 3, VOLTS_DC));
        dataItems.put(THERMALCOUPLE4, new Lm2MtsDataItem("TC-4", 4, VOLTS_DC));
    }

    public String getId() {
        return getClass().getName();
    }

    public String getName() {
        return "Innovate MTS";
    }

    public String getVersion() {
        return "0.04";
    }

    public List<? extends ExternalDataItem> getDataItems() {
    	return unmodifiableList(new ArrayList<Lm2MtsDataItem>(dataItems.values()));
    }

    public Action getMenuAction(EcuLogger logger) {
        return new Lm2MtsPluginMenuAction(logger, this);
    }

    public void setPort(String port) {
        mtsPort = mtsPort(port);
    }

    public String getPort() {
        return "" + mtsPort;
    }

    public void connect() {
        runner = new MTSRunner(mtsPort, dataItems);
        runAsDaemon(runner);
    }

    public void disconnect() {
        if (runner != null) runner.stop();
    }

    private int mtsPort(String port) {
        try {
            return parseInt(port);
        } catch (Exception e) {
            LOGGER.warn("Bad MTS port: " + port);
            return -1;
        }
    }
}