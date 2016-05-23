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

package com.romraider.logger.external.aem.xwifi.plugin;

import static com.romraider.logger.external.aem.xwifi.plugin.AemSensorType.EGT1;
import static com.romraider.logger.external.aem.xwifi.plugin.AemSensorType.EGT2;
import static com.romraider.logger.external.aem.xwifi.plugin.AemSensorType.UEGO;
import static com.romraider.logger.external.core.SensorConversionsLambda.AFR_146;
import static com.romraider.logger.external.core.SensorConversionsLambda.AFR_147;
import static com.romraider.logger.external.core.SensorConversionsLambda.AFR_155;
import static com.romraider.logger.external.core.SensorConversionsLambda.AFR_172;
import static com.romraider.logger.external.core.SensorConversionsLambda.AFR_34;
import static com.romraider.logger.external.core.SensorConversionsLambda.AFR_64;
import static com.romraider.logger.external.core.SensorConversionsLambda.AFR_90;
import static com.romraider.logger.external.core.SensorConversionsLambda.LAMBDA;
import static com.romraider.logger.external.core.SensorConversionsOther.EXHAUST_DEG_F;
import static com.romraider.logger.external.core.SensorConversionsOther.EXHAUST_DEG_F2C;
import static com.romraider.util.ThreadUtil.runAsDaemon;
import static java.util.Collections.unmodifiableList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.swing.Action;

import com.romraider.logger.ecu.EcuLogger;
import com.romraider.logger.external.aem.xwifi.io.AemRunner;
import com.romraider.logger.external.core.ExternalDataItem;
import com.romraider.logger.external.core.ExternalDataSource;


public final class AemDataSource implements ExternalDataSource {
    private final Map<AemSensorType, AemDataItem> dataItems = new HashMap<AemSensorType, AemDataItem>();
    private AemRunner runner;
    private String port;

    {
        dataItems.put(UEGO, new AemDataItem("Wideband", AFR_147, LAMBDA, AFR_90, AFR_146, AFR_64, AFR_155, AFR_172, AFR_34));
        dataItems.put(EGT1, new AemDataItem("EGT 1", EXHAUST_DEG_F, EXHAUST_DEG_F2C));
        dataItems.put(EGT2, new AemDataItem("EGT 2", EXHAUST_DEG_F, EXHAUST_DEG_F2C));
    }

    public String getId() {
        return getClass().getName();
    }

    public String getName() {
        return "AEM X-Wifi Controller";
    }

    public String getVersion() {
        return "0.01";
    }

    public List<? extends ExternalDataItem> getDataItems() {
        return unmodifiableList(new ArrayList<AemDataItem>(dataItems.values()));
    }

    public Action getMenuAction(final EcuLogger logger) {
        return null;
    }

    public void setPort(final String port) {
        this.port = port;
    }

    public String getPort() {
        return port;
    }

    public void setProperties(Properties properties) {
    }

    public void connect() {
        runner = new AemRunner(port, dataItems);
        runAsDaemon(runner);
    }

    public void disconnect() {
        if (runner != null) runner.stop();
    }
}