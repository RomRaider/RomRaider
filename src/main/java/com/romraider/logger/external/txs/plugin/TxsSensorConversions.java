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

package com.romraider.logger.external.txs.plugin;

import com.romraider.logger.ecu.ui.handler.dash.GaugeMinMax;
import com.romraider.logger.external.core.ExternalSensorConversions;

public enum TxsSensorConversions implements ExternalSensorConversions{
    TXS_RPM        ("rpm", "x", "0", new GaugeMinMax(0,10000,1000)),
    TXS_BOOST    ("psi", "x", "0.0", new GaugeMinMax(-100,100,5)),
    TXS_MAFV    ("mafv", "x", "0.00", new GaugeMinMax(0,5,0.05)),
    TXS_TPS        ("tps", "x", "0", new GaugeMinMax(0,100,5)),
    TXS_LOAD    ("load", "x", "0", new GaugeMinMax(0,100,10)),
    TXS_KNOCK    ("knock", "x", "0", new GaugeMinMax(0,100,1)),
    TXS_IGN        ("ign", "x", "0.00", new GaugeMinMax(-15,60,5)),
    TXS_IDC        ("%", "x", "0", new GaugeMinMax(0,125,5)),
    TXS_MAPVE    ("mapve", "x", "0", new GaugeMinMax(0,200,5)),
    TXS_MODFUEL    ("modfuel %", "x", "0.00", new GaugeMinMax(-50,50,.05));
    
    private final String units;
    private final String expression;
    private final String format;
    private final GaugeMinMax gaugeMinMax;
    
    TxsSensorConversions(String units, String expression,
                         String format, GaugeMinMax gaugeMinMax) {
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
