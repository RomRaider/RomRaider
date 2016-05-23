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

package com.romraider.logger.external.ecotrons.plugin;

import static com.romraider.logger.external.core.SensorConversionsLambda.AFR_146;
import static com.romraider.logger.external.core.SensorConversionsLambda.AFR_147;
import static com.romraider.logger.external.core.SensorConversionsLambda.AFR_155;
import static com.romraider.logger.external.core.SensorConversionsLambda.AFR_172;
import static com.romraider.logger.external.core.SensorConversionsLambda.AFR_34;
import static com.romraider.logger.external.core.SensorConversionsLambda.AFR_64;
import static com.romraider.logger.external.core.SensorConversionsLambda.AFR_90;
import static com.romraider.logger.external.core.SensorConversionsLambda.LAMBDA;
import static com.romraider.logger.external.core.SensorConversionsOther.ENGINE_RPM;
import static com.romraider.logger.external.core.SensorConversionsOther.EXHAUST_DEG_C;
import static com.romraider.logger.external.core.SensorConversionsOther.EXHAUST_DEG_C2F;
import static com.romraider.logger.external.core.SensorConversionsOther.PERCENT;
import static com.romraider.logger.external.core.SensorConversionsOther.VOLTS_5DC;
import static com.romraider.logger.external.ecotrons.plugin.AlmSensorType.AFR1;
import static com.romraider.logger.external.ecotrons.plugin.AlmSensorType.AFR2;
import static com.romraider.logger.external.ecotrons.plugin.AlmSensorType.O21;
import static com.romraider.logger.external.ecotrons.plugin.AlmSensorType.O22;
import static com.romraider.logger.external.ecotrons.plugin.AlmSensorType.RPM;
import static com.romraider.logger.external.ecotrons.plugin.AlmSensorType.TEMP1;
import static com.romraider.logger.external.ecotrons.plugin.AlmSensorType.TEMP2;
import static com.romraider.logger.external.ecotrons.plugin.AlmSensorType.VDC1;
import static com.romraider.logger.external.ecotrons.plugin.AlmSensorType.VDC2;
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
import com.romraider.logger.external.ecotrons.io.AlmRunner;

public final class AlmDataSource implements ExternalDataSource {
    private final Map<AlmSensorType, AlmDataItem> dataItems = new HashMap<AlmSensorType, AlmDataItem>();
    private AlmRunner runner;
    private String port;

    {
        dataItems.put(AFR1,  new AlmDataItem("lambda 1", AFR_147, LAMBDA, AFR_90, AFR_146, AFR_64, AFR_155, AFR_172, AFR_34));
        dataItems.put(AFR2,  new AlmDataItem("lambda 2", AFR_147, LAMBDA, AFR_90, AFR_146, AFR_64, AFR_155, AFR_172, AFR_34));
        dataItems.put(RPM,   new AlmDataItem("RPM", ENGINE_RPM));
        dataItems.put(TEMP1, new AlmDataItem("Temperature 1", EXHAUST_DEG_C, EXHAUST_DEG_C2F));
        dataItems.put(TEMP2, new AlmDataItem("Temperature 2", EXHAUST_DEG_C, EXHAUST_DEG_C2F));
        dataItems.put(VDC1,  new AlmDataItem("VDC 1", VOLTS_5DC));
        dataItems.put(VDC2,  new AlmDataItem("VDC 2", VOLTS_5DC));
        dataItems.put(O21,   new AlmDataItem("O2 Concentration 1", PERCENT));
        dataItems.put(O22,   new AlmDataItem("O2 Concentration 2", PERCENT));
    }

    public String getId() {
        return getClass().getName();
    }

    public String getName() {
        return "ECOTRONS Accurate Lambda Meter";
    }

    public String getVersion() {
        return "0.01";
    }

    public List<? extends ExternalDataItem> getDataItems() {
        return unmodifiableList(new ArrayList<AlmDataItem>(dataItems.values()));
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
        runner = new AlmRunner(port, dataItems);
        runAsDaemon(runner);
    }

    public void disconnect() {
        if (runner != null) runner.stop();
    }
}