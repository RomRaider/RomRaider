package enginuity.logger.ui;

import enginuity.logger.definition.EcuData;

import javax.swing.table.AbstractTableModel;
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
                return paramRow.getSelected();
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

    public synchronized void clear() {
        broker.clear();
        paramRowMap.clear();
        registeredEcuData.clear();
        fireTableDataChanged();
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
