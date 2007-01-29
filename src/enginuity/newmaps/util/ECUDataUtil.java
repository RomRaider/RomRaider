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

import enginuity.newmaps.ecudata.TableData;
import enginuity.newmaps.ecumetadata.Scale;
import enginuity.newmaps.ecumetadata.Table3DMetadata;
import enginuity.newmaps.ecumetadata.TableMetadata;
import enginuity.util.ByteUtil;
import enginuity.util.JEPUtil;

public final class ECUDataUtil {
    
    public static TableData getTableData(byte[] data, TableMetadata metadata) {
        
        if (metadata instanceof Table3DMetadata) {           
            
        }
        
        return null;
    }
    
    
    public static float binaryToReal(byte[] data, int dataType, int endian, String to_real) {
        
        float output = 0;
        
        // Check endian
        if (endian == Scale.ENDIAN_LITTLE) data = ByteUtil.reverseEndian(data);

        // Calculate ECU values
        if (dataType == Scale.STORAGE_TYPE_FLOAT) output = ByteUtil.asFloat(data);
        else if (dataType == Scale.STORAGE_TYPE_INT8) output = ByteUtil.asSignedInt(data);
        else if (dataType == Scale.STORAGE_TYPE_INT16) output = ByteUtil.asSignedInt(data);
        else if (dataType == Scale.STORAGE_TYPE_INT32) output = ByteUtil.asSignedInt(data);
        else if (dataType == Scale.STORAGE_TYPE_UINT8) output = ByteUtil.asUnsignedInt(data);
        else if (dataType == Scale.STORAGE_TYPE_UINT16) output = ByteUtil.asUnsignedInt(data);
        else if (dataType == Scale.STORAGE_TYPE_UINT32) output = ByteUtil.asUnsignedInt(data);
                
        return (float)JEPUtil.evaluate(to_real, (double)output);
    }
    
    
    public static byte[] realToBinary(float realValue, int dataType, int endian, String to_byte) {
        
        realValue = (float)JEPUtil.evaluate(to_byte, (double)realValue);
        
        if (dataType == Scale.STORAGE_TYPE_FLOAT) return ByteUtil.asBytes(realValue);
        else if (dataType == Scale.STORAGE_TYPE_INT8) return ByteUtil.asSignedBytes((int)realValue);
        else if (dataType == Scale.STORAGE_TYPE_INT16) return ByteUtil.asSignedBytes((int)realValue);
        else if (dataType == Scale.STORAGE_TYPE_INT32) return ByteUtil.asSignedBytes((int)realValue);
        else if (dataType == Scale.STORAGE_TYPE_UINT8) return ByteUtil.asUnsignedBytes((int)realValue);
        else if (dataType == Scale.STORAGE_TYPE_UINT16) return ByteUtil.asUnsignedBytes((int)realValue);
        else if (dataType == Scale.STORAGE_TYPE_UINT32) return ByteUtil.asUnsignedBytes((int)realValue);
        
        return null;
    }
    
    
    public static byte[] getTableAsBytes() {
        // TODO: build byte arrays from table data
        return null;
    }
    
    
}
