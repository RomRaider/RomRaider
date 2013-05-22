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
import java.awt.Color;

import javax.swing.JLabel;

import com.romraider.Settings;

public class Table1D extends Table {
    private static final long serialVersionUID = -8747180767803835631L;
    private Color axisColor = new Color(255, 255, 255);

    public Table1D() {
        super();
    }

    @Override
    public void populateTable(byte[] input) {
        centerLayout.setRows(1);
        centerLayout.setColumns(this.getDataSize());
        super.populateTable(input);

        // add to table
        for (int i = 0; i < this.getDataSize(); i++) {
            centerPanel.add(this.getDataCell(i));
        }
        add(new JLabel(name + " (" + scales.get(scaleIndex).getUnit() + ")", JLabel.CENTER), BorderLayout.NORTH);
    }

    @Override
    public String toString() {
        return super.toString() + " (1D)";
    }

    public boolean isIsAxis() {
        return isAxis;
    }

    public void setIsAxis(boolean isAxis) {
        this.isAxis = isAxis;
    }

    @Override
    public void clearSelection() {
        super.clearSelection();
        //if (isAxis) axisParent.clearSelection();
    }

    public void clearSelection(boolean calledByParent) {
        if (calledByParent) {
            super.clearSelection();
        } else {
            this.clearSelection();
        }
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
            if (axisParent.getType() == Settings.TABLE_3D) {
                if (highlightY < getDataSize() - 1 && data[highlightY].isSelected()) {
                    selectCellAt(highlightY + 1);
                }
            } else if (axisParent.getType() == Settings.TABLE_2D) {
                if (data[highlightY].isSelected()) {
                    axisParent.selectCellAt(highlightY);
                }
            }
        } else if (type == Settings.TABLE_X_AXIS && data[highlightY].isSelected()) {
            ((Table3D) axisParent).selectCellAt(highlightY, this);
        } else if (type == Settings.TABLE_1D) {
            // no where to move down to
        }
    }

    @Override
    public void cursorLeft() {
        if (type == Settings.TABLE_Y_AXIS) {
            // X axis is on left.. nothing happens
            if (axisParent.getType() == Settings.TABLE_2D) {
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
            if (axisParent.getType() == Settings.TABLE_3D) {
                ((Table3D) axisParent).selectCellAt(highlightY, this);
            } else if (axisParent.getType() == Settings.TABLE_2D) {
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
        if (isAxis) {
            axisParent.clearSelection();
        }
        super.startHighlight(x, y);
    }

    @Override
    public String getCellAsString(int index) {
        return data[index].getText();
    }

    public Color getAxisColor() {
        return axisColor;
    }

    @Override
    public void setAxisColor() {
        this.axisColor = getSettings().getAxisColor();
    }

    @Override
    public void setLiveValue(String value) {
        liveValue = value;
        Table parent = getAxisParent();
        if (parent != null) {
            parent.highlightLiveData();
        }
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

            if(this.isIsAxis()) {
                if(!otherTable.isIsAxis()) {
                    return false;
                }

                if(! this.isStatic() == otherTable.isStatic()) {
                    return false;
                }
            } else {
                // TODO: Possibly Log Error.  It appears that Table1D is always an Axis.
                if(!this.getName().equalsIgnoreCase(otherTable.getName())) {
                    return false;
                }
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
                if(this.data[i].getBinValue() != otherTable.data[i].getBinValue()) {
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