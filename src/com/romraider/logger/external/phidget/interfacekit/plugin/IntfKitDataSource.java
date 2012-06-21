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

package com.romraider.logger.external.phidget.interfacekit.plugin;

import static com.romraider.util.ThreadUtil.runAsDaemon;
import static java.util.Collections.unmodifiableList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.Action;

import com.romraider.logger.ecu.EcuLogger;
import com.romraider.logger.external.core.ExternalDataItem;
import com.romraider.logger.external.core.ExternalDataSource;
import com.romraider.logger.external.phidget.interfacekit.io.IntfKitManager;
import com.romraider.logger.external.phidget.interfacekit.io.IntfKitRunner;
import com.romraider.logger.external.phidget.interfacekit.io.IntfKitSensor;

/**
 * The IntfKitDataSource class is called when the Logger starts up and the 
 * call to load the external plug-ins is made.  This class with its helpers
 * will open each PhidgetInterfaceKit and find all available inputs.  It will
 * interrogate the inputs then dynamically build a list of inputs found based
 * on the serial number and input number.
 * @see ExternalDataSource
 */
public final class IntfKitDataSource implements ExternalDataSource {
    private final Map<String, IntfKitDataItem> dataItems =
    		new HashMap<String, IntfKitDataItem>();
    private IntfKitRunner runner;
    private Integer[] kits;

    {
    	kits = IntfKitManager.findIntfkits();
    	if (kits.length > 0) {
    		IntfKitManager.loadIk();
	    	for (int serial : kits) {
		    	final Set<IntfKitSensor> sensors = IntfKitManager.getSensors(serial);
		    	if (!sensors.isEmpty()) {
			    	for (IntfKitSensor sensor : sensors) {
			    		final String inputName = String.format("%d:%d",
			    				serial,
			    				sensor.getInputNumber());
			    		dataItems.put(
			    			inputName,
							new IntfKitDataItem(
								sensor.getInputName(),
								sensor.getUnits(),
								sensor.getMinValue(),
								sensor.getMaxValue()
							)
			    		);
			    	}
		    	}
	    	}
    	}
    }

    public String getId() {
        return getClass().getName();
    }

    public String getName() {
        return "Phidget InterfaceKit";
    }

    public String getVersion() {
        return "0.01";
    }

    public List<? extends ExternalDataItem> getDataItems() {
    	return unmodifiableList(new ArrayList<IntfKitDataItem>(dataItems.values()));
    }

    public Action getMenuAction(final EcuLogger logger) {
        return new IntfKitPluginMenuAction(logger);
    }

    public void connect() {
        runner = new IntfKitRunner(kits, dataItems);
        runAsDaemon(runner);
    }

    public void disconnect() {
        if (runner != null) runner.stop();
    }

	public void setPort(final String port) {
	}

	public String getPort() {
		return "HID USB";
	}
}
