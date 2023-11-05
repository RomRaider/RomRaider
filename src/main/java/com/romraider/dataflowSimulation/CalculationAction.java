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
import java.util.LinkedList;
import java.util.Map;

import com.romraider.maps.Rom;
import com.romraider.maps.Table;
import com.romraider.util.JEPUtil;

public class CalculationAction extends GenericAction {
	private String expression;
	private String currentOutputText;
	private String currentInputText;
	private String currentCenterText;

	public CalculationAction(String output, String expression) {
		super(output);
		this.expression = expression;
	}

	public String getExpression() {
		return expression;
	}

	private void updateCurrentInputText(HashMap<String, Double> variables) {
		currentInputText = "<html>";
		for (Map.Entry<String, Double> entry : variables.entrySet()) {
			if (expression.matches(".*\\b" + entry.getKey() + "\\b.*")) {
				currentInputText += entry.getKey() + ": " + DEFAULT_FORMATTER.format(entry.getValue()) + "<br>";
			}
		}
	}

	private void updateCurrentCenterText(HashMap<String, Double> variables) {
		currentCenterText = expression;
		for (Map.Entry<String, Double> entry : variables.entrySet()) {
			String newValue = DEFAULT_FORMATTER.format(entry.getValue());
			currentCenterText = currentCenterText.replaceAll("(\\b)" + entry.getKey() + "(\\b)", entry.getKey() + "(" + newValue + ")");
		}		
	}

	public Double calculate(HashMap<String, Double> variables) {
		Double output = JEPUtil.evaluate(expression, variables);
		currentOutputText = super.getOutputName() + ": "
				+ (Double.isNaN(output) || Double.isInfinite(output) ? "Error" : DEFAULT_FORMATTER.format(output));
		updateCurrentInputText(variables);
		updateCurrentCenterText(variables);
		return output;
	}

	public boolean isSetupValid() {
		return super.isSetupValid() && !expression.isEmpty();
	}

	public boolean isCurrentlyValid(HashMap<String, Double> variables) {
		Double value = JEPUtil.evaluate(expression, variables);
		return !Double.isNaN(value) && !Double.isInfinite(value);
	}

	@Override
	public GenericActionType getType() {
		return GenericActionType.CALCULATION;
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
		return currentCenterText;
	}

	@Override
	public Table getTable() {
		return null;
	}

	@Override
	public void init(Rom rom) {
	}

	@Override
	public LinkedList<Double> getInputs() {
		return null;
	}
}