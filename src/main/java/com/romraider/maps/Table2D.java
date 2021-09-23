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

import static com.romraider.util.ParamChecker.isNullOrEmpty;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.util.StringTokenizer;

import javax.naming.NameNotFoundException;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;

import com.romraider.Settings;
import com.romraider.editor.ecu.ECUEditorManager;
import com.romraider.util.SettingsManager;

public class Table2D extends Table {
    private static final long serialVersionUID = -7684570967109324784L;
    private Table1D axis = new Table1D(Table.TableType.Y_AXIS);

    private CopyTable2DWorker copyTable2DWorker;
    private CopySelection2DWorker copySelection2DWorker;


    @Override
    public TableType getType() {
        return TableType.TABLE_2D;
    }

    public Table1D getAxis() {
        return axis;
    }
    
    
    public void setAxis(Table1D axis) {
        this.axis = axis;
        axis.setAxisParent(this);
    }

    @Override
    public String toString() {
        return super.toString() + " (2D)";// + axis;
    }    
    
    @Override
    public void populateCompareValues(Table otherTable) {
        if(null == otherTable || !(otherTable instanceof Table2D)) {
            return;
        }

        Table2D compareTable2D = (Table2D) otherTable;
        if(data.length != compareTable2D.data.length ||
                axis.data.length != compareTable2D.axis.data.length) {
            return;
        }

        super.populateCompareValues(otherTable);
        axis.populateCompareValues(compareTable2D.getAxis());
    }

    @Override
    public void refreshCompare() {
        populateCompareValues(getCompareTable());
        axis.refreshCompare();
    }

    @Override
    public void populateTable(byte[] input, int romRamOffset) throws ArrayIndexOutOfBoundsException, IndexOutOfBoundsException {   	
        try {
            axis.populateTable(input, romRamOffset);
            super.populateTable(input, romRamOffset);
        } catch (ArrayIndexOutOfBoundsException ex) {
            throw new ArrayIndexOutOfBoundsException();
        }
    }


    @Override
    public void setRevertPoint() {
        super.setRevertPoint();
        axis.setRevertPoint();
    }

    @Override
    public void undoAll() throws UserLevelException {
        super.undoAll();
        axis.undoAll();
    }
    

    @Override
    public byte[] saveFile(byte[] binData) {
        /*
        binData = super.saveFile(binData);
        binData = axis.saveFile(binData);*/
    	
        return binData;
    }

    @Override
    public String getLogParamString() {
        StringBuilder sb = new StringBuilder();
        sb.append(axis.getLogParamString()+ ", ");
        sb.append(getName()+ ":" + getLogParam());
        return sb.toString();
    }
    
    @Override
    public boolean isLiveDataSupported() {
        return !isNullOrEmpty(axis.getLogParam());
    }

    @Override
    public boolean isButtonSelected() {
        return true;
    }
  
    @Override
    public void setCompareValueType(Settings.DataType compareValueType) {
        super.setCompareValueType(compareValueType);
        axis.setCompareValueType(compareValueType);
    }

    @Override
    public void setCurrentScale(Scale curScale) {
        if(SettingsManager.getSettings().isScaleHeadersAndData() && !axis.isStaticDataTable()) {
            try {
                this.axis.setScaleByName(curScale.getName());
            } catch (NameNotFoundException e) {
                try {
                    this.axis.setScaleByName(SettingsManager.getSettings().getDefaultScale());
                } catch (NameNotFoundException e1) {
                }
            }
        }
        this.curScale = curScale;
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

            if(!(other instanceof Table2D)) {
                return false;
            }

            Table2D otherTable = (Table2D)other;

            if( (null == this.getName() && null == otherTable.getName())
                    || (this.getName().isEmpty() && otherTable.getName().isEmpty()) ) {
                ;// Skip name compare if name is null or empty.
            } else if (!this.getName().equalsIgnoreCase(otherTable.getName())) {
                return false;
            }

            if(!this.axis.equals(otherTable.axis)) {
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
            for(int i = 0 ; i < this.data.length ; i++) {
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