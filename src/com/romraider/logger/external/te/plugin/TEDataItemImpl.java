/*
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2010 RomRaider.com
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

package com.romraider.logger.external.te.plugin;

import com.romraider.logger.external.te.plugin.TESensorUnits;

public final class TEDataItemImpl implements TEDataItem {
    private final TEConverter converter = new TEConverterImpl();
    private final TESensorUnits sensorUnits;
    private final TESensorType sensorType;
    private final String[] units;
    private final String name;
    private int[] raw;

    public TEDataItemImpl(String name, String[] units, TESensorType sensorType, TESensorUnits sensorUnits) {
        super();
        this.name = name;
        this.units = units;
        this.sensorType = sensorType;
        this.sensorUnits = sensorUnits;
    }

    public String getName() {
        return "Tech Edge " + name;
    }

    public String getDescription() {
        return "Tech Edge " + name + " data";
    }

    public String getUnits() {
        return units[0];
    }

    public double getData() {
        return converter.convert(sensorType, sensorUnits, raw);
    }

    public void setRaw(int... raw) {
        this.raw = raw;
    }
}
