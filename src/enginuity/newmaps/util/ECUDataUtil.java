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
import enginuity.newmaps.ecumetadata.Table3DMetadata;
import enginuity.newmaps.ecumetadata.TableMetadata;
import enginuity.newmaps.ecumetadata.Scale;

public final class ECUDataUtil {
    
    public static TableData getTableData(byte[] data, TableMetadata metadata) {
        
        if (metadata instanceof Table3DMetadata) {           
            
        }
        
        return null;
    }
    
    
    public static byte[] getTableAsBytes() {
        // TODO: build byte arrays from table data
        return null;
    }
    
    
}
