/*
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2012 RomRaider.com
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

import java.awt.BorderLayout;

import javax.swing.JLabel;

import com.romraider.Settings;
import com.romraider.editor.ecu.ECUEditorManager;

public class Table1D extends Table {
    private static final long serialVersionUID = -8747180767803835631L;
    private Table parent = null;

    private final boolean isStatic;
    private final boolean isAxis;

    public Table1D(boolean isStatic, boolean isAxis) {
        super();
        this.isStatic = isStatic;
        this.isAxis = isAxis;
    }

    public void setAxisParent(Table axisParent) {
        this.parent = axisParent;
    }

    public Table getAxisParent() {
        return parent;
    }

    public void addStaticDataCell(DataCell input) {
        loaded = true;
        for(int i = 0; i < data.length; i++) {
            if(data[i] == null) {
                data[i] = input;
                break;
            }
        }
    }

    @Override
    public void populateTable(byte[] input, int ramOffset) {
        loaded = false;
        centerLayout.setRows(1);
        centerLayout.setColumns(this.getDataSize());

        super.populateTable(input, ramOffset);
        loaded = false;

        // add to table
        for (int i = 0; i < this.getDataSize(); i++) {
            centerPanel.add(this.getDataCell(i));
        }

        if(null == name || name.length() < 1 || "" == name) {
            ;// Do not add label.
        } else if(isStatic || "0x" == getScale().getUnit()) {
            // static or no scale exists.
            add(new JLabel(name, JLabel.CENTER), BorderLayout.NORTH);
        } else {
            add(new JLabel(name + " (" + getScale().getUnit() + ")", JLabel.CENTER), BorderLayout.NORTH);
        }
        loaded = true;
    }

    @Override
    public String toString() {
        return super.toString() + " (1D)";
    }

    @Override
    public void cursorUp() {
        if (type == Settings.TABLE_Y_AXIS) {
            if (highlightY > 0 && data[highlightY].isSelected()) {
                selectCellAt(highlightY - 1);
            }
        } else if (type == Settings.TABLE_X_AXIS) {
            // Y axis is on top.. nothing happens
        } else if (type == Settings.TABLE_1D) {
            // no where to move up to
        }
    }

    @Override
    public void cursorDown() {
        if (type == Settings.TABLE_Y_AXIS) {
            if (getAxisParent().getType() == Settings.TABLE_3D) {
                if (highlightY < getDataSize() - 1 && data[highlightY].isSelected()) {
                    selectCellAt(highlightY + 1);
                }
            } else if (getAxisParent().getType() == Settings.TABLE_2D) {
                if (data[highlightY].isSelected()) {
                    getAxisParent().selectCellAt(highlightY);
                }
            }
        } else if (type == Settings.TABLE_X_AXIS && data[highlightY].isSelected()) {
            ((Table3D) getAxisParent()).selectCellAt(highlightY, this);
        } else if (type == Settings.TABLE_1D) {
            // no where to move down to
        }
    }

    @Override
    public void cursorLeft() {
        if (type == Settings.TABLE_Y_AXIS) {
            // X axis is on left.. nothing happens
            if (getAxisParent().getType() == Settings.TABLE_2D) {
                if (data[highlightY].isSelected()) {
                    selectCellAt(highlightY - 1);
                }
            }
        } else if (type == Settings.TABLE_X_AXIS && data[highlightY].isSelected()) {
            if (highlightY > 0) {
                selectCellAt(highlightY - 1);
            }
        } else if (type == Settings.TABLE_1D && data[highlightY].isSelected()) {
            if (highlightY > 0) {
                selectCellAt(highlightY - 1);
            }
        }
    }

    @Override
    public void cursorRight() {
        if (type == Settings.TABLE_Y_AXIS && data[highlightY].isSelected()) {
            if (getAxisParent().getType() == Settings.TABLE_3D) {
                ((Table3D) getAxisParent()).selectCellAt(highlightY, this);
            } else if (getAxisParent().getType() == Settings.TABLE_2D) {
                selectCellAt(highlightY + 1);
            }
        } else if (type == Settings.TABLE_X_AXIS && data[highlightY].isSelected()) {
            if (highlightY < getDataSize() - 1) {
                selectCellAt(highlightY + 1);
            }
        } else if (type == Settings.TABLE_1D && data[highlightY].isSelected()) {
            if (highlightY < getDataSize() - 1) {
                selectCellAt(highlightY + 1);
            }
        }
    }

    @Override
    public void startHighlight(int x, int y) {
        getAxisParent().clearSelection();
        super.startHighlight(x, y);
        ECUEditorManager.getECUEditor().getTableToolBar().updateTableToolBar(this);
    }

    @Override
    public String getCellAsString(int index) {
        return data[index].getText();
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
        return isAxis;
    }

    public boolean isStatic() {
        return isStatic;
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