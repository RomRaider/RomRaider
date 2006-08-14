package enginuity.logger.ui;

import enginuity.logger.definition.EcuParameter;

import javax.swing.table.AbstractTableModel;

public final class LoggerDataRow {
    private EcuParameter ecuParam;
    private AbstractTableModel parentTableModel;
    private double minValue;
    private double maxValue;
    private double currentValue;
    private boolean updated = false;

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
        if (currentValue < minValue || !updated) {
            minValue = currentValue;
        }
        if (currentValue > maxValue || !updated) {
            maxValue = currentValue;
        }
        parentTableModel.fireTableDataChanged();
        updated = true;
    }

    public void setParentTableModel(AbstractTableModel parentTableModel) {
        this.parentTableModel = parentTableModel;
    }
}