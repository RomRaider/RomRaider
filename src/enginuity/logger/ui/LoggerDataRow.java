package enginuity.logger.ui;

import enginuity.logger.definition.EcuParameter;

import javax.swing.table.AbstractTableModel;

public final class LoggerDataRow {
    private EcuParameter ecuParam;
    private AbstractTableModel parentTableModel;
    private double minValue;
    private double maxValue;
    private double currentValue;

    public LoggerDataRow(EcuParameter ecuParam) {
        this.ecuParam = ecuParam;
    }

    public String getName() {
        return ecuParam.getName();
    }

    public String getMinValue() {
        return ecuParam.getConvertor().format(minValue);
    }

    public String getMaxValue() {
        return ecuParam.getConvertor().format(maxValue);
    }

    public String getCurrentValue() {
        return ecuParam.getConvertor().format(currentValue);
    }

    public String getUnits() {
        return ecuParam.getConvertor().getUnits();
    }

    public void updateValue(byte[] bytes) {
        currentValue = ecuParam.getConvertor().convert(bytes);
        if (currentValue < minValue || minValue == 0.0) {
            minValue = currentValue;
        }
        if (currentValue > maxValue || maxValue == 0.0) {
            maxValue = currentValue;
        }
        parentTableModel.fireTableDataChanged();
    }

    public void setParentTableModel(AbstractTableModel parentTableModel) {
        this.parentTableModel = parentTableModel;
    }
}