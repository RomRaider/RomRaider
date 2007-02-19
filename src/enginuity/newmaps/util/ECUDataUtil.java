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

package enginuity.newmaps.util;

import enginuity.newmaps.ecudata.DataCell;
import enginuity.newmaps.ecudata.TableData;
import enginuity.newmaps.ecumetadata.Scale;
import enginuity.newmaps.ecumetadata.Table3DMetadata;
import enginuity.newmaps.ecumetadata.TableMetadata;
import enginuity.util.ByteUtil;

public final class ECUDataUtil {

    public static byte[] getTableAsBytes() {
        // TODO: build byte arrays from table data
        return null;
    }


    //
    // Populate 1d and 2d tables
    //
    public static DataCell[] buildValues(byte[] input, TableMetadata metadata) {

    	DataCell[] output = new DataCell[metadata.getSize()];
    	int dataSize = ByteUtil.getLengthInBytes(metadata.getScale().getStorageType());
    	int storageType = metadata.getScale().getStorageType();
    	int endian = metadata.getScale().getEndian();
    	int address = metadata.getAddress();

    	for (int j = 0; j < metadata.getSize(); j++) {

    			// Build single datacell bytes
    			byte[] cellBytes = new byte[dataSize];
    			for (int i = 0; i < dataSize; i++) {
    				cellBytes[i] = input[address + dataSize * j];
    			}

    			// Get DataCell and add to array
    			output[j] = new DataCell(cellBytes, storageType, endian);
    	}
    	return output;
    }


    //
    // Populate 3d tables
    //
    public static DataCell[][] buildValues(byte[] input, Table3DMetadata metadata) {

    	DataCell[][] output = new DataCell[metadata.getSizeX()][metadata.getSizeY()];
    	int dataSize = ByteUtil.getLengthInBytes(metadata.getScale().getStorageType());
    	int storageType = metadata.getScale().getStorageType();
    	int endian = metadata.getScale().getEndian();
    	int address = metadata.getAddress();

    	for (int y = 0; y < metadata.getSizeY(); y++) {
    		for (int x = 0; x < metadata.getSizeX(); x++) {

    			// Build single datacell bytes
    			byte[] cellBytes = new byte[dataSize];
    			for (int i = 0; i < dataSize; i++) {
    				cellBytes[i] = input[address + dataSize * (y * metadata.getSizeX() + x)];
    			}

    			// Get DataCell and add to array
    			output[x][y] = new DataCell(cellBytes, storageType, endian);
    		}
    	}
    	return output;
    }


}
