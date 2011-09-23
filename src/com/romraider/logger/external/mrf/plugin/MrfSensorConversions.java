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

package com.romraider.logger.external.mrf.plugin;

public enum MrfSensorConversions {
	// AFR conversion assumes reported DATA value is Gas AFR
	LAMBDA	("Lambda", "x*0.0680272108843537", "0.00"),
	AFR_147	("AFR Gasoline", "x", "0.00"),	   		         // gasoline
	AFR_90	("AFR Ethonal", "x*0.6122448979591837", "0.00"), // ethanol
	AFR_146	("AFR Diesel", "x*0.9931972789115646", "0.00"),  // diesel
	AFR_64	("AFR Methonal", "x*0.4353741496598639", "0.00"),// methanol
	AFR_155	("AFR LPG", "x*1.054421768707483", "0.00"), 	 // LPG
	AFR_172	("AFR CNG", "x*1.170068027210884", "0.00"), 	 // CNG
	AFR_34	("AFR Hydrogen", "x*2.312925170068027", "0.00"), // Hydrogen
	DEG_F	("F", "x", "0.0"),
	DEG_C	("C", "(x-32)*5/9", "0.0"),
	PSI		("psi", "x", "0.00"),
	BAR		("bar", "x*0.0689475728", "0.00"), // converts from PSI to bar
	KPA		("kPa", "x*6.89475728", "0.0"); // converts from PSI to kpa
	
	private final String units;
	private final String expression;
	private final String format;
	
	MrfSensorConversions(String units, String expression, String format) {
		this.units = units;
		this.expression = expression;
		this.format = format;
	}

	public String units() 		{ return units; }
	public String expression()  { return expression; }
	public String format() 		{ return format; }
}
