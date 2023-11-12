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

package com.romraider.dataflowSimulation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;

import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;
import static java.util.Collections.synchronizedMap;

import com.romraider.maps.Rom;
import com.romraider.swing.DataflowFrame;

public class DataflowSimulation {
	protected static final Logger LOGGER = Logger.getLogger(DataflowSimulation.class);

	private String name;
	private Rom rom;
	private String description = "";
	private HashSet<String> inputsWithLogParams = new HashSet<String>();
	private LinkedList<String> inputs = new LinkedList<String>();
	private Map<String, Double> variables = synchronizedMap(new HashMap<String, Double>());
	private LinkedList<GenericAction> dataflow = new LinkedList<GenericAction>();
	private boolean updateFromLogger = false;
	private DataflowFrame frame = null;

	public DataflowSimulation(Rom rom, String name) {
		this.name = name;
		this.rom = rom;
	}

	public void setDescription(String desc) {
		this.description = desc;
	}

	public String getDescription() {
		return description;
	}

	public void setFrame(DataflowFrame frame) {
		this.frame = frame;
	}

	public void setUpdateFromLogger(boolean value) {
		this.updateFromLogger = value;
	}

	public String getName() {
		return this.name;
	}

	public void addInput(String name, boolean hasLogParam) {
		if (!name.isEmpty()) {
			if (!inputs.contains(name)) {
				inputs.add(name);
				variables.put(name, 0.0);
				if (hasLogParam) {
					inputsWithLogParams.add(name);
				}
			} else {
				LOGGER.warn("Variable name " + name + " already exists!");
			}
		} else {
			LOGGER.warn("Ignoring empty input name!");
		}
	}

	public void addAction(GenericAction action) {
		// Check if the user is playing by our rules
		if (action.isSetupValid()) {
			dataflow.add(action);
		} else {
			LOGGER.warn("Action with output " + action.getOutputName() + " is not valid!");
		}
	}

	public int getNumberOfActions() {
		return dataflow.size();
	}

	public LinkedList<String> getInputs() {
		return inputs;
	}

	public HashSet<String> getInputsWithLogParam() {
		return inputsWithLogParams;
	}

	public Double getVariableValue(String varName) {
		return variables.get(varName);
	}

	public Double setVariableValue(String varName, Double value) {
		return variables.put(varName, value);
	}

	public GenericAction getAction(int index) {
		return dataflow.get(index);
	}

	public Rom getRom() {
		return this.rom;
	}

	public Double simulate(int index) {
		Double result = 0.0;
		GenericAction a = dataflow.get(index);
		if (a.isCurrentlyValid(variables)) {
			result = a.calculate(variables);
			variables.put(a.outputName, result);
		} else {
			LOGGER.warn("Action with output " + a.getOutputName() + " is not valid!");
		}

		return result;
	}

	public void updateVariableFromLogger(String key, double dataValue) {
		if (updateFromLogger) {
			setVariableValue(key, dataValue);
			if (frame != null) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						frame.updateContentPanel();
					}
				});
			}
		}
	}
}