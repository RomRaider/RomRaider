/*
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2012 RomRaider.com
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
 */

package com.romraider.logger.ecu.ui.handler.dash;

import com.romraider.logger.ecu.comms.query.Response;
import com.romraider.logger.ecu.definition.ConvertorUpdateListener;
import com.romraider.logger.ecu.definition.LoggerData;
import com.romraider.logger.ecu.ui.handler.DataUpdateHandler;
import static com.romraider.util.ThreadUtil.run;
import static java.util.Collections.synchronizedMap;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.Container;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

public final class DashboardUpdateHandler implements DataUpdateHandler, ConvertorUpdateListener {
    private static final Class[] STYLES = {PlainGaugeStyle.class, SmallGaugeStyle.class, NoFrillsGaugeStyle.class, DialGaugeStyle.class, SmallDialGaugeStyle.class};
    private final Map<LoggerData, Gauge> gauges = synchronizedMap(new HashMap<LoggerData, Gauge>());
    private final JPanel dashboardPanel;
    public int styleIndex;

    public DashboardUpdateHandler(JPanel dashboardPanel, int styleIndex) {
        this.dashboardPanel = dashboardPanel;
        this.styleIndex = styleIndex;
    }

    public synchronized void registerData(final LoggerData loggerData) {
        GaugeStyle style = getGaugeStyle(STYLES[styleIndex], loggerData);
        Gauge gauge = new Gauge(style);
        gauges.put(loggerData, gauge);
        dashboardPanel.add(gauge);
        repaintDashboardPanel();
    }

    public synchronized void handleDataUpdate(Response response) {
    	if (dashboardPanel.isShowing()) {
	        for (LoggerData loggerData : response.getData()) {
	            Gauge gauge = gauges.get(loggerData);
	            if (gauge != null) {
	                double value = response.getDataValue(loggerData);
	                gauge.updateValue(value);
	            }
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

    public synchronized void toggleGaugeStyle() {
        Class<? extends GaugeStyle> styleClass = getNextStyleClass();
        for (Map.Entry<LoggerData, Gauge> entry : gauges.entrySet()) {
            GaugeStyle style = getGaugeStyle(styleClass, entry.getKey());
            entry.getValue().setGaugeStyle(style);
        }
        repaintDashboardPanel();
    }

    private Class<? extends GaugeStyle> getNextStyleClass() {
        styleIndex = styleIndex == STYLES.length - 1 ? 0 : styleIndex + 1;
        return STYLES[styleIndex];
    }

    private GaugeStyle getGaugeStyle(Class<? extends GaugeStyle> styleClass, LoggerData loggerData) {
        try {
            Constructor<? extends GaugeStyle> constructor = styleClass.getDeclaredConstructor(LoggerData.class);
            return constructor.newInstance(loggerData);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private void repaintDashboardPanel() {
        run(new Runnable() {
            public void run() {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        Container parent = dashboardPanel.getParent();
                        if (parent != null) parent.validate();
                        else dashboardPanel.validate();
                        dashboardPanel.repaint();
                    }
                });
            }
        });
    }

}
