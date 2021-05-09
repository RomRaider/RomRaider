/*
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2021 RomRaider.com
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

import static java.awt.BorderLayout.CENTER;
import static java.awt.BorderLayout.WEST;
import static javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER;
import static javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS;

import java.awt.BorderLayout;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.romraider.editor.ecu.ECUEditor;
import com.romraider.logger.ecu.definition.EcuParameter;
import com.romraider.logger.ecu.definition.EcuSwitch;
import com.romraider.logger.ecu.definition.ExternalData;
import com.romraider.logger.ecu.ui.DataRegistrationBroker;
import com.romraider.util.ResourceUtil;

public final class DynoTabImpl extends JPanel implements DynoTab {
    private static final long serialVersionUID = 2787020251963102201L;
    private static final ResourceBundle rb = new ResourceUtil().getBundle(
            DynoTabImpl.class.getName());
    private final DynoChartPanel chartPanel = new DynoChartPanel(
            rb.getString("XAXISLBL"),
            rb.getString("HPAXISLBL"),
            rb.getString("TQAXISLBL"));
    private final DynoControlPanel controlPanel;

    public DynoTabImpl(DataRegistrationBroker broker, ECUEditor ecuEditor) {
        super(new BorderLayout(2, 2));
        controlPanel = new DynoControlPanel(this, broker, ecuEditor, chartPanel);
        JScrollPane scrollPane = new JScrollPane(controlPanel, VERTICAL_SCROLLBAR_ALWAYS, HORIZONTAL_SCROLLBAR_NEVER);
        add(scrollPane, WEST);
        add(chartPanel, CENTER);
    }

    public DynoControlPanel getDynoControlPanel() {
    	return controlPanel;
    }
    
    @Override
    public double calcRpm(double vs) {
        return controlPanel.calcRpm(vs);
    }

    @Override
    public void updateEnv(double iat, double pressure) {
        controlPanel.updateEnv(iat, pressure);
    }

    @Override
    public boolean isValidET(long now, double vs) {
        return controlPanel.isValidET(now, vs);
    }

    @Override
    public boolean isRecordET() {
        return controlPanel.isRecordET();
    }

    @Override
    public boolean isRecordData() {
        return controlPanel.isRecordData();
    }

    @Override
    public boolean isManual() {
        return controlPanel.isManual();
    }

    @Override
    public boolean getEnv() {
        return controlPanel.getEnv();
    }

    @Override
    public boolean isValidData(double rpm, double ta) {
        return controlPanel.isValidData(rpm, ta);
    }

    @Override
    public void addData(double rpm, double hp, double tq) {
        chartPanel.addData(rpm, hp, tq);
    }

    @Override
    public void addRawData(double time, double rpm) {
        chartPanel.addRawData(time, rpm);
    }

    @Override
    public void addData(double rpm, double hp) {
    }

    @Override
    public int getSampleCount() {
        return chartPanel.getSampleCount();
    }

    @Override
    public void setEcuParams(List<EcuParameter> params) {
        controlPanel.setEcuParams(params);
    }

    @Override
    public void setEcuSwitches(List<EcuSwitch> switches) {
        controlPanel.setEcuSwitches(switches);
    }

    @Override
    public void setExternalDatas(List<ExternalData> external) {
        controlPanel.setExternalDatas(external);
    }

    @Override
    public JPanel getPanel() {
        return this;
    }

    @Override
    public void saveSettings() {
        controlPanel.saveSettings();
    }
}
