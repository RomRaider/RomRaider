/*
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2012 RomRaider.com
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
import static com.romraider.logger.external.txs.plugin.TxsSensorConversions.TXS_BOOST;
import static com.romraider.logger.external.txs.plugin.TxsSensorConversions.TXS_IDC;
import static com.romraider.logger.external.txs.plugin.TxsSensorConversions.TXS_IGN;
import static com.romraider.logger.external.txs.plugin.TxsSensorConversions.TXS_KNOCK;
import static com.romraider.logger.external.txs.plugin.TxsSensorConversions.TXS_LOAD;
import static com.romraider.logger.external.txs.plugin.TxsSensorConversions.TXS_MAFV;
import static com.romraider.logger.external.txs.plugin.TxsSensorConversions.TXS_MAPVE;
import static com.romraider.logger.external.txs.plugin.TxsSensorConversions.TXS_MODFUEL;
import static com.romraider.logger.external.txs.plugin.TxsSensorConversions.TXS_RPM;
import static com.romraider.logger.external.txs.plugin.TxsSensorConversions.TXS_TPS;
import static com.romraider.util.ThreadUtil.runAsDaemon;
import static java.util.Collections.unmodifiableList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.Action;

import com.romraider.logger.ecu.EcuLogger;
import com.romraider.logger.external.core.ExternalDataItem;
import com.romraider.logger.external.core.ExternalDataSource;
import com.romraider.logger.external.txs.io.TxsRunner;

public class TxsUtecLogger1DataSource implements ExternalDataSource {
	private static final String LOGGER = "1";
	private static final String DEVICE = "u";
	private static final String DEVICE_NAME = "TXS UTEC ";
	private static final int RPM = 0;
	private static final int BOOST = 1;
	private static final int MAFV = 2;
	private static final int TPS = 3;
	private static final int LOAD = 4;
	private static final int KNOCK = 5;
	private static final int ECUIGN = 7;
	private static final int IDC = 8;
	private static final int MODIGN = 9;
	private static final int MAPVE_MODFUEL = 10;
	private static final int MODMAFV = 12;
	private static final int AFR = 13;
		
	private final HashMap<Integer, TxsDataItem> dataItems =
						new HashMap<Integer, TxsDataItem>();
	private TxsRunner runner;
	private String port;
	
	{
		dataItems.put(RPM, 				new TxsDataItem(DEVICE_NAME + "RPM", TXS_RPM));
		dataItems.put(BOOST, 			new TxsDataItem(DEVICE_NAME + "BOOST", TXS_BOOST));
		dataItems.put(MAFV, 			new TxsDataItem(DEVICE_NAME + "MAFV", TXS_MAFV));
		dataItems.put(TPS, 				new TxsDataItem(DEVICE_NAME + "TPS", TXS_TPS));
		dataItems.put(LOAD, 			new TxsDataItem(DEVICE_NAME + "LOAD", TXS_LOAD));
		dataItems.put(KNOCK, 			new TxsDataItem(DEVICE_NAME + "KNOCK", TXS_KNOCK));
		dataItems.put(ECUIGN, 			new TxsDataItem(DEVICE_NAME + "ECUIGN", TXS_IGN));
		dataItems.put(IDC, 				new TxsDataItem(DEVICE_NAME + "IDC", TXS_IDC));
		dataItems.put(MODIGN, 			new TxsDataItem(DEVICE_NAME + "MODIGN", TXS_IGN));
		dataItems.put(MAPVE_MODFUEL,	new TxsDataItem(DEVICE_NAME + "MAPVE/MODFUEL", TXS_MODFUEL, TXS_MAPVE));
		dataItems.put(MODMAFV, 			new TxsDataItem(DEVICE_NAME + "MODMAFV", TXS_MAFV));
		dataItems.put(AFR, 				new TxsDataItem(DEVICE_NAME + "AFR", AFR_147, LAMBDA, AFR_90, AFR_146, AFR_64, AFR_155, AFR_172, AFR_34));
	}
	
	public String getId() {
		return getClass().getName();
	}
	
	public String getName() {
		return DEVICE_NAME + "Logger";
	}
	
	public String getVersion() {
		return "0.01";
	}
	
	public List<? extends ExternalDataItem> getDataItems() {
		return unmodifiableList(new ArrayList<TxsDataItem>(dataItems.values()));
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
