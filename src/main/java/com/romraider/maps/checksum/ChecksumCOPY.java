/*
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2022 RomRaider.com
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

import java.util.Map;
import com.romraider.util.HexUtil;

/**
 * This class implements a copy "checksum", which can be useful for old Motronic
 * ECUs. It simply checks if an array of data is identical to a specified
 * location and copies it over if necessary
 */
public final class ChecksumCOPY implements ChecksumManager {
	private static final String START = "start";
	private static final String END = "end";
	private static final String LOC = "loc";
	private int start; // Start of checksum area
	private int end; // End of area (inclusive)
	private int loc; // Start of location where data is copied

	@Override
	public void configure(Map<String, String> vars) {
		if (start >= end) {
			this.start = HexUtil.hexToInt(vars.get(START));
			this.end = HexUtil.hexToInt(vars.get(END));
			this.loc = HexUtil.hexToInt(vars.get(LOC));
		}
	}

	@Override
	public int getNumberOfChecksums() {
		return 1;
	}

	@Override
	public int validate(byte[] binData) {
		for (int i = 0; i <= end - start; i++) {
			if (binData[start + i] != binData[loc + i]) {
				return 0;
			}
		}
		return 1;
	}

	@Override
	public int update(byte[] binData) {
		int updateNeeded = 1 - validate(binData);

		if (updateNeeded > 0) {
			System.arraycopy(binData, start, binData, loc, end - start + 1);
		}
		return updateNeeded;
	}
}