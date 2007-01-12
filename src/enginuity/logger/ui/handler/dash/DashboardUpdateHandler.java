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

    public void handleDataUpdate(EcuData ecuData, double value, long timestamp) {
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
