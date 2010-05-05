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

package com.romraider.logger.innovate.lm2.mts.plugin;

import com.romraider.logger.ecu.external.ExternalDataItem;
import com.romraider.logger.innovate.generic.mts.plugin.DataListener;

public final class Lm2MtsDataItem implements ExternalDataItem, DataListener {
    private double data;

    public String getName() {
        return "Innovate LM-2 [mts]";
    }

    public String getDescription() {
        return "Innovate LM-2 AFR data";
    }

    public String getUnits() {
        return "AFR";
    }

    public double getData() {
        return data;
    }

    public void setData(double data) {
        this.data = data;
    }
}