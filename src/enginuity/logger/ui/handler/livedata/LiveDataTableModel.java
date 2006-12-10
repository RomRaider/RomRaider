package enginuity.logger.ui.handler.livedata;

import enginuity.logger.definition.EcuData;

import javax.swing.table.AbstractTableModel;
import static java.util.Collections.synchronizedList;
import static java.util.Collections.synchronizedMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public final class LiveDataTableModel extends AbstractTableModel {
    private final String[] columnNames = {"ECU Data", "Min Value", "Current Value", "Max Value", "Units"};
    private final List<EcuData> registeredEcuData = synchronizedList(new LinkedList<EcuData>());
    private final Map<EcuData, LiveDataRow> dataRowMap = synchronizedMap(new LinkedHashMap<EcuData, LiveDataRow>());

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
        LiveDataRow dataRow = dataRowMap.get(registeredEcuData.get(row));
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

    public synchronized void addParam(EcuData ecuData) {
        if (!registeredEcuData.contains(ecuData)) {
            dataRowMap.put(ecuData, new LiveDataRow(ecuData));
            registeredEcuData.add(ecuData);
            fireTableDataChanged();
        }
    }

    public synchronized void removeParam(EcuData ecuData) {
        registeredEcuData.remove(ecuData);
        dataRowMap.remove(ecuData);
        fireTableDataChanged();
    }

    public synchronized void updateParam(EcuData ecuData, byte[] bytes) {
        LiveDataRow dataRow = dataRowMap.get(ecuData);
        if (dataRow != null) {
            dataRow.updateValue(bytes);
            int index = registeredEcuData.indexOf(ecuData);
            fireTableRowsUpdated(index, index);
        }
    }

}
