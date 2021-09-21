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

public class Table1D extends Table {
    private static final long serialVersionUID = -8747180767803835631L;
    private Table axisParent = null;
    private final TableType type;
    
    public Table1D(TableType type) {
        this.type = type;
    }

    @Override
    public TableType getType() {
        return type;
    }

    public void setAxisParent(Table axisParent) {
        this.axisParent = axisParent;
    }

    public Table getAxisParent() {
        return axisParent;
    }
    
	@Override
	public byte[] saveFile(byte[] binData) {
		return binData;
	}
	
    @Override
    public void populateTable(byte[] input, int romRamOffset) throws ArrayIndexOutOfBoundsException, IndexOutOfBoundsException  {
        super.populateTable(input, romRamOffset);
        /*
    	centerLayout.setRows(1);
        centerLayout.setColumns(this.getDataSize());

        // add to table
        for (int i = 0; i < this.getDataSize(); i++) {
            centerPanel.add(this.getDataCell(i));
        }

        if(null == name || name.isEmpty()) {
            ;// Do not add label.
        } else if(null == getCurrentScale () || "0x" == getCurrentScale().getUnit()) {
            // static or no scale exists.
            tableLabel = new JLabel(getName(), JLabel.CENTER);
            add(tableLabel, BorderLayout.NORTH);
        } else {
            tableLabel = new JLabel(getName() + " (" + getCurrentScale().getUnit() + ")", JLabel.CENTER);
            add(tableLabel, BorderLayout.NORTH);
        }
        
        if(tableLabel != null)
        	tableLabel.setBorder(new EmptyBorder(2, 4, 2, 4));
        
        if(presetPanel != null) presetPanel.populatePanel();*/
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

    public boolean isAxis() {
        return getType() == Table.TableType.X_AXIS ||
                getType() == Table.TableType.Y_AXIS || isStaticDataTable();
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

            if(this.isAxis() != otherTable.isAxis()) {
                return false;
            }

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