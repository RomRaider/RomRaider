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

package com.romraider.logger.ecu.ui.tab.injector;

import static com.romraider.logger.ecu.ui.tab.TableFinder.findTableStartsWith;
import static com.romraider.util.ParamChecker.checkNotNull;
import static java.awt.GridBagConstraints.CENTER;
import static java.awt.GridBagConstraints.HORIZONTAL;
import static java.awt.GridBagConstraints.NONE;
import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.OK_OPTION;
import static javax.swing.JOptionPane.WARNING_MESSAGE;
import static javax.swing.JOptionPane.YES_NO_OPTION;
import static javax.swing.JOptionPane.showConfirmDialog;
import static javax.swing.JOptionPane.showMessageDialog;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.border.TitledBorder;

import org.apache.log4j.Logger;

import com.romraider.editor.ecu.ECUEditor;
import com.romraider.logger.ecu.definition.EcuParameter;
import com.romraider.logger.ecu.definition.EcuSwitch;
import com.romraider.logger.ecu.definition.ExternalData;
import com.romraider.logger.ecu.definition.LoggerData;
import com.romraider.logger.ecu.ui.DataRegistrationBroker;
import com.romraider.logger.ecu.ui.tab.LoggerChartPanel;
import com.romraider.maps.DataCell;
import com.romraider.maps.Rom;
import com.romraider.maps.Table;
import com.romraider.maps.Table2D;
import com.romraider.util.ResourceUtil;

public final class InjectorControlPanel extends JPanel {
    /**
     *
     */
    private static final long serialVersionUID = -3570410894599258706L;
    private static final Logger LOGGER = Logger.getLogger(
            InjectorControlPanel.class);
    private static final ResourceBundle rb = new ResourceUtil().getBundle(
            InjectorControlPanel.class.getName());
    private static final String COOLANT_TEMP = "P2";
    private static final String ENGINE_SPEED = "P8";
    private static final String INTAKE_AIR_TEMP = "P11";
    private static final String MASS_AIR_FLOW = "P12";
    private static final String MASS_AIR_FLOW_V = "P18";
    private static final String AFR = "P58";
    private static final String CL_OL_16 = "E3";
    private static final String CL_OL_32 = "E33";
    private static final String PULSE_WIDTH_16 = "E28";
    private static final String PULSE_WIDTH_32 = "E60";
    private static final String TIP_IN_THROTTLE_16 = "E23";
    private static final String TIP_IN_THROTTLE_32 = "E54";
    private static final String ENGINE_LOAD_16 = "E2";
    private static final String ENGINE_LOAD_32 = "E32";
    private final JToggleButton recordDataButton = new JToggleButton(
            rb.getString("RECORDDATA"));
    private final JTextField mafvMin = new JTextField("1.20", 3);
    private final JTextField mafvMax = new JTextField("2.60", 3);
    private final JTextField afrMin = new JTextField("13.0", 3);
    private final JTextField afrMax = new JTextField("16.0", 3);
    private final JTextField rpmMin = new JTextField("0", 3);
    private final JTextField rpmMax = new JTextField("4500", 3);
    private final JTextField mafMin = new JTextField("20", 3);
    private final JTextField mafMax = new JTextField("100", 3);
    private final JTextField iatMax = new JTextField("45", 3);
    private final JTextField coolantMin = new JTextField("70", 3);
    private final JTextField mafvChangeMax = new JTextField("0.1", 3);
    private final JTextField fuelStoichAfr = new JTextField("14.7", 5);
    private final JTextField fuelDensity = new JTextField("732", 5);
    private final JTextField flowScaling = new JTextField("", 5);
    private final JTextField latencyOffset = new JTextField("", 5);
    private final DataRegistrationBroker broker;
    private final LoggerChartPanel chartPanel;
    private final ECUEditor ecuEditor;
    private final Component parent;
    private List<EcuParameter> params;
    private List<EcuSwitch> switches;
    private List<ExternalData> externals;

    public InjectorControlPanel(Component parent,
            DataRegistrationBroker broker, ECUEditor ecuEditor,
            LoggerChartPanel chartPanel) {
        checkNotNull(parent, broker, chartPanel);
        this.broker = broker;
        this.parent = parent;
        this.chartPanel = chartPanel;
        this.ecuEditor = ecuEditor;
        addControls();
    }

