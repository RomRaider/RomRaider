/*
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2022 RomRaider.com
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

package com.romraider.maps;

import java.util.LinkedList;
import com.romraider.util.ByteUtil;

public class TableBitwiseSwitch extends TableSwitch {
	private static final long serialVersionUID = -4887718305447362308L;
	private LinkedList<Integer> bits = new LinkedList<Integer>();
	
	public TableBitwiseSwitch() {
		storageType = 1;		
	}

	@Override
    public void populateTable(Rom rom) throws ArrayIndexOutOfBoundsException, IndexOutOfBoundsException {      
    	if(isStaticDataTable()) return;       
        validateScaling();

        // temporarily remove lock;
        boolean tempLock = locked;
        locked = false;

        if (!beforeRam) {
            this.ramOffset = rom.getRomID().getRamOffset();
        }

        setDataSize(bits.size());
        int i = 0;
        for (int bit : bits) {
            data[i] = new DataCell(this, 0, rom); //Offset is always 0
            data[i].setBitMask(ByteUtil.bitToMask(bit));
            data[i].updateBinValueFromMemory();
            i++;
        }

        // reset locked status
        locked = tempLock;
        calcCellRanges();

        //Add Raw Scale
        addScale(new Scale());
    }
	
    @Override
    public void clearData() {   
    	super.clearData();
    	bits.clear();
    }	
	
    @Override
	public void setPresetValues(String name, String bitPos) {
    	if (bitPos != null && bitPos.length() > 0) {
	    	bits.add(Integer.parseInt(bitPos));
	    	super.setPresetValues(name, "1", bits.size() - 1);
    	}
	}
}
