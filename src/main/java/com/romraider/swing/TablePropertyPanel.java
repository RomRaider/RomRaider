/*
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2012 RomRaider.com
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

package com.romraider.swing;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.LayoutStyle.ComponentPlacement;

import com.romraider.Settings;
import com.romraider.maps.Scale;
import com.romraider.maps.Table;

public class TablePropertyPanel extends javax.swing.JPanel {

    private static final long serialVersionUID = -5817685772039277602L;

    public TablePropertyPanel(Table table) {
        initComponents();
        setVisible(true);

        tableName.setText(table.getName() + " (" + table.getType() + "D)");
        category.setText(table.getCategory());

        String intType;

        if (Settings.TABLE_SWITCH == table.getType()) {
            intType = "DTC";
            // TODO: fill out other DTC specific properties.
            textPaneScales.setText(Settings.INVALID_ATTRIBUTE_TEXT);
        } else {
            if (table.isSignedData()) {
                intType = "int";
            }
            else {
                intType = "uint";
            }

            String scaleText = "";
            for(Scale scale : table.getScales()) {
                scaleText += scale.toString();
            }

            textPaneScales.setText(scaleText);
        }

        textPaneScales.setCaretPosition(0);

        storageSize.setText(intType + (table.getStorageType() * 8));
        storageAddress.setText("0x" + Integer.toHexString(table.getStorageAddress()));

        if (table.getEndian() == Settings.ENDIAN_BIG) {
            endian.setText("big");
        } else {
            endian.setText("little");
        }

        description.setText(table.getDescription());

        if (table.getUserLevel() == 1) {
            userLevel.setText("Beginner");
        } else if (table.getUserLevel() == 2) {
            userLevel.setText("Intermediate");
        } else if (table.getUserLevel() == 3) {
            userLevel.setText("Advanced");
        } else if (table.getUserLevel() == 4) {
            userLevel.setText("All");
        } else if (table.getUserLevel() == 5) {
            userLevel.setText("Debug");
        }

    }

    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        lblTable = new javax.swing.JLabel();
        tableName = new javax.swing.JLabel();
        lblCategory = new javax.swing.JLabel();
        category = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        lblStorageAddress = new javax.swing.JLabel();
        lblStorageSize = new javax.swing.JLabel();
        lblEndian = new javax.swing.JLabel();
        endian = new javax.swing.JLabel();
        storageSize = new javax.swing.JLabel();
        storageAddress = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        description = new javax.swing.JTextArea();
        jLabel5 = new javax.swing.JLabel();
        userLevel = new javax.swing.JLabel();
        textPaneScales = new JTextPane();
        textPaneScales.setEditable(false);
        scrollPane = new JScrollPane();

        setAutoscrolls(true);
        setFont(new java.awt.Font("Tahoma", 0, 12));
        setInheritsPopupMenu(true);
        lblTable.setText("Table:");
        lblTable.setFocusable(false);

        tableName.setText("Tablename (3D)");
        tableName.setFocusable(false);

        lblCategory.setText("Category:");
        lblCategory.setFocusable(false);

        category.setText("Category");
        category.setFocusable(false);

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createTitledBorder("Conversions")));

        GroupLayout jPanel1Layout = new GroupLayout(jPanel1);
        jPanel1Layout.setHorizontalGroup(
                jPanel1Layout.createParallelGroup(Alignment.LEADING)
                .addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 170, Short.MAX_VALUE)
                );
        jPanel1Layout.setVerticalGroup(
                jPanel1Layout.createParallelGroup(Alignment.LEADING)
                .addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 122, Short.MAX_VALUE)
                );

        scrollPane.setViewportView(textPaneScales);
        jPanel1.setLayout(jPanel1Layout);

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Storage"));
        lblStorageAddress.setText("Storage Address:");
        lblStorageAddress.setFocusable(false);

        lblStorageSize.setText("Data Type:");
        lblStorageSize.setFocusable(false);

        lblEndian.setText("Endian:");
        lblEndian.setFocusable(false);

        endian.setText("little");
        endian.setFocusable(false);

        storageSize.setText("unkn");
        storageSize.setFocusable(false);

        storageAddress.setText("0x00");
        storageAddress.setFocusable(false);

        org.jdesktop.layout.GroupLayout jPanel2Layout = new org.jdesktop.layout.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
                jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(jPanel2Layout.createSequentialGroup()
                        .addContainerGap()
                        .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                .add(lblStorageAddress)
                                .add(lblStorageSize)
                                .add(lblEndian))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                        .add(endian)
                                        .add(storageSize)
                                        .add(storageAddress))
                                        .addContainerGap(28, Short.MAX_VALUE))
                );
        jPanel2Layout.setVerticalGroup(
                jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(jPanel2Layout.createSequentialGroup()
                        .addContainerGap()
                        .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                                .add(lblStorageSize)
                                .add(storageSize))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                                        .add(lblStorageAddress)
                                        .add(storageAddress))
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                                                .add(lblEndian)
                                                .add(endian))
                                                .addContainerGap(37, Short.MAX_VALUE))
                );

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Description"));
        jScrollPane1.setBorder(null);
        description.setBackground(new java.awt.Color(236, 233, 216));
        description.setColumns(20);
        description.setEditable(false);
        description.setFont(new java.awt.Font("Tahoma", 0, 12));
        description.setLineWrap(true);
        description.setRows(5);
        description.setText("Description");
        description.setWrapStyleWord(true);
        description.setBorder(null);
        description.setOpaque(false);
        description.setRequestFocusEnabled(false);
        jScrollPane1.setViewportView(description);

        org.jdesktop.layout.GroupLayout jPanel3Layout = new org.jdesktop.layout.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
                jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(org.jdesktop.layout.GroupLayout.TRAILING, jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 360, Short.MAX_VALUE)
                );
        jPanel3Layout.setVerticalGroup(
                jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(jPanel3Layout.createSequentialGroup()
                        .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 102, Short.MAX_VALUE)
                        .addContainerGap())
                );

        jLabel5.setText("User Level:");

        userLevel.setText("Beginner");

        GroupLayout layout = new GroupLayout(this);
        layout.setHorizontalGroup(
                layout.createParallelGroup(Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(Alignment.LEADING)
                                .addComponent(jPanel1, GroupLayout.DEFAULT_SIZE, 430, Short.MAX_VALUE)
                                .addComponent(jPanel3, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 430, Short.MAX_VALUE)
                                .addGroup(layout.createSequentialGroup()
                                        .addGroup(layout.createParallelGroup(Alignment.LEADING)
                                                .addComponent(lblCategory)
                                                .addComponent(lblTable))
                                                .addPreferredGap(ComponentPlacement.RELATED)
                                                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                                                        .addGroup(layout.createSequentialGroup()
                                                                .addComponent(category)
                                                                .addGap(110)
                                                                .addComponent(jLabel5)
                                                                .addPreferredGap(ComponentPlacement.RELATED)
                                                                .addComponent(userLevel))
                                                                .addComponent(tableName, GroupLayout.DEFAULT_SIZE, 375, Short.MAX_VALUE)))
                                                                .addComponent(jPanel2, GroupLayout.DEFAULT_SIZE, 430, Short.MAX_VALUE))
                                                                .addContainerGap())
                );
        layout.setVerticalGroup(
                layout.createParallelGroup(Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                                .addComponent(tableName)
                                .addComponent(lblTable))
                                .addPreferredGap(ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                                        .addComponent(lblCategory)
                                        .addComponent(category)
                                        .addComponent(jLabel5)
                                        .addComponent(userLevel))
                                        .addPreferredGap(ComponentPlacement.RELATED)
                                        .addComponent(jPanel1, GroupLayout.DEFAULT_SIZE, 101, Short.MAX_VALUE)
                                        .addPreferredGap(ComponentPlacement.RELATED)
                                        .addComponent(jPanel2, GroupLayout.PREFERRED_SIZE, 89, GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(ComponentPlacement.RELATED)
                                        .addComponent(jPanel3, GroupLayout.PREFERRED_SIZE, 136, GroupLayout.PREFERRED_SIZE)
                                        .addContainerGap())
                );
        this.setLayout(layout);
    }// </editor-fold>//GEN-END:initComponents
    private javax.swing.JLabel category;
    private javax.swing.JTextArea description;
    private javax.swing.JLabel endian;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblCategory;
    private javax.swing.JLabel lblEndian;
    private javax.swing.JLabel lblStorageAddress;
    private javax.swing.JLabel lblStorageSize;
    private javax.swing.JLabel lblTable;
    private javax.swing.JLabel storageAddress;
    private javax.swing.JLabel storageSize;
    private javax.swing.JLabel tableName;
    private javax.swing.JLabel userLevel;
    private JTextPane textPaneScales;
    private JScrollPane scrollPane;
}