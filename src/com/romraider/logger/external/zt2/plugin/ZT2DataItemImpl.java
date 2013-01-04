/*
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2012 RomRaider.com
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

package com.romraider.logger.external.zt2.plugin;

import static com.romraider.logger.external.core.ExternalDataConvertorLoader.loadConvertors;

import com.romraider.logger.ecu.definition.EcuDataConvertor;
import com.romraider.logger.external.core.ExternalSensorConversions;

public final class ZT2DataItemImpl implements ZT2DataItem {
    private EcuDataConvertor[] convertors;
    private final String name;
    private int[] raw;

    public ZT2DataItemImpl(String name, ExternalSensorConversions... convertorList) {
        super();
        this.name = name;
        convertors = new EcuDataConvertor[convertorList.length];
        convertors = loadConvertors(this, convertors, convertorList);
    }

    public String getName() {
        return "Zeitronix ZT-2 " + name;
    }

    public String getDescription() {
        return "Zeitronix ZT-2 " + name + " data";
    }

    public double getData() {
        if (name.equalsIgnoreCase("MAP") && ((raw[1] & 128) == 128)) {
            // special handling on high byte - if 8th bit is 1 (means that it's negative)
            // We are supposed to clear the 8 bit, calc, then restore the sign.
            return 0 - (raw[0] + (raw[1] & ~(1 << 7)) * 256d);
        }
        else {
            if (raw.length == 1)
                return raw[0];
            if (raw.length == 2)
                return raw[0] + (raw[1] * 256d);
        }
        return 0;
    }

    public void setRaw(int... raw) {
        this.raw = raw;
    }

    public EcuDataConvertor[] getConvertors() {
        return convertors;
    }
}
