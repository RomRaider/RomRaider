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
import enginuity.logger.ui.DataRegistrationBroker;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import static java.util.Collections.synchronizedList;
import static java.util.Collections.synchronizedMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public final class ParameterListTableModel extends AbstractTableModel {
    private final String[] columnNames;
    private final List<EcuData> registeredEcuData = synchronizedList(new LinkedList<EcuData>());
    private final Map<EcuData, ParameterRow> paramRowMap = synchronizedMap(new LinkedHashMap<EcuData, ParameterRow>());
    private final DataRegistrationBroker broker;

    public ParameterListTableModel(DataRegistrationBroker broker, String dataType) {
        this.broker = broker;
        columnNames = new String[]{"Selected?", dataType, "Units"};
    }

    public synchronized int getRowCount() {
        return paramRowMap.size();
    }

    public int getColumnCount() {
        return columnNames.length;
    }

    public String getColumnName(int col) {
        return columnNames[col];
    }

    public boolean isCellEditable(int row, int col) {
        return col == 0 || col == 2;
    }

    public synchronized Object getValueAt(int row, int col) {
        ParameterRow paramRow = paramRowMap.get(registeredEcuData.get(row));
        switch (col) {
            case 0:
                return paramRow.isSelected();
            case 1:
                return paramRow.getEcuData().getName();
            case 2:
                EcuData ecuData = paramRow.getEcuData();
                return ecuData.getConvertors().length > 1 ? ecuData : ecuData.getSelectedConvertor().getUnits();
            default:
                return "Error!";
        }
    }

    public synchronized void setValueAt(Object value, int row, int col) {
        ParameterRow paramRow = paramRowMap.get(registeredEcuData.get(row));
        if (col == 0 && paramRow != null) {
            Boolean selected = (Boolean) value;
            setSelected(paramRow, selected);
            fireTableRowsUpdated(row, row);
        }
    }

    public Class<?> getColumnClass(int col) {
        return getValueAt(0, col).getClass();
    }

    public synchronized void addParam(EcuData ecuData, boolean selected) {
        if (!registeredEcuData.contains(ecuData)) {
            ParameterRow paramRow = new ParameterRow(ecuData);
            paramRowMap.put(ecuData, paramRow);
            registeredEcuData.add(ecuData);
            setSelected(paramRow, selected);
            fireTableDataChanged();
        }
    }

    public synchronized void selectParam(EcuData ecuData, boolean selected) {
        if (registeredEcuData.contains(ecuData)) {
            setSelected(paramRowMap.get(ecuData), selected);
            fireTableDataChanged();
        }
    }

    public synchronized void clear() {
        broker.clear();
        paramRowMap.clear();
        registeredEcuData.clear();
        fireTableDataChanged();
    }

    public List<ParameterRow> getParameterRows() {
        return new ArrayList<ParameterRow>(paramRowMap.values());
    }

    private void setSelected(ParameterRow paramRow, Boolean selected) {
        paramRow.setSelected(selected);
        if (selected) {
            broker.registerEcuDataForLogging(paramRow.getEcuData());
        } else {
            broker.deregisterEcuDataFromLogging(paramRow.getEcuData());
        }
    }
}
