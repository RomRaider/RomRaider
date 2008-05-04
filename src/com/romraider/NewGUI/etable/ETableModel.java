/*
 *
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
 *
 */

package com.romraider.NewGUI.etable;

import com.romraider.NewGUI.data.TableMetaData;
import org.apache.log4j.Logger;
import javax.swing.table.AbstractTableModel;
import java.text.DecimalFormat;

public class ETableModel extends AbstractTableModel {
    private static final Logger LOGGER = Logger.getLogger(ETableModel.class);
    private String[] columnNames = new String[11];
    private Double[][] data = new Double[11][40];
    private TableMetaData tableMetaData;
    private DecimalFormat formatter = new DecimalFormat("#.#");

    public ETableModel(TableMetaData metaData, Double[][] initialData) {
        this.tableMetaData = metaData;
        this.data = initialData;

        if (metaData.getColumnLabels() == null) {
            for (int i = 0; i < columnNames.length; i++) {
                columnNames[i] = i + "";
            }
        } else {
            this.columnNames = metaData.getColumnLabels();
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
        //LOGGER.debug(" Updated:"+(String)value+": ");
        // Set new data in table
        double temp = data[col][row];

        if (value instanceof String) {
            try {
                LOGGER.debug("value:" + value + ":");
                //String tempString = formatter.format(value);
                //temp = Double.parseDouble(tempString);
                temp = Double.parseDouble((String) value);
            } catch (NumberFormatException e) {
                LOGGER.error("Not a valid number entered.", e);
            }
            data[col][row] = temp;
        } else if (value instanceof Double) {
            String tempString = formatter.format(value);
            data[col][row] = Double.parseDouble(tempString);
        }

        this.fireTableDataChanged();
    }

    public void setDoubleData(int row, int col, double value) {
        String tempString = formatter.format(value);
        this.data[col][row] = Double.parseDouble(tempString);
    }


    public void replaceData(Double[][] newData) {

        this.copyData(newData);

        this.fireTableDataChanged();
        this.fireTableStructureChanged();
    }

    /**
     * ARG Why????
     * <p/>
     * Seem to be getting some pass by refence issues.
     *
     * @param data
     */
    private void copyData(Double[][] data) {
        int width = data.length;
        int height = data[0].length;

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                double tempData = data[i][j];
                String tempString = formatter.format(tempData);
                this.data[i][j] = Double.parseDouble(tempString);
            }
        }
    }

    public Double[][] getData() {
        return data;
    }

    public void refresh() {
        this.fireTableDataChanged();
    }

}