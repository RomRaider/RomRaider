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

package com.romraider.logger.external.zt2.plugin;

import static com.romraider.logger.external.core.ExternalSensorType.EGT;
import static com.romraider.logger.external.core.ExternalSensorType.ENGINE_SPEED;
import static com.romraider.logger.external.core.ExternalSensorType.MAP;
import static com.romraider.logger.external.core.ExternalSensorType.TPS;
import static com.romraider.logger.external.core.ExternalSensorType.USER1;
import static com.romraider.logger.external.core.ExternalSensorType.WIDEBAND;
import static com.romraider.logger.external.core.SensorConversionsOther.EXHAUST_DEG_C;
import static com.romraider.logger.external.core.SensorConversionsOther.EXHAUST_DEG_C2F;
import static com.romraider.logger.external.core.SensorConversionsOther.PERCENT;
import static com.romraider.logger.external.core.SensorConversionsOther.VOLTS_5DC;
import static com.romraider.logger.external.zt2.plugin.ZT2SensorConversions.AFR_146;
import static com.romraider.logger.external.zt2.plugin.ZT2SensorConversions.AFR_147;
import static com.romraider.logger.external.zt2.plugin.ZT2SensorConversions.AFR_155;
import static com.romraider.logger.external.zt2.plugin.ZT2SensorConversions.AFR_172;
import static com.romraider.logger.external.zt2.plugin.ZT2SensorConversions.AFR_34;
import static com.romraider.logger.external.zt2.plugin.ZT2SensorConversions.AFR_64;
import static com.romraider.logger.external.zt2.plugin.ZT2SensorConversions.AFR_90;
import static com.romraider.logger.external.zt2.plugin.ZT2SensorConversions.BOOST_BAR;
import static com.romraider.logger.external.zt2.plugin.ZT2SensorConversions.BOOST_KGCM2;
import static com.romraider.logger.external.zt2.plugin.ZT2SensorConversions.BOOST_KPA;
import static com.romraider.logger.external.zt2.plugin.ZT2SensorConversions.BOOST_PSI;
import static com.romraider.logger.external.zt2.plugin.ZT2SensorConversions.LAMBDA;
import static com.romraider.logger.external.zt2.plugin.ZT2SensorConversions.RPM;
import static com.romraider.util.ThreadUtil.runAsDaemon;
import static java.util.Collections.unmodifiableList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.swing.Action;

import com.romraider.logger.ecu.EcuLogger;
import com.romraider.logger.external.core.ExternalDataItem;
import com.romraider.logger.external.core.ExternalDataSource;
import com.romraider.logger.external.core.ExternalSensorType;
import com.romraider.logger.external.zt2.io.ZT2Runner;

public final class ZT2DataSource implements ExternalDataSource {
    private final Map<ExternalSensorType, ZT2DataItem> dataItems = new HashMap<ExternalSensorType, ZT2DataItem>();
    private ZT2Runner runner;
    private String port;

    {
        dataItems.put(WIDEBAND, new ZT2DataItemImpl("Wideband O2", AFR_147, LAMBDA, AFR_90, AFR_146, AFR_64, AFR_155, AFR_172, AFR_34));
        dataItems.put(TPS, new ZT2DataItemImpl("Throttle Poistion", PERCENT));
        dataItems.put(ENGINE_SPEED, new ZT2DataItemImpl("Engine Speed", RPM));
        dataItems.put(MAP, new ZT2DataItemImpl("MAP", BOOST_PSI, BOOST_BAR, BOOST_KPA, BOOST_KGCM2));
        dataItems.put(EGT, new ZT2DataItemImpl("EGT", EXHAUST_DEG_C, EXHAUST_DEG_C2F));
        dataItems.put(USER1, new ZT2DataItemImpl("User Input", VOLTS_5DC));
    }

    public String getId() {
        return getClass().getName();
    }

    public String getName() {
        return "Zeitronix ZT-2";
    }

    public String getVersion() {
        return "0.03";
    }

    public List<? extends ExternalDataItem> getDataItems() {
        return unmodifiableList(new ArrayList<ZT2DataItem>(dataItems.values()));
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
        runner = new ZT2Runner(port, dataItems);
        runAsDaemon(runner);
    }

    public void disconnect() {
        if (runner != null) runner.stop();
    }
}
