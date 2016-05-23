/*
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2015 RomRaider.com
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

package com.romraider.logger.external.txs.plugin;

import static com.romraider.logger.external.core.ExternalDataConvertorLoader.loadConvertors;

import com.romraider.logger.ecu.definition.EcuDataConvertor;
import com.romraider.logger.external.core.DataListener;
import com.romraider.logger.external.core.ExternalDataItem;
import com.romraider.logger.external.core.ExternalSensorConversions;

public final class TxsDataItem implements ExternalDataItem, DataListener {
    private EcuDataConvertor[] convertors;
    private final String name;
    private final int itemIndex;
    private double data;

    public TxsDataItem(String name, int itemIndex, ExternalSensorConversions... convertorList) {
        super();
        this.name = name;
        this.itemIndex = itemIndex;
        convertors = new EcuDataConvertor[convertorList.length];
        convertors = loadConvertors(this, convertors, convertorList);
    }

    public int getItemIndex(){
        return itemIndex;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return name + " data";
    }

    public double getData() {
        return data;
    }

    public void setData(double data) {
        this.data = data;
    }

    public EcuDataConvertor[] getConvertors() {
        return convertors;
    }
}
