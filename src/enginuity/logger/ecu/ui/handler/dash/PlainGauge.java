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

import enginuity.logger.ecu.definition.LoggerData;
import static enginuity.util.ParamChecker.checkNotNull;

import static javax.swing.BorderFactory.createLineBorder;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import static java.awt.BorderLayout.CENTER;
import static java.awt.BorderLayout.NORTH;
import static java.awt.BorderLayout.SOUTH;
import java.awt.Color;
import static java.awt.Color.BLACK;
import static java.awt.Color.WHITE;
import java.awt.Dimension;
import java.awt.FlowLayout;
import static java.awt.Font.BOLD;
import static java.awt.Font.PLAIN;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public final class PlainGauge extends Gauge implements ActionListener {
    private static final Color RED = new Color(190, 30, 30);
    private static final Color GREEN = new Color(34, 139, 34);
    private static final Color DARK_GREY = new Color(40, 40, 40);
    private static final Color LIGHT_GREY = new Color(56, 56, 56);
    private static final Color LIGHTER_GREY = new Color(120, 120, 120);
    private static final String BLANK = "";
    private static final String ABOVE = "above";
    private static final String BELOW = "below";
    private final String zeroText;
    private final LoggerData loggerData;
    private final JPanel liveValuePanel = new JPanel(new BorderLayout());
    private final JLabel liveValueLabel = new JLabel(BLANK, JLabel.CENTER);
    private final JLabel maxLabel = new JLabel(BLANK, JLabel.CENTER);
    private final JLabel minLabel = new JLabel(BLANK, JLabel.CENTER);
    private final JLabel title = new JLabel(BLANK, JLabel.CENTER);
    private final JProgressBar progressBar = new JProgressBar(JProgressBar.VERTICAL);
    private final JCheckBox warnCheckBox = new JCheckBox("Warn");
    private final JComboBox warnType = new JComboBox(new Object[]{ABOVE, BELOW});
    private final JTextField warnTextField = new JTextField();
    private double max = Double.MIN_VALUE;
    private double min = Double.MAX_VALUE;

    public PlainGauge(LoggerData loggerData) {
        checkNotNull(loggerData, "loggerData");
        this.loggerData = loggerData;
        zeroText = format(0.0);
        initGaugeLayout();
    }

    public void refreshTitle() {
        title.setText(loggerData.getName() + " (" + loggerData.getSelectedConvertor().getUnits() + ')');
    }

    public void updateValue(double value) {
        refreshValue(value);
        if (warnCheckBox.isSelected() && isValidWarnThreshold()) {
            if (warnType.getSelectedItem() == ABOVE) {
                setWarning(value >= getWarnThreshold());
            } else if (warnType.getSelectedItem() == BELOW) {
                setWarning(value <= getWarnThreshold());
            }
        }
    }

    public void resetValue() {
        liveValueLabel.setText(zeroText);
        max = Double.MIN_VALUE;
        maxLabel.setText(zeroText);
        min = Double.MAX_VALUE;
        minLabel.setText(zeroText);
        progressBar.setMinimum(scaleForProgressBar(min));
        progressBar.setMaximum(scaleForProgressBar(max));
        progressBar.setValue(scaleForProgressBar(min));
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == warnCheckBox) {
            if (!warnCheckBox.isSelected()) {
                setWarning(false);
            }
        }
    }

    private void initGaugeLayout() {
        refreshTitle();
        resetValue();
        setPreferredSize(new Dimension(236, 144));
        setBackground(LIGHT_GREY);
        setLayout(new BorderLayout(3, 0));

        // title
        title.setFont(getFont().deriveFont(BOLD, 12F));
        title.setForeground(WHITE);
        add(title, NORTH);

        // data panel
        JPanel data = new JPanel(new FlowLayout(FlowLayout.CENTER, 3, 1));
        data.setBackground(BLACK);
        liveValueLabel.setFont(getFont().deriveFont(PLAIN, 40F));
        liveValueLabel.setForeground(WHITE);
        liveValuePanel.setBackground(LIGHT_GREY);
        liveValuePanel.setPreferredSize(new Dimension(140, 80));
        liveValuePanel.add(liveValueLabel, CENTER);
        data.add(liveValuePanel);

        // max/min panel
        JPanel maxMinPanel = new JPanel(new BorderLayout(2, 2));
        maxMinPanel.setBackground(BLACK);
        JPanel maxPanel = buildMaxMinPanel("max", maxLabel);
        JPanel minPanel = buildMaxMinPanel("min", minLabel);
        maxMinPanel.add(maxPanel, NORTH);
        maxMinPanel.add(minPanel, SOUTH);
        data.add(maxMinPanel);

        // progress bar
        progressBar.setStringPainted(false);
        progressBar.setIndeterminate(false);
        progressBar.setPreferredSize(new Dimension(20, 82));
        progressBar.setBackground(WHITE);
        progressBar.setForeground(GREEN);
        data.add(progressBar);

        // warn setting
        JPanel warnPanel = new JPanel();
        warnPanel.setBackground(BLACK);
        JPanel warnFormPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        warnFormPanel.setPreferredSize(new Dimension(226, 34));
        warnFormPanel.setBackground(BLACK);
        warnFormPanel.setBorder(createLineBorder(LIGHT_GREY, 1));
        warnCheckBox.setFont(getFont().deriveFont(PLAIN, 10F));
        warnCheckBox.setBackground(BLACK);
        warnCheckBox.setForeground(LIGHTER_GREY);
        warnCheckBox.setSelected(false);
        warnCheckBox.addActionListener(this);
        warnType.setPreferredSize(new Dimension(60, 20));
        warnType.setFont(getFont().deriveFont(PLAIN, 10F));
        warnType.setBackground(BLACK);
        warnType.setForeground(LIGHTER_GREY);
        warnTextField.setColumns(4);
        warnTextField.setBackground(BLACK);
        warnTextField.setForeground(LIGHTER_GREY);
        warnTextField.setCaretColor(LIGHTER_GREY);
        warnFormPanel.add(warnCheckBox);
        warnFormPanel.add(warnType);
        warnFormPanel.add(warnTextField);
        warnPanel.add(warnFormPanel);

        // add panels
        add(data, CENTER);
        add(warnPanel, SOUTH);
    }

    private JPanel buildMaxMinPanel(String title, JLabel label) {
        label.setFont(getFont().deriveFont(PLAIN, 12F));
        label.setForeground(WHITE);
        JPanel panel = new JPanel(new BorderLayout(1, 1));
        panel.setPreferredSize(new Dimension(60, 38));
        panel.setBackground(LIGHT_GREY);
        JLabel titleLabel = new JLabel(title, JLabel.CENTER);
        titleLabel.setFont(getFont().deriveFont(BOLD, 12F));
        titleLabel.setForeground(WHITE);
        JPanel dataPanel = new JPanel(new BorderLayout());
        dataPanel.setBackground(DARK_GREY);
        dataPanel.add(label, CENTER);
        panel.add(titleLabel, NORTH);
        panel.add(dataPanel, CENTER);
        return panel;
    }

    private void refreshValue(final double value) {
        final String text = format(value);
        final int scaledValue = scaleForProgressBar(value);
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if (value > max) {
                    max = value;
                    maxLabel.setText(text);
                    progressBar.setMaximum(scaledValue);
                }
                if (value < min) {
                    min = value;
                    minLabel.setText(text);
                    progressBar.setMinimum(scaledValue);
                }
                liveValueLabel.setText(text);
                progressBar.setValue(scaledValue);
            }
        });
    }

    private boolean isValidWarnThreshold() {
        try {
            getWarnThreshold();
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private double getWarnThreshold() {
        return Double.parseDouble(warnTextField.getText());
    }

    private void setWarning(final boolean enabled) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if (enabled) {
                    setBackground(RED);
                    liveValuePanel.setBackground(RED);
                    progressBar.setForeground(RED);
                } else {
                    setBackground(LIGHT_GREY);
                    liveValuePanel.setBackground(LIGHT_GREY);
                    progressBar.setForeground(GREEN);
                }
            }
        });
    }

    private String format(double value) {
        return loggerData.getSelectedConvertor().format(value);
    }

    private int scaleForProgressBar(double value) {
        return (int) (value * 1000.0);
    }
}
