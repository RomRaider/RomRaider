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

package com.romraider.logger.fourteenpoint7.plugin;

import com.romraider.logger.ecu.external.ExternalDataItem;

public final class NawDataItem implements ExternalDataItem, NawDataListener {
    private final NawConvertor convertor = new NawConvertorImpl();
    private byte[] bytes;

    public String getName() {
        return "14Point7 NAW_7S UEGO";
    }

    public String getDescription() {
        return "14Point7 NAW_7S AFR data";
    }

    public String getUnits() {
        return "AFR";
    }

    public double getData() {
        if (bytes == null) return 0.0;
        return convertor.convert(bytes);
    }

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }
}
