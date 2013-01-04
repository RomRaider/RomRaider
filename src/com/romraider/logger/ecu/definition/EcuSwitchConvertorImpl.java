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

package com.romraider.logger.ecu.definition;

import com.romraider.logger.ecu.ui.handler.dash.GaugeMinMax;
import static com.romraider.util.ParamChecker.checkBit;

public final class EcuSwitchConvertorImpl implements EcuDataConvertor {
    private static final GaugeMinMax GAUGE_MIN_MAX = new GaugeMinMax(0.0, 1.0, 1.0);
    private static final String FORMAT = "0";
    private final int bit;

    public EcuSwitchConvertorImpl(int bit) {
        checkBit(bit);
        this.bit = bit;
    }

    public double convert(byte[] bytes) {
        return (bytes[0] & (1 << bit)) > 0 ? 1 : 0;
    }

    public String getUnits() {
        return "On/Off";
    }

    public GaugeMinMax getGaugeMinMax() {
        return GAUGE_MIN_MAX;
    }

    public String getFormat() {
        return FORMAT;
    }

    public String format(double value) {
        //return value > 0 ? "On" : "Off";
        return value > 0 ? "1" : "0";
    }

    public String toString() {
        return getUnits();
    }

}
