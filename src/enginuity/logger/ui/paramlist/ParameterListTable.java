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

package enginuity.logger.ui.paramlist;

import enginuity.logger.definition.EcuData;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

public final class ParameterListTable extends JTable {
    private UnitsComboBoxEditor comboBoxEditor = new UnitsComboBoxEditor();
    private UnitsComboBoxRenderer comboBoxRenderer = new UnitsComboBoxRenderer();

    public ParameterListTable(TableModel tableModel) {
        super(tableModel);
    }

    public TableCellRenderer getCellRenderer(int row, int col) {
        return displayComboBox(row, col) ? comboBoxRenderer : super.getCellRenderer(row, col);
    }

    public TableCellEditor getCellEditor(int row, int col) {
        return displayComboBox(row, col) ? comboBoxEditor : super.getCellEditor(row, col);
    }

    private boolean displayComboBox(int row, int col) {
        Object value = getValueAt(row, col);
        if (EcuData.class.isAssignableFrom(value.getClass())) {
            EcuData ecuData = (EcuData) value;
            if (ecuData.getConvertors().length > 1) {
                return true;
            }
        }
        return false;
    }
}
