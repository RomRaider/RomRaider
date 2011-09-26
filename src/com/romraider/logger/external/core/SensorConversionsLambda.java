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

public enum SensorConversionsLambda implements ExternalSensorConversions {
	// AFR conversion assumes reported DATA value is Lambda
	LAMBDA	("Lambda", "x", "0.00"),
	AFR_147	("AFR Gasoline", "x*14.7", "0.00"),// gasoline
	AFR_90	("AFR Ethonal", "x*9.0", "0.00"),  // ethanol
	AFR_146	("AFR Diesel", "x*14.6", "0.00"),  // diesel
	AFR_64	("AFR Methonal", "x*6.4", "0.00"), // methanol
	AFR_155	("AFR LPG", "x*15.5", "0.00"), 	   // LPG
	AFR_172	("AFR CNG", "x*17.2", "0.00"), 	   // CNG
	AFR_34	("AFR Hydrogen", "x*34", "0.00");  // Hydrogen
	
	private final String units;
	private final String expression;
	private final String format;
	
	SensorConversionsLambda(String units, String expression, String format) {
		this.units = units;
		this.expression = expression;
		this.format = format;
	}

	public String units() 		{ return units; }
	public String expression()  { return expression; }
	public String format() 		{ return format; }
}