    public double getFuelStoichAfr() {
        return getProperty(fuelStoichAfr, rb.getString("FUELAFR"));
    }

    public double getFuelDensity() {
        return getProperty(fuelDensity, rb.getString("FUELDENSITY"));
    }

    public boolean isRecordData() {
        return recordDataButton.isSelected();
    }

    public boolean isValidClOl(double value) {
        return value == 8;
    }

    public boolean isValidAfr(double value) {
        return checkInRange(rb.getString("AFR"), afrMin, afrMax, value);
    }

    public boolean isValidRpm(double value) {
        return checkInRange(rb.getString("RPM"), rpmMin, rpmMax, value);
    }

    public boolean isValidMaf(double value) {
        return checkInRange(rb.getString("MAF"), mafMin, mafMax, value);
    }

    public boolean isValidMafv(double value) {
        return checkInRange(rb.getString("MAFV"), mafvMin, mafvMax, value);
    }

    public boolean isValidCoolantTemp(double value) {
        return checkGreaterThan(rb.getString("ECT"), coolantMin, value);
    }

    public boolean isValidIntakeAirTemp(double value) {
        return checkLessThan(rb.getString("IAT"), iatMax, value);
    }

    public boolean isValidMafvChange(double value) {
        return checkLessThan(rb.getString("DMAFVDT"), mafvChangeMax, value);
    }

    public boolean isValidTipInThrottle(double value) {
        return value == 0.0;
    }

    private double getProperty(JTextField field, String name) {
        if (isNumber(field)) return parseDouble(field);
        showMessageDialog(parent, MessageFormat.format(
                rb.getString("INVALIDVALUE"), name),
                rb.getString("ERROR"), ERROR_MESSAGE);
        recordDataButton.setSelected(false);
        return 0.0;
    }

    private boolean checkInRange(String name, JTextField min,
            JTextField max, double value) {
        if (isValidRange(min, max)) {
            return inRange(value, min, max);
        } else {
            showMessageDialog(parent, MessageFormat.format(
                    rb.getString("INVALIDRANGE"), name),
                    rb.getString("ERROR"), ERROR_MESSAGE);
            recordDataButton.setSelected(false);
            return false;
        }
    }

    private boolean checkGreaterThan(String name, JTextField min, double value) {
        if (isNumber(min)) {
            return value >= parseDouble(min);
        } else {
            showMessageDialog(parent, MessageFormat.format(
                    rb.getString("INVALIDMINMAX"), name),
                    rb.getString("ERROR"), ERROR_MESSAGE);
            recordDataButton.setSelected(false);
            return false;
        }
    }

    private boolean checkLessThan(String name, JTextField max, double value) {
        if (isNumber(max)) {
            return value <= parseDouble(max);
        } else {
            showMessageDialog(parent, MessageFormat.format(
                    rb.getString("INVALIDMINMAX"), name),
                    rb.getString("ERROR"), ERROR_MESSAGE);
            recordDataButton.setSelected(false);
            return false;
        }
    }

    private void addControls() {
        JPanel panel = new JPanel();
        GridBagLayout gridBagLayout = new GridBagLayout();
        panel.setLayout(gridBagLayout);
        add(panel, gridBagLayout, buildFuelPropertiesPanel(), 0, 0, 1, HORIZONTAL);
        add(panel, gridBagLayout, buildFilterPanel(), 0, 1, 1, HORIZONTAL);
        add(panel, gridBagLayout, buildInterpolatePanel(), 0, 2, 1, HORIZONTAL);
        add(panel, gridBagLayout, buildUpdateInjectorPanel(), 0, 3, 1, HORIZONTAL);
        add(panel, gridBagLayout, buildResetPanel(), 0, 4, 1, HORIZONTAL);
        add(panel);
    }

    private void add(JPanel panel, GridBagLayout gridBagLayout,
            JComponent component, int x, int y, int spanX, int fillType) {
        GridBagConstraints constraints = buildBaseConstraints();
        updateConstraints(constraints, x, y, spanX, 1, 1, 1, fillType);
        gridBagLayout.setConstraints(component, constraints);
        panel.add(component);
    }

