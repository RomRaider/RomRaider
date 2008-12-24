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

package com.romraider.NewGUI.etable;

import javax.swing.table.AbstractTableModel;

public class ETableRowLabel extends AbstractTableModel {

    private int length;
    private String[] labels;
    private int counter = 0;

    public ETableRowLabel(int length, String[] labels) {
        this.length = length;
        this.labels = labels;
    }

    public int getRowCount() {
        return length;
    }

    public int getColumnCount() {
        return length;
    }

    public Object getValueAt(int arg0, int arg1) {
        if (this.labels == null) {
            return arg0;
        }

        return this.labels[arg0];
    }

    public String getColumnName(int col) {

        if (this.labels == null) {
            return counter++ + "";
        }

        return this.labels[col];
    }

}
