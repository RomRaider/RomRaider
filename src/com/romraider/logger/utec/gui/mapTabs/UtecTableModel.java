/*
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2008 RomRaider.com
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

package com.romraider.logger.utec.gui.mapTabs;

import javax.swing.table.AbstractTableModel;

public class UtecTableModel extends AbstractTableModel {

    private String[] columnNames = new String[11];

    private Double[][] data = new Double[11][40];

    String test = "";

    private int identifier = 0;

    public UtecTableModel(int identifier, Double[][] initialData) {
        this.identifier = identifier;

        this.data = initialData;

        for (int i = 0; i < columnNames.length; i++) {
            columnNames[i] = i + "";
        }

    }

    public int getColumnCount() {
        return columnNames.length;
    }

    public int getRowCount() {
        return 40;
    }

    public Object getValueAt(int row, int col) {
        return data[col][row];
    }

    public String getColumnName(int col) {
        return columnNames[col];
    }

    public boolean isCellEditable(int row, int col) {
        return true;
    }


    public void setValueAt(Object value, int row, int col) {
        //System.out.print(" Updated:"+(String)value+": ");
        // Set new data in table
        double temp = data[col][row];

        if (value instanceof String) {
            try {
                temp = Double.parseDouble((String) value);
            } catch (NumberFormatException e) {
                System.out.println("Not a valid number entered.");
            }
            data[col][row] = temp;
        } else if (value instanceof Double) {
            data[col][row] = (Double) value;
        }

        // Update current map in scope
        if (this.identifier == MapJPanel.FUELMAP) {
            UtecDataManager.setFuelMapValue(row, col, temp);
        } else if (this.identifier == MapJPanel.TIMINGMAP) {
            UtecDataManager.setTimingMapValue(row, col, temp);
        } else if (this.identifier == MapJPanel.BOOSTMAP) {
            UtecDataManager.setBoostMapValue(row, col, temp);
        }

        this.fireTableDataChanged();
    }

    public void setDoubleData(int row, int col, double value) {
        this.data[col][row] = value;
    }


    public void replaceData(Double[][] newData) {
        System.out.println("Model data being replaced in full.");
        this.data = newData;
        this.fireTableDataChanged();
    }

}