/*
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2023 RomRaider.com
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

package com.romraider.logger.ecu.ui.handler.dataflow;

import static java.util.Collections.synchronizedMap;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Map;

import com.romraider.logger.ecu.comms.query.Response;
import com.romraider.logger.ecu.definition.LoggerData;
import com.romraider.logger.ecu.ui.handler.DataUpdateHandler;
import com.romraider.dataflowSimulation.DataflowSimulation;

public final class DataflowSimulationHandler implements DataUpdateHandler {
	private static final DataflowSimulationHandler INSTANCE = new DataflowSimulationHandler();
	// LoggerID --> List of (Variable Name, Simulation)
	private final Map<String, LinkedList<Map.Entry<String, DataflowSimulation>>> simulations = synchronizedMap(
			new HashMap<String, LinkedList<Map.Entry<String, DataflowSimulation>>>());

	public DataflowSimulationHandler() {
		simulations.clear();
	}

	@Override
	public void registerData(LoggerData loggerData) {
	}

	@Override
	public void handleDataUpdate(Response response) {
		if (!simulations.isEmpty()) {
			for (LoggerData loggerData : response.getData()) {
				synchronized (simulations) {
					LinkedList<Map.Entry<String, DataflowSimulation>> simsForID = simulations.get(loggerData.getId());
					if (simsForID != null) {
						double dataValue = response.getDataValue(loggerData);

						for (ListIterator<Map.Entry<String, DataflowSimulation>> item = simsForID.listIterator(); item
								.hasNext();) {
							Map.Entry<String, DataflowSimulation> value = item.next();
							value.getValue().updateVariableFromLogger(value.getKey(), dataValue);
						}
					}
				}
			}
		}
	}

	@Override
	public void deregisterData(LoggerData loggerData) {
	}

	@Override
	public void cleanUp() {
		simulations.clear();
	}

	@Override
	public void reset() {
	}

	public void registerInput(String logParam, String varName, DataflowSimulation sim) {
		if(!logParam.isEmpty() && !varName.isEmpty() && sim != null)
		{
			if (!simulations.containsKey(logParam)) {
				LinkedList<Map.Entry<String, DataflowSimulation>> list = new LinkedList<Map.Entry<String, DataflowSimulation>>();
				list.add(new AbstractMap.SimpleImmutableEntry<String, DataflowSimulation>(varName, sim));
				simulations.put(logParam, list);
			} else {
				LinkedList<Map.Entry<String, DataflowSimulation>> list = simulations.get(logParam);
				list.add(new AbstractMap.SimpleImmutableEntry<String, DataflowSimulation>(varName, sim));
			}
		}
	}

	public static DataflowSimulationHandler getInstance() {
		return INSTANCE;
	}
}
