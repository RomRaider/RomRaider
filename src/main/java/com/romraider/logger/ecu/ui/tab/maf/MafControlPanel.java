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

package com.romraider.logger.ecu.ui.tab.maf;

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
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.border.TitledBorder;

import org.apache.log4j.Logger;
import org.jfree.ui.KeyedComboBoxModel;

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

public final class MafControlPanel extends JPanel {
    private static final long serialVersionUID = 5787020251107365950L;
    private static final Logger LOGGER = Logger.getLogger(MafControlPanel.class);
    private static final ResourceBundle rb = new ResourceUtil().getBundle(
            MafControlPanel.class.getName());
    private static final String COOLANT_TEMP = "P2";
    private static final String AF_CORRECTION_1 = "P3";
    private static final String AF_LEARNING_1 = "P4";
    private static final String ENGINE_SPEED = "P8";
    private static final String INTAKE_AIR_TEMP = "P11";
    private static final String MASS_AIR_FLOW = "P12";
    private static final String MASS_AIR_FLOW_V = "P18";
    private static final String AFR = "P58";
    private static final String CL_OL_16 = "E3";
    private static final String CL_OL_32 = "E33";
    private static final String TIP_IN_THROTTLE_16 = "E23";
    private static final String TIP_IN_THROTTLE_32 = "E54";
    private final JToggleButton recordDataButton = new JToggleButton(
            rb.getString("RECORDDATA"));
    private final JTextField mafvMin = new JTextField("1.20", 3);
    private final JTextField mafvMax = new JTextField("2.60", 3);
    private final JTextField afrMin = new JTextField("13.0", 3);
    private final JTextField afrMax = new JTextField("16.0", 3);
    private final JTextField rpmMin = new JTextField("0", 3);
    private final JTextField rpmMax = new JTextField("4500", 3);
    private final JTextField mafMin = new JTextField("0", 3);
    private final JTextField mafMax = new JTextField("100", 3);
    private final JTextField iatMax = new JTextField("100", 3);
    private final JTextField coolantMin = new JTextField("70", 3);
    private final JTextField mafvChangeMax = new JTextField("0.1", 3);
    private final JComboBox afrSourceList = new JComboBox();
    private final DataRegistrationBroker broker;
    private final LoggerChartPanel chartPanel;
    private final ECUEditor ecuEditor;
    private final Component parent;
    private List<ExternalData> externals = new ArrayList<ExternalData>();
    private List<EcuParameter> params = new ArrayList<EcuParameter>();
    private List<EcuSwitch> switches = new ArrayList<EcuSwitch>();

