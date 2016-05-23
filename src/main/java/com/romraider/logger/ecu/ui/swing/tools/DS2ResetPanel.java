/*
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2015 RomRaider.com
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
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.border.EmptyBorder;
import com.romraider.logger.ecu.EcuLogger;

public final class DS2ResetPanel extends JDialog {
    private static final long serialVersionUID = 2406346286060014312L;
    private static final String DIALOG_TITLE = "Reset DS2 Adaptation Selection";
    private static final String OK_BUTTON_TEXT = "OK";
    private static final String CANCEL_BUTTON_TEXT = "Cancel";
    private static final String PANEL_TT = "Unsupported items are ignored by the ECU if selected.";
    private static int results;
    private final JCheckBox selectAll = new JCheckBox("Select All");
    private final ResetCheckBox[] resetItems = {
            new ResetCheckBox("Knock Adaptations", 0x0100),
            new ResetCheckBox("Idle Speed Adaptations", 0x0200),
            new ResetCheckBox("Lambda Adaptations", 0x0400),
            new ResetCheckBox("Throttle Adaptations", 0x0800),
            new ResetCheckBox("Altitude Correction", 0x1000),
            new ResetCheckBox("Undefined Adaptation", 0x2000),
            new ResetCheckBox("Undefined Adaptation", 0x4000),
            new ResetCheckBox("Undefined Adaptation", 0x8000),
            new ResetCheckBox("Undefined Adaptation", 0x0001),
            new ResetCheckBox("External Load History (M3 only)", 0x0002),
            new ResetCheckBox("Undefined Adaptation (MS43 only)", 0x0004),
            new ResetCheckBox("Undefined Adaptation (MS43 only)", 0x0008)
    };
    
    public DS2ResetPanel(EcuLogger logger) {
        
        super(logger, true);
        setIconImage(logger.getIconImage());
        setTitle(DIALOG_TITLE);
        setBounds(
                logger.getX() + (logger.getWidth() / 2) - 180,
                logger.getY() + 90,
                286,
                377);

        final JPanel contentPanel = new JPanel();
        contentPanel.setToolTipText(PANEL_TT);
        contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(contentPanel, BorderLayout.CENTER);

        selectAll.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                for (ResetCheckBox item : resetItems) {
                    item.setSelected(selectAll.isSelected());
                }
            }
        });

        for (ResetCheckBox checkBox : resetItems) {
            contentPanel.add(checkBox);
        }
        contentPanel.add(new JSeparator());
        contentPanel.add(selectAll);

        final JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);

        final JButton okButton = new JButton(OK_BUTTON_TEXT);
        okButton.setActionCommand(OK_BUTTON_TEXT);
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                for (ResetCheckBox checkBox : resetItems) {
                    if(checkBox.isSelected()) {
                        results |= checkBox.mask; 
                    }
                }
                closeDialog();
            }
        });
        buttonPanel.add(okButton);

        final JButton cancelButton = new JButton(CANCEL_BUTTON_TEXT);
        cancelButton.setActionCommand(CANCEL_BUTTON_TEXT);
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                results = 0;
                closeDialog();
            }
        });
        buttonPanel.add(cancelButton);
        getRootPane().setDefaultButton(cancelButton);
    }

    public final void showResetPanel() {
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent we) {
                results = 0;
                closeDialog();
            }
        });
        results = 0;
        setVisible(true);
    }

    public final int getResults() {
        return results;
    }

    private final void closeDialog() {
        setVisible(false);
        dispose();
    }

    class ResetCheckBox extends JCheckBox {
        private static final long serialVersionUID = 6324945868965147832L;
        public int mask;

        public ResetCheckBox(String name, int mask) {
            this.mask = mask;
            this.setText(name);
            this.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    final JCheckBox source = (JCheckBox) actionEvent.getSource();
                    if (!source.isSelected()) {
                        selectAll.setSelected(false);
                    }
                }
            });
        }
    }
}
