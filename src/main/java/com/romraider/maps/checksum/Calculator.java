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

import java.util.Map;

/**
 * The Calculator interface is implemented by CalculateXXX classes
 * which perform the actual checksum calculation.
 */
public interface Calculator {

    /**
     * Calculate the sum and xor total over the address range provided.
     * @param	range	- a map of with the address ranges use.
     * @param   binData - the binary data t calculate over.
     * @param   results - a map containing the keys for sumt and xort
     */
    void calculate(
            Map<String, Integer> range, byte[] binData, Map<String, Integer> results);
}
