/*
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2015 RomRaider.com
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

import com.romraider.logger.ecu.ui.handler.dash.GaugeMinMax;

public enum SensorConversionsOther implements ExternalSensorConversions {
    AIR_DEG_C    ("C", "x", "0.0", new GaugeMinMax(-40,60,10)),
    AIR_DEG_F    ("F", "x", "0.0", new GaugeMinMax(-40,140,20)),
    AIR_DEG_F2C    ("C", "(x-32)*5/9", "0.0", new GaugeMinMax(-40,60,10)),
    AIR_DEG_C2F    ("F", "x*9/5+32 ", "0.0", new GaugeMinMax(-40,140,20)),
    EXHAUST_DEG_C    ("C", "x", "0.0", new GaugeMinMax(-40,1000,100)),
    EXHAUST_DEG_F    ("F", "x", "0.0", new GaugeMinMax(-40,2000,200)),
    EXHAUST_DEG_F2C    ("C", "(x-32)*5/9", "0.0", new GaugeMinMax(-40,1000,100)),
    EXHAUST_DEG_C2F    ("F", "x*9/5+32 ", "0.0", new GaugeMinMax(-40,2000,200)),
    FLUID_DEG_C    ("C", "x", "0.0", new GaugeMinMax(-40,160,20)),
    FLUID_DEG_F    ("F", "x", "0.0", new GaugeMinMax(-40,320,40)),
    FLUID_DEG_F2C    ("C", "(x-32)*5/9", "0.0", new GaugeMinMax(-40,160,20)),
    FLUID_DEG_C2F    ("F", "x*9/5+32 ", "0.0", new GaugeMinMax(-40,320,40)),
    AIR_ABS_PSI        ("psi", "x", "0.00", new GaugeMinMax(10,40,5)),
    AIR_ABS_PSI2BAR    ("bar", "x*0.0689475728", "0.00", new GaugeMinMax(0.5,4.5,0.5)),          // converts from PSI to bar
    AIR_ABS_PSI2KPA    ("kPa", "x*6.89475728", "0.0", new GaugeMinMax(100,300,20)),              // converts from PSI to kpa
    AIR_ABS_PSI2KGCM2    ("kg/cm^2", "x*0.0703068835943", "0.0", new GaugeMinMax(0.5,4.5,0.5)),// converts from PSI to kpa
    AIR_ABS_KPA2PSI    ("psi", "x*0.14503774", "0.00", new GaugeMinMax(10,40,5)),               // converts from kPa
    AIR_ABS_KPA2BAR    ("bar", "x*0.01", "0.00", new GaugeMinMax(0.5,4.5,0.5)),                    // converts from kPa
    AIR_ABS_KPA        ("kPa", "x", "0.0", new GaugeMinMax(100,300,20)),
    AIR_ABS_KPA2KGCM2    ("kg/cm^2", "x*0.01019716", "0.00", new GaugeMinMax(0.5,4.5,0.5)),     // converts from kPa
    AIR_REL_PSI        ("psi", "x", "0.00", new GaugeMinMax(-10,30,5)),
    AIR_REL_PSI2BAR    ("bar", "x*0.0689475728", "0.00", new GaugeMinMax(-0.5,2.5,0.3)),            // converts from PSI to bar
    AIR_REL_PSI2KPA    ("kPa", "x*6.89475728", "0.0", new GaugeMinMax(98,120,2)),                // converts from PSI to kpa
    AIR_REL_PSI2KGCM2    ("kg/cm^2", "x*0.0703068835943", "0.0", new GaugeMinMax(-0.5,2.5,0.3)),// converts from PSI to kpa
    AIR_REL_KPA2PSI    ("psi", "x*0.14503774", "0.00", new GaugeMinMax(-10,30,5)),             // converts from kPa
    AIR_REL_KPA2BAR    ("bar", "x*0.01", "0.00", new GaugeMinMax(-0.5,2.5,0.3)),                       // converts from kPa
    AIR_REL_KPA        ("kPa", "x", "0.0", new GaugeMinMax(98,120,2)),
    AIR_REL_KPA2KGCM2    ("kg/cm^2", "x*0.01019716", "0.00", new GaugeMinMax(-0.5,2.5,0.3)),     // converts from kPa
    FUEL_PSI        ("psi", "x", "0.00", new GaugeMinMax(0,50,5)),
    FUEL_PSI2BAR    ("bar", "x*0.0689475728", "0.00", new GaugeMinMax(0,4,0.5)),            // converts from PSI to bar
    FUEL_PSI2KPA    ("kPa", "x*6.89475728", "0.0", new GaugeMinMax(0,350,50)),                // converts from PSI to kpa
    FUEL_PSI2KGCM2    ("kg/cm^2", "x*0.0703068835943", "0.0", new GaugeMinMax(0,4,0.5)),// converts from PSI to kpa
    FUEL_KPA2PSI    ("psi", "x*0.14503774", "0.00", new GaugeMinMax(0,50,5)),             // converts from kPa
    FUEL_KPA2BAR    ("bar", "x*0.01", "0.00", new GaugeMinMax(0,4,0.5)),                       // converts from kPa
    FUEL_KPA        ("kPa", "x", "0.0", new GaugeMinMax(0,350,50)),
    FUEL_KPA2KGCM2    ("kg/cm^2", "x*0.01019716", "0.00", new GaugeMinMax(0,4,0.5)),     // converts from kPa
    OIL_PSI        ("psi", "x", "0.00", new GaugeMinMax(0,150,15)),
    OIL_PSI2BAR    ("bar", "x*0.0689475728", "0.00", new GaugeMinMax(0,10,1)),            // converts from PSI to bar
    OIL_PSI2KPA    ("kPa", "x*6.89475728", "0.0", new GaugeMinMax(0,1035,100)),                // converts from PSI to kpa
    OIL_PSI2KGCM2    ("kg/cm^2", "x*0.0703068835943", "0.0", new GaugeMinMax(0,11,1)),// converts from PSI to kpa
    OIL_KPA2PSI    ("psi", "x*0.14503774", "0.00", new GaugeMinMax(0,150,15)),             // converts from kPa
    OIL_KPA2BAR    ("bar", "x*0.01", "0.00", new GaugeMinMax(0,10,1)),                       // converts from kPa
    OIL_KPA        ("kPa", "x", "0.0", new GaugeMinMax(0,1035,100)),
    OIL_KPA2KGCM2    ("kg/cm^2", "x*0.01019716", "0.00", new GaugeMinMax(0,11,1)),     // converts from kPa
    MAF_GS    ("g/sec", "x", "0.00", new GaugeMinMax(0,400,50)),
    MAF_GS2LB    ("lb/min", "x/7.54", "0.00", new GaugeMinMax(0,50,5)),
    PERCENT    ("%", "x", "0.0", new GaugeMinMax(0,100,10)),
    ENGINE_RPM  ("rpm", "x", "0", new GaugeMinMax(0,15000,1500)),
    VOLTS_5DC("VDC", "x", "0.0", new GaugeMinMax(0,5,0.5)),
    VOLTS_12DC("VDC", "x", "0.0", new GaugeMinMax(0,15,1.5));

    
    private final String units;
    private final String expression;
    private final String format;
    private final GaugeMinMax gaugeMinMax;
    
    SensorConversionsOther(String units, String expression, String format, GaugeMinMax gaugeMinMax) {
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
