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

import enginuity.logger.ecu.comms.query.Response;
import enginuity.logger.ecu.definition.ConvertorUpdateListener;
import enginuity.logger.ecu.definition.LoggerData;
import enginuity.logger.ecu.ui.handler.DataUpdateHandler;
import static enginuity.util.ThreadUtil.run;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import static java.util.Collections.synchronizedMap;
import java.util.HashMap;
import java.util.Map;

public final class DashboardUpdateHandler implements DataUpdateHandler, ConvertorUpdateListener {
    private final JPanel dashboardPanel;
    private final Map<LoggerData, Gauge> gauges = synchronizedMap(new HashMap<LoggerData, Gauge>());

    public DashboardUpdateHandler(JPanel dashboardPanel) {
        this.dashboardPanel = dashboardPanel;
    }

    public synchronized void registerData(LoggerData loggerData) {
        Gauge gauge = new PlainGauge(loggerData);
        gauges.put(loggerData, gauge);
        dashboardPanel.add(gauge);
        repaintDashboardPanel();
    }

    public synchronized void handleDataUpdate(Response response) {
        for (LoggerData loggerData : response.getData()) {
            Gauge gauge = gauges.get(loggerData);
            if (gauge != null) {
                gauge.updateValue(response.getDataValue(loggerData));
            }
        }
    }

    public synchronized void deregisterData(LoggerData loggerData) {
        dashboardPanel.remove(gauges.get(loggerData));
        gauges.remove(loggerData);
        repaintDashboardPanel();
    }

    public synchronized void cleanUp() {
    }

    public synchronized void reset() {
        for (Gauge gauge : gauges.values()) {
            gauge.resetValue();
        }
    }

    public synchronized void notifyConvertorUpdate(LoggerData updatedLoggerData) {
        Gauge gauge = gauges.get(updatedLoggerData);
        if (gauge != null) {
            gauge.resetValue();
            gauge.refreshTitle();
        }
    }

    private void repaintDashboardPanel() {
        run(new Runnable() {
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
        });
    }

}
