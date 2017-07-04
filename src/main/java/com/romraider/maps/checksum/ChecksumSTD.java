/*
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2017 RomRaider.com
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

import com.romraider.util.HexUtil;

/**
 * This class implements the "std" checksum validation and calculations
 * for Nissan ROMs.
 */
    public final class ChecksumSTD implements ChecksumManager {
    private static final String START = "start";
    private static final String END = "end";
    private static final String SUMLOC = "sumloc";
    private static final String XORLOC = "xorloc";
    private int start;
    private int end;
    private int sumloc;
    private int xorloc;
    private int sumt;
    private int xort;

    public ChecksumSTD() {
    }

    @Override
    public void configure(Map<String, String> vars) {
        this.start = HexUtil.hexToInt(vars.get(START));
        this.end = HexUtil.hexToInt(vars.get(END));
        this.sumloc = HexUtil.hexToInt(vars.get(SUMLOC));
        this.xorloc = HexUtil.hexToInt(vars.get(XORLOC));
    }

    @Override
    public boolean validate(byte[] binData) {
        calculate(binData);
        final boolean valid =
                (sumt == (int)parseByteValue(binData, 0, sumloc, 4, true)) &&
                (xort == (int)parseByteValue(binData, 0, xorloc, 4, true));
        return valid;
    }

    @Override
    public void update(byte[] binData) {
        calculate(binData);
        System.arraycopy(parseIntegerValue(sumt, 0, 4), 0, binData, sumloc, 4);
        System.arraycopy(parseIntegerValue(xort, 0, 4), 0, binData, xorloc, 4);
    }

    private void calculate(byte[] binData) {
        sumt = 0;
        xort = 0;
        int dw = 0;
        for (int i = start; i < end; i += 4) {
            if ((i == sumloc) || (i == xorloc)) continue;
            dw = (int)parseByteValue(binData, 0, i, 4, true);
            sumt += dw;
            xort ^= dw;
        }
    }
}
