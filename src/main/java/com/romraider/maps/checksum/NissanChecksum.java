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

import static com.romraider.xml.RomAttributeParser.parseByteValue;
import static com.romraider.xml.RomAttributeParser.parseIntegerValue;

import java.util.HashMap;
import java.util.Map;

import com.romraider.Settings;
import com.romraider.util.HexUtil;

/**
 * This class provides common methods implemented by different
 * checksum calculation type classes.
 */
    abstract class NissanChecksum implements ChecksumManager {
        public static final String START = "start";
        public static final String END = "end";
        public static final String SUMLOC = "sumloc";
        public static final String XORLOC = "xorloc";
        public static final String SUMT = "sumt";
        public static final String XORT = "xort";
        protected final Map<String, Integer> range = new HashMap<String, Integer>();
        protected final Map<String, Integer> results = new HashMap<String, Integer>();
        protected Calculator calculator;

    @Override
    public void configure(Map<String, String> vars) {
        range.put(START, HexUtil.hexToInt(vars.get(START)));
        range.put(END, HexUtil.hexToInt(vars.get(END)));
        range.put(SUMLOC, HexUtil.hexToInt(vars.get(SUMLOC)));
        range.put(XORLOC, HexUtil.hexToInt(vars.get(XORLOC)));
    }

    @Override
    public int getNumberOfChecksums() {
    	return 2;
    }

    @Override
    public int validate(byte[] binData) {
        calculator.calculate(range, binData, results);
        int valid = 0;

        if(results.get(SUMT) == (int)parseByteValue(binData, Settings.Endian.BIG, range.get(SUMLOC), 4, true)) {
        	valid++;
        }

        if((results.get(XORT) == (int)parseByteValue(binData, Settings.Endian.BIG, range.get(XORLOC), 4, true))) {
        	valid++;
        }

        return valid;
    }

    @Override
    public int update(byte[] binData) {
        calculator.calculate(range, binData, results);
        System.arraycopy(parseIntegerValue(results.get(SUMT), Settings.Endian.BIG, 4), 0, binData, range.get(SUMLOC), 4);
        System.arraycopy(parseIntegerValue(results.get(XORT), Settings.Endian.BIG, 4), 0, binData, range.get(XORLOC), 4);
        return getNumberOfChecksums();
    }
}
