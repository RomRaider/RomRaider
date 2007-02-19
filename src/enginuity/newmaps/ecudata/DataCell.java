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

import enginuity.newmaps.ecumetadata.Scale;
import enginuity.util.ByteUtil;

public class DataCell extends Number {

    byte[] bytes;
    int endian;
    int storageType;

    public DataCell(byte[] bytes, int storageType, int endian) {
        this.bytes = bytes;
        this.storageType = storageType;
        this.endian = endian;
    }

    public int intValue() {
        return ByteUtil.asSignedInt(bytes, endian);
    }

    public long longValue() {
        // TODO: this..
        return 0;
    }

    public float floatValue() {
        return ByteUtil.asFloat(bytes, endian);
    }

    public double doubleValue() {
        // TODO: this..
        return 0;
    }

    public byte[] byteValues() {
        return bytes;
    }

    public String toString() {
    	if (storageType == Scale.STORAGE_TYPE_CHAR) {

    		return new String(bytes);


    	} else if (storageType == Scale.STORAGE_TYPE_FLOAT) {

    		return ByteUtil.asFloat(bytes, endian)+"";


    	} else if (storageType == Scale.STORAGE_TYPE_HEX) {

    		// TODO: Hex..
    		return "Hex";


    	} else if (storageType == Scale.STORAGE_TYPE_INT8 ||
    				storageType == Scale.STORAGE_TYPE_INT16 ||
    				storageType == Scale.STORAGE_TYPE_INT32) {

    		return ByteUtil.asSignedInt(bytes, endian)+"";


    	} else if (storageType == Scale.STORAGE_TYPE_UINT8 ||
					storageType == Scale.STORAGE_TYPE_UINT16 ||
					storageType == Scale.STORAGE_TYPE_UINT32) {

    		return ByteUtil.asUnsignedInt(bytes, endian)+"";


    	} else {
    		return "Blah";
    	}
    }

}
