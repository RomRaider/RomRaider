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

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import com.romraider.maps.Rom;
import com.romraider.maps.Table;

public abstract class GenericAction {
	public static final DecimalFormat DEFAULT_FORMATTER = new DecimalFormat("#.##");
	String outputName;

	public enum GenericActionType {
		TABLE, CALCULATION
	};

	public GenericAction(String output) {
		this.outputName = output;
	}

	public String getOutputName() {
		return outputName;
	}

	public abstract LinkedList<Double> getInputs();

	public abstract void init(Rom rom);

	public abstract Double calculate(Map<String, Double> variables);

	public abstract boolean isCurrentlyValid(Map<String, Double> variables);

	public boolean isSetupValid() {
		return !outputName.isEmpty();
	}

	public abstract GenericAction.GenericActionType getType();

	public abstract String getInputText();

	public abstract String getOutputText();

	public abstract String getCenterTextReference();

	public abstract Table getTable();

}