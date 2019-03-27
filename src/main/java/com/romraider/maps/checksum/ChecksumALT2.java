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
}
