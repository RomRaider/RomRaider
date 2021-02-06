/*
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2021 RomRaider.com
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

package com.romraider.maps.checksum;

import static com.romraider.xml.RomAttributeParser.parseByteValue;
import static com.romraider.xml.RomAttributeParser.parseIntegerValue;

import java.util.Map;

import com.romraider.Settings;
import com.romraider.util.HexUtil;

/**
 * This class implements the "alt2" checksum validation and calculations
 * for Nissan ROMs.
 */
public final class ChecksumALT2 extends NissanChecksum {
    protected static final String SKIPLOC = "skiploc";

    public ChecksumALT2() {
        calculator = new CalculateALT2();
    }

    public void configure(Map<String, String> vars) {
        super.configure(vars);
        if (vars.containsKey(SKIPLOC)) {
            range.put(SKIPLOC, HexUtil.hexToInt(vars.get(SKIPLOC)));
        }
        else {
            range.put(SKIPLOC, 0x20000);
        }
    }

    public boolean validate(byte[] binData) {
        calculator.calculate(range, binData, results);
        final boolean valid =
                (results.get(SUMT) == (int)parseByteValue(binData, Settings.Endian.BIG, range.get(SUMLOC), 4, true)) &&
                (results.get(XORT) == (int)parseByteValue(binData, Settings.Endian.BIG, range.get(XORLOC), 4, true)) &&
                (results.get(START) == (short)parseByteValue(binData, Settings.Endian.BIG, range.get(START), 2, false)) &&
                (results.get(SKIPLOC) == (short)parseByteValue(binData, Settings.Endian.BIG, range.get(SKIPLOC), 2, false));
            return valid;
    }

    public void update(byte[] binData) {
        super.update(binData);
        System.arraycopy(parseIntegerValue(results.get(START), Settings.Endian.BIG, 2), 0, binData, range.get(START), 2);
        System.arraycopy(parseIntegerValue(results.get(SKIPLOC), Settings.Endian.BIG, 2), 0, binData, range.get(SKIPLOC), 2);
    }
}
