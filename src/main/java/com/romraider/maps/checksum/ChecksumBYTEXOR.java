/*
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2020 RomRaider.com
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

import java.util.Map;

import com.romraider.Settings;
import com.romraider.util.HexUtil;

/**
 * This class implements the XOR single byte checksum validation and calculations
 * for some BMW non-engine ECU ROMs.
 */
    public final class ChecksumBYTEXOR implements ChecksumManager {
    private static final String START = "start";
    private static final String END = "end";
    private static final String XORLOC = "xorloc";
    private int start;
    private int end;
    private int xorloc;
    private byte xort;  

    @Override
    public void configure(Map<String, String> vars) {
        this.start = HexUtil.hexToInt(vars.get(START));
        this.end = HexUtil.hexToInt(vars.get(END));
        this.xorloc = HexUtil.hexToInt(vars.get(XORLOC));
    }
    
	@Override
	public int getNumberOfChecksums() {
		return 1;
	}

    @Override
    public int validate(byte[] binData) {
        calculate(binData);
        int valid = 0;
        
        if(xort == (byte)parseByteValue(binData, Settings.Endian.BIG, xorloc, 1, false))
        	valid++;
        	
        return valid;  	
    }

    @Override
    public int update(byte[] binData) {
    	int updateNeeded = 0;
		calculate(binData);
		
		if(binData[xorloc] != xort) updateNeeded++;
		
    	binData[xorloc] = xort;	
    	
    	return updateNeeded;
    }

    private void calculate(byte[] binData) {
        xort = 0;
        int dw = 0;
        for (int i = start; i < end; i += 1) {
            if ((i == xorloc)) continue;
            dw = (byte)parseByteValue(binData, Settings.Endian.BIG, i, 1, false);
            xort ^= dw;
        }
    }
}