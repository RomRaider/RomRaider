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

import com.romraider.logger.external.core.ExternalSensorConversions;

public enum ZT2SensorConversions implements ExternalSensorConversions {
	// AFR conversion assumes reported DATA value is Gas AFR
	LAMBDA	("Lambda", "x*0.00680272108843537", "0.00"),
	AFR_147	("AFR Gasoline", "x*0.1", "0.00"),	   		      // gasoline
	AFR_90	("AFR Ethonal", "x*0.06122448979591837", "0.00"), // ethanol
	AFR_146	("AFR Diesel", "x*0.09931972789115646", "0.00"),  // diesel
	AFR_64	("AFR Methonal", "x*0.04353741496598639", "0.00"),// methanol
	AFR_155	("AFR LPG", "x*0.1054421768707483", "0.00"), 	  // LPG
	AFR_172	("AFR CNG", "x*0.1170068027210884", "0.00"), 	  // CNG
	AFR_34	("AFR Hydrogen", "x*0.2312925170068027", "0.00"), // Hydrogen
	PERCENT	("%", "x", "0.0"),
	DEG_C	("C", "x", "0.0"),
	DEG_F	("F", "(x/.555+32)", "0.0"),
	BOOST_PSI	("psi", "x*0.1", "0.00"),
	BOOST_BAR	("bar", "x*0.0068947573", "0.00"),		 // converts from PSI
	BOOST_KPA	("kPa", "x*0.6894757282", "0.0"),  		 // converts from PSI
	BOOST_KGCM2	("kg/cm^2", "x*0.0070306958", "0.00"),	 // converts from PSI
	VDC		("vdc", "x", "0.00"),
	RPM		("rpm", "round(((1000000/x)*4.59)/2)", "0");

	private final String units;
	private final String expression;
	private final String format;
	
	ZT2SensorConversions(String units, String expression, String format) {
		this.units = units;
		this.expression = expression;
		this.format = format;
	}

	public String units() 		{ return units; }
	public String expression()  { return expression; }
	public String format() 		{ return format; }
}
