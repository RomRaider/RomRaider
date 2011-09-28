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

import static com.romraider.util.ThreadUtil.runAsDaemon;
import static java.lang.Integer.parseInt;
import static java.util.Collections.unmodifiableList;
import static org.apache.log4j.Logger.getLogger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.Action;

import org.apache.log4j.Logger;

import com.romraider.logger.ecu.EcuLogger;
import com.romraider.logger.external.core.ExternalDataItem;
import com.romraider.logger.external.core.ExternalDataSource;
import com.romraider.logger.external.innovate.generic.mts.io.MTSConnector;
import com.romraider.logger.external.innovate.generic.mts.io.MTSRunner;

public final class Lm2MtsDataSource implements ExternalDataSource {
    private static final Logger LOGGER = getLogger(Lm2MtsDataSource.class);
    private final Map<Integer, Lm2MtsDataItem> dataItems = new HashMap<Integer, Lm2MtsDataItem>();
    private MTSRunner runner;
    private int mtsPort = 0;

    {
    	MTSConnector mts = new MTSConnector(mtsPort);
    	Set<Lm2Sensor> sensors = mts.getSensors();
    	dataItems.put(0, new Lm2MtsDataItem("LM-2", 0, "AFR")); // a default entry
    	for (Lm2Sensor sensor : sensors) {
    		dataItems.put(sensor.getInputNumber(), new Lm2MtsDataItem(sensor.getDeviceName(), sensor.getDeviceChannel(), sensor.getUnits()));
    	}
//        dataItems.put(LC_1_0, new Lm2MtsDataItem("LM-2", 0, LAMBDA, AFR_147, AFR_90, AFR_146, AFR_64, AFR_155, AFR_172, AFR_34));
//        dataItems.put(TC_4_1, new Lm2MtsDataItem("TC-4", 1, DEG_F));
//        dataItems.put(TC_4_2, new Lm2MtsDataItem("TC-4", 2, DEG_C));
//        dataItems.put(TC_4_3, new Lm2MtsDataItem("TC-4", 3, DEG_C));
//        dataItems.put(TC_4_4, new Lm2MtsDataItem("TC-4", 4, DEG_C));
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