/*
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2019 RomRaider.com
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

import static com.romraider.maps.checksum.NissanChecksum.END;
import static com.romraider.maps.checksum.NissanChecksum.START;
import static com.romraider.maps.checksum.NissanChecksum.SUMLOC;
import static com.romraider.maps.checksum.NissanChecksum.SUMT;
import static com.romraider.maps.checksum.NissanChecksum.XORLOC;
import static com.romraider.maps.checksum.NissanChecksum.XORT;
import static com.romraider.xml.RomAttributeParser.parseByteValue;

import java.util.Map;

import com.romraider.Settings;

public final class CalculateSTD implements Calculator {

    public CalculateSTD() {
    }

    public final void calculate(
            Map<String, Integer> range,
            byte[] binData,
            Map<String, Integer> results) {

        int sumt = 0;
        int xort = 0;
        int dw = 0;
        for (int i = range.get(START); i < range.get(END); i += 4) {
            if ((i == range.get(SUMLOC)) || (i == range.get(XORLOC))) continue;
            dw = (int)parseByteValue(binData, Settings.Endian.BIG, i, 4, true);
            sumt += dw;
            xort ^= dw;
        }
        results.put(SUMT, sumt);
        results.put(XORT, xort);
    }
}