    public MafControlPanel(Component parent, DataRegistrationBroker broker,
            ECUEditor ecuEditor, LoggerChartPanel chartPanel) {
        checkNotNull(parent, broker, chartPanel);
        this.parent = parent;
        this.broker = broker;
        this.chartPanel = chartPanel;
        this.ecuEditor = ecuEditor;
        addControls();
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

    private boolean checkInRange(String name, JTextField min, JTextField max, double value) {
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

        //        add(panel, gridBagLayout, buildAfrSourcePanel(), 0, 0, 1, HORIZONTAL);
        //        add(panel, gridBagLayout, buildFilterPanel(), 0, 1, 1, HORIZONTAL);
        //        add(panel, gridBagLayout, buildInterpolatePanel(), 0, 2, 1, HORIZONTAL);
        //        add(panel, gridBagLayout, buildUpdateMafPanel(), 0, 3, 1, HORIZONTAL);
        //        add(panel, gridBagLayout, buildResetPanel(), 0, 4, 1, HORIZONTAL);

        add(panel, gridBagLayout, buildFilterPanel(), 0, 0, 1, HORIZONTAL);
        add(panel, gridBagLayout, buildInterpolatePanel(), 0, 1, 1, HORIZONTAL);
        add(panel, gridBagLayout, buildUpdateMafPanel(), 0, 2, 1, HORIZONTAL);
        add(panel, gridBagLayout, buildResetPanel(), 0, 3, 1, HORIZONTAL);

        add(panel);
    }

    private void add(JPanel panel, GridBagLayout gridBagLayout, JComponent component, int x, int y, int spanX, int fillType) {
        GridBagConstraints constraints = buildBaseConstraints();
        updateConstraints(constraints, x, y, spanX, 1, 1, 1, fillType);
        gridBagLayout.setConstraints(component, constraints);
        panel.add(component);
    }

    private JPanel buildAfrSourcePanel() {
        JPanel panel = new JPanel();
        panel.setBorder(new TitledBorder(rb.getString("AFRSRC")));
        panel.add(afrSourceList);
        return panel;
    }

    private JPanel buildResetPanel() {
        JPanel panel = new JPanel();
        panel.setBorder(new TitledBorder(rb.getString("RESET")));
        panel.add(buildResetButton());
        return panel;
    }

    private JPanel buildUpdateMafPanel() {
        JPanel panel = new JPanel();
        panel.setBorder(new TitledBorder(rb.getString("UPDATEMAF")));

        GridBagLayout gridBagLayout = new GridBagLayout();
        panel.setLayout(gridBagLayout);

        addMinMaxFilter(panel, gridBagLayout, rb.getString("MAFVRANGE"),
                mafvMin, mafvMax, 0);
        addComponent(panel, gridBagLayout, buildUpdateMafButton(), 3);

        return panel;
    }

    private JPanel buildInterpolatePanel() {
        JPanel panel = new JPanel();
        panel.setBorder(new TitledBorder(rb.getString("INTERPOLATE")));

        GridBagLayout gridBagLayout = new GridBagLayout();
        panel.setLayout(gridBagLayout);

        JComboBox orderComboBox = buildPolyOrderComboBox();
        addLabeledComponent(panel, gridBagLayout, rb.getString("POLYORDER"),
                orderComboBox, 0);
        addComponent(panel, gridBagLayout, buildInterpolateButton(orderComboBox), 2);

        return panel;
    }

    private void addLabeledComponent(JPanel panel, GridBagLayout gridBagLayout,
            String name, JComponent component, int y) {
        add(panel, gridBagLayout, new JLabel(name), 0, y, 3, HORIZONTAL);
        add(panel, gridBagLayout, component, 0, y + 1, 3, NONE);
    }

    private JPanel buildFilterPanel() {
        JPanel panel = new JPanel();
        panel.setBorder(new TitledBorder(rb.getString("FILTERDATA")));

        GridBagLayout gridBagLayout = new GridBagLayout();
        panel.setLayout(gridBagLayout);

        addMinMaxFilter(panel, gridBagLayout, rb.getString("AFRRANGE"), afrMin, afrMax, 0);
        addMinMaxFilter(panel, gridBagLayout, rb.getString("RPMRANGE"), rpmMin, rpmMax, 3);
        addMinMaxFilter(panel, gridBagLayout, rb.getString("MAFRANGE"), mafMin, mafMax, 6);
        addLabeledComponent(panel, gridBagLayout, rb.getString("MINECT"), coolantMin, 9);
        addLabeledComponent(panel, gridBagLayout, rb.getString("MAXIAT"), iatMax, 12);
        addLabeledComponent(panel, gridBagLayout, rb.getString("MAXDELTA"), mafvChangeMax, 15);
        addComponent(panel, gridBagLayout, buildRecordDataButton(), 18);

        return panel;
    }

    private JToggleButton buildRecordDataButton() {
        recordDataButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (recordDataButton.isSelected()) {
                    //                    afrSourceList.setEnabled(false);
                    //                    registerAfr();
                    registerData(COOLANT_TEMP, AF_CORRECTION_1, AF_LEARNING_1,
                            ENGINE_SPEED, INTAKE_AIR_TEMP, MASS_AIR_FLOW,
                            MASS_AIR_FLOW_V, CL_OL_16, CL_OL_32,
                            TIP_IN_THROTTLE_16, TIP_IN_THROTTLE_32);
                } else {
                    //                    deregisterAfr();
                    deregisterData(COOLANT_TEMP, AF_CORRECTION_1, AF_LEARNING_1,
                            ENGINE_SPEED, INTAKE_AIR_TEMP, MASS_AIR_FLOW,
                            MASS_AIR_FLOW_V, CL_OL_16, CL_OL_32,
                            TIP_IN_THROTTLE_16, TIP_IN_THROTTLE_32);
                    //                    afrSourceList.setEnabled(true);
                }
            }
        });
        return recordDataButton;
    }

    private void registerAfr() {
        LoggerData data = getSelectedAfrSource();
        if (data != null) broker.registerLoggerDataForLogging(data);
    }

    private void deregisterAfr() {
        LoggerData data = getSelectedAfrSource();
        if (data != null) broker.deregisterLoggerDataFromLogging(data);
    }

    private LoggerData getSelectedAfrSource() {
        KeyedComboBoxModel model = (KeyedComboBoxModel) afrSourceList.getModel();
        return (LoggerData) model.getSelectedKey();
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
        return null;
    }

    private void addComponent(JPanel panel, GridBagLayout gridBagLayout, JComponent component, int y) {
        add(panel, gridBagLayout, component, 0, y, 3, HORIZONTAL);
    }

    private void addMinMaxFilter(JPanel panel, GridBagLayout gridBagLayout, String name, JTextField min, JTextField max, int y) {
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

    private void updateConstraints(GridBagConstraints constraints, int gridx, int gridy, int gridwidth, int gridheight, int weightx, int weighty, int fill) {
        constraints.gridx = gridx;
        constraints.gridy = gridy;
        constraints.gridwidth = gridwidth;
        constraints.gridheight = gridheight;
        constraints.weightx = weightx;
        constraints.weighty = weighty;
        constraints.fill = fill;
    }

    private void updateAfrSourceList() {
        List<LoggerData> sources = new ArrayList<LoggerData>();
        LoggerData afr = findData(AFR);
        if (afr != null) sources.add(afr);
        sources.addAll(externals);
        List<String> keys = new ArrayList<String>();
        for (LoggerData source : sources) keys.add(source.getName());
        afrSourceList.setModel(new KeyedComboBoxModel(sources.toArray(new LoggerData[sources.size()]), keys.toArray(new String[keys.size()])));
        afrSourceList.setSelectedIndex(0);
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

    private JButton buildInterpolateButton(final JComboBox orderComboBox) {
        JButton interpolateButton = new JButton(rb.getString("INTERPOLATE"));
        interpolateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                chartPanel.interpolate((Integer) orderComboBox.getSelectedItem());
                parent.repaint();
            }
        });
        return interpolateButton;
    }

    private JComboBox buildPolyOrderComboBox() {
        final JComboBox orderComboBox = new JComboBox(new Object[]{3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20});
        orderComboBox.setSelectedItem(10);
        return orderComboBox;
    }

    private JButton buildUpdateMafButton() {
        final JButton updateMafButton = new JButton(rb.getString("UPDATEMAF"));
        updateMafButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                try {
                    if (showUpdateMafConfirmation() == OK_OPTION) {
                        Table2D table = getMafTable(ecuEditor);
                        if (table != null) {
                            if (isValidRange(mafvMin, mafvMax)) {
                                DataCell[] axisCells = table.getAxis().getData();
                                double[] x = new double[axisCells.length];
                                for (int i = 0; i < axisCells.length; i++) {
                                    DataCell cell = axisCells[i];
                                    x[i] = cell.getRealValue();
                                }
                                double[] percentChange = chartPanel.calculate(x);
                                DataCell[] dataCells = table.getData();
                                for (int i = 0; i < dataCells.length; i++) {
                                    if (inRange(axisCells[i].getRealValue(), mafvMin, mafvMax)) {
                                        DataCell cell = dataCells[i];
                                        double value = cell.getRealValue();
                                        cell.setRealValue("" + (value * (1.0 + percentChange[i] / 100.0)));
                                    }
                                }
                            } else {
                                showMessageDialog(parent,
                                        rb.getString("INVALIDMAFRANGE"),
                                        rb.getString("ERROR"), ERROR_MESSAGE);
                            }
                        } else {
                            showMessageDialog(parent,
                                    rb.getString("MAFTBLNOTFND"),
                                    rb.getString("ERROR"), ERROR_MESSAGE);
                        }
                    }
                } catch (Exception e) {
                    String msg = e.getMessage() != null && e.getMessage().length() > 0 ? e.getMessage() : "Unknown";
                    showMessageDialog(parent, MessageFormat.format(
                            rb.getString("ERRORMSG"), msg),
                            rb.getString("ERROR"), ERROR_MESSAGE);
                }
            }
        });
        return updateMafButton;
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

    private int showUpdateMafConfirmation() {
        return showConfirmDialog(parent, 
                rb.getString("UPDATETABLE"),
                rb.getString("CONFIRM"), YES_NO_OPTION, WARNING_MESSAGE);
    }

    private Table2D getMafTable(ECUEditor ecuEditor) {
        return getTable(ecuEditor, "MAF Sensor Scaling");
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
        //        updateAfrSourceList();
    }

    public void setEcuSwitches(List<EcuSwitch> switches) {
        this.switches = new ArrayList<EcuSwitch>(switches);
    }

    public void setExternalDatas(List<ExternalData> externals) {
        this.externals = new ArrayList<ExternalData>(externals);
        //        updateAfrSourceList();
    }
}
