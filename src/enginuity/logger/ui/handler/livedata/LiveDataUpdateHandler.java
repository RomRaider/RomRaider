/*
 *
 * Enginuity Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006 Enginuity.org
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
 *
 */

package enginuity.logger.ui.handler.livedata;

import enginuity.logger.definition.ConvertorUpdateListener;
import enginuity.logger.definition.EcuData;
import enginuity.logger.ui.handler.DataUpdateHandler;

public final class LiveDataUpdateHandler implements DataUpdateHandler, ConvertorUpdateListener {
    private final LiveDataTableModel dataTableModel;

    public LiveDataUpdateHandler(LiveDataTableModel dataTableModel) {
        this.dataTableModel = dataTableModel;
    }

    public void registerData(EcuData ecuData) {
        // add to datatable
        dataTableModel.addParam(ecuData);
    }

    public void handleDataUpdate(EcuData ecuData, byte[] value, long timestamp) {
        // update data table
        dataTableModel.updateParam(ecuData, value);
    }

    public void deregisterData(EcuData ecuData) {
        // remove from datatable
        dataTableModel.removeParam(ecuData);
    }

    public void cleanUp() {
    }

    public void notifyConvertorUpdate(EcuData updatedEcuData) {
        dataTableModel.fireTableDataChanged();
    }
}
