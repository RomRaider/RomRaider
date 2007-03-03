/*
 *
 * Enginuity Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006 Enginuity.org
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 */

package enginuity.logger.ecu.ui.handler.dash;

import enginuity.logger.ecu.definition.ConvertorUpdateListener;
import enginuity.logger.ecu.definition.EcuData;
import enginuity.logger.ecu.ui.handler.DataUpdateHandler;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import static java.util.Collections.synchronizedMap;
import java.util.HashMap;
import java.util.Map;

public final class DashboardUpdateHandler implements DataUpdateHandler, ConvertorUpdateListener {
    private final JPanel dashboardPanel;
    private final Map<EcuData, Gauge> gauges = synchronizedMap(new HashMap<EcuData, Gauge>());

    public DashboardUpdateHandler(JPanel dashboardPanel) {
        this.dashboardPanel = dashboardPanel;
    }

    public synchronized void registerData(EcuData ecuData) {
        Gauge gauge = new PlainGauge(ecuData);
        gauges.put(ecuData, gauge);
        dashboardPanel.add(gauge);
        repaintDashboardPanel();
    }

    public synchronized void handleDataUpdate(EcuData ecuData, double value, long timestamp) {
        Gauge gauge = gauges.get(ecuData);
        if (gauge != null) {
            gauge.updateValue(value);
        }
    }

    public synchronized void deregisterData(EcuData ecuData) {
        dashboardPanel.remove(gauges.get(ecuData));
        gauges.remove(ecuData);
        repaintDashboardPanel();
    }

    public synchronized void cleanUp() {
    }

    public synchronized void reset() {
        for (Gauge gauge : gauges.values()) {
            gauge.resetValue();
        }
    }

    public synchronized void notifyConvertorUpdate(EcuData updatedEcuData) {
        Gauge gauge = gauges.get(updatedEcuData);
        if (gauge != null) {
            gauge.resetValue();
            gauge.refreshTitle();
        }
    }

    private void repaintDashboardPanel() {
        new Thread(new Runnable() {
            public void run() {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        dashboardPanel.doLayout();
                        for (Gauge gauge : gauges.values()) {
                            gauge.doLayout();
                            gauge.repaint();
                        }
                        dashboardPanel.repaint();
                    }
                });
            }
        }).start();
    }

}
