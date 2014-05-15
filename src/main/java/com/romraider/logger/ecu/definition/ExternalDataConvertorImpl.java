/*
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2014 RomRaider.com
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

package com.romraider.logger.ecu.definition;

import static com.romraider.util.JEPUtil.evaluate;

import java.text.DecimalFormat;

import com.romraider.logger.ecu.ui.handler.dash.GaugeMinMax;
import com.romraider.logger.external.core.ExternalDataItem;

public final class ExternalDataConvertorImpl implements EcuDataConvertor {
    private final String units;
    private final String expression;
    private final GaugeMinMax gaugeMinMax;
    private final ExternalDataItem dataItem; 
    private DecimalFormat format;
    
    public ExternalDataConvertorImpl(ExternalDataItem dataItem, String units, String expression,
                                     String format,
                                     GaugeMinMax gaugeMinMax
                                     ) {
        this.dataItem = dataItem;
        this.units = units;
        this.expression = expression;
        this.format = new DecimalFormat(format);
        this.gaugeMinMax = gaugeMinMax;
    }

    public double convert(byte[] bytes) {
        double value = dataItem.getData();
        double result = evaluate(expression, value);
        return Double.isNaN(result) || Double.isInfinite(result) ? 0.0 : result;
    }

    public String format(double value) {
        return format.format(value);
    }

    public String getUnits() {
        return units;
    }

    public GaugeMinMax getGaugeMinMax() {
        return gaugeMinMax;
    }

    public String getFormat() {
        return format.toPattern();
    }

    public String toString() {
        return getUnits();
    }

    public String getExpression() {
        return expression;
    }

    @Override
    public String getDataType() {
        return null;
    }
}
