/*
 *
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2008 RomRaider.com
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

package com.romraider.logger.ecu.ui.handler.dash;

import com.romraider.logger.ecu.definition.EcuParameterWarningType;
import com.romraider.logger.ecu.definition.EcuParameterWarning;
import com.romraider.logger.ecu.definition.LoggerData;
import com.romraider.tts.Speaker;
import com.romraider.swing.GaugeWarningSettingsDialog;
import static com.romraider.util.ParamChecker.checkNotNull;
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
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;


public class PlainGaugeStyle implements GaugeStyle, ActionListener, MouseListener {
	private static final String BLANK = "";
    protected static final Color RED = new Color(190, 30, 30);
    protected static final Color GREEN = new Color(34, 139, 34);
    protected static final Color DARK_GREY = new Color(40, 40, 40);
    protected static final Color LIGHT_GREY = new Color(56, 56, 56);
    protected static final Color LIGHTER_GREY = new Color(120, 120, 120);
    protected final JPanel liveValuePanel = new JPanel(new BorderLayout());
    protected final JLabel liveValueLabel = new JLabel(BLANK, JLabel.CENTER);
    protected final JLabel maxLabel = new JLabel(BLANK, JLabel.CENTER);
    protected final JLabel minLabel = new JLabel(BLANK, JLabel.CENTER);
    protected final JLabel title = new JLabel(BLANK, JLabel.CENTER);
    protected final JProgressBar progressBar = new JProgressBar(JProgressBar.VERTICAL);
    private final String zeroText;
    private final LoggerData loggerData;
    private double max = Double.MIN_VALUE;
    private double min = Double.MAX_VALUE;
    private JPanel panel = new JPanel();

    public PlainGaugeStyle(LoggerData loggerData) {
        checkNotNull(loggerData, "loggerData");
        this.loggerData = loggerData;
        zeroText = format(loggerData, 0.0);
        liveValueLabel.addMouseListener(this);
    }

    public void refreshTitle() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                title.setText(loggerData.getName() + " (" + loggerData.getSelectedConvertor().getUnits() + ')');
            }
        });
    }

    public void updateValue(double value) {
        refreshValue(value);
        if (loggerData.getWarning().getWarningType() != EcuParameterWarningType.WARN_NONE) {
            if (loggerData.getWarning().getWarningType() == EcuParameterWarningType.WARN_ABOVE) {
                setWarningFlag(value >= getWarnThreshold());
            } else if (loggerData.getWarning().getWarningType() == EcuParameterWarningType.WARN_BELOW) {
                setWarningFlag(value <= getWarnThreshold());
            }
        }
    }

    public void resetValue() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                liveValueLabel.setText(zeroText);
                max = Double.MIN_VALUE;
                maxLabel.setText(zeroText);
                min = Double.MAX_VALUE;
                minLabel.setText(zeroText);
                progressBar.setMinimum(scaleForProgressBar(min));
                progressBar.setMaximum(scaleForProgressBar(max));
                progressBar.setValue(scaleForProgressBar(min));
            }
        });
    }

    public void actionPerformed(ActionEvent e) {
    }

    public void apply(JPanel panel) {
        this.panel = panel;
        doApply(panel);
    }

    protected void doApply(JPanel panel) {
        refreshTitle();
        resetValue();
        panel.setPreferredSize(new Dimension(236, 144));
        panel.setBackground(LIGHT_GREY);
        panel.setLayout(new BorderLayout(3, 0));

        // title
        title.setFont(panel.getFont().deriveFont(BOLD, 12F));
        title.setForeground(WHITE);
        panel.add(title, NORTH);

        // data panel
        JPanel data = new JPanel(new FlowLayout(FlowLayout.CENTER, 3, 1));
        data.setBackground(BLACK);
        liveValueLabel.setFont(panel.getFont().deriveFont(PLAIN, 40F));
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
        warnPanel.add(warnFormPanel);

        // add panels
        panel.add(data, CENTER);
        panel.add(warnPanel, SOUTH);
    }

    private JPanel buildMaxMinPanel(String title, JLabel label) {
        JPanel panel = new JPanel(new BorderLayout(1, 1));
        label.setFont(panel.getFont().deriveFont(PLAIN, 12F));
        label.setForeground(WHITE);
        panel.setPreferredSize(new Dimension(60, 38));
        panel.setBackground(LIGHT_GREY);
        JLabel titleLabel = new JLabel(title, JLabel.CENTER);
        titleLabel.setFont(panel.getFont().deriveFont(BOLD, 12F));
        titleLabel.setForeground(WHITE);
        JPanel dataPanel = new JPanel(new BorderLayout());
        dataPanel.setBackground(DARK_GREY);
        dataPanel.add(label, CENTER);
        panel.add(titleLabel, NORTH);
        panel.add(dataPanel, CENTER);
        return panel;
    }

    private void refreshValue(final double value) {
        final String text = format(loggerData, value);
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

    private double getWarnThreshold() {
        return Double.valueOf(loggerData.getWarning().getWarningValue());
    }

    private void setWarningFlag(final boolean enabled) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if (enabled) {
                    panel.setBackground(RED);
                    liveValuePanel.setBackground(RED);
                    progressBar.setForeground(RED);
                    Speaker.say("Warning!");
                } else {
                    panel.setBackground(LIGHT_GREY);
                    liveValuePanel.setBackground(LIGHT_GREY);
                    progressBar.setForeground(GREEN);
                }
            }
        });
    }

    private String format(LoggerData loggerData, double value) {
        return loggerData.getSelectedConvertor().format(value);
    }

    private int scaleForProgressBar(double value) {
        return (int) (value * 1000.0);
    }

	public void mouseClicked(MouseEvent arg0) {
		if(arg0.getButton() == MouseEvent.BUTTON1)
		{
			GaugeWarningSettingsDialog setDiag = new GaugeWarningSettingsDialog(loggerData.getWarning());
			setDiag.setModal(true);
			setDiag.setVisible(true);
			loggerData.setWarning(setDiag.getWarningSettings());
			setDiag.dispose();
		}
	}

	public void mouseEntered(MouseEvent arg0) {
	}

	public void mouseExited(MouseEvent arg0) {
	}

	public void mousePressed(MouseEvent arg0) {
	}

	public void mouseReleased(MouseEvent arg0) {
	}
}
