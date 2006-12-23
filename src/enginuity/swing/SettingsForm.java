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

package enginuity.swing;

import ZoeloeSoft.projects.JFontChooser.JFontChooser;
import enginuity.ECUEditor;
import enginuity.Settings;
import enginuity.util.FileAssociator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.StringTokenizer;

public class SettingsForm extends JFrame implements MouseListener {

    Settings settings;
    ECUEditor parent;

    public SettingsForm(ECUEditor parent) {
        this.parent = parent;
        settings = parent.getSettings();
        initComponents();
        initSettings();

        maxColor.addMouseListener(this);
        minColor.addMouseListener(this);
        highlightColor.addMouseListener(this);
        axisColor.addMouseListener(this);
        increaseColor.addMouseListener(this);
        decreaseColor.addMouseListener(this);
        warningColor.addMouseListener(this);

        btnOk.addMouseListener(this);
        btnApply.addMouseListener(this);
        btnCancel.addMouseListener(this);
        btnChooseFont.addMouseListener(this);
        reset.addMouseListener(this);
        btnAddAssocs.addMouseListener(this);
        btnRemoveAssocs.addMouseListener(this);

        tableClickCount.setBackground(Color.WHITE);
        
        // disable file assocation buttons if user is not in Windows        
        StringTokenizer osName = new StringTokenizer(System.getProperties().getProperty("os.name"));
        if (!osName.nextToken().equalsIgnoreCase("windows")) {
            btnAddAssocs.setEnabled(false);
            btnRemoveAssocs.setEnabled(false);
            extensionHex.setEnabled(false);
            extensionBin.setEnabled(false);
        }

    }

    private void initSettings() {

        obsoleteWarning.setSelected(settings.isObsoleteWarning());
        calcConflictWarning.setSelected(settings.isCalcConflictWarning());
        displayHighTables.setSelected(settings.isDisplayHighTables());
        saveDebugTables.setSelected(settings.isSaveDebugTables());
        debug.setSelected(settings.isDebug());

        maxColor.setBackground(settings.getMaxColor());
        minColor.setBackground(settings.getMinColor());
        highlightColor.setBackground(settings.getHighlightColor());
        axisColor.setBackground(settings.getAxisColor());
        increaseColor.setBackground(settings.getIncreaseBorder());
        decreaseColor.setBackground(settings.getDecreaseBorder());

        cellWidth.setText(((int) settings.getCellSize().getWidth()) + "");
        cellHeight.setText(((int) settings.getCellSize().getHeight()) + "");

        btnChooseFont.setFont(settings.getTableFont());
        btnChooseFont.setText(settings.getTableFont().getFontName());

        if (settings.getTableClickCount() == 1) { // single click opens table
            tableClickCount.setSelectedIndex(0);
        } else { // double click opens table
            tableClickCount.setSelectedIndex(1);
        }

        valueLimitWarning.setSelected(settings.isValueLimitWarning());
        warningColor.setBackground(settings.getWarningColor());
    }

    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        obsoleteWarning = new javax.swing.JCheckBox();
        calcConflictWarning = new javax.swing.JCheckBox();
        debug = new javax.swing.JCheckBox();
        btnCancel = new javax.swing.JButton();
        btnOk = new javax.swing.JButton();
        btnApply = new javax.swing.JButton();
        reset = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        lblAxis = new javax.swing.JLabel();
        lblHighlight = new javax.swing.JLabel();
        lblMin = new javax.swing.JLabel();
        lblMax = new javax.swing.JLabel();
        maxColor = new javax.swing.JLabel();
        minColor = new javax.swing.JLabel();
        highlightColor = new javax.swing.JLabel();
        axisColor = new javax.swing.JLabel();
        warningColor = new javax.swing.JLabel();
        lblWarning = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        lblIncrease = new javax.swing.JLabel();
        increaseColor = new javax.swing.JLabel();
        decreaseColor = new javax.swing.JLabel();
        lblDecrease = new javax.swing.JLabel();
        lblCellHeight = new javax.swing.JLabel();
        cellHeight = new javax.swing.JTextField();
        cellWidth = new javax.swing.JTextField();
        lblCellWidth = new javax.swing.JLabel();
        lblFont = new javax.swing.JLabel();
        btnChooseFont = new javax.swing.JButton();
        saveDebugTables = new javax.swing.JCheckBox();
        displayHighTables = new javax.swing.JCheckBox();
        valueLimitWarning = new javax.swing.JCheckBox();
        jPanel4 = new javax.swing.JPanel();
        extensionHex = new javax.swing.JCheckBox();
        extensionBin = new javax.swing.JCheckBox();
        btnAddAssocs = new javax.swing.JButton();
        btnRemoveAssocs = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        tableClickCount = new javax.swing.JComboBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Enginuity Settings");
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        setFont(new java.awt.Font("Tahoma", 0, 11));
        obsoleteWarning.setText("Warn me when opening out of date ECU image revision");
        obsoleteWarning.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        obsoleteWarning.setMargin(new java.awt.Insets(0, 0, 0, 0));

