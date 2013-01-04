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

package com.romraider.logger.external.fourteenpoint7.plugin;

import static com.romraider.logger.external.core.ExternalDataConvertorLoader.loadConvertors;

import com.romraider.logger.ecu.definition.EcuDataConvertor;
import com.romraider.logger.external.core.ExternalDataItem;
import com.romraider.logger.external.core.ExternalSensorConversions;
import com.romraider.logger.external.core.RawDataListener;

public final class NawDataItem implements ExternalDataItem, RawDataListener {
    private final NawConvertor convertor = new NawConvertorImpl();
    private EcuDataConvertor[] convertors;
    private byte[] bytes;

    public NawDataItem(ExternalSensorConversions... convertorList) {
        super();
        convertors = new EcuDataConvertor[convertorList.length];
        convertors = loadConvertors(this, convertors, convertorList);
    }

    public String getName() {
        return "14Point7 NAW_7S UEGO";
    }

    public String getDescription() {
        return "14Point7 NAW_7S Wideband data";
    }

    public double getData() {
        if (bytes == null) return 0.0;
        return convertor.convert(bytes);
    }

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }

    public EcuDataConvertor[] getConvertors() {
        return convertors;
    }
}
