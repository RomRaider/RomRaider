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

import com.romraider.maps.Rom;

import javax.swing.*;

public class RomPropertyPanel extends javax.swing.JPanel {

    private static final long serialVersionUID = 5583360728106071942L;
    Rom rom = new Rom();

    public RomPropertyPanel(Rom rom) {
        initComponents();

        // populate fields
        fileName.setText(rom.getFileName());
        xmlID.setText(rom.getRomID().getXmlid());
        ecuVersion.setText(rom.getRomID().getCaseId());
        fileSize.setText((rom.getRealFileSize() / 1024) + "kb");
        internalID.setText(rom.getRomID().getInternalIdString());
        storageAddress.setText("0x" + Integer.toHexString(rom.getRomID().getInternalIdAddress()));

        make.setText(rom.getRomID().getMake());
        market.setText(rom.getRomID().getMarket());
        year.setText(rom.getRomID().getYear() + "");
        model.setText(rom.getRomID().getModel());
        submodel.setText(rom.getRomID().getSubModel());
        transmission.setText(rom.getRomID().getTransmission());
        editStamp.setText(rom.getRomID().getEditStamp());

        tableList.setListData(rom.getTables());
    }

    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        lblFilename = new javax.swing.JLabel();
        fileName = new javax.swing.JLabel();
        lblECURevision = new javax.swing.JLabel();
        xmlID = new javax.swing.JLabel();
        lblFilesize = new javax.swing.JLabel();
        fileSize = new javax.swing.JLabel();
        lblEcuVersion = new javax.swing.JLabel();
        ecuVersion = new javax.swing.JLabel();
        lblInternalId = new javax.swing.JLabel();
        internalID = new javax.swing.JLabel();
        lblStorageAddress = new javax.swing.JLabel();
        storageAddress = new javax.swing.JLabel();
        lblMake = new javax.swing.JLabel();
        lblMarket = new javax.swing.JLabel();
        lblTransmission = new javax.swing.JLabel();
        lblModel = new javax.swing.JLabel();
        lblSubmodel = new javax.swing.JLabel();
        lblYear = new javax.swing.JLabel();
        make = new javax.swing.JLabel();
        market = new javax.swing.JLabel();
        year = new javax.swing.JLabel();
        model = new javax.swing.JLabel();
        submodel = new javax.swing.JLabel();
        transmission = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tableList = new javax.swing.JList();
        lblTables = new javax.swing.JLabel();
        lblEditStamp = new javax.swing.JLabel();
        editStamp = new javax.swing.JLabel();

        lblEditStamp.setText("Edit Stamp:");
        
        editStamp.setText("stamp");
        
        lblFilename.setText("Filename:");

        fileName.setText("Filename");

        lblECURevision.setText("ECU Revision:");

        xmlID.setText("XMLID");

        lblFilesize.setText("Filesize:");

        fileSize.setText("999kb");

        lblEcuVersion.setText("ECU Version:");

        ecuVersion.setText("ECUVER");

        lblInternalId.setText("Internal ID:");

        internalID.setText("INTERNAL");

        lblStorageAddress.setText("ID Storage Address:");

        storageAddress.setText("0x00");

        lblMake.setText("Make:");

        lblMarket.setText("Market:");

        lblTransmission.setText("Transmission:");

        lblModel.setText("Model:");

        lblSubmodel.setText("Submodel:");

        lblYear.setText("Year:");

        make.setText("Make");

        market.setText("Market");

        year.setText("Year");

        model.setText("Model");

        submodel.setText("Submodel");

        transmission.setText("Transmission");

        tableList.setModel(new javax.swing.AbstractListModel() {
            /**
             *
             */
            private static final long serialVersionUID = -8498656966410761726L;
            String[] strings = {"Item 1", "Item 2", "Item 3", "Item 4", "Item 5"};

            public int getSize() {
                return strings.length;
            }

            public Object getElementAt(int i) {
                return strings[i];
            }
        });
        jScrollPane1.setViewportView(tableList);