        calcConflictWarning.setText("Warn me when real and byte value calculations conflict");
        calcConflictWarning.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        calcConflictWarning.setMargin(new java.awt.Insets(0, 0, 0, 0));

        debug.setText("Debug mode");
        debug.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        debug.setEnabled(false);
        debug.setMargin(new java.awt.Insets(0, 0, 0, 0));

        btnCancel.setMnemonic('C');
        btnCancel.setText("Cancel");

        btnOk.setMnemonic('O');
        btnOk.setText("OK");

        btnApply.setMnemonic('A');
        btnApply.setText("Apply");

        reset.setText("Restore Defaults");

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Table Display"));
        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Background"));
        lblAxis.setText("Axis Cell:");

        lblHighlight.setText("Highlighted Cell:");

        lblMin.setText("Minimum Value:");

        lblMax.setText("Maximum Value:");

        maxColor.setBackground(new java.awt.Color(255, 0, 0));
        maxColor.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        maxColor.setOpaque(true);

        minColor.setBackground(new java.awt.Color(255, 0, 0));
        minColor.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        minColor.setOpaque(true);

        highlightColor.setBackground(new java.awt.Color(255, 0, 0));
        highlightColor.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        highlightColor.setOpaque(true);

        axisColor.setBackground(new java.awt.Color(255, 0, 0));
        axisColor.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        axisColor.setOpaque(true);

        warningColor.setBackground(new java.awt.Color(255, 0, 0));
        warningColor.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        warningColor.setOpaque(true);

        lblWarning.setText("Warning:");

