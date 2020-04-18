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

package com.romraider.logger.external.innovate.lm2.mts.plugin;

import static com.romraider.util.ThreadUtil.runAsDaemon;
import static java.lang.Integer.parseInt;
import static java.util.Collections.unmodifiableList;
import static org.apache.log4j.Logger.getLogger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
//import java.util.Set;

import javax.swing.Action;

import org.apache.log4j.Logger;

import com.romraider.logger.ecu.EcuLogger;
import com.romraider.logger.ecu.exception.PluginNotInstalledException;
import com.romraider.logger.external.core.ExternalDataItem;
import com.romraider.logger.external.core.ExternalDataSource;
//import com.romraider.logger.external.innovate.generic.mts.io.MTSSensor;
import com.romraider.logger.external.innovate.generic.mts.io.MTSConnector;
import com.romraider.logger.external.innovate.generic.mts.io.MTSRunner;

public final class Lm2MtsDataSource implements ExternalDataSource {
    private static final Logger LOGGER = getLogger(Lm2MtsDataSource.class);
    private Map<Integer, Lm2MtsDataItem> dataItems = new HashMap<Integer, Lm2MtsDataItem>();
    private MTSRunner runner;
    private int mtsPort = -1;
    private boolean pollMode;

    /**
     * The Lm2MtsDataSource class is called when the Logger starts up and the 
     * call to load the external plug-ins is made.  The class with its helpers
     * will open the MTS SDK and find all available ports.  It will interrogate
     * the ports for available streams then dynamically build a list of sensors
     * reported in the MTS streams.  If there is more than one MTS stream, only
     * one stream can be processed.
     */
    {
    	try {
	        final MTSConnector connector = new MTSConnector();
	        int[] ports = connector.getMtsPorts();
	        if (ports != null) {
	            for (int i = 0; i < ports.length; i++) {
	                connector.usePort(i);
	                dataItems = connector.getSensors();
	            }
	        }
	        
	        connector.dispose();
    	}
    	catch(com4j.ExecutionException e) {
    		throw new PluginNotInstalledException("Innovate LogWorks MTS control does not appear to be installed on this computer");
    	}
    }

    public String getId() {
        return getClass().getName();
    }

    public String getName() {
        return "Innovate MTS";
    }

    public String getVersion() {
        return "0.06";
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

    public void setProperties(Properties properties) {
        final String pollMode = properties.getProperty("datasource.pollmode");
        this.pollMode = Boolean.parseBoolean(pollMode);
    }

    public void connect() {
        runner = new MTSRunner(mtsPort, dataItems, pollMode);
        runAsDaemon(runner);
    }

    public void disconnect() {
        if (runner != null) runner.stop();
    }

    private int mtsPort(String port) {
        try {
            return parseInt(port);
        } catch (Exception e) {
            LOGGER.warn("Bad Innovate MTS port: " + port);
            return -1;
        }
    }
}