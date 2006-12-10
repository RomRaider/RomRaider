package enginuity.logger.ui.paramlist;

import enginuity.logger.definition.EcuData;
import enginuity.logger.definition.EcuDataConvertor;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public final class UnitsComboBoxEditor extends AbstractCellEditor implements TableCellEditor, ActionListener {
    private static final String EDIT_COMMAND = "EDIT";
    private EcuData currentEcuData;

    public Object getCellEditorValue() {
        return currentEcuData.getSelectedConvertor();
    }

    public Component getTableCellEditorComponent(JTable table, Object ecuData, boolean isSelected, int row, int column) {
        currentEcuData = (EcuData) ecuData;
        EcuDataConvertor[] convertors = currentEcuData.getConvertors();
        JComboBox comboBox = new JComboBox();
        for (EcuDataConvertor convertor : convertors) {
            comboBox.addItem(convertor);
        }
        comboBox.setSelectedItem(currentEcuData.getSelectedConvertor());
        comboBox.setEditable(false);
        comboBox.setEnabled(true);
        comboBox.setActionCommand(EDIT_COMMAND);
        comboBox.addActionListener(this);
        return comboBox;
    }

    public void actionPerformed(ActionEvent actionEvent) {
        if (EDIT_COMMAND.equals(actionEvent.getActionCommand())) {
            Object source = actionEvent.getSource();
            if (source != null && JComboBox.class.isAssignableFrom(source.getClass())) {
                JComboBox comboBox = (JComboBox) source;
                currentEcuData.selectConvertor((EcuDataConvertor) comboBox.getSelectedItem());
                fireEditingStopped();
            }
        }
    }
}
