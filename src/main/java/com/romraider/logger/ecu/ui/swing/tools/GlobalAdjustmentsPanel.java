/*
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2019 RomRaider.com
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

package com.romraider.logger.ecu.ui.swing.tools;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Collection;
import java.util.ResourceBundle;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;

import com.romraider.logger.ecu.EcuLogger;
import com.romraider.logger.ecu.comms.query.EcuQuery;
import com.romraider.util.ResourceUtil;

public final class GlobalAdjustmentsPanel extends JDialog {
    private static final long serialVersionUID = 6751698409230811074L;
    private static final ResourceBundle rb = new ResourceUtil().getBundle(
            GlobalAdjustmentsPanel.class.getName());
    private static final String DIALOG_TITLE = rb.getString("DIALOGTITLE");
    private static final String TIMING_ADJUST_LABEL_TEXT =
            rb.getString("TIMINGADJ");
    private static final String[] COMBO_DATA =
            new String[] {"0", "-1", "-2", "-3", "-4", "-5"};
    private static final String TIMING_UNITS_LABEL_TEXT = "\u00B0";
    private static final String IDLE_AC_OFF_LABEL_TEXT =
            rb.getString("IDLEACOFF");
    private static final String IDLE_RPM_LABEL_TEXT = rb.getString("RPM");
    private static final String IDLE_RPM_TT_TEXT =
            rb.getString("IDLERPMTT");
    private static final SpinnerNumberModel OFF_RPM_SPINNER =
            new SpinnerNumberModel(0, -100, 300, 25);
    private static final SpinnerNumberModel ON_RPM_SPINNER =
            new SpinnerNumberModel(0, -100, 300, 25);
    private static final String IDLE_AC_ON_LABEL_TEXT =
            rb.getString("IDLEACON");
    private static final String RESET_WARNING =
            rb.getString("RESETWARNING");
    private static final String APPLY_BUTTON_TEXT = rb.getString("APPLY");
    private static final String CANCEL_BUTTON_TEXT = rb.getString("CANCEL");
    private static int[] results;

    public GlobalAdjustmentsPanel(
            EcuLogger logger, Collection<EcuQuery> queries) {
        
        super(logger, true);
        setIconImage(logger.getIconImage());
        setTitle(DIALOG_TITLE);
        setBounds(
                logger.getX() + (logger.getWidth() / 2) - 140,
                logger.getY() + 90,
                280,
                245);

        final JPanel contentPanel = new JPanel();
        contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        contentPanel.setLayout(null);
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(contentPanel, BorderLayout.CENTER);
        
        final JLabel timingAdjustmentLabel = new JLabel(TIMING_ADJUST_LABEL_TEXT);
        timingAdjustmentLabel.setBounds(26, 33, 139, 20);
        contentPanel.add(timingAdjustmentLabel);

        final JComboBox timingAdjustmentComboBox = new JComboBox();
        timingAdjustmentComboBox.setBounds(166, 33, 54, 20);
        final DefaultListCellRenderer cellRndr = new DefaultListCellRenderer();
        cellRndr.setHorizontalAlignment(DefaultListCellRenderer.RIGHT);
        timingAdjustmentComboBox.setRenderer(cellRndr);
        final DefaultComboBoxModel comboDataModel =
                new DefaultComboBoxModel(COMBO_DATA);
        timingAdjustmentComboBox.setModel(comboDataModel);
        contentPanel.add(timingAdjustmentComboBox);

        final JLabel timingUintsLabel = new JLabel(TIMING_UNITS_LABEL_TEXT);
        timingUintsLabel.setBounds(224, 33, 32, 20);
        contentPanel.add(timingUintsLabel);
        
        final JLabel idleSpeedOffLabel = new JLabel(IDLE_AC_OFF_LABEL_TEXT);
        idleSpeedOffLabel.setBounds(26, 70, 139, 20);
        contentPanel.add(idleSpeedOffLabel);
        
        final JSpinner idleSpeedOffRpm = new JSpinner(OFF_RPM_SPINNER);
        idleSpeedOffRpm.setToolTipText(IDLE_RPM_TT_TEXT);
        idleSpeedOffRpm.setBounds(166, 70, 54, 20);
        contentPanel.add(idleSpeedOffRpm);
        
        final JLabel offRpmUnitsLabel = new JLabel(IDLE_RPM_LABEL_TEXT);
        offRpmUnitsLabel.setBounds(224, 70, 32, 20);
        contentPanel.add(offRpmUnitsLabel);

        final JLabel idleSpeedOnLabel = new JLabel(IDLE_AC_ON_LABEL_TEXT);
        idleSpeedOnLabel.setBounds(26, 107, 139, 20);
        contentPanel.add(idleSpeedOnLabel);

        final JSpinner idleSpeedOnRpm = new JSpinner(ON_RPM_SPINNER);
        idleSpeedOnRpm.setToolTipText(IDLE_RPM_TT_TEXT);
        idleSpeedOnRpm.setBounds(166, 107, 54, 20);
        contentPanel.add(idleSpeedOnRpm);

        final JLabel onRpmUnitsLabel = new JLabel(IDLE_RPM_LABEL_TEXT);
        onRpmUnitsLabel.setBounds(224, 107, 32, 20);
        contentPanel.add(onRpmUnitsLabel);

        final JLabel resetWarningLabel = new JLabel(RESET_WARNING);
        resetWarningLabel.setFont(new Font("Tahoma", Font.PLAIN, 10));
        resetWarningLabel.setForeground(Color.BLUE);
        resetWarningLabel.setBounds(11, 138, 246, 20);
        contentPanel.add(resetWarningLabel);

        final JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);

        final JButton applyButton = new JButton(APPLY_BUTTON_TEXT);
        applyButton.setActionCommand(APPLY_BUTTON_TEXT);
        applyButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                results[0] = Integer.parseInt(
                        timingAdjustmentComboBox.getSelectedItem().toString()) * -1;
                results[1] = Integer.parseInt(
                        idleSpeedOffRpm.getValue().toString());
                results[2] = Integer.parseInt(
                        idleSpeedOnRpm.getValue().toString());
                closeDialog();
            }
        });
        buttonPanel.add(applyButton);

        final JButton cancelButton = new JButton(CANCEL_BUTTON_TEXT);
        cancelButton.setActionCommand(CANCEL_BUTTON_TEXT);
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                results = null;
                closeDialog();
            }
        });
        buttonPanel.add(cancelButton);
        getRootPane().setDefaultButton(cancelButton);

        for (EcuQuery query : queries) {
            if (query.getLoggerData().getId().equals("P239")) {
                final String timingValue =
                        String.valueOf((int) query.getResponse());
                timingAdjustmentComboBox.setSelectedItem(timingValue);
            }
            if (query.getLoggerData().getId().equals("P240")) {
                idleSpeedOffRpm.setValue((int) query.getResponse());
            }
            if (query.getLoggerData().getId().equals("P241")) {
                idleSpeedOnRpm.setValue((int) query.getResponse());
            }
        }
    }

    public final void showGlobalAdjustPanel() {
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) {
                results = null;
                closeDialog();
            }
        });
        results = new int[3];
        setVisible(true);
    }

    public final int[] getResults() {
        return results;
    }

    private final void closeDialog() {
        setVisible(false);
        dispose();
    }
}
