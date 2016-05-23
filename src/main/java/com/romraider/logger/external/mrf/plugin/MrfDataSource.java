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

package com.romraider.logger.external.mrf.plugin;

import static com.romraider.logger.external.core.SensorConversionsAFR.AFR_146;
import static com.romraider.logger.external.core.SensorConversionsAFR.AFR_147;
import static com.romraider.logger.external.core.SensorConversionsAFR.AFR_155;
import static com.romraider.logger.external.core.SensorConversionsAFR.AFR_172;
import static com.romraider.logger.external.core.SensorConversionsAFR.AFR_34;
import static com.romraider.logger.external.core.SensorConversionsAFR.AFR_64;
import static com.romraider.logger.external.core.SensorConversionsAFR.AFR_90;
import static com.romraider.logger.external.core.SensorConversionsAFR.LAMBDA;
import static com.romraider.logger.external.core.SensorConversionsOther.AIR_DEG_F;
import static com.romraider.logger.external.core.SensorConversionsOther.AIR_DEG_F2C;
import static com.romraider.logger.external.core.SensorConversionsOther.AIR_REL_PSI;
import static com.romraider.logger.external.core.SensorConversionsOther.AIR_REL_PSI2BAR;
import static com.romraider.logger.external.core.SensorConversionsOther.AIR_REL_PSI2KPA;
import static com.romraider.logger.external.core.SensorConversionsOther.EXHAUST_DEG_F;
import static com.romraider.logger.external.core.SensorConversionsOther.EXHAUST_DEG_F2C;
import static com.romraider.logger.external.core.SensorConversionsOther.FLUID_DEG_F;
import static com.romraider.logger.external.core.SensorConversionsOther.FLUID_DEG_F2C;
import static com.romraider.logger.external.core.SensorConversionsOther.FUEL_PSI;
import static com.romraider.logger.external.core.SensorConversionsOther.FUEL_PSI2BAR;
import static com.romraider.logger.external.core.SensorConversionsOther.FUEL_PSI2KPA;
import static com.romraider.logger.external.core.SensorConversionsOther.OIL_PSI;
import static com.romraider.logger.external.core.SensorConversionsOther.OIL_PSI2BAR;
import static com.romraider.logger.external.core.SensorConversionsOther.OIL_PSI2KPA;
import static com.romraider.logger.external.mrf.plugin.MrfSensorType.AFR;
import static com.romraider.logger.external.mrf.plugin.MrfSensorType.EGT;
import static com.romraider.logger.external.mrf.plugin.MrfSensorType.FUEL_PRESS;
import static com.romraider.logger.external.mrf.plugin.MrfSensorType.MANIFOLD_TEMP;
import static com.romraider.logger.external.mrf.plugin.MrfSensorType.MAP;
import static com.romraider.logger.external.mrf.plugin.MrfSensorType.OIL_PRESS;
import static com.romraider.logger.external.mrf.plugin.MrfSensorType.OIL_TEMP;
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
import com.romraider.logger.external.mrf.io.MrfRunner;

public final class MrfDataSource implements ExternalDataSource {
    private final Map<MrfSensorType, MrfDataItem> dataItems = new HashMap<MrfSensorType, MrfDataItem>();
    private MrfRunner runner;
    private String port;

    {
        dataItems.put(AFR, new MrfDataItem("Wideband", AFR_147, LAMBDA, AFR_90, AFR_146, AFR_64, AFR_155, AFR_172, AFR_34));
        dataItems.put(MAP, new MrfDataItem("MAP", AIR_REL_PSI, AIR_REL_PSI2BAR, AIR_REL_PSI2KPA));
        dataItems.put(EGT, new MrfDataItem("EGT", EXHAUST_DEG_F, EXHAUST_DEG_F2C));
        dataItems.put(OIL_TEMP, new MrfDataItem("Oil Temp", FLUID_DEG_F, FLUID_DEG_F2C));
        dataItems.put(OIL_PRESS, new MrfDataItem("Oil Press", OIL_PSI, OIL_PSI2BAR, OIL_PSI2KPA));
        dataItems.put(FUEL_PRESS, new MrfDataItem("Fuel Press", FUEL_PSI, FUEL_PSI2BAR, FUEL_PSI2KPA));
        dataItems.put(MANIFOLD_TEMP, new MrfDataItem("Manifold Temp", AIR_DEG_F, AIR_DEG_F2C));
    }

    public String getId() {
        return getClass().getName();
    }

    public String getName() {
        return "MRF Stealth Gauge";
    }

    public String getVersion() {
        return "0.02";
    }

    public List<? extends ExternalDataItem> getDataItems() {
        return unmodifiableList(new ArrayList<MrfDataItem>(dataItems.values()));
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
        runner = new MrfRunner(port, dataItems);
        runAsDaemon(runner);
    }

    public void disconnect() {
        if (runner != null) runner.stop();
    }
}