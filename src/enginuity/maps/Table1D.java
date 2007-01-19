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

package enginuity.maps;

import java.awt.*;
import javax.swing.*;
import enginuity.Settings;

public class Table1D extends Table {
    private Color axisColor = new Color(255, 255, 255);

    public Table1D(Settings settings) {
        super(settings);
    }

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

    public String toString() {
        return super.toString() + " (1D)";
    }

    public boolean isIsAxis() {
        return isAxis;
    }

    public void setIsAxis(boolean isAxis) {
        this.isAxis = isAxis;
    }

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

    public void colorize() {
        super.colorize();
    }

    public void cursorUp() {
        if (type == Table.TABLE_Y_AXIS) {
            if (highlightY > 0 && data[highlightY].isSelected()) {
                selectCellAt(highlightY - 1);
            }
        } else if (type == Table.TABLE_X_AXIS) {
            // Y axis is on top.. nothing happens
        } else if (type == Table.TABLE_1D) {
            // no where to move up to
        }
    }

    public void cursorDown() {
        if (type == Table.TABLE_Y_AXIS) {
            if (axisParent.getType() == Table.TABLE_3D) {
                if (highlightY < getDataSize() - 1 && data[highlightY].isSelected()) {
                    selectCellAt(highlightY + 1);
                }
            } else if (axisParent.getType() == Table.TABLE_2D) {
                if (data[highlightY].isSelected()) {
                    axisParent.selectCellAt(highlightY);
                }
            }
        } else if (type == Table.TABLE_X_AXIS && data[highlightY].isSelected()) {
            ((Table3D) axisParent).selectCellAt(highlightY, this);
        } else if (type == Table.TABLE_1D) {
            // no where to move down to
        }
    }

    public void cursorLeft() {
        if (type == Table.TABLE_Y_AXIS) {
            // X axis is on left.. nothing happens
            if (axisParent.getType() == Table.TABLE_2D) {
                if (data[highlightY].isSelected()) {
                    selectCellAt(highlightY - 1);
                }
            }
        } else if (type == Table.TABLE_X_AXIS && data[highlightY].isSelected()) {
            if (highlightY > 0) {
                selectCellAt(highlightY - 1);
            }
        } else if (type == Table.TABLE_1D && data[highlightY].isSelected()) {
            if (highlightY > 0) {
                selectCellAt(highlightY - 1);
            }
        }
    }

    public void cursorRight() {
        if (type == Table.TABLE_Y_AXIS && data[highlightY].isSelected()) {
            if (axisParent.getType() == Table.TABLE_3D) {
                ((Table3D) axisParent).selectCellAt(highlightY, this);
            } else if (axisParent.getType() == Table.TABLE_2D) {
                selectCellAt(highlightY + 1);
            }
        } else if (type == Table.TABLE_X_AXIS && data[highlightY].isSelected()) {
            if (highlightY < getDataSize() - 1) {
                selectCellAt(highlightY + 1);
            }
        } else if (type == Table.TABLE_1D && data[highlightY].isSelected()) {
            if (highlightY < getDataSize() - 1) {
                selectCellAt(highlightY + 1);
            }
        }
    }

    public void startHighlight(int x, int y) {
        if (isAxis) {
            axisParent.clearSelection();
        }
        super.startHighlight(x, y);
    }

    public StringBuffer getTableAsString() {
        StringBuffer output = new StringBuffer("");
        for (int i = 0; i < getDataSize(); i++) {
            output.append(data[i].getText());
            if (i < getDataSize() - 1) {
                output.append("\t");
            }
        }
        return output;
    }

    public String getCellAsString(int index) {
        return data[index].getText();
    }

    public Color getAxisColor() {
        return axisColor;
    }

    public void setAxisColor(Color axisColor) {
        this.axisColor = axisColor;
    }

    public void setLiveValue(String value) {
        liveValue = value;
        Table parent = getAxisParent();
        if (parent != null) {
            parent.highlightLiveData();
        }
    }

    public boolean isLiveDataSupported() {
        return false;
    }
}