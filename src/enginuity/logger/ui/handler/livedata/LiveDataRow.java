/*
 *
 * Enginuity Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006 Enginuity.org
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
 *
 */

package enginuity.logger.ui.handler.livedata;

import enginuity.logger.definition.EcuData;
import static enginuity.util.ParamChecker.checkNotNull;

public final class LiveDataRow {
    private static final double ZERO = 0.0;
    private final EcuData ecuData;
    private double minValue;
    private double maxValue;
    private double currentValue;
    private boolean updated = false;

    public LiveDataRow(EcuData ecuData) {
        checkNotNull(ecuData, "ecuData");
        this.ecuData = ecuData;
    }

    public EcuData getEcuData() {
        return ecuData;
    }

    public String getName() {
        return ecuData.getName();
    }

    public String getMinValue() {
        return ecuData.getSelectedConvertor().format(minValue);
    }

    public String getMaxValue() {
        return ecuData.getSelectedConvertor().format(maxValue);
    }

    public String getCurrentValue() {
        return ecuData.getSelectedConvertor().format(currentValue);
    }

    public String getUnits() {
        return ecuData.getSelectedConvertor().getUnits();
    }

    public void updateValue(double value) {
        currentValue = value;
        if (currentValue < minValue || !updated) {
            minValue = currentValue;
        }
        if (currentValue > maxValue || !updated) {
            maxValue = currentValue;
        }
        updated = true;
    }

    public void reset() {
        minValue = ZERO;
        maxValue = ZERO;
        currentValue = ZERO;
        updated = false;
    }
}