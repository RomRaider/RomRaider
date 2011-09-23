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

package com.romraider.logger.external.innovate.generic.serial.plugin;

import com.romraider.logger.ecu.definition.EcuDataConvertor;
import com.romraider.logger.ecu.definition.ExternalDataConvertorImpl;
import com.romraider.logger.external.core.DataListener;
import com.romraider.logger.external.core.ExternalDataItem;

public final class InnovateDataItem implements ExternalDataItem, DataListener {
    private final EcuDataConvertor[] convertors;
    private double data;

    public InnovateDataItem(InnovateSensorConversions... convertorList) {
        super();
        convertors = new EcuDataConvertor[convertorList.length];
        int i = 0;
        for (InnovateSensorConversions convertor :convertorList) {
        	convertors[i] = new ExternalDataConvertorImpl(
        			this,
        			convertor.units(),
        			convertor.expression(),
        			convertor.format()
        	);
        	i++;
        }
    }

    public String getName() {
        return "Innovate Wideband AFR";
    }

    public String getDescription() {
        return "Innovate Wideband AFR data";
    }

    public double getData() {
        return data;
    }

    public void setData(double data) {
        this.data = data;
    }

	public EcuDataConvertor[] getConvertors() {
		return convertors;
	}
}