    private JPanel buildResetPanel() {
        JPanel panel = new JPanel();
        panel.setBorder(new TitledBorder(rb.getString("RESET")));
        panel.add(buildResetButton());
        return panel;
    }

    private JPanel buildInterpolatePanel() {
        JPanel panel = new JPanel();
        panel.setBorder(new TitledBorder(rb.getString("INTERPOLATE")));
        GridBagLayout gridBagLayout = new GridBagLayout();
        panel.setLayout(gridBagLayout);
        addComponent(panel, gridBagLayout, buildInterpolateButton(), 2);
        return panel;
    }

    private JPanel buildUpdateInjectorPanel() {
        JPanel panel = new JPanel();
        panel.setBorder(new TitledBorder(rb.getString("UPDATEINJ")));
        GridBagLayout gridBagLayout = new GridBagLayout();
        panel.setLayout(gridBagLayout);
        flowScaling.setEditable(false);
        latencyOffset.setEditable(false);
        addLabeledComponent(panel, gridBagLayout, rb.getString("FLOWSCALING"),
                flowScaling, 0);
        addComponent(panel, gridBagLayout, buildUpdateInjectorScalerButton(), 2);
        addLabeledComponent(panel, gridBagLayout, rb.getString("LATENCYOFFSET"),
                latencyOffset, 3);
        addComponent(panel, gridBagLayout, buildUpdateInjectorLatencyButton(), 5);
        return panel;
    }

    private void addLabeledComponent(JPanel panel, GridBagLayout gridBagLayout,
            String name, JComponent component, int y) {
        add(panel, gridBagLayout, new JLabel(name), 0, y, 3, HORIZONTAL);
        add(panel, gridBagLayout, component, 0, y + 1, 3, NONE);
    }

    private JPanel buildFuelPropertiesPanel() {
        JPanel panel = new JPanel();
        panel.setBorder(new TitledBorder(rb.getString("FUELPROPERTIES")));
        GridBagLayout gridBagLayout = new GridBagLayout();
        panel.setLayout(gridBagLayout);
        addLabeledComponent(panel, gridBagLayout,
                rb.getString("STOICHAFR"), fuelStoichAfr, 0);
        addLabeledComponent(panel, gridBagLayout,
                rb.getString("DENSITY"), fuelDensity, 3);
        return panel;
    }

    private JPanel buildFilterPanel() {
        JPanel panel = new JPanel();
        panel.setBorder(new TitledBorder(rb.getString("FILTERDATA")));
        GridBagLayout gridBagLayout = new GridBagLayout();
        panel.setLayout(gridBagLayout);
        addMinMaxFilter(panel, gridBagLayout,
                rb.getString("AFRRANGE"), afrMin, afrMax, 0);
        addMinMaxFilter(panel, gridBagLayout,
                rb.getString("RPMRANGE"), rpmMin, rpmMax, 3);
        addMinMaxFilter(panel, gridBagLayout,
                rb.getString("MAFRANGE"), mafMin, mafMax, 6);
        addLabeledComponent(panel, gridBagLayout,
                rb.getString("MINECT"), coolantMin, 9);
        addLabeledComponent(panel, gridBagLayout,
                rb.getString("MAXIAT"), iatMax, 12);
        addLabeledComponent(panel, gridBagLayout,
                rb.getString("MAXDELTA"), mafvChangeMax, 15);
        addComponent(panel, gridBagLayout, buildRecordDataButton(), 18);
        return panel;
    }

