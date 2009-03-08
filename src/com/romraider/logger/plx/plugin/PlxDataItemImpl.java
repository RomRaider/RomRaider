/*
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2008 RomRaider.com
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

package com.romraider.logger.plx.plugin;

import com.romraider.logger.plx.io.PlxSensorUnits;
import com.romraider.logger.plx.io.PlxSensorType;

public final class PlxDataItemImpl implements PlxDataItem {
    private final PlxConvertor convertor = new PlxConvertorImpl();
    private final PlxSensorUnits sensorUnits;
    private final PlxSensorType sensorType;
    private final String units;
    private final String name;
    private int raw;

    public PlxDataItemImpl(String name, String units, PlxSensorType sensorType, PlxSensorUnits sensorUnits) {
        this.sensorType = sensorType;
        this.sensorUnits = sensorUnits;
        this.units = units;
        this.name = name;
    }

    public String getName() {
        return "PLX " + name;
    }

    public String getDescription() {
        return "PLX " + name + " data";
    }

    public String getUnits() {
        return units;
    }

    public double getData() {
        return convertor.convert(raw, sensorType, sensorUnits);
    }

    public void setRaw(int raw) {
        this.raw = raw;
    }
}