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

import enginuity.logger.ecu.definition.LoggerData;
import enginuity.logger.ecu.ui.DataRegistrationBroker;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import static java.util.Collections.synchronizedList;
import java.util.LinkedList;
import java.util.List;

public final class ParameterListTableModel extends AbstractTableModel {
    private final String[] columnNames;
    private final List<LoggerData> registeredLoggerData = synchronizedList(new LinkedList<LoggerData>());
    private final DataRegistrationBroker broker;

    public ParameterListTableModel(DataRegistrationBroker broker, String dataType) {
        this.broker = broker;
        columnNames = new String[]{"Selected?", dataType, "Units"};
    }

    public synchronized int getRowCount() {
        return registeredLoggerData.size();
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
        LoggerData loggerData = registeredLoggerData.get(row);
        switch (col) {
            case 0:
                return loggerData.isSelected();
            case 1:
                return loggerData.getName();
            case 2:
                return loggerData.getConvertors().length > 1 ? loggerData : loggerData.getSelectedConvertor().getUnits();
            default:
                return "Error!";
        }
    }

    public synchronized void setValueAt(Object value, int row, int col) {
        LoggerData loggerData = registeredLoggerData.get(row);
        if (col == 0 && loggerData != null) {
            Boolean selected = (Boolean) value;
            setSelected(loggerData, selected);
            fireTableRowsUpdated(row, row);
        }
    }

    public Class<?> getColumnClass(int col) {
        return getValueAt(0, col).getClass();
    }

    public synchronized void addParam(LoggerData loggerData, boolean selected) {
        if (!registeredLoggerData.contains(loggerData)) {
            registeredLoggerData.add(loggerData);
            setSelected(loggerData, selected);
            fireTableDataChanged();
        }
    }

    public synchronized void selectParam(LoggerData loggerData, boolean selected) {
        if (registeredLoggerData.contains(loggerData)) {
            setSelected(loggerData, selected);
            fireTableDataChanged();
        }
    }

    public synchronized void clear() {
        broker.clear();
        registeredLoggerData.clear();
        fireTableDataChanged();
    }

    public List<LoggerData> getLoggerData() {
        return new ArrayList<LoggerData>(registeredLoggerData);
    }

    private void setSelected(LoggerData loggerData, boolean selected) {
        loggerData.setSelected(selected);
        if (selected) {
            broker.registerLoggerDataForLogging(loggerData);
        } else {
            broker.deregisterLoggerDataFromLogging(loggerData);
        }
    }
}