    private JToggleButton buildRecordDataButton() {
        recordDataButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (recordDataButton.isSelected()) {
                    registerData(COOLANT_TEMP, ENGINE_SPEED, INTAKE_AIR_TEMP,
                            MASS_AIR_FLOW, MASS_AIR_FLOW_V, AFR, CL_OL_16,
                            CL_OL_32, TIP_IN_THROTTLE_16, TIP_IN_THROTTLE_32,
                            PULSE_WIDTH_16, PULSE_WIDTH_32, ENGINE_LOAD_16,
                            ENGINE_LOAD_32);
                } else {
                    deregisterData(COOLANT_TEMP, ENGINE_SPEED, INTAKE_AIR_TEMP,
                            MASS_AIR_FLOW, MASS_AIR_FLOW_V, AFR, CL_OL_16,
                            CL_OL_32, TIP_IN_THROTTLE_16, TIP_IN_THROTTLE_32,
                            PULSE_WIDTH_16, PULSE_WIDTH_32, ENGINE_LOAD_16,
                            ENGINE_LOAD_32);
                }
            }
        });
        return recordDataButton;
    }

    private void registerData(String... ids) {
        for (String id : ids) {
            LoggerData data = findData(id);
            if (data != null) broker.registerLoggerDataForLogging(data);
        }
    }

    private void deregisterData(String... ids) {
        for (String id : ids) {
            LoggerData data = findData(id);
            if (data != null) broker.deregisterLoggerDataFromLogging(data);
        }
    }

    private LoggerData findData(String id) {
        for (EcuParameter param : params) {
            if (id.equals(param.getId())) return param;
        }
        for (EcuSwitch sw : switches) {
            if (id.equals(sw.getId())) return sw;
        }
        for (ExternalData external : externals) {
            if (id.equals(external.getId())) return external;
        }
        LOGGER.warn("Logger data not found for id: " + id);
        return null;
    }

    private void addComponent(JPanel panel, GridBagLayout gridBagLayout,
            JComponent component, int y) {
        add(panel, gridBagLayout, component, 0, y, 3, HORIZONTAL);
    }

    private void addMinMaxFilter(JPanel panel, GridBagLayout gridBagLayout,
            String name, JTextField min, JTextField max, int y) {
        add(panel, gridBagLayout, new JLabel(name), 0, y, 3, HORIZONTAL);
        y += 1;
        add(panel, gridBagLayout, min, 0, y, 1, NONE);
        add(panel, gridBagLayout, new JLabel(" - "), 1, y, 1, NONE);
        add(panel, gridBagLayout, max, 2, y, 1, NONE);
    }

    private GridBagConstraints buildBaseConstraints() {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.anchor = CENTER;
        constraints.fill = NONE;
        return constraints;
    }

    private void updateConstraints(GridBagConstraints constraints, int gridx,
            int gridy, int gridwidth, int gridheight, int weightx, int weighty,
            int fill) {
        constraints.gridx = gridx;
        constraints.gridy = gridy;
        constraints.gridwidth = gridwidth;
        constraints.gridheight = gridheight;
        constraints.weightx = weightx;
        constraints.weighty = weighty;
        constraints.fill = fill;
    }

    private JButton buildResetButton() {
        JButton resetButton = new JButton(rb.getString("RESETDATA"));
        resetButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                chartPanel.clear();
                parent.repaint();
            }
        });
        return resetButton;
    }

    private JButton buildInterpolateButton() {
        JButton interpolateButton = new JButton(rb.getString("INTERPOLATE"));
        interpolateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                chartPanel.interpolate(1);
                double[] coefficients = chartPanel.getPolynomialCoefficients();
                double scaling = coefficients[0] * 1000 * 60;
                DecimalFormat format = new DecimalFormat("0.00");
                flowScaling.setText(format.format(scaling));
                double offset = -1 * coefficients[1] / coefficients[0];
                latencyOffset.setText(format.format(offset));
                parent.repaint();
            }
        });
        return interpolateButton;
    }

    private JButton buildUpdateInjectorScalerButton() {
        final JButton updateButton = new JButton(rb.getString("UPDATESCALING"));
        updateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                try {
                    if (showUpdateTableConfirmation(
                            rb.getString("INJFLOWSCALING")) == OK_OPTION) {
                        Table2D table = getInjectorFlowTable(ecuEditor);
                        if (table != null) {
                            DataCell[] cells = table.getData();
                            if (cells.length == 1) {
                                if (isNumber(flowScaling)) {
                                    String value = flowScaling.getText().trim();
                                    cells[0].setRealValue(value);
                                } else {
                                    showMessageDialog(parent,
                                            rb.getString("INVALIDSCALING"),
                                            rb.getString("ERROR"), ERROR_MESSAGE);
                                }
                            }
                        } else {
                            showMessageDialog(parent,
                                    rb.getString("INJTABLENOTFND"),
                                    rb.getString("ERROR"), ERROR_MESSAGE);
                        }
                    }
                } catch (Exception e) {
                    String msg = e.getMessage() != null
                            && e.getMessage().length() > 0
                            ? e.getMessage() : rb.getString("UNKNOWN");
                    showMessageDialog(parent, MessageFormat.format(
                            rb.getString("ERRORMSG"), msg),
                            rb.getString("ERROR"), ERROR_MESSAGE);
                }
            }
        });
        return updateButton;
    }

    private JButton buildUpdateInjectorLatencyButton() {
        final JButton updateButton = new JButton(rb.getString("UPDATELAT"));
        updateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                try {
                    if (showUpdateTableConfirmation(rb.getString("INJLAT")) == OK_OPTION) {
                        Table2D table = getInjectorLatencyTable(ecuEditor);
                        if (table != null) {
                            DataCell[] cells = table.getData();
                            if (isNumber(latencyOffset)) {
                                for (DataCell cell : cells) {
                                    double newLatency = cell.getRealValue()
                                            + parseDouble(latencyOffset);
                                    cell.setRealValue("" + newLatency);
                                }
                            } else {
                                showMessageDialog(parent,
                                        rb.getString("INVALIFINJOFF"),
                                        rb.getString("ERROR"), ERROR_MESSAGE);
                            }
                        } else {
                            showMessageDialog(parent,
                                    rb.getString("LATTBLNOTFND"),
                                    rb.getString("ERROR"), ERROR_MESSAGE);
                        }
                    }
                } catch (Exception e) {
                    String msg = e.getMessage() != null
                            && e.getMessage().length() > 0
                            ? e.getMessage() : rb.getString("UNKNOWN");
                    showMessageDialog(parent, MessageFormat.format(
                            rb.getString("ERRORMSG"), msg),
                            rb.getString("ERROR"), ERROR_MESSAGE);
                }
            }
        });
        return updateButton;
    }

    private boolean areNumbers(JTextField... textFields) {
        for (JTextField field : textFields) {
            if (!isNumber(field)) return false;
        }
        return true;
    }

    private boolean isValidRange(JTextField min, JTextField max) {
        return areNumbers(min, max) && parseDouble(min) < parseDouble(max);
    }

    private boolean isNumber(JTextField textField) {
        try {
            parseDouble(textField);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean inRange(double val, double min, double max) {
        return val >= min && val <= max;
    }

    private boolean inRange(double value, JTextField min, JTextField max) {
        return inRange(value, parseDouble(min), parseDouble(max));
    }

    private double parseDouble(JTextField field) {
        return Double.parseDouble(field.getText().trim());
    }

    private int showUpdateTableConfirmation(String table) {
        return showConfirmDialog(parent, MessageFormat.format(
                rb.getString("UPDATETABLE"), table),
                rb.getString("CONFIRM"), YES_NO_OPTION, WARNING_MESSAGE);
    }

    private Table2D getInjectorFlowTable(ECUEditor ecuEditor) {
        return getTable(ecuEditor, "Injector Flow Scaling");
    }

    private Table2D getInjectorLatencyTable(ECUEditor ecuEditor) {
        return getTable(ecuEditor, "Injector Latency");
    }

    private <T extends Table> T getTable(ECUEditor ecuEditor, String name) {
        try {
            Rom rom = ecuEditor.getLastSelectedRom();
            return (T) findTableStartsWith(rom, name);
        } catch (Exception e) {
            LOGGER.warn("Error getting " + name + " table", e);
            return null;
        }
    }

    public void setEcuParams(List<EcuParameter> params) {
        this.params = new ArrayList<EcuParameter>(params);
    }

    public void setEcuSwitches(List<EcuSwitch> switches) {
        this.switches = new ArrayList<EcuSwitch>(switches);
    }

    public void setExternalDatas(List<ExternalData> externals) {
        this.externals = new ArrayList<ExternalData>(externals);
    }
}