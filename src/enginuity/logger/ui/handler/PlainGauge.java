package enginuity.logger.ui.handler;

import enginuity.logger.definition.EcuData;
import static enginuity.util.ParamChecker.checkNotNull;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;
import static java.awt.BorderLayout.CENTER;
import static java.awt.BorderLayout.NORTH;

public final class PlainGauge extends Gauge {
    private static final double ZERO = 0.0;
    private final EcuData ecuData;
    private final JLabel data = new JLabel();
    private final JLabel title = new JLabel();

    public PlainGauge(EcuData ecuData) {
        checkNotNull(ecuData, "ecuData");
        this.ecuData = ecuData;
        initLabelLayout();
        initGaugeLayout();
    }

    public void refreshTitle() {
        title.setText(ecuData.getName() + " (" + ecuData.getSelectedConvertor().getUnits() + ')');
    }

    public void updateValue(byte[] value) {
        refreshValue(ecuData.getSelectedConvertor().convert(value));
    }

    public void resetValue() {
        refreshValue(ZERO);
    }

    private void initLabelLayout() {
        data.setMinimumSize(new Dimension(150, 100));
        data.setMaximumSize(new Dimension(150, 100));
        data.setPreferredSize(new Dimension(150, 100));
        data.setBorder(new BevelBorder(BevelBorder.LOWERED));
        data.setFont(data.getFont().deriveFont(Font.PLAIN, 50F));
    }

    private void initGaugeLayout() {
        refreshValue(ZERO);
        refreshTitle();
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(150, 150));
        setBackground(Color.GREEN);
        add(data, CENTER);
        add(title, NORTH);
    }

    private void refreshValue(double value) {
        data.setText(ecuData.getSelectedConvertor().format(value));
    }

}
