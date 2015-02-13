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

package com.romraider.logger.external.txs.plugin;

import static com.romraider.logger.external.core.SensorConversionsAFR.AFR_146;
import static com.romraider.logger.external.core.SensorConversionsAFR.AFR_147;
import static com.romraider.logger.external.core.SensorConversionsAFR.AFR_155;
import static com.romraider.logger.external.core.SensorConversionsAFR.AFR_172;
import static com.romraider.logger.external.core.SensorConversionsAFR.AFR_34;
import static com.romraider.logger.external.core.SensorConversionsAFR.AFR_64;
import static com.romraider.logger.external.core.SensorConversionsAFR.AFR_90;
import static com.romraider.logger.external.core.SensorConversionsAFR.LAMBDA;
import static com.romraider.logger.external.txs.plugin.TxsSensorConversions.TXS_KNOCK;
import static com.romraider.logger.external.txs.plugin.TxsSensorConversions.TXS_RPM;
import static com.romraider.util.ThreadUtil.runAsDaemon;
import static java.util.Collections.unmodifiableList;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.swing.Action;

import com.romraider.logger.ecu.EcuLogger;
import com.romraider.logger.external.core.ExternalDataItem;
import com.romraider.logger.external.core.ExternalDataSource;
import com.romraider.logger.external.txs.io.TxsRunner;

public class TxsTunerDataSource implements ExternalDataSource {
    
    private static final String LOGGER = "1";
    private static final String DEVICE = "t";
    private static final String DEVICE_NAME = "TXS Tuner ";
    private static final int TUNER_LITE_AFR = 0;
    private static final int TUNER_PRO_RPM = 0;
    private static final int TUNER_PRO_AFR = 1;
    private static final int TUNER_PRO_KNOCK = 11;
        
    private final ArrayList<TxsDataItem> dataItems =
                    new ArrayList<TxsDataItem>();
    private TxsRunner runner;
    private String port;
    
    {
        dataItems.add(new TxsDataItem(DEVICE_NAME + "Lite AFR", TUNER_LITE_AFR, AFR_147, LAMBDA, AFR_90, AFR_146, AFR_64, AFR_155, AFR_172, AFR_34));
        dataItems.add(new TxsDataItem(DEVICE_NAME + "Pro RPM", TUNER_PRO_RPM, TXS_RPM));
        dataItems.add(new TxsDataItem(DEVICE_NAME + "Pro AFR", TUNER_PRO_AFR, LAMBDA, AFR_147, AFR_90, AFR_146, AFR_64, AFR_155, AFR_172, AFR_34));
        dataItems.add(new TxsDataItem(DEVICE_NAME + "Pro Knock", TUNER_PRO_KNOCK, TXS_KNOCK));
    }
    
    public String getId() {
        return getClass().getName();
    }
    
    public String getName() {
        return DEVICE_NAME + "Logger";
    }
    
    public String getVersion() {
        return "2013.04.04";
    }
    
    public List<? extends ExternalDataItem> getDataItems() {
        return unmodifiableList(dataItems);
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
        runner = new TxsRunner(port, dataItems, LOGGER, DEVICE);
        runAsDaemon(runner);
    }
    
    public void disconnect() {
        if(runner!=null) {
            runner.stop();
        }
    }
}
