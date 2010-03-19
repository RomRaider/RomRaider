/*
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2009 RomRaider.com
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

package com.romraider.logger.ecu.ui.tab.dyno;

import com.romraider.editor.ecu.ECUEditor;
import com.romraider.logger.ecu.definition.EcuParameter;
import com.romraider.logger.ecu.definition.EcuSwitch;
import com.romraider.logger.ecu.definition.ExternalData;
import com.romraider.logger.ecu.ui.DataRegistrationBroker;
import com.romraider.logger.ecu.ui.tab.DynoChartPanel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import static javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER;
import static javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS;
import java.awt.BorderLayout;
import static java.awt.BorderLayout.CENTER;
import static java.awt.BorderLayout.WEST;
import java.util.List;

public final class DynoTabImpl extends JPanel implements DynoTab {
    private static final long serialVersionUID = 2787020251963102201L;
    private final DynoChartPanel chartPanel = new DynoChartPanel("Engine Speed (RPM)", "Calculated Wheel Power", "Calculated Wheel Torque");
    private final DynoControlPanel controlPanel;

    public DynoTabImpl(DataRegistrationBroker broker, ECUEditor ecuEditor) {
        super(new BorderLayout(2, 2));
        controlPanel = new DynoControlPanel(this, broker, ecuEditor, chartPanel);
        JScrollPane scrollPane = new JScrollPane(controlPanel, VERTICAL_SCROLLBAR_ALWAYS, HORIZONTAL_SCROLLBAR_NEVER);
        add(scrollPane, WEST);
        add(chartPanel, CENTER);
    }

    public double calcRpm(double vs){
    	return controlPanel.calcRpm(vs);
    }

    public void updateEnv(double iat, double pressure) {
    	controlPanel.updateEnv(iat, pressure);
    }

    public boolean isRecordData() {
        return controlPanel.isRecordData();
    }

    public boolean isManual() {
        return controlPanel.isManual();
    }

    public boolean getEnv() {
        return controlPanel.getEnv();
    }

    public boolean isValidData(double rpm, double ta) {
        return controlPanel.isValidData(rpm, ta);
    }

    public void addData(double rpm, double hp, double tq) {
        chartPanel.addData(rpm, hp, tq);
    }

    public void addRawData(double time, double rpm) {
        chartPanel.addRawData(time, rpm);
    }

    public void addData(double rpm, double hp) {
    }

    public int getSampleCount() {
    	return chartPanel.getSampleCount();
    }

    public void setEcuParams(List<EcuParameter> params) {
        controlPanel.setEcuParams(params);
    }

    public void setEcuSwitches(List<EcuSwitch> switches) {
        controlPanel.setEcuSwitches(switches);
    }

    public void setExternalDatas(List<ExternalData> external) {
        controlPanel.setExternalDatas(external);
    }

    public JPanel getPanel() {
        return this;
    }
}
