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

public final class DS2ResetItemsPanel extends JDialog {
    private static final long serialVersionUID = 2406346286060014312L;
    private static final String DIALOG_TITLE = "Reset DS2 Adaption Selection";
    private static final String OK_BUTTON_TEXT = "OK";
    private static final String CANCEL_BUTTON_TEXT = "Cancel";
    private static int results;
    private final JCheckBox selectAll = new JCheckBox("Select All");
    private final ResetItem[] resetItems = {
            new ResetItem("Byte 1 bit 0", 0x0100),
            new ResetItem("Byte 1 bit 1", 0x0200),
            new ResetItem("Byte 1 bit 2", 0x0400),
            new ResetItem("Byte 1 bit 3", 0x0800),
            new ResetItem("Byte 1 bit 4", 0x1000),
            new ResetItem("Byte 1 bit 5", 0x2000),
            new ResetItem("Byte 1 bit 6", 0x4000),
            new ResetItem("Byte 1 bit 7", 0x8000),
            new ResetItem("Byte 0 bit 0", 0x0001),
            new ResetItem("Byte 0 bit 1", 0x0002)
    };
    
    public DS2ResetItemsPanel(EcuLogger logger) {
        
        super(logger, true);
        setIconImage(logger.getIconImage());
        setTitle(DIALOG_TITLE);
        setBounds(
                logger.getX() + (logger.getWidth() / 2) - 166,
                logger.getY() + 90,
                332,
                332);

        final JPanel contentPanel = new JPanel();
        contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(contentPanel, BorderLayout.CENTER);

        selectAll.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                for (ResetItem item : resetItems) {
                    item.checkBox.setSelected(selectAll.isSelected());
                }
            }
        });
        contentPanel.add(selectAll);

        contentPanel.add(new JSeparator());

        for (ResetItem item : resetItems) {
            contentPanel.add(item.checkBox);
        }

        final JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);

        final JButton okButton = new JButton(OK_BUTTON_TEXT);
        okButton.setActionCommand(OK_BUTTON_TEXT);
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                for (ResetItem item : resetItems) {
                    if(item.checkBox.isSelected()) {
                        results |= item.mask; 
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

    public final void showResetItemsPanel() {
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

    class ResetItem {
        public String name;
        public int mask;
        public JCheckBox checkBox;

        public ResetItem(String name, int mask) {
            this.name = name;
            this.mask = mask;
            checkBox = new JCheckBox(name);
            checkBox.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    final JCheckBox source = (JCheckBox) actionEvent.getSource();
                    if (!source.isSelected()) {
                        selectAll.setSelected(false);
                    }
                }
            });
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
