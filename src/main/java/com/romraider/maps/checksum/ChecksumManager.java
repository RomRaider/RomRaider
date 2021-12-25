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

import java.util.Map;

/**
 * The Checksum Manager interface is implemented by Checksum classes
 * which perform validation and calculation of ROM checksums upon open
 * and save.
 */
public interface ChecksumManager {

    /**
     * Once the ChecksumManager is created configure it with the
     * information needed to perform checksums.
     * @param	vars	- a map of variables specific to the type of
     * 					checksums being performed
     */
	void configure(Map<String, String> vars);
	
    /**
     * Returns the amount of checksums
     * @return	Number of total checksums
     */
	int getNumberOfChecksums();
	
    /**
     * Perform the checksum validation upon ROM file loading.
     * @param	binData	- the ROM file to validate
     * @return	Number of correct checksums
     */
	int validate(byte[] data);

    /**
     * Update the checksum upon saving the ROM file.
     * @param	data - the ROM file to update
     * @return Number of checksums which needed to be updated
     */
	int update(byte[] data);
}
