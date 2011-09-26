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

package com.romraider.logger.external.plx.plugin;

import com.romraider.logger.external.core.ExternalSensorConversions;

public enum PlxSensorConversions implements ExternalSensorConversions {
	LAMBDA	("Lambda", "(x/3.75+68)/100", "0.00"),
	AFR_147	("AFR Gasoline", "(x/2.55+100)/10", "0.00"),  // gasoline
	AFR_90	("AFR Ethonal", "(x/4.167+61.7)/10", "0.00"), // ethanol
	AFR_146	("AFR Diesel", "(x/2.58+100)/10", "0.00"),    // diesel
	AFR_64	("AFR Methonal", "(x/5.856+43.5)/10", "0.00"),// methanol
	AFR_155	("AFR LPG", "(x/2.417+105.6)/10", "0.00"), 	  // LPG
	AFR_172	("AFR CNG", "(x/2.18+117)/10", "0.00"), 	  // CNG
	AFR_34	("AFR Hydrogen", "(x/3.75+68)*0.34", "0.00"), // Hydrogen
	DEG_C	("C", "x", "0.0"),
	DEG_F	("F", "(x/.555+32)", "0.0"),
	VACUUM_IN	("in/Hg", "-(x/11.39-29.93)", "0.00"),
	VACUUM_MM	("mm/Hg", "-(x*2.23+760.4)", "0.00"),
	BOOST_PSI	("psi", "x/22.73", "0.00"),
	BOOST_BAR	("bar", "x*0.00303333", "0.00"), // converts from PSI to bar
	BOOST_KPA	("kPa", "x*0.30333292", "0.0"),  // converts from PSI to kpa
	BOOST_KGCM2	("kg/cm^2", "x/329.47", "0.00"),
	RPM		("rpm", "x*19.55", "0"),
	MPH		("mph", "x/6.39", "0.0"),
	KPH		("kph", "x/3.97", "0.0"),
	PERCENT	("%", "x", "0.0"),
	FLUID_PSI	("psi", "x/5.115", "0.00"),
	FLUID_BAR	("bar", "x/74.22", "0.00"),
	FLUID_KPA	("kPa", "x*1.34794864", "0.00"), 	 // converts from PSI to kpa
	FLUID_KGCM2	("kg/cm^2", "x/72.73", "0.00"),
	DEGREES	("deg", "x-64", "0.00"),
	MAF_GS	("g/sec", "x", "0.00"),
	MAF_LB	("lb/min", "x/7.54", "0.00"),
	FUEL_TRIM	("%", "x-100", "0.00"),
	NB_P	("%", "x", "0.00"),
	NB_V	("vdc", "x/78.43", "0.00"),
	BATTERY	("vdc", "x/51.15", "0.00"),
	KNOCK_VDC	("vdc", "x/204.6", "0.00"),
	DC_POS	("+%", "x/10.23", "0.0"),
	DC_NEG	("-%", "100-(x/10.23)", "0.0");
	
	private final String units;
	private final String expression;
	private final String format;
	
	PlxSensorConversions(String units, String expression, String format) {
		this.units = units;
		this.expression = expression;
		this.format = format;
	}

	public String units() 		{ return units; }
	public String expression()  { return expression; }
	public String format() 		{ return format; }
}
