package enginuity.logger.ui;

import enginuity.logger.definition.EcuData;
import static enginuity.util.ParamChecker.isNullOrEmpty;

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
    private final ParameterRegistrationBroker broker;

    public ParameterListTableModel(ParameterRegistrationBroker broker, String dataType) {
        this.broker = broker;
        columnNames = new String[]{"Selected?", dataType};
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
        ParameterRow paramRow = paramRowMap.get(registeredEcuData.get(row));
        switch (col) {
            case 0:
                return paramRow.getSelected();
            case 1:
                EcuData ecuData = paramRow.getEcuData();
                String units = ecuData.getConvertor().getUnits();
                return ecuData.getName() + (isNullOrEmpty(units) ? "" : " (" + units + ")");
            default:
                return "Error!";
        }
    }

    public synchronized void setValueAt(Object value, int row, int col) {
        ParameterRow paramRow = paramRowMap.get(registeredEcuData.get(row));
        if (col == 0 && paramRow != null) {
            Boolean selected = (Boolean) value;
            paramRow.setSelected(selected);
            if (selected) {
                broker.registerEcuParameterForLogging(paramRow.getEcuData());
            } else {
                broker.deregisterEcuParameterFromLogging(paramRow.getEcuData());
            }
            fireTableRowsUpdated(row, row);
        }
    }

    public Class<?> getColumnClass(int col) {
        return getValueAt(0, col).getClass();
    }

    public synchronized void addParam(EcuData ecuData) {
        if (!registeredEcuData.contains(ecuData)) {
            paramRowMap.put(ecuData, new ParameterRow(ecuData));
            registeredEcuData.add(ecuData);
            fireTableDataChanged();
        }
    }
}
