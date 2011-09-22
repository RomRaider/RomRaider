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

package com.romraider.logger.external.zt2.plugin;

import com.romraider.logger.ecu.definition.EcuDataConvertor;
import com.romraider.logger.ecu.definition.ExternalDataConvertorImpl;

public final class ZT2DataItemImpl implements ZT2DataItem {
    private final ZT2Converter converter = new ZT2ConverterImpl();
    private final ZT2SensorType sensorType;
    private final String units;
    private final String name;
    private int[] raw;

    public ZT2DataItemImpl(String name, String units, ZT2SensorType sensorType) {
        super();
        this.name = name;
        this.units = units;
        this.sensorType = sensorType;
    }

    public String getName() {
        return "Zeitronix ZT-2 " + name;
    }

    public String getDescription() {
        return "Zeitronix ZT-2 " + name + " data";
    }

//    public String getUnits() {
//        return units;
//    }

    public double getData() {
        return converter.convert(sensorType, raw);
    }

    public void setRaw(int... raw) {
        this.raw = raw;
    }

//	public String getFormat() {
//		return "0.##";
//	}
//
//	public String getExpression() {
//		return "x";
//	}

	public EcuDataConvertor[] getConvertors() {
		String expression = "x";
		String format = "0.##";
        EcuDataConvertor[] convertors = {new ExternalDataConvertorImpl(this, units, expression, format)};
		return convertors;
	}
}
