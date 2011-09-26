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

package com.romraider.logger.external.te.plugin;

import com.romraider.logger.external.core.ExternalSensorConversions;

public enum TESensorConversions implements ExternalSensorConversions {
	LAMBDA	("Lambda", "x/8192+0.5", "0.00"),
	AFR_147	("AFR Gasoline", "(x/8192+0.5)*14.7", "0.00"),// gasoline
	AFR_90	("AFR Ethonal", "(x/8192+0.5)*9.0", "0.00"),  // ethanol
	AFR_146	("AFR Diesel", "(x/8192+0.5)*14.6", "0.00"),  // diesel
	AFR_64	("AFR Methonal", "(x/8192+0.5)*6.4", "0.00"), // methanol
	AFR_155	("AFR LPG", "(x/8192+0.5)*15.5", "0.00"), 	  // LPG
	AFR_172	("AFR CNG", "(x/8192+0.5)*17.2", "0.00"), 	  // CNG
	AFR_34	("AFR Hydrogen", "(x/8192+0.5)*34", "0.00"),  // Hydrogen
	VDC		("VDC", "x*0.000610948", "0.00"),
	TC		("raw", "x*0.004887586", "0.00"),
	THERM	("raw", "x", "0.00"),
	RPM_4	("RPM", "60/(x*0.00001)", "0");
	
	private final String units;
	private final String expression;
	private final String format;
	
	TESensorConversions(String units, String expression, String format) {
		this.units = units;
		this.expression = expression;
		this.format = format;
	}

	public String units() 		{ return units; }
	public String expression()  { return expression; }
	public String format() 		{ return format; }
}
