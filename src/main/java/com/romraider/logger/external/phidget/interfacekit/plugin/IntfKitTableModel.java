/*
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2020 RomRaider.com
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

package com.romraider.logger.external.phidget.interfacekit.plugin;

import java.util.List;
import java.util.ResourceBundle;

import javax.swing.table.DefaultTableModel;

import com.romraider.util.ResourceUtil;

/**
 * PhidgetInterfaceKit Table Model used to populate user defined convertor
 * dialog. 
 */
public final class IntfKitTableModel extends DefaultTableModel {
    private static final long serialVersionUID = -1733139047249681709L;
    private static final ResourceBundle rb = new ResourceUtil().getBundle(
            IntfKitTableModel.class.getName());
    private List<List<String>> ikData;
    private String[] columnNames = new String[]{
                rb.getString("SENSORID"),
                rb.getString("EXPR"),
                rb.getString("FORMAT"),
                rb.getString("UNITS"),
                rb.getString("MIN"),
                rb.getString("MAX"),
                rb.getString("STEP")
            };

    @Override
    public final int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public final String getColumnName(int column) {
        return columnNames[column];
    }

    @Override
    public final Object getValueAt(int row, int column) {
        if (ikData != null) {
            return ikData.get(row).get(column);
        }
        else {
            return null;
        }
    }

    @Override
    public void setValueAt(Object value, int row, int column) {
        ikData.get(row).set(column, (String) value);
        fireTableCellUpdated(row, column);
    }

    @Override
    public final int getRowCount() {
        return (ikData != null) ? ikData.size() : 0;
    }

    @Override
    public final Class<? extends Object> getColumnClass(int column) {
        return getValueAt(0, column).getClass();
    }

    @Override
    public final boolean isCellEditable(int row, int column) {
        return (column == 0) ? false : true;
    }

    public final void setIkRowData(List<List<String>> ikData) {
        this.ikData = ikData;
    }
}
