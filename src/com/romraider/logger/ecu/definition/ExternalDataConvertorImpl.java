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

import com.romraider.logger.ecu.ui.handler.dash.GaugeMinMax;
import com.romraider.logger.external.core.ExternalDataItem;

import java.text.DecimalFormat;

public final class ExternalDataConvertorImpl implements EcuDataConvertor {
	private final ExternalDataItem dataItem; 
    private static final String FORMAT = "0.##";
    private DecimalFormat format = new DecimalFormat(FORMAT);
	
	public ExternalDataConvertorImpl(ExternalDataItem dataItem) {
		this.dataItem = dataItem;
	}

    public double convert(byte[] bytes) {
        return dataItem.getData();
    }

    public String format(double value) {
        return format.format(value);
    }

    public String getUnits() {
        return dataItem.getUnits();
    }

    public GaugeMinMax getGaugeMinMax() {
        return getMaxMin(getUnits());
    }

    public String getFormat() {
        return FORMAT;
    }
}
