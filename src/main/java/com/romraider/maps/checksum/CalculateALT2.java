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

import static com.romraider.maps.checksum.ChecksumALT2.SKIPLOC;
import static com.romraider.maps.checksum.NissanChecksum.END;
import static com.romraider.maps.checksum.NissanChecksum.START;
import static com.romraider.maps.checksum.NissanChecksum.SUMLOC;
import static com.romraider.maps.checksum.NissanChecksum.SUMT;
import static com.romraider.maps.checksum.NissanChecksum.XORLOC;
import static com.romraider.maps.checksum.NissanChecksum.XORT;
import static com.romraider.xml.RomAttributeParser.parseByteValue;

import java.util.Map;

import com.romraider.Settings;

public final class CalculateALT2 implements Calculator {

    public CalculateALT2() {
    }

    public final void calculate(
            Map<String, Integer> range,
            byte[] binData,
            Map<String, Integer> results) {

        int sumt = 0;
        int xort = 0;
        int dw = 0;
        for (int i = range.get(START) + 4; i < range.get(END); i += 4) {
            if ((i == range.get(SUMLOC))
                    || (i == range.get(XORLOC)
                    || (i == range.get(SKIPLOC)))) continue;
            dw = (int)parseByteValue(binData, Settings.Endian.BIG, i, 4, true); //this works, but shouldn't it be unsigned?
            sumt += dw;
            xort ^= dw;
        }
        results.put(SUMT, sumt);
        results.put(XORT, xort);
        //need to save SUMT and XORT before doing 16bit checksums
        short sumCAL = 0;
        short sumCODE = 0;
        short d = 0;
        for (int i = range.get(START) + 2; i < range.get(SKIPLOC); i += 2) {
            if (i == range.get(SUMLOC)){
                dw = results.get(SUMT);
                sumCAL += (short)((dw >> 16) & 0xffff);
                sumCAL += (short)(dw & 0xffff);
                i += 2; //skip over final 2 bytes of SUMT
                continue;
            }
            if (i == range.get(XORLOC)){
                dw = results.get(XORT);
                sumCAL += (short)((dw >> 16) & 0xffff);
                sumCAL += (short)(dw & 0xffff);
                i += 2; //skip over final 2 bytes of XORT
                continue;
            }        
            d = (short)parseByteValue(binData, Settings.Endian.BIG, i, 2, false); 
            sumCAL += d;
        }
        for (int i = range.get(SKIPLOC) + 2; i < range.get(END); i += 2) {
            d = (short)parseByteValue(binData, Settings.Endian.BIG, i, 2, false);
            sumCODE += d;
        }
        results.put(START, (int)sumCAL); //will this work casting as int?
        results.put(SKIPLOC, (int)sumCODE); //will this work casting as int?
    }
}
