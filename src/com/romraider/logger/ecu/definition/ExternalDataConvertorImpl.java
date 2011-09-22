/*
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2010 RomRaider.com
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

import static com.romraider.logger.ecu.definition.xml.ConverterMaxMinDefaults.getMaxMin;
import static com.romraider.util.JEPUtil.evaluate;
import com.romraider.logger.ecu.ui.handler.dash.GaugeMinMax;
import com.romraider.logger.external.core.ExternalDataItem;

import java.text.DecimalFormat;

public final class ExternalDataConvertorImpl implements EcuDataConvertor {
    private final String units;
    private final String expression;
    private final GaugeMinMax gaugeMinMax = new GaugeMinMax(0,0,0);
	private final ExternalDataItem dataItem; 
    private DecimalFormat format;
    
//    <conversion units="psi" expr="x*37/255" format="0.00" gauge_min="-20" gauge_max="40" gauge_step="5" />
//    <conversion units="kPa" expr="x*37/255/14.50377*100" format="0" gauge_min="-120" gauge_max="280" gauge_step="40" />
//    <conversion units="hPa" expr="x*37/255/14.50377*1000" format="0" gauge_min="-1200" gauge_max="2800" gauge_step="400" />
//    <conversion units="bar" expr="x*37/255/14.50377" format="0.000" gauge_min="-1.2" gauge_max="2.8" gauge_step="0.4" />
	
	public ExternalDataConvertorImpl(ExternalDataItem dataItem, String units, String expression,
									 String format
//									 GaugeMinMax gaugeMinMax
									 ) {
		this.dataItem = dataItem;
		this.units = units;
		this.expression = expression;
		this.format = new DecimalFormat(format);
	}

    public double convert(byte[] bytes) {
        double value = dataItem.getData();
//    	int value = asUnsignedInt(bytes);
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
        return getMaxMin(getUnits());
    }

    public String getFormat() {
        return format.toPattern();
    }

    public String toString() {
        return getUnits();
    }
}
