package enginuity.logger.ecu.ui.tab.maf;

import enginuity.ECUEditor;
import enginuity.logger.ecu.definition.EcuParameter;
import enginuity.logger.ecu.definition.EcuSwitch;
import enginuity.logger.ecu.definition.ExternalData;
import enginuity.logger.ecu.definition.LoggerData;
import enginuity.logger.ecu.ui.DataRegistrationBroker;
import enginuity.logger.ecu.ui.handler.maf.XYTrendline;
import enginuity.maps.DataCell;
import enginuity.maps.Rom;
import enginuity.maps.Table2D;
import static enginuity.util.ParamChecker.checkNotNull;
import org.apache.log4j.Logger;
import org.jfree.data.xy.XYSeries;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.OK_OPTION;
import static javax.swing.JOptionPane.WARNING_MESSAGE;
import static javax.swing.JOptionPane.YES_NO_OPTION;
import static javax.swing.JOptionPane.showConfirmDialog;
import static javax.swing.JOptionPane.showMessageDialog;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.border.TitledBorder;
import java.awt.Component;
import java.awt.GridBagConstraints;
import static java.awt.GridBagConstraints.CENTER;
import static java.awt.GridBagConstraints.HORIZONTAL;
import static java.awt.GridBagConstraints.NONE;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public final class MafControlPanel extends JPanel {
    private static final Logger LOGGER = Logger.getLogger(MafControlPanel.class);
    private static final String COOLANT_TEMP = "P2";
    private static final String AF_CORRECTION_1 = "P3";
    private static final String AF_LEARNING_1 = "P4";
    private static final String ENGINE_SPEED = "P8";
    private static final String INTAKE_AIR_TEMP = "P11";
    private static final String MASS_AIR_FLOW = "P12";
    private static final String MASS_AIR_FLOW_V = "P18";
    private static final String AFR = "P58";
    private static final String CL_OL_16 = "E3";
    private static final String CL_OL_32 = "E27";
    private final JToggleButton recordDataButton = new JToggleButton("Record Data");
    private final JTextField mafvMin = new JTextField("1.20", 3);
    private final JTextField mafvMax = new JTextField("2.60", 3);
    private final JTextField afrMin = new JTextField("14.0", 3);
    private final JTextField afrMax = new JTextField("16.0", 3);
    private final JTextField rpmMin = new JTextField("0", 3);
    private final JTextField rpmMax = new JTextField("4500", 3);
    private final JTextField mafMin = new JTextField("0", 3);
    private final JTextField mafMax = new JTextField("100", 3);
    private final JTextField iatMin = new JTextField("25", 3);
    private final JTextField iatMax = new JTextField("35", 3);
    private final JTextField coolantMin = new JTextField("70", 3);
    private final JCheckBox clCheckbox = new JCheckBox("Closed Loop Only", true);
    private final Component parent;
    private final XYTrendline trendline;
    private final XYSeries series;
    private final ECUEditor ecuEditor;
    private final DataRegistrationBroker broker;
    private List<EcuParameter> params;
    private List<EcuSwitch> switches;
    private List<ExternalData> externals;

    public MafControlPanel(Component parent, XYTrendline trendline, XYSeries series, DataRegistrationBroker broker, ECUEditor ecuEditor) {
        this.broker = broker;
        checkNotNull(parent, trendline, series);
        this.parent = parent;
        this.trendline = trendline;
        this.series = series;
        this.ecuEditor = ecuEditor;
        addControls();
    }

    public boolean isRecordData() {
        return recordDataButton.isSelected();
    }

    public boolean isValidClOl(double value) {
        return value == 8 || (!clCheckbox.isSelected() && value == 10);
    }

    public boolean isValidAfr(double value) {
        return checkInRange("AFR", afrMin, afrMax, value);
    }

    public boolean isValidRpm(double value) {
        return checkInRange("RPM", rpmMin, rpmMax, value);
    }

    public boolean isValidMaf(double value) {
        return checkInRange("MAF", mafMin, mafMax, value);
    }

    public boolean isValidMafv(double value) {
        return checkInRange("MAFv", mafvMin, mafvMax, value);
    }

    public boolean isValidCoolantTemp(double value) {
        return checkGreaterThan("Coolant Temp.", coolantMin, value);
    }

    public boolean isValidIntakeAirTemp(double value) {
        return checkInRange("Intake Air Temp.", iatMin, iatMax, value);
    }

    private boolean checkInRange(String name, JTextField min, JTextField max, double value) {
        if (isValidRange(min, max)) {
            return inRange(value, min, max);
        } else {
            showMessageDialog(parent, "Invalid " + name + " range specified.", "Error", ERROR_MESSAGE);
            recordDataButton.setSelected(false);
            return false;
        }
    }

    private boolean checkGreaterThan(String name, JTextField min, double value) {
        if (isNumber(min)) {
            return value >= parseDouble(min);
        } else {
            showMessageDialog(parent, "Invalid " + name + " specified.", "Error", ERROR_MESSAGE);
            recordDataButton.setSelected(false);
            return false;
        }
    }

    private void addControls() {
        JPanel panel = new JPanel();

        GridBagLayout gridBagLayout = new GridBagLayout();
        panel.setLayout(gridBagLayout);

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

    private JPanel buildResetPanel() {
        JPanel panel = new JPanel();
        panel.setBorder(new TitledBorder("Reset"));
        panel.add(buildResetButton());
        return panel;
    }

    private JPanel buildUpdateMafPanel() {
        JPanel panel = new JPanel();
        panel.setBorder(new TitledBorder("Update MAF"));

        GridBagLayout gridBagLayout = new GridBagLayout();
        panel.setLayout(gridBagLayout);

        addMinMaxFilter(panel, gridBagLayout, "MAFv Range", mafvMin, mafvMax, 0);
        addComponent(panel, gridBagLayout, buildUpdateMafButton(), 3);

        return panel;
    }

    private JPanel buildInterpolatePanel() {
        JPanel panel = new JPanel();
        panel.setBorder(new TitledBorder("Interpolate"));

        GridBagLayout gridBagLayout = new GridBagLayout();
        panel.setLayout(gridBagLayout);

        JComboBox orderComboBox = buildPolyOrderComboBox();
        addLabeledComponent(panel, gridBagLayout, "Poly. order", orderComboBox, 0);
        addComponent(panel, gridBagLayout, buildInterpolateButton(orderComboBox), 2);

        return panel;
    }

    private void addLabeledComponent(JPanel panel, GridBagLayout gridBagLayout, String name, JComponent component, int y) {
        add(panel, gridBagLayout, new JLabel(name), 0, y, 3, HORIZONTAL);
        add(panel, gridBagLayout, component, 0, y + 1, 3, NONE);
    }

    private JPanel buildFilterPanel() {
        JPanel panel = new JPanel();
        panel.setBorder(new TitledBorder("Filter Data"));

        GridBagLayout gridBagLayout = new GridBagLayout();
        panel.setLayout(gridBagLayout);

        addComponent(panel, gridBagLayout, clCheckbox, 0);
        addMinMaxFilter(panel, gridBagLayout, "AFR Range", afrMin, afrMax, 1);
        addMinMaxFilter(panel, gridBagLayout, "RPM Range", rpmMin, rpmMax, 4);
        addMinMaxFilter(panel, gridBagLayout, "MAF Range (g/s)", mafMin, mafMax, 7);
        addMinMaxFilter(panel, gridBagLayout, "IAT Range", iatMin, iatMax, 10);
        addLabeledComponent(panel, gridBagLayout, "Min. Coolant Temp.", coolantMin, 13);
        addComponent(panel, gridBagLayout, buildRecordDataButton(), 16);

        return panel;
    }

    private JToggleButton buildRecordDataButton() {
        recordDataButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                if (recordDataButton.isSelected()) {
                    registerData(COOLANT_TEMP, AF_CORRECTION_1, AF_LEARNING_1, ENGINE_SPEED, INTAKE_AIR_TEMP, MASS_AIR_FLOW, MASS_AIR_FLOW_V, AFR, CL_OL_16, CL_OL_32);
                } else {
                    deregisterData(COOLANT_TEMP, AF_CORRECTION_1, AF_LEARNING_1, ENGINE_SPEED, INTAKE_AIR_TEMP, MASS_AIR_FLOW, MASS_AIR_FLOW_V, AFR, CL_OL_16, CL_OL_32);
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

    private JButton buildResetButton() {
        JButton resetButton = new JButton("Reset Data");
        resetButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                trendline.clear();
                series.clear();
                parent.repaint();
            }
        });
        return resetButton;
    }

    private JButton buildInterpolateButton(final JComboBox orderComboBox) {
        JButton interpolateButton = new JButton("Interpolate");
        interpolateButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                trendline.update(series, (Integer) orderComboBox.getSelectedItem());
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
        final JButton updateMafButton = new JButton("Update MAF");
        updateMafButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                try {
                    Table2D table = getMafTable(ecuEditor);
                    if (table != null) {
                        if (showUpdateMafConfirmation() == OK_OPTION) {
                            if (isValidRange(mafvMin, mafvMax)) {
                                DataCell[] axisCells = table.getAxis().getData();
                                double[] x = new double[axisCells.length];
                                for (int i = 0; i < axisCells.length; i++) {
                                    DataCell cell = axisCells[i];
                                    x[i] = cell.getValue();
                                }
                                double[] percentChange = trendline.calculate(x);
                                DataCell[] dataCells = table.getData();
                                for (int i = 0; i < dataCells.length; i++) {
                                    if (inRange(axisCells[i].getValue(), mafvMin, mafvMax)) {
                                        DataCell cell = dataCells[i];
                                        double value = cell.getValue();
                                        cell.setRealValue(String.valueOf(value * (1.0 + percentChange[i] / 100.0)));
                                    }
                                }
                                table.colorize();
                            } else {
                                showMessageDialog(parent, "Invalid MAFv range specified.", "Error", ERROR_MESSAGE);
                            }
                        }
                    } else {
                        showMessageDialog(parent, "MAF Sensor Scaling table not found.", "Error", ERROR_MESSAGE);
                    }
                } catch (Exception e) {
                    String msg = e.getMessage() != null && e.getMessage().length() > 0 ? e.getMessage() : "Unknown";
                    showMessageDialog(parent, "Error: " + msg, "Error", ERROR_MESSAGE);
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
        return showConfirmDialog(parent, "Update MAF Sensor Scaling table?", "Confirm Update", YES_NO_OPTION, WARNING_MESSAGE);
    }

    private Table2D getMafTable(ECUEditor ecuEditor) {
        try {
            Rom rom = ecuEditor.getLastSelectedRom();
            return (Table2D) rom.getTable("MAF Sensor Scaling");
        } catch (Exception e) {
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
