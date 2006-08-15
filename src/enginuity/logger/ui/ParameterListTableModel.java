package enginuity.logger.ui;

import enginuity.logger.definition.EcuParameter;

import javax.swing.table.AbstractTableModel;
import static java.util.Collections.synchronizedList;
import static java.util.Collections.synchronizedMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public final class ParameterListTableModel extends AbstractTableModel {
    private final String[] columnNames = {"Selected?", "ECU Parameter"};
    private final List<EcuParameter> registeredEcuParams = synchronizedList(new LinkedList<EcuParameter>());
    private final Map<EcuParameter, ParameterRow> paramRowMap = synchronizedMap(new LinkedHashMap<EcuParameter, ParameterRow>());
    private final ParameterRegistrationBroker broker;

    public ParameterListTableModel(ParameterRegistrationBroker broker) {
        this.broker = broker;
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
        return col == 0;
    }

    public synchronized Object getValueAt(int row, int col) {
        ParameterRow paramRow = paramRowMap.get(registeredEcuParams.get(row));
        switch (col) {
            case 0:
                return paramRow.getSelected();
            case 1:
                return paramRow.getEcuParam().getName();
            default:
                return "Error!";
        }
    }

    public synchronized void setValueAt(Object value, int row, int col) {
        ParameterRow paramRow = paramRowMap.get(registeredEcuParams.get(row));
        if (col == 0 && paramRow != null) {
            Boolean selected = (Boolean) value;
            paramRow.setSelected(selected);
            if (selected) {
                broker.registerEcuParameterForLogging(paramRow.getEcuParam());
            } else {
                broker.deregisterEcuParameterFromLogging(paramRow.getEcuParam());
            }
            fireTableRowsUpdated(row, row);
        }
    }

    public Class<?> getColumnClass(int col) {
        return getValueAt(0, col).getClass();
    }

    public synchronized void addParam(EcuParameter ecuParam) {
        if (!registeredEcuParams.contains(ecuParam)) {
            paramRowMap.put(ecuParam, new ParameterRow(ecuParam));
            registeredEcuParams.add(ecuParam);
            fireTableDataChanged();
        }
    }
}
