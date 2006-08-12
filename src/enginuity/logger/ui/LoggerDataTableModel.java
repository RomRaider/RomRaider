package enginuity.logger.ui;

import javax.swing.table.AbstractTableModel;

public final class LoggerDataTableModel extends AbstractTableModel {
    String[] columnNames = {"ECU Parameter", "Min Value", "Current Value", "Max Value", "Units"};
    LoggerDataRow[] data = new LoggerDataRow[0];

    public int getRowCount() {
        return data.length;
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

    public Object getValueAt(int row, int col) {
        switch (col) {
            case 0:
                return data[row].getName();
            case 1:
                return data[row].getMinValue();
            case 2:
                return data[row].getCurrentValue();
            case 3:
                return data[row].getMaxValue();
            case 4:
                return data[row].getUnits();
            default:
                return "Error!";
        }
    }

    public synchronized void addRow(LoggerDataRow newRow) {
        newRow.setParentTableModel(this);
        LoggerDataRow[] newData = new LoggerDataRow[data.length + 1];
        System.arraycopy(data, 0, newData, 0, data.length);
        newData[newData.length - 1] = newRow;
        data = newData;
    }
}