        lblTables.setText("Tables:");

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addGroup(layout.createSequentialGroup()
                                                .addComponent(lblFilename)
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(fileName, GroupLayout.PREFERRED_SIZE, 302, GroupLayout.PREFERRED_SIZE))
                                        .addGroup(layout.createSequentialGroup()
                                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                        .addGroup(layout.createSequentialGroup()
                                                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                                        .addComponent(lblECURevision)
                                                                        .addComponent(lblEcuVersion)
                                                                        .addComponent(lblFilesize))
                                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                                        .addComponent(fileSize)
                                                                        .addComponent(ecuVersion)
                                                                        .addComponent(xmlID)))
                                                        .addGroup(layout.createSequentialGroup()
                                                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                                        .addComponent(lblYear)
                                                                        .addComponent(lblModel)
                                                                        .addComponent(lblSubmodel)
                                                                        .addComponent(lblTransmission)
                                                                        .addComponent(lblMarket)
                                                                        .addComponent(lblMake))
                                                                .addGap(7, 7, 7)
                                                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                                        .addComponent(make)
                                                                        .addComponent(market)
                                                                        .addComponent(year)
                                                                        .addGroup(layout.createSequentialGroup()
                                                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                                                        .addComponent(transmission)
                                                                                        .addComponent(submodel)))
                                                                        .addComponent(model))))
                                                .addGap(32, 32, 32)
                                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                        .addGroup(layout.createSequentialGroup()
                                                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                                        .addComponent(lblInternalId)
                                                                        .addComponent(lblStorageAddress)
                                                                        .addComponent(lblEditStamp))
                                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 53, Short.MAX_VALUE)
                                                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                                        .addComponent(internalID)
                                                                        .addComponent(storageAddress)
                                                                        .addComponent(editStamp))
                                                                .addGap(36, 36, 36))
                                                        .addComponent(lblTables)
                                                        .addComponent(jScrollPane1, GroupLayout.PREFERRED_SIZE, 226, GroupLayout.PREFERRED_SIZE))))
                                .addContainerGap())
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addGap(21, 21, 21)
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                                .addComponent(lblFilename)
                                                .addComponent(fileName))
                                        .addGroup(layout.createSequentialGroup()
                                                .addGap(40, 40, 40)
                                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                                        .addComponent(lblECURevision)
                                                        .addComponent(xmlID)
                                                        .addComponent(lblInternalId)
                                                        .addComponent(internalID))
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                                        .addComponent(ecuVersion)
                                                        .addComponent(lblEcuVersion)
                                                        .addComponent(storageAddress)
                                                        .addComponent(lblStorageAddress))
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                                        .addComponent(lblFilesize)
                                                        .addComponent(fileSize)
                                                        .addComponent(lblEditStamp)
                                                        .addComponent(editStamp))))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(lblTables)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addGroup(layout.createSequentialGroup()
                                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                                        .addComponent(lblMake)
                                                        .addComponent(make))
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                                        .addComponent(lblMarket)
                                                        .addComponent(market))
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                                        .addComponent(lblYear)
                                                        .addComponent(year))
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                                        .addComponent(lblModel)
                                                        .addComponent(model))
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                                        .addComponent(lblSubmodel)
                                                        .addComponent(submodel))
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                                        .addComponent(lblTransmission)
                                                        .addComponent(transmission)))
                                        .addComponent(jScrollPane1, 0, 0, Short.MAX_VALUE))
                                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel ecuVersion;
    private javax.swing.JLabel fileName;
    private javax.swing.JLabel fileSize;
    private javax.swing.JLabel internalID;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblECURevision;
    private javax.swing.JLabel lblEcuVersion;
    private javax.swing.JLabel lblFilename;
    private javax.swing.JLabel lblFilesize;
    private javax.swing.JLabel lblInternalId;
    private javax.swing.JLabel lblMake;
    private javax.swing.JLabel lblMarket;
    private javax.swing.JLabel lblModel;
    private javax.swing.JLabel lblStorageAddress;
    private javax.swing.JLabel lblSubmodel;
    private javax.swing.JLabel lblTables;
    private javax.swing.JLabel lblTransmission;
    private javax.swing.JLabel lblYear;
    private javax.swing.JLabel make;
    private javax.swing.JLabel market;
    private javax.swing.JLabel model;
    private javax.swing.JLabel storageAddress;
    private javax.swing.JLabel submodel;
    private javax.swing.JList tableList;
    private javax.swing.JLabel transmission;
    private javax.swing.JLabel xmlID;
    private javax.swing.JLabel year;
    private javax.swing.JLabel lblEditStamp;
    private javax.swing.JLabel editStamp;
    // End of variables declaration//GEN-END:variables

}