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

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Vector;

import javax.swing.*;

import com.romraider.editor.ecu.ECUEditorManager;
import com.romraider.util.SettingsManager;

public class DefinitionManager extends javax.swing.JFrame implements ActionListener {

    private static final long serialVersionUID = -3920843496218196737L;
    public static int MOVE_UP = 0;
    public static int MOVE_DOWN = 1;

    Vector<String> fileNames;

    public DefinitionManager() {
        this.setIconImage(ECUEditorManager.getECUEditor().getIconImage());
        initComponents();
        initSettings();

        definitionList.setFont(new Font("Tahoma", Font.PLAIN, 11));
        definitionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        btnCancel.addActionListener(this);
        btnSave.addActionListener(this);
        btnAddDefinition.addActionListener(this);
        btnRemoveDefinition.addActionListener(this);
        btnMoveUp.addActionListener(this);
        btnMoveDown.addActionListener(this);
        btnApply.addActionListener(this);
        btnUndo.addActionListener(this);

    }

    private void initSettings() {
        // add definitions to list
        Vector<File> definitionFiles = SettingsManager.getSettings().getEcuDefinitionFiles();
        fileNames = new Vector<String>();

        for (int i = 0; i < definitionFiles.size(); i++) {
            fileNames.add(definitionFiles.get(i).getAbsolutePath());
        }

        updateListModel();
    }

    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        jScrollPane1 = new javax.swing.JScrollPane();
        definitionList = new javax.swing.JList();
        defLabel = new javax.swing.JLabel();
        btnMoveUp = new javax.swing.JButton();
        btnMoveDown = new javax.swing.JButton();
        btnAddDefinition = new javax.swing.JButton();
        btnRemoveDefinition = new javax.swing.JButton();
        btnSave = new javax.swing.JButton();
        btnCancel = new javax.swing.JButton();
        btnApply = new javax.swing.JButton();
        btnUndo = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Definition File Manager");
        jScrollPane1.setViewportView(definitionList);

        defLabel.setText("ECU Definition File Priority");

        btnMoveUp.setText("Move Up");

        btnMoveDown.setText("Move Down");

        btnAddDefinition.setText("Add...");

        btnRemoveDefinition.setText("Remove");

        btnSave.setText("Save");

        btnCancel.setText("Cancel");

        btnApply.setText("Apply");

        btnUndo.setText("Undo");

        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                .addComponent(jScrollPane1, GroupLayout.DEFAULT_SIZE, 448, Short.MAX_VALUE)
                                .addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                                                .addGroup(layout.createSequentialGroup()
                                                        .addComponent(btnSave)
                                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                        .addComponent(btnApply)
                                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                        .addComponent(btnUndo)
                                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                        .addComponent(btnCancel))
                                                .addGroup(layout.createSequentialGroup()
                                                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                                .addComponent(defLabel)
                                                                .addGroup(layout.createSequentialGroup()
                                                                        .addComponent(btnMoveDown)
                                                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                                        .addComponent(btnMoveUp)))
                                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 80, Short.MAX_VALUE)
                                                        .addComponent(btnAddDefinition)))
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(btnRemoveDefinition)))
                        .addContainerGap())
                );

        layout.linkSize(SwingConstants.HORIZONTAL, new Component[]{btnAddDefinition, btnMoveDown, btnMoveUp, btnRemoveDefinition});

        layout.setVerticalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(defLabel)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jScrollPane1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(btnMoveUp)
                                        .addComponent(btnMoveDown)
                                        .addComponent(btnRemoveDefinition, GroupLayout.PREFERRED_SIZE, 23, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(btnAddDefinition))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(btnSave)
                                        .addComponent(btnApply)
                                        .addComponent(btnUndo)
                                        .addComponent(btnCancel))
                                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                );
        pack();
    }// </editor-fold>//GEN-END:initComponents

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnCancel) {
            dispose();

        } else if (e.getSource() == btnSave) {
            saveSettings();
            dispose();

        } else if (e.getSource() == btnApply) {
            saveSettings();

        } else if (e.getSource() == btnMoveUp) {
            moveSelection(MOVE_UP);

        } else if (e.getSource() == btnMoveDown) {
            moveSelection(MOVE_DOWN);

        } else if (e.getSource() == btnAddDefinition) {
            addFile();

        } else if (e.getSource() == btnRemoveDefinition) {
            removeSelection();

        } else if (e.getSource() == btnUndo) {
            initSettings();

        }

    }

    public void saveSettings() {
        Vector<File> output = new Vector<File>();

        // create file vector
        for (int i = 0; i < fileNames.size(); i++) {
            output.add(new File(fileNames.get(i)));
        }

        // save
        SettingsManager.getSettings().setEcuDefinitionFiles(output);
    }

    public void addFile() {
        JFileChooser fc = new JFileChooser("./");
        fc.setFileFilter(new XMLFilter());

        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            fileNames.add(fc.getSelectedFile().getAbsolutePath());
            updateListModel();
        }
    }

    public void moveSelection(int direction) {
        int selectedIndex = definitionList.getSelectedIndex();
        String fileName = fileNames.get(selectedIndex);

        if (direction == MOVE_UP && selectedIndex > 0) {
            fileNames.remove(selectedIndex);
            fileNames.add(--selectedIndex, fileName);

        } else if (direction == MOVE_DOWN && selectedIndex < definitionList.getModel().getSize()) {
            fileNames.remove(selectedIndex);
            fileNames.add(++selectedIndex, fileName);

        }
        updateListModel();
        definitionList.setSelectedIndex(selectedIndex);
    }

    public void removeSelection() {
        int index = definitionList.getSelectedIndex();
        if (index < 0) return;
        fileNames.remove(index);
        updateListModel();

    }

    public void updateListModel() {
        definitionList.setListData(fileNames);
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAddDefinition;
    private javax.swing.JButton btnApply;
    private javax.swing.JButton btnCancel;
    private javax.swing.JButton btnMoveDown;
    private javax.swing.JButton btnMoveUp;
    private javax.swing.JButton btnRemoveDefinition;
    private javax.swing.JButton btnSave;
    private javax.swing.JButton btnUndo;
    private javax.swing.JLabel defLabel;
    private javax.swing.JList definitionList;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables

}
