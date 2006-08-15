package enginuity.logger.ui;

import enginuity.logger.definition.EcuParameter;

import javax.swing.table.AbstractTableModel;
import static java.util.Collections.synchronizedList;
import static java.util.Collections.synchronizedMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public final class LoggerDataTableModel extends AbstractTableModel {
    private final String[] columnNames = {"ECU Parameter", "Min Value", "Current Value", "Max Value", "Units"};
    private final List<EcuParameter> registeredEcuParams = synchronizedList(new LinkedList<EcuParameter>());
    private final Map<EcuParameter, LoggerDataRow> dataRowMap = synchronizedMap(new LinkedHashMap<EcuParameter, LoggerDataRow>());

    public synchronized int getRowCount() {
        return dataRowMap.size();
    }

    public int getColumnCount() {
        return columnNames.length;
    }

    public String getColumnName(int col) {
        return columnNames[col];
    }

    public boolean isCellEditable(int row, int col) {
        return false;
    }

    public synchronized Object getValueAt(int row, int col) {
        LoggerDataRow dataRow = dataRowMap.get(registeredEcuParams.get(row));
        switch (col) {
            case 0:
                return dataRow.getName();
            case 1:
                return dataRow.getMinValue();
            case 2:
                return dataRow.getCurrentValue();
            case 3:
                return dataRow.getMaxValue();
            case 4:
                return dataRow.getUnits();
            default:
                return "Error!";
        }
    }

    public synchronized void addParam(EcuParameter ecuParam) {
        if (!registeredEcuParams.contains(ecuParam)) {
            dataRowMap.put(ecuParam, new LoggerDataRow(ecuParam));
            registeredEcuParams.add(ecuParam);
            fireTableDataChanged();
        }
    }

    public synchronized void removeParam(EcuParameter ecuParam) {
        registeredEcuParams.remove(ecuParam);
        dataRowMap.remove(ecuParam);
        fireTableDataChanged();
    }

    public synchronized void updateParam(EcuParameter ecuParam, byte[] bytes) {
        LoggerDataRow dataRow = dataRowMap.get(ecuParam);
        if (dataRow != null) {
            dataRow.updateValue(bytes);
            int index = registeredEcuParams.indexOf(ecuParam);
            fireTableRowsUpdated(index, index);
        }
    }

}
