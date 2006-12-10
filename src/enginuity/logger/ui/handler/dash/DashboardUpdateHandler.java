package enginuity.logger.ui.handler.dash;

import enginuity.logger.definition.ConvertorUpdateListener;
import enginuity.logger.definition.EcuData;
import enginuity.logger.ui.handler.DataUpdateHandler;

import javax.swing.*;
import java.awt.*;
import static java.util.Collections.synchronizedMap;
import java.util.HashMap;
import java.util.Map;

public final class DashboardUpdateHandler implements DataUpdateHandler, ConvertorUpdateListener {
    private final JPanel dashboardPanel;
    private final Map<EcuData, Gauge> gauges = synchronizedMap(new HashMap<EcuData, Gauge>());

    public DashboardUpdateHandler(JPanel dashboardPanel) {
        this.dashboardPanel = dashboardPanel;
    }

    public void registerData(EcuData ecuData) {
        Gauge gauge = new PlainGauge(ecuData);
        gauges.put(ecuData, gauge);
        dashboardPanel.add(gauge);
        repaintDashboardPanel();
    }

    public void handleDataUpdate(EcuData ecuData, byte[] value, long timestamp) {
        Gauge gauge = gauges.get(ecuData);
        if (gauge != null) {
            gauge.updateValue(value);
        }
    }

    public void deregisterData(EcuData ecuData) {
        dashboardPanel.remove(gauges.get(ecuData));
        gauges.remove(ecuData);
        repaintDashboardPanel();
    }

    public void cleanUp() {
    }

    public void notifyConvertorUpdate(EcuData updatedEcuData) {
        Gauge gauge = gauges.get(updatedEcuData);
        if (gauge != null) {
            gauge.resetValue();
            gauge.refreshTitle();
        }
    }

    private void repaintDashboardPanel() {
        dashboardPanel.doLayout();
        for (Component component : dashboardPanel.getComponents()) {
            component.doLayout();
        }
        dashboardPanel.repaint();
    }

}
