package enginuity.logger.ui.handler;

import enginuity.logger.definition.EcuData;
import enginuity.logger.definition.EcuDataConvertor;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public final class DashboardUpdateHandler implements DataUpdateHandler {
    private final JPanel dashboardPanel;
    private final Map<EcuData, JLabel> gauges = new HashMap<EcuData, JLabel>();

    public DashboardUpdateHandler(JPanel dashboardPanel) {
        this.dashboardPanel = dashboardPanel;
    }

    public void registerData(EcuData ecuData) {
        JLabel label = new JLabel();
        label.setPreferredSize(new Dimension(100, 100));
        label.setBorder(new BevelBorder(BevelBorder.LOWERED));
        label.setFont(label.getFont().deriveFont(Font.PLAIN, 50F));
        label.setText(ecuData.getConvertor().format(0.0));
        gauges.put(ecuData, label);
        dashboardPanel.add(label);
        repaintDashboardPanel();
    }

    public void handleDataUpdate(EcuData ecuData, byte[] value, long timestamp) {
        JLabel label = gauges.get(ecuData);
        EcuDataConvertor convertor = ecuData.getConvertor();
        label.setText(convertor.format(convertor.convert(value)));
    }

    public void deregisterData(EcuData ecuData) {
        dashboardPanel.remove(gauges.get(ecuData));
        gauges.remove(ecuData);
        repaintDashboardPanel();
    }

    public void cleanUp() {
    }

    private void repaintDashboardPanel() {
        dashboardPanel.doLayout();
        dashboardPanel.repaint();
    }

}
