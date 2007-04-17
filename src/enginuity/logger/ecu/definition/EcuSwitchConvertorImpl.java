/*
 *
 * Enginuity Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006 Enginuity.org
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
 *
 */

package enginuity.logger.ecu.definition;

import static enginuity.util.ParamChecker.checkBit;

public final class EcuSwitchConvertorImpl implements EcuDataConvertor {
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

    public String format(double value) {
        //return value > 0 ? "On" : "Off";
        return value > 0 ? "1" : "0";
    }

    public String toString() {
        return getUnits();
    }

}