        org.jdesktop.layout.GroupLayout jPanel2Layout = new org.jdesktop.layout.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel2Layout.createSequentialGroup()
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(lblWarning)
                    .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                        .add(jPanel2Layout.createSequentialGroup()
                            .add(4, 4, 4)
                            .add(lblMin))
                        .add(lblMax)))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel2Layout.createSequentialGroup()
                        .add(maxColor, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 50, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 22, Short.MAX_VALUE)
                        .add(lblHighlight)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(highlightColor, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 50, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(jPanel2Layout.createSequentialGroup()
                        .add(minColor, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 50, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 55, Short.MAX_VALUE)
                        .add(lblAxis)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(axisColor, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 50, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(warningColor, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 50, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel2Layout.createSequentialGroup()
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lblMax)
                    .add(maxColor, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 15, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(highlightColor, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 15, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(lblHighlight))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lblMin)
                    .add(minColor, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 15, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(axisColor, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 15, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(lblAxis))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(warningColor, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 15, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(lblWarning)))
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Cell Borders"));
        lblIncrease.setText("Increased:");

        increaseColor.setBackground(new java.awt.Color(255, 0, 0));
        increaseColor.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        increaseColor.setOpaque(true);

        decreaseColor.setBackground(new java.awt.Color(255, 0, 0));
        decreaseColor.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        decreaseColor.setOpaque(true);

        lblDecrease.setText("Decreased:");

        org.jdesktop.layout.GroupLayout jPanel3Layout = new org.jdesktop.layout.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .add(lblIncrease)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(increaseColor, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 50, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 59, Short.MAX_VALUE)
                .add(lblDecrease)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(decreaseColor, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 50, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                .add(decreaseColor, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 15, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(lblDecrease)
                .add(lblIncrease)
                .add(increaseColor, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 15, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );

        lblCellHeight.setText("Cell Height:");

        lblCellWidth.setText("Cell Width:");

        lblFont.setText("Font:");

        btnChooseFont.setText("Choose");

        saveDebugTables.setText("Save changes made on tables in debug mode");
        saveDebugTables.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        saveDebugTables.setMargin(new java.awt.Insets(0, 0, 0, 0));

        displayHighTables.setText("List tables that are above my userlevel");
        displayHighTables.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        displayHighTables.setMargin(new java.awt.Insets(0, 0, 0, 0));

        valueLimitWarning.setText("Warn when values exceed limits");
        valueLimitWarning.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        valueLimitWarning.setMargin(new java.awt.Insets(0, 0, 0, 0));

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder("File Associations"));
        extensionHex.setText("HEX");
        extensionHex.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        extensionHex.setMargin(new java.awt.Insets(0, 0, 0, 0));

        extensionBin.setText("BIN");
        extensionBin.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        extensionBin.setMargin(new java.awt.Insets(0, 0, 0, 0));

        btnAddAssocs.setText("Add Associations");

        btnRemoveAssocs.setText("Remove Associations");

        org.jdesktop.layout.GroupLayout jPanel4Layout = new org.jdesktop.layout.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(extensionBin)
                    .add(extensionHex))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 93, Short.MAX_VALUE)
                .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(btnAddAssocs, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(btnRemoveAssocs))
                .add(25, 25, 25))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel4Layout.createSequentialGroup()
                .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(btnAddAssocs)
                    .add(extensionHex))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(btnRemoveAssocs)
                    .add(extensionBin)))
        );

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .add(jPanel3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .add(jPanel4, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                        .add(saveDebugTables)
                        .add(displayHighTables)
                        .add(valueLimitWarning))
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(lblCellHeight)
                            .add(lblFont))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(btnChooseFont)
                            .add(jPanel1Layout.createSequentialGroup()
                                .add(cellHeight, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 50, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 42, Short.MAX_VALUE)
                                .add(lblCellWidth)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(cellWidth, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 50, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))))
                .add(47, 47, 47))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .add(jPanel2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel4, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(22, 22, 22)
                .add(saveDebugTables)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(displayHighTables)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(valueLimitWarning)
                .add(27, 27, 27)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lblCellWidth)
                    .add(cellWidth, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(lblCellHeight)
                    .add(cellHeight, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lblFont)
                    .add(btnChooseFont, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 18, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
        );

        jLabel1.setText("click to open tables");

        tableClickCount.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Single", "Double" }));

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jPanel1, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, calcConflictWarning)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, obsoleteWarning)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                        .add(tableClickCount, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jLabel1))
                    .add(org.jdesktop.layout.GroupLayout.LEADING, debug)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                        .add(reset)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 34, Short.MAX_VALUE)
                        .add(btnApply)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(btnOk)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(btnCancel)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(tableClickCount, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 18, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(obsoleteWarning)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(calcConflictWarning)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(debug)
                .add(17, 17, 17)
                .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(22, 22, 22)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(btnCancel)
                    .add(btnApply)
                    .add(reset)
                    .add(btnOk))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        pack();
    }// </editor-fold>//GEN-END:initComponents

    public void mouseClicked(MouseEvent e) {
        if (e.getSource() == maxColor) {
            Color color = JColorChooser.showDialog(this.getContentPane(),
                    "Background Color", settings.getMaxColor());
            if (color != null) {
                maxColor.setBackground(color);
            }
        } else if (e.getSource() == minColor) {
            Color color = JColorChooser.showDialog(this.getContentPane(),
                    "Background Color", settings.getMinColor());
            if (color != null) {
                minColor.setBackground(color);
            }
        } else if (e.getSource() == highlightColor) {
            Color color = JColorChooser.showDialog(this.getContentPane(),
                    "Background Color", settings.getHighlightColor());
            if (color != null) {
                highlightColor.setBackground(color);
            }
        } else if (e.getSource() == axisColor) {
            Color color = JColorChooser.showDialog(this.getContentPane(),
                    "Background Color", settings.getAxisColor());
            if (color != null) {
                axisColor.setBackground(color);
            }
        } else if (e.getSource() == increaseColor) {
            Color color = JColorChooser.showDialog(this.getContentPane(),
                    "Background Color", settings.getIncreaseBorder());
            if (color != null) {
                increaseColor.setBackground(color);
            }
        } else if (e.getSource() == decreaseColor) {
            Color color = JColorChooser.showDialog(this.getContentPane(),
                    "Background Color", settings.getDecreaseBorder());
            if (color != null) {
                decreaseColor.setBackground(color);
            }
        } else if (e.getSource() == warningColor) {
            Color color = JColorChooser.showDialog(this.getContentPane(),
                    "Warning Color", settings.getWarningColor());
            if (color != null) {
                warningColor.setBackground(color);
            }
        } else if (e.getSource() == btnApply) {
            applySettings();
        } else if (e.getSource() == btnOk) {
            applySettings();
            this.dispose();
        } else if (e.getSource() == btnCancel) {
            this.dispose();
        } else if (e.getSource() == btnChooseFont) {
            JFontChooser fc = new JFontChooser(this);
            fc.setLocationRelativeTo(this);
            if (fc.showDialog(settings.getTableFont()) == JFontChooser.OK_OPTION) {
                btnChooseFont.setFont(fc.getFont());
                btnChooseFont.setText(fc.getFont().getFontName());
            }
        } else if (e.getSource() == reset) {
            settings = new Settings();
            initSettings();
        } else if (e.getSource() == btnAddAssocs) {
            // add file associations for selected file types
            try {
                if (extensionHex.isSelected()) {
                    FileAssociator.addAssociation("HEX", new File(".").getCanonicalPath() + "\\Enginuity.exe", "ECU Image");   
                }

                if (extensionBin.isSelected()) {
                    FileAssociator.addAssociation("BIN", new File(".").getCanonicalPath() + "\\Enginuity.exe", "ECU Image");                
                }
            } catch (Exception ex) { }
            
        } else if (e.getSource() == btnRemoveAssocs) {
            // remove file associations for selected file types
            if (extensionHex.isSelected()) {
                FileAssociator.removeAssociation("HEX");
            }
            
            if (extensionBin.isSelected()) {
                FileAssociator.removeAssociation("HEX");                
            }
            
        }
    }

    public void applySettings() {
        try {
            Integer.parseInt(cellHeight.getText());
        } catch (NumberFormatException ex) {
            // number formatted imporperly, reset
            cellHeight.setText((int) (settings.getCellSize().getHeight()) + "");
        }
        try {
            Integer.parseInt(cellWidth.getText());
        } catch (NumberFormatException ex) {
            // number formatted imporperly, reset
            cellWidth.setText((int) (settings.getCellSize().getWidth()) + "");
        }

        settings.setObsoleteWarning(obsoleteWarning.isSelected());
        settings.setCalcConflictWarning(calcConflictWarning.isSelected());
        settings.setDisplayHighTables(displayHighTables.isSelected());
        settings.setSaveDebugTables(saveDebugTables.isSelected());
        settings.setDebug(debug.isSelected());

        settings.setMaxColor(maxColor.getBackground());
        settings.setMinColor(minColor.getBackground());
        settings.setHighlightColor(highlightColor.getBackground());
        settings.setAxisColor(axisColor.getBackground());
        settings.setIncreaseBorder(increaseColor.getBackground());
        settings.setDecreaseBorder(decreaseColor.getBackground());

        settings.setCellSize(new Dimension(Integer.parseInt(cellWidth.getText()),
                Integer.parseInt(cellHeight.getText())));

        settings.setTableFont(btnChooseFont.getFont());

        if (tableClickCount.getSelectedIndex() == 0) { // single click opens table
            settings.setTableClickCount(1);
        } else { // double click opens table
            settings.setTableClickCount(2);
        }

        settings.setValueLimitWarning(valueLimitWarning.isSelected());
        settings.setWarningColor(warningColor.getBackground());

        parent.setSettings(settings);
    }

    public void mousePressed(MouseEvent e) {
    }

    public void mouseReleased(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel axisColor;
    private javax.swing.JButton btnAddAssocs;
    private javax.swing.JButton btnApply;
    private javax.swing.JButton btnCancel;
    private javax.swing.JButton btnChooseFont;
    private javax.swing.JButton btnOk;
    private javax.swing.JButton btnRemoveAssocs;
    private javax.swing.JCheckBox calcConflictWarning;
    private javax.swing.JTextField cellHeight;
    private javax.swing.JTextField cellWidth;
    private javax.swing.JCheckBox debug;
    private javax.swing.JLabel decreaseColor;
    private javax.swing.JCheckBox displayHighTables;
    private javax.swing.JCheckBox extensionBin;
    private javax.swing.JCheckBox extensionHex;
    private javax.swing.JLabel highlightColor;
    private javax.swing.JLabel increaseColor;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JLabel lblAxis;
    private javax.swing.JLabel lblCellHeight;
    private javax.swing.JLabel lblCellWidth;
    private javax.swing.JLabel lblDecrease;
    private javax.swing.JLabel lblFont;
    private javax.swing.JLabel lblHighlight;
    private javax.swing.JLabel lblIncrease;
    private javax.swing.JLabel lblMax;
    private javax.swing.JLabel lblMin;
    private javax.swing.JLabel lblWarning;
    private javax.swing.JLabel maxColor;
    private javax.swing.JLabel minColor;
    private javax.swing.JCheckBox obsoleteWarning;
    private javax.swing.JButton reset;
    private javax.swing.JCheckBox saveDebugTables;
    private javax.swing.JComboBox tableClickCount;
    private javax.swing.JCheckBox valueLimitWarning;
    private javax.swing.JLabel warningColor;
    // End of variables declaration//GEN-END:variables

}