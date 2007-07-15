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

package enginuity.logger.ecu.ui.paramlist;

import enginuity.logger.ecu.definition.EcuData;
import static enginuity.util.ParamChecker.isNullOrEmpty;

import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.event.MouseEvent;
import java.util.List;

public final class ParameterListTable extends JTable {
    private UnitsComboBoxEditor comboBoxEditor = new UnitsComboBoxEditor();
    private UnitsComboBoxRenderer comboBoxRenderer = new UnitsComboBoxRenderer();
    private final ParameterListTableModel tableModel;

    public ParameterListTable(ParameterListTableModel tableModel) {
        super(tableModel);
        this.tableModel = tableModel;
    }

    public TableCellRenderer getCellRenderer(int row, int col) {
        return displayComboBox(row, col) ? comboBoxRenderer : super.getCellRenderer(row, col);
    }

    public TableCellEditor getCellEditor(int row, int col) {
        return displayComboBox(row, col) ? comboBoxEditor : super.getCellEditor(row, col);
    }

    public String getToolTipText(MouseEvent mouseEvent) {
        List<ParameterRow> parameterRows = tableModel.getParameterRows();
        if (!isNullOrEmpty(parameterRows)) {
            ParameterRow parameterRow = parameterRows.get(rowAtPoint(mouseEvent.getPoint()));
            if (parameterRow != null) {
                String description = parameterRow.getLoggerData().getDescription();
                if (!isNullOrEmpty(description)) {
                    return description;
                }
            }
        }
        return super.getToolTipText(mouseEvent);
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
