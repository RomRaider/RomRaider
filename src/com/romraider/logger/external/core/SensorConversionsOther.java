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

package com.romraider.logger.external.core;

import com.romraider.logger.external.core.ExternalSensorConversions;

public enum SensorConversionsOther implements ExternalSensorConversions {
	DEG_C	("C", "x", "0.0"),
	DEG_F	("F", "x", "0.0"),
	DEG_F2C	("C", "(x-32)*5/9", "0.0"),
	DEG_C2F	("F", "x*9/5+32 ", "0.0"),
	PSI		("psi", "x", "0.00"),
	PSI2BAR	("bar", "x*0.0689475728", "0.00"),			// converts from PSI to bar
	PSI2KPA	("kPa", "x*6.89475728", "0.0"),				// converts from PSI to kpa
	PSI2KGCM2	("kg/cm^2", "x*0.0703068835943", "0.0"),// converts from PSI to kpa
	KPA2PSI	("psi", "x*0.14503774", "0.00"), 			// converts from kPa
	KPA2BAR	("bar", "x*0.01", "0.00"), 		 		 	// converts from kPa
	KPA		("kPa", "x", "0.0"),
	KPA2KGCM2	("kg/cm^2", "x*0.01019716", "0.00"), 	// converts from kPa
	PERCENT	("%", "x", "0.0"),
	VOLTS_DC("vdc", "x", "0.0");

	
	private final String units;
	private final String expression;
	private final String format;
	
	SensorConversionsOther(String units, String expression, String format) {
		this.units = units;
		this.expression = expression;
		this.format = format;
	}

	public String units() 		{ return units; }
	public String expression()  { return expression; }
	public String format() 		{ return format; }
}
