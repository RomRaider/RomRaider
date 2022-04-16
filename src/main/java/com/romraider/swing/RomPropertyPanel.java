/*
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2022 RomRaider.com
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

import java.util.ResourceBundle;
import com.romraider.maps.Rom;
import com.romraider.maps.RomID;
import com.romraider.util.ResourceUtil;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;

public class RomPropertyPanel extends JPanel {

    private static final long serialVersionUID = 5583360728106071942L;
    private static final ResourceBundle rb = new ResourceUtil().getBundle(
            RomPropertyPanel.class.getName());
    Rom rom;

    public RomPropertyPanel(Rom rom) {
        initComponents();
        RomID romID = rom.getRomID();

        // populate fields
        fileName.setText(rom.getFileName());
        xmlID.setText(romID.getXmlid());
        ecuVersion.setText(romID.getCaseId());
        if (rom.getRealFileSize() > 1024) {
            fileSize.setText((rom.getRealFileSize() / 1024) + "kB");
        }
        else {
            fileSize.setText(rom.getRealFileSize() + "B");
        }
        internalID.setText(romID.getInternalIdString());
        storageAddress.setText("0x" + Integer.toHexString(romID.getInternalIdAddress()));

        make.setText(romID.getMake());
        market.setText(romID.getMarket());
        year.setText(romID.getYear());
        model.setText(romID.getModel());
        submodel.setText(romID.getSubModel());
        transmission.setText(romID.getTransmission());
        editStamp.setText(romID.getEditStamp());
        checksum.setText(romID.getChecksum());
        version.setText(romID.getVersion());
        author.setText(romID.getAuthor());
        lblTables.setText(String.format(rb.getString("LBLTBLS"), rom.getTables().size()));
        tableList.setListData(rom.getTables());
    }

    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        lblFilename = new JLabel();
        fileName = new JLabel();
        lblECURevision = new JLabel();
        xmlID = new JLabel();
        lblFilesize = new JLabel();
        fileSize = new JLabel();
        lblEcuVersion = new JLabel();
        ecuVersion = new JLabel();
        lblInternalId = new JLabel();
        internalID = new JLabel();
        lblStorageAddress = new JLabel();
        storageAddress = new JLabel();
        lblMake = new JLabel();
        lblMarket = new JLabel();
        lblTransmission = new JLabel();
        lblModel = new JLabel();
        lblSubmodel = new JLabel();
        lblYear = new JLabel();
        lblVersion = new JLabel();
        lblAuthor = new JLabel();
        version = new JLabel();
        author = new JLabel();
        make = new JLabel();
        market = new JLabel();
        year = new JLabel();
        model = new JLabel();
        submodel = new JLabel();
        transmission = new JLabel();
        jScrollPane1 = new JScrollPane();
        tableList = new JList();
        lblTables = new JLabel();
        lblEditStamp = new JLabel();
        editStamp = new JLabel();
        lblChecksum = new JLabel();
        checksum = new JLabel();

        lblChecksum.setText(rb.getString("LBLCHKSUM"));
        lblEditStamp.setText(rb.getString("LBLEDIT"));
        lblFilename.setText(rb.getString("LBLFN"));
        lblECURevision.setText(rb.getString("LBLECU"));
        lblFilesize.setText(rb.getString("LBLFS"));
        lblEcuVersion.setText(rb.getString("LBLVER"));
        lblInternalId.setText(rb.getString("LBLID"));
        lblStorageAddress.setText(rb.getString("LBLADDR"));
        lblMake.setText(rb.getString("LBLMAKE"));
        lblMarket.setText(rb.getString("LBLMRKT"));
        lblTransmission.setText(rb.getString("LBLTRANS"));
        lblModel.setText(rb.getString("LBLMDL"));
        lblSubmodel.setText(rb.getString("LBLSMDL"));
        lblYear.setText(rb.getString("LBLYR"));
        lblAuthor.setText(rb.getString("LBLAUT"));
        lblVersion.setText(rb.getString("LBLDEFVER"));
        jScrollPane1.setViewportView(tableList);

        GroupLayout layout = new GroupLayout(this);
        layout.setHorizontalGroup(
            layout.createParallelGroup(Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(layout.createParallelGroup(Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                            .addComponent(lblFilename)
                            .addPreferredGap(ComponentPlacement.RELATED)
                            .addComponent(fileName, GroupLayout.PREFERRED_SIZE, 302, GroupLayout.PREFERRED_SIZE))
                        .addGroup(layout.createSequentialGroup()
                                .addComponent(lblAuthor)
                                .addPreferredGap(ComponentPlacement.RELATED)
                                .addComponent(author, GroupLayout.PREFERRED_SIZE, 302, GroupLayout.PREFERRED_SIZE))
                        .addGroup(layout.createSequentialGroup()
                                .addComponent(lblVersion)
                                .addPreferredGap(ComponentPlacement.RELATED)
                                .addComponent(version, GroupLayout.PREFERRED_SIZE, 302, GroupLayout.PREFERRED_SIZE))
                        .addGroup(layout.createSequentialGroup()
                            .addGroup(layout.createParallelGroup(Alignment.LEADING)
                                .addComponent(lblECURevision)
                                .addComponent(lblEcuVersion)
                                .addComponent(lblFilesize)
                                .addComponent(lblChecksum)
                                .addComponent(lblMake)
                                .addComponent(lblMarket)
                                .addComponent(lblYear)
                                .addComponent(lblModel)
                                .addComponent(lblSubmodel)
                                .addComponent(lblTransmission))
                            .addPreferredGap(ComponentPlacement.RELATED)
                            .addGroup(layout.createParallelGroup(Alignment.LEADING)
                                .addComponent(transmission)
                                .addComponent(submodel)
                                .addComponent(model)
                                .addComponent(year)
                                .addComponent(market)
                                .addComponent(make)
                                .addComponent(checksum, GroupLayout.PREFERRED_SIZE, 70, GroupLayout.PREFERRED_SIZE)
                                .addComponent(fileSize)
                                .addComponent(ecuVersion)
                                .addComponent(xmlID))
                            .addGap(20)
                            .addGroup(layout.createParallelGroup(Alignment.LEADING)
                                .addComponent(lblTables)
                                .addComponent(lblEditStamp)
                                .addComponent(lblStorageAddress)
                                .addComponent(lblInternalId))
                            .addGap(18)
                            .addGroup(layout.createParallelGroup(Alignment.LEADING)
                                .addComponent(editStamp)
                                .addComponent(storageAddress)
                                .addComponent(internalID)))
                        .addGroup(layout.createSequentialGroup()
                            .addGap(163)
                            .addComponent(jScrollPane1, GroupLayout.PREFERRED_SIZE, 226, GroupLayout.PREFERRED_SIZE)))
                    .addContainerGap(51, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(Alignment.TRAILING)
                .addGroup(layout.createSequentialGroup()
                    .addGap(21)
                    .addGroup(layout.createParallelGroup(Alignment.LEADING)
                        .addComponent(lblFilename)
                        .addComponent(fileName))
                    .addPreferredGap(ComponentPlacement.RELATED)
                    .addGroup(layout.createParallelGroup(Alignment.LEADING)
                        .addComponent(lblAuthor)
                        .addComponent(author))
                    .addPreferredGap(ComponentPlacement.RELATED)
                    .addGroup(layout.createParallelGroup(Alignment.LEADING)
                        .addComponent(lblVersion)
                        .addComponent(version))
                    .addGap(26)
                    .addGroup(layout.createParallelGroup(Alignment.LEADING)
                        .addComponent(lblECURevision)
                        .addComponent(xmlID)
                        .addComponent(lblInternalId)
                        .addComponent(internalID))
                    .addPreferredGap(ComponentPlacement.RELATED)
                    .addGroup(layout.createParallelGroup(Alignment.LEADING)
                        .addComponent(lblEcuVersion)
                        .addComponent(ecuVersion)
                        .addComponent(lblStorageAddress)
                        .addComponent(storageAddress))
                    .addPreferredGap(ComponentPlacement.RELATED)
                    .addGroup(layout.createParallelGroup(Alignment.LEADING)
                        .addComponent(lblFilesize)
                        .addComponent(fileSize)
                        .addComponent(lblEditStamp)
                        .addComponent(editStamp))
                    .addPreferredGap(ComponentPlacement.RELATED)
                    .addGroup(layout.createParallelGroup(Alignment.LEADING)
                        .addComponent(lblChecksum)
                        .addComponent(checksum)
                        .addComponent(lblTables))
                    .addPreferredGap(ComponentPlacement.RELATED)
                    .addGroup(layout.createParallelGroup(Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                            .addGroup(layout.createParallelGroup(Alignment.LEADING)
                                .addComponent(lblMake)
                                .addComponent(make))
                            .addPreferredGap(ComponentPlacement.RELATED)
                            .addGroup(layout.createParallelGroup(Alignment.LEADING)
                                .addComponent(lblMarket)
                                .addComponent(market))
                            .addPreferredGap(ComponentPlacement.RELATED)
                            .addGroup(layout.createParallelGroup(Alignment.LEADING)
                                .addComponent(lblYear)
                                .addComponent(year))
                            .addPreferredGap(ComponentPlacement.RELATED)
                            .addGroup(layout.createParallelGroup(Alignment.LEADING)
                                .addComponent(lblModel)
                                .addComponent(model))
                            .addPreferredGap(ComponentPlacement.RELATED)
                            .addGroup(layout.createParallelGroup(Alignment.LEADING)
                                .addComponent(lblSubmodel)
                                .addComponent(submodel))
                            .addPreferredGap(ComponentPlacement.RELATED)
                            .addGroup(layout.createParallelGroup(Alignment.LEADING)
                                .addComponent(lblTransmission)
                                .addComponent(transmission)))
                        .addComponent(jScrollPane1, 0, 0, Short.MAX_VALUE))
                    .addContainerGap())
        );
        this.setLayout(layout);
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JLabel ecuVersion;
    private JLabel fileName;
    private JLabel fileSize;
    private JLabel internalID;
    private JScrollPane jScrollPane1;
    private JLabel lblECURevision;
    private JLabel lblEcuVersion;
    private JLabel lblFilename;
    private JLabel lblFilesize;
    private JLabel lblInternalId;
    private JLabel lblMake;
    private JLabel lblMarket;
    private JLabel lblModel;
    private JLabel lblStorageAddress;
    private JLabel lblSubmodel;
    private JLabel lblTables;
    private JLabel lblTransmission;
    private JLabel lblYear;
    private JLabel make;
    private JLabel market;
    private JLabel model;
    private JLabel storageAddress;
    private JLabel submodel;
    private JList tableList;
    private JLabel transmission;
    private JLabel xmlID;
    private JLabel year;
    private JLabel lblEditStamp;
    private JLabel editStamp;
    private JLabel lblChecksum;
    private JLabel checksum;
    private JLabel lblVersion;
    private JLabel version;
    private JLabel author;
    private JLabel lblAuthor;
    // End of variables declaration//GEN-END:variables

}