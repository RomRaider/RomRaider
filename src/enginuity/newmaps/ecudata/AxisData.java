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

import enginuity.newmaps.ecumetadata.AxisMetadata;
import enginuity.newmaps.ecumetadata.Scale;
import enginuity.newmaps.util.ECUDataUtil;

public class AxisData extends TableData {

    public AxisData(byte[] data, AxisMetadata metadata) {
        this.metadata = metadata;
        populate(data);
    }

    public boolean populate(byte[] data) {
        Scale scale = metadata.getScale();
        /*float values[] = ECUDataUtil.calcRealValues(data, scale.getStorageType(),
                                    metadata.getAddress(), metadata.getSize(),
                                    scale.getEndian(), scale.getUnit().getTo_real());*/
        return true;
    }

    public int getSize() {
    	return metadata.getSize();
    }

    public byte[] returnValues() {

        return null;
    }

}
