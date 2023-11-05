/*
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2021 RomRaider.com
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
import com.romraider.Settings;

public class Table1D extends Table {
    private static final long serialVersionUID = -8747180767803835631L;
    private Table axisParent = null;
    
    @Override
    public TableType getType() {
        return TableType.TABLE_1D;
    }
    
    public void setAxisParent(Table axisParent) {
        this.axisParent = axisParent;
    }

    public Table getAxisParent() {
        return axisParent;
    }

    @Override
    public StringBuffer getTableAsString() {
        if(isStaticDataTable()) {
            StringBuffer output = new StringBuffer(Settings.BLANK);
            for (int i = 0; i < data.length; i++) {
                output.append(data[i].getStaticText());
                if (i < data.length - 1) {
                    output.append(Settings.TAB);
                }
            }
            return output;
        } else {
            return super.getTableAsString();
      
        }
    }
    
    @Override
    public double queryTable(Double input_x, Double input_y)  {
    	// No axis, so nothing to query
    	return 0;
    }
    
    @Override
    public void clearData() {
    	super.clearData();
    	axisParent = null;
    }

    @Override
    public String toString() {
        return super.toString() + " (1D)";
    }

    @Override
    public boolean isLiveDataSupported() {
        return false;
    }

    @Override
    public boolean isButtonSelected() {
        return true;
    }
    
    @Override
    public boolean equals(Object other) {
        try {
            if(null == other) {
                return false;
            }

            if(other == this) {
                return true;
            }

            if(!(other instanceof Table1D)) {
                return false;
            }

            Table1D otherTable = (Table1D)other;

            if(this.data.length != otherTable.data.length)
            {
                return false;
            }

            if(this.data.equals(otherTable.data))
            {
                return true;
            }

            // Compare Bin Values
            for(int i=0 ; i < this.data.length ; i++) {
                if(! this.data[i].equals(otherTable.data[i])) {
                    return false;
                }
            }

            return true;
        } catch(Exception ex) {
            // TODO: Log Exception.
            return false;
        }
    }
}

    