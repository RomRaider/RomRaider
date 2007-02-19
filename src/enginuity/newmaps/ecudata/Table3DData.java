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

package enginuity.newmaps.ecudata;

import enginuity.newmaps.ecumetadata.Table3DMetadata;
import enginuity.newmaps.exception.DataPopulationException;
import enginuity.newmaps.util.ECUDataUtil;

public class Table3DData extends TableData {

    private DataCell[][] values;
    private AxisData xAxis;
    private AxisData yAxis;

    public Table3DData(byte[] data, Table3DMetadata metadata) throws DataPopulationException {
        this.metadata = metadata;

        xAxis = new AxisData(data, metadata.getXaxis());
        yAxis = new AxisData(data, metadata.getYaxis());

        populate(data);
    }

    public DataCell[][] getValues() {
        return values;
    }

    public DataCell getValueAt(int x, int y) throws IndexOutOfBoundsException {
        return values[x][y];
    }

    public byte[] returnValues() {
        // Returns updated byte values to ECUData

        // TODO: Find return values (using ECUDataUtil)
        return null;
    }

    public boolean populate(byte[] data) {

        // populate axes first
        xAxis.populate(data);
        yAxis.populate(data);

        // Now populate the table itself
        values = ECUDataUtil.buildValues(data, (Table3DMetadata)metadata);
        return true;
    }

}
