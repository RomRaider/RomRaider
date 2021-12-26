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
 * This class implements the Single Checksum for some older Motronic Ecus
 */
    public class ChecksumMOTRONICSINGLE implements ChecksumManager {
    protected static final String START = "start";
    protected static final String END = "end";
    protected static final String LOC = "loc";
    protected static final String INITIAL = "initial";
    
    protected int start;
    protected int end;
    protected int loc;
    protected short initial = 0;

    @Override
    public void configure(Map<String, String> vars) {
        this.start = HexUtil.hexToInt(vars.get(START));
        this.end = HexUtil.hexToInt(vars.get(END));
        this.loc = HexUtil.hexToInt(vars.get(LOC));
        
        if(vars.get(INITIAL) != null)
        	this.initial = (short) HexUtil.hexToInt(vars.get(INITIAL));
    }
    
	@Override
	public int getNumberOfChecksums() {
		return 1;
	}

    @Override
    public int validate(byte[] binData) {
        short checksum = calculate(initial, binData, start, end);
        short checksumInBin = (short) parseByteValue(binData, Settings.Endian.BIG, loc, 2, false);
        int valid = 0;
        
        if(checksum == checksumInBin)
        	valid++;
        	
        return valid;  	
    }

    @Override
    public int update(byte[] binData) {
    	int updateNeeded = 0;
		short checksum = calculate(initial, binData, start, end);
		short checksumInBin = (short)parseByteValue(binData, Settings.Endian.BIG, loc, 2, false);
		
		if(checksumInBin != checksum) {
			updateNeeded++;
			
	    	binData[loc] = (byte)((checksum >> 8) & 0xFF);
	    	binData[loc+1] = (byte)((checksum) & 0xFF);
		}
		 	
    	return updateNeeded;
    }
    
    public static short calculate(short initalValue, byte[] binData, int startAddress, int endAddress) {
        short value = initalValue;
        for (int i = startAddress; i <= endAddress; i++) {
        	value += parseByteValue(binData, Settings.Endian.BIG, i, 1, false);
        }
        
        return value;
    }
}