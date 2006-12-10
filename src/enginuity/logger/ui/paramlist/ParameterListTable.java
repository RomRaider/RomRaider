package enginuity.logger.ui.paramlist;

import enginuity.logger.definition.EcuData;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

public final class ParameterListTable extends JTable {
    private UnitsComboBoxEditor comboBoxEditor = new UnitsComboBoxEditor();
    private UnitsComboBoxRenderer comboBoxRenderer = new UnitsComboBoxRenderer();

    public ParameterListTable(TableModel tableModel) {
        super(tableModel);
    }

    public TableCellRenderer getCellRenderer(int row, int col) {
        return displayComboBox(row, col) ? comboBoxRenderer : super.getCellRenderer(row, col);
    }

    public TableCellEditor getCellEditor(int row, int col) {
        return displayComboBox(row, col) ? comboBoxEditor : super.getCellEditor(row, col);
    }

    private boolean displayComboBox(int row, int col) {
        Object value = getValueAt(row, col);
        if (EcuData.class.isAssignableFrom(value.getClass())) {
            EcuData ecuData = (EcuData) value;
            if (ecuData.getConvertors().length > 1) {
                return true;
            }
        }
        return false;
    }
}
