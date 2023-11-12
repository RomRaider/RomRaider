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
import java.util.LinkedList;
import java.util.Map;

import com.romraider.maps.Rom;
import com.romraider.maps.Table;

public class TableAction extends GenericAction {
	protected static final DecimalFormat TABLE_FORMATTER = new DecimalFormat();

	private String refTable;
	private String input_x;
	private String input_y;

	private String currentInputText;
	private String currentOutputText;
	private Table resolvedTable;
	private LinkedList<Double> currentInputs = new LinkedList<Double>();

	public TableAction(String output, String reference, String input_x, String input_y) {
		super(output);
		this.refTable = reference;
		this.input_x = input_x;
		this.input_y = input_y;
	}

	public boolean isSetupValid() {
		return super.isSetupValid() && !refTable.isEmpty() && (!input_x.isEmpty() || !input_y.isEmpty());
	}

	public boolean isCurrentlyValid(Map<String, Double> variables) {
		return (input_x.isEmpty() ? true : variables.containsKey(input_x))
				&& (input_y.isEmpty() ? true : variables.containsKey(input_y));
	}

	private String updateInputText(Double inputXValue, Double inputYValue) {
		currentInputText = "<html>";
		// currentInputText += resolvedTable.getName() + "<br><br>";

		if (inputXValue != null) {
			currentInputText += input_x + " : " + DEFAULT_FORMATTER.format(inputXValue);
		}
		if (inputYValue != null) {
			if (inputXValue != null) {
				currentInputText += "<br>";
			}
			currentInputText += input_y + " : " + DEFAULT_FORMATTER.format(inputYValue);
		}
		return currentInputText;
	}

	public Double calculate(Map<String, Double> variables) {
		if (resolvedTable != null) {
			Double inputXValue = variables.get(input_x);
			Double inputYValue = variables.get(input_y);

			currentInputs.clear();
			if (!input_x.isEmpty() && inputXValue != null)
				currentInputs.add(inputXValue);
			if (!input_y.isEmpty() && inputYValue != null)
				currentInputs.add(inputYValue);

			Double output = resolvedTable.queryTable((Double) inputXValue, (Double) inputYValue);
			currentInputText = updateInputText(inputXValue, inputYValue);
			currentOutputText = super.getOutputName() + ": " + TABLE_FORMATTER.format(output);
			return output;
		} else {
			DataflowSimulation.LOGGER.warn("Failed to find table " + this.refTable);
			return 0.0;
		}
	}

	public LinkedList<Double> getInputs() {
		return currentInputs;
	}

	@Override
	public GenericActionType getType() {
		return GenericActionType.TABLE;
	}

	@Override
	public String getInputText() {
		return currentInputText;
	}

	@Override
	public String getOutputText() {
		return currentOutputText;
	}

	@Override
	public String getCenterTextReference() {
		return null;
	}

	@Override
	public Table getTable() {
		return resolvedTable;
	}

	@Override
	public void init(Rom rom) {
		resolvedTable = rom.getTableByName(this.refTable);
		if (resolvedTable != null) {
			TABLE_FORMATTER.applyPattern(resolvedTable.getCurrentScale().getFormat());
		} else {
			DataflowSimulation.LOGGER.warn("Failed to find table " + this.refTable);
		}
	}
}