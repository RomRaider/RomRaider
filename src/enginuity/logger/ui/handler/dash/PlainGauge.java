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

import java.awt.*;
import static java.awt.BorderLayout.*;
import static java.awt.Color.BLACK;
import static java.awt.Color.WHITE;
import static java.awt.Font.BOLD;
import static java.awt.Font.PLAIN;
import javax.swing.*;
import javax.swing.border.BevelBorder;
import static javax.swing.border.BevelBorder.LOWERED;
import enginuity.logger.definition.EcuData;
import static enginuity.util.ParamChecker.checkNotNull;

public final class PlainGauge extends Gauge {
    private static final Color RED = new Color(190, 30, 30);
    private static final Color DARK_GREY = new Color(40, 40, 40);
    private static final Color LIGHT_GREY = new Color(56, 56, 56);
    private static final String BLANK = "";
    private final String zeroText;
    private final EcuData ecuData;
    private final JLabel liveValueLabel = new JLabel(BLANK, JLabel.CENTER);
    private final JLabel maxLabel = new JLabel(BLANK, JLabel.CENTER);
    private final JLabel minLabel = new JLabel(BLANK, JLabel.CENTER);
    private final JLabel title = new JLabel(BLANK, JLabel.CENTER);
    private final JProgressBar progressBar = new JProgressBar(JProgressBar.VERTICAL);
    private double max = Double.MIN_VALUE;
    private double min = Double.MAX_VALUE;

    public PlainGauge(EcuData ecuData) {
        checkNotNull(ecuData, "ecuData");
        this.ecuData = ecuData;
        zeroText = format(0.0);
        initGaugeLayout();
    }

    public void refreshTitle() {
        title.setText(ecuData.getName() + " (" + ecuData.getSelectedConvertor().getUnits() + ')');
    }

    public void updateValue(double value) {
        refreshValue(value);
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

    private void initGaugeLayout() {
        refreshTitle();
        resetValue();
        setPreferredSize(new Dimension(60, 40));
        setBackground(LIGHT_GREY);
        setLayout(new BorderLayout(3, 3));

        // title
        title.setFont(getFont().deriveFont(BOLD, 12F));
        title.setForeground(WHITE);
        add(title, NORTH);

        // data panel
        JPanel data = new JPanel(new FlowLayout(FlowLayout.CENTER, 3, 3));
        data.setBackground(BLACK);
        data.setBorder(new BevelBorder(LOWERED));
        liveValueLabel.setFont(getFont().deriveFont(PLAIN, 40F));
        liveValueLabel.setForeground(WHITE);
        JPanel liveValuePanel = new JPanel(new BorderLayout());
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
        progressBar.setForeground(RED);
        data.add(progressBar);
        add(data, CENTER);
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

    private void refreshValue(double value) {
        String text = format(value);
        int scaledValue = scaleForProgressBar(value);
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

    private String format(double value) {
        return ecuData.getSelectedConvertor().format(value);
    }

    private int scaleForProgressBar(double value) {
        return (int) (value * 1000.0);
    }

}
