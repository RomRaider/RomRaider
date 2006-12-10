package enginuity.logger.ui.paramlist;

import enginuity.logger.definition.EcuData;
import enginuity.logger.definition.EcuDataConvertor;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

public final class UnitsComboBoxRenderer extends JComboBox implements TableCellRenderer {

    public Component getTableCellRendererComponent(JTable table, Object ecuData, boolean isSelected, boolean hasFocus, int row, int column) {
        EcuData currentEcuData = (EcuData) ecuData;
        EcuDataConvertor[] convertors = currentEcuData.getConvertors();
        JComboBox comboBox = new JComboBox();
        for (EcuDataConvertor convertor : convertors) {
            comboBox.addItem(convertor);
        }
        comboBox.setSelectedItem(currentEcuData.getSelectedConvertor());
        return comboBox;
    }
}
