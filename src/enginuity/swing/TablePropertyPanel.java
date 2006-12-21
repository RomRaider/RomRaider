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

import enginuity.maps.Table;

public class TablePropertyPanel extends javax.swing.JPanel {

    public TablePropertyPanel(Table table) {
        initComponents();
        setVisible(true);

        tableName.setText(table.getName() + " (" + table.getType() + "D)");
        category.setText(table.getCategory());
        unit.setText(table.getScale().getUnit());
        byteToReal.setText(table.getScale().getExpression());
        realToByte.setText(table.getScale().getByteExpression());
        storageSize.setText("uint" + (table.getStorageType() * 8));
        storageAddress.setText("0x" + Integer.toHexString(table.getStorageAddress()));

        if (table.getEndian() == Table.ENDIAN_BIG) {
            endian.setText("big");
        } else {
            endian.setText("little");
        }

        description.setText(table.getDescription());
        fine.setText(table.getScale().getFineIncrement() + "");
        coarse.setText(table.getScale().getCoarseIncrement() + "");

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

    private TablePropertyPanel() {
    }

    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        lblTable = new javax.swing.JLabel();
        tableName = new javax.swing.JLabel();
        lblCategory = new javax.swing.JLabel();
        category = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        lblUnit = new javax.swing.JLabel();
        unit = new javax.swing.JLabel();
        lblByteToReal = new javax.swing.JLabel();
        byteToReal = new javax.swing.JLabel();
        realToByte = new javax.swing.JLabel();
        lblRealToByte = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        coarse = new javax.swing.JLabel();
        fine = new javax.swing.JLabel();
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

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createTitledBorder("Conversion")));
        lblUnit.setText("Unit:");
        lblUnit.setFocusable(false);

        unit.setText("unit");
        unit.setFocusable(false);

        lblByteToReal.setText("Byte to Real:");
        lblByteToReal.setFocusable(false);

        byteToReal.setText("bytetoreal");
        byteToReal.setFocusable(false);

        realToByte.setText("realtobyte");
        realToByte.setFocusable(false);

        lblRealToByte.setText("Real to Byte:");
        lblRealToByte.setFocusable(false);

        jLabel1.setText("Coarse adjust:");

        jLabel2.setText("Fine adjust:");

        coarse.setText("coarse");

        fine.setText("fine");

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
                jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                        .add(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                .add(lblUnit)
                                .add(lblByteToReal)
                                .add(lblRealToByte)
                                .add(jLabel1)
                                .add(jLabel2))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 14, Short.MAX_VALUE)
                        .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                .add(jPanel1Layout.createSequentialGroup()
                                        .add(2, 2, 2)
                                        .add(unit))
                                .add(byteToReal)
                                .add(realToByte)
                                .add(coarse)
                                .add(fine))
                        .add(27, 27, 27))
        );
        jPanel1Layout.setVerticalGroup(
                jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                        .add(jPanel1Layout.createSequentialGroup()
                        .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                                .add(jPanel1Layout.createSequentialGroup()
                                        .add(unit)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(byteToReal)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(realToByte))
                                .add(jPanel1Layout.createSequentialGroup()
                                .add(lblUnit)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(lblByteToReal)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(lblRealToByte)))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                                .add(jLabel1)
                                .add(coarse))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(jLabel2)
                        .add(fine)))
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Storage"));
        lblStorageAddress.setText("Storage Address:");
        lblStorageAddress.setFocusable(false);

        lblStorageSize.setText("Storage Size:");
        lblStorageSize.setFocusable(false);

        lblEndian.setText("Endian:");
        lblEndian.setFocusable(false);

        endian.setText("little");
        endian.setFocusable(false);

        storageSize.setText("uint16");
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
        description.setFont(new java.awt.Font("Tahoma", 0, 11));
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

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                        .add(layout.createSequentialGroup()
                        .addContainerGap()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                .add(layout.createSequentialGroup()
                                        .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(jPanel2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .add(layout.createSequentialGroup()
                                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                                .add(lblCategory)
                                                .add(lblTable))
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                        .add(layout.createSequentialGroup()
                                                .add(category)
                                                .add(110, 110, 110)
                                                .add(jLabel5)
                                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                                .add(userLevel))
                                        .add(tableName, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 321, Short.MAX_VALUE)))
                                .add(jPanel3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addContainerGap())
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                        .add(layout.createSequentialGroup()
                        .addContainerGap()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                                .add(tableName)
                                .add(lblTable))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                                .add(lblCategory)
                                .add(category)
                                .add(jLabel5)
                                .add(userLevel))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                                .add(jPanel2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jPanel3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel byteToReal;
    private javax.swing.JLabel category;
    private javax.swing.JLabel coarse;
    private javax.swing.JTextArea description;
    private javax.swing.JLabel endian;
    private javax.swing.JLabel fine;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblByteToReal;
    private javax.swing.JLabel lblCategory;
    private javax.swing.JLabel lblEndian;
    private javax.swing.JLabel lblRealToByte;
    private javax.swing.JLabel lblStorageAddress;
    private javax.swing.JLabel lblStorageSize;
    private javax.swing.JLabel lblTable;
    private javax.swing.JLabel lblUnit;
    private javax.swing.JLabel realToByte;
    private javax.swing.JLabel storageAddress;
    private javax.swing.JLabel storageSize;
    private javax.swing.JLabel tableName;
    private javax.swing.JLabel unit;
    private javax.swing.JLabel userLevel;
    // End of variables declaration//GEN-END:variables
}