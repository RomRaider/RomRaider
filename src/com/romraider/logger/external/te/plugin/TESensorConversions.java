/*
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2012 RomRaider.com
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

import com.romraider.logger.ecu.ui.handler.dash.GaugeMinMax;
import com.romraider.logger.external.core.ExternalSensorConversions;

public enum TESensorConversions implements ExternalSensorConversions {
    LAMBDA    ("Lambda", "x/8192+0.5", "0.00", new GaugeMinMax(0.6,1.4,0.08)),
    AFR_147    ("AFR Gasoline", "(x/8192+0.5)*14.7", "0.00", new GaugeMinMax(9,20,1)),// gasoline
    AFR_90    ("AFR Ethonal", "(x/8192+0.5)*9.0", "0.00", new GaugeMinMax(5,12,1)),  // ethanol
    AFR_146    ("AFR Diesel", "(x/8192+0.5)*14.6", "0.00", new GaugeMinMax(9,20,1)),  // diesel
    AFR_64    ("AFR Methonal", "(x/8192+0.5)*6.4", "0.00", new GaugeMinMax(4,9,1)), // methanol
    AFR_155    ("AFR LPG", "(x/8192+0.5)*15.5", "0.00", new GaugeMinMax(9,20,1)),       // LPG
    AFR_172    ("AFR CNG", "(x/8192+0.5)*17.2", "0.00", new GaugeMinMax(9,20,1)),       // CNG
    AFR_34    ("AFR Hydrogen", "(x/8192+0.5)*34", "0.00", new GaugeMinMax(20,46,2.5)),  // Hydrogen
    VDC        ("VDC", "x*0.000610948", "0.00", new GaugeMinMax(0,12,1)),
    TC        ("raw", "x*0.004887586", "0.00", new GaugeMinMax(0,1000,100)),
    THERM    ("raw", "x", "0.00", new GaugeMinMax(0,1000,100)),
    RPM_4    ("RPM", "60/(x*0.00001)", "0", new GaugeMinMax(0,10000,1000));
    
    private final String units;
    private final String expression;
    private final String format;
    private final GaugeMinMax gaugeMinMax;
    
    TESensorConversions(String units, String expression, String format, GaugeMinMax gaugeMinMax) {
        this.units = units;
        this.expression = expression;
        this.format = format;
        this.gaugeMinMax = gaugeMinMax;
    }

    public String units()         { return units; }
    public String expression()  { return expression; }
    public String format()         { return format; }
    public GaugeMinMax gaugeMinMax() {return gaugeMinMax; }
}
