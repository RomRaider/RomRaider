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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSeparator;

import com.romraider.maps.Table;

public class TableMenuBar extends JMenuBar implements ActionListener {

    private static final long serialVersionUID = -695692646459410510L;
    private final Table table;
    private final JMenu fileMenu = new JMenu("Table");
    private final JMenuItem graph = new JMenuItem("View Graph");
    //private JRadioButtonMenuItem overlay = new JRadioButtonMenuItem("Overlay Log");

    private final JMenu compareMenu = new JMenu("Compare");
    private final JRadioButtonMenuItem compareOriginal = new JRadioButtonMenuItem("Show Changes");
    private final JRadioButtonMenuItem compareMap = new JRadioButtonMenuItem("Compare to Another Map");
    private final JRadioButtonMenuItem compareOff = new JRadioButtonMenuItem("Off");
    private final JMenu compareDisplay = new JMenu("Display");
    private final JRadioButtonMenuItem comparePercent = new JRadioButtonMenuItem("Percent Difference");
    private final JRadioButtonMenuItem compareAbsolute = new JRadioButtonMenuItem("Absolute Difference");

    private final JMenuItem close = new JMenuItem("Close Table");
    private final JMenu editMenu = new JMenu("Edit");
    private final JMenuItem undoSel = new JMenuItem("Undo Selected Changes");
    private final JMenuItem undoAll = new JMenuItem("Undo All Changes");
    private final JMenuItem revert = new JMenuItem("Set Revert Point");
    private final JMenuItem copySel = new JMenuItem("Copy Selection");
    private final JMenuItem copyTable = new JMenuItem("Copy Table");
    private final JMenuItem paste = new JMenuItem("Paste");
    private final JMenu viewMenu = new JMenu("View");
    private final JMenuItem tableProperties = new JMenuItem("Table Properties");

    private final ButtonGroup compareGroup = new ButtonGroup();
    private final ButtonGroup compareDisplayGroup = new ButtonGroup();

    public TableMenuBar(Table table) {
        this.table = table;
        this.add(fileMenu);
        fileMenu.add(graph);
        fileMenu.add(compareMenu);
        compareMenu.add(compareOriginal);
        compareMenu.add(compareMap);
        compareMenu.add(compareOff);
        compareMenu.add(new JSeparator());
        compareMenu.add(compareDisplay);
        compareDisplay.add(comparePercent);
        compareDisplay.add(compareAbsolute);
        fileMenu.add(new JSeparator());
        fileMenu.add(close);
        close.setText("Close " + table.getName());

        compareMenu.setMnemonic('C');
        compareOriginal.setMnemonic('C');
        compareMap.setMnemonic('M');
        compareOff.setMnemonic('O');
        compareOff.setSelected(true);
        compareDisplay.setMnemonic('D');
        comparePercent.setMnemonic('P');
        compareAbsolute.setMnemonic('A');
        compareAbsolute.setSelected(true);

        compareGroup.add(compareOriginal);
        compareGroup.add(compareMap);
        compareGroup.add(compareOff);

        compareDisplayGroup.add(comparePercent);
        compareDisplayGroup.add(compareAbsolute);

        compareOriginal.addActionListener(this);
        compareMap.addActionListener(this);
        compareOff.addActionListener(this);
        comparePercent.addActionListener(this);
        compareAbsolute.addActionListener(this);

        this.add(editMenu);
        editMenu.add(undoSel);
        editMenu.add(undoAll);
        editMenu.add(revert);
        editMenu.add(new JSeparator());
        editMenu.add(copySel);
        editMenu.add(copyTable);
        editMenu.add(new JSeparator());
        editMenu.add(paste);
        editMenu.setMnemonic('E');
        copySel.setMnemonic('C');
        copyTable.setMnemonic('T');
        paste.setMnemonic('P');
        copySel.addActionListener(this);
        copyTable.addActionListener(this);
        paste.addActionListener(this);

        this.add(viewMenu);
        viewMenu.add(tableProperties);
        viewMenu.setMnemonic('V');
        tableProperties.setMnemonic('P');
        tableProperties.addActionListener(this);

        graph.addActionListener(this);
        undoSel.addActionListener(this);
        undoAll.addActionListener(this);
        revert.addActionListener(this);
        close.addActionListener(this);

        fileMenu.setMnemonic('F');
        fileMenu.setMnemonic('T');
        graph.setMnemonic('G');
        undoSel.setMnemonic('U');
        undoAll.setMnemonic('A');
        revert.setMnemonic('R');
        close.setMnemonic('X');

        graph.setEnabled(false);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == undoAll) {
            table.undoAll();

        } else if (e.getSource() == revert) {
            table.setRevertPoint();

        } else if (e.getSource() == undoSel) {
            table.undoSelected();

        } else if (e.getSource() == close) {
            table.getEditor().removeDisplayTable(table.getFrame());

        } else if (e.getSource() == tableProperties) {
            JOptionPane.showMessageDialog(table, new TablePropertyPanel(table),
                    table.getName() + " Table Properties", JOptionPane.INFORMATION_MESSAGE);

        } else if (e.getSource() == copySel) {
            table.copySelection();

        } else if (e.getSource() == copyTable) {
            table.copyTable();

        } else if (e.getSource() == paste) {
            table.paste();

        } else if (e.getSource() == compareOff) {
            table.compare(Table.COMPARE_OFF);

        } else if (e.getSource() == compareOriginal) {
            table.compare(Table.COMPARE_ORIGINAL);

        } else if (e.getSource() == compareMap) {
            JTableChooser chooser = new JTableChooser();
            Table selectedTable = chooser.showChooser(table);

            if(null != selectedTable) {
                table.compare(selectedTable, Table.COMPARE_TABLE);
            }
        } else if (e.getSource() == compareAbsolute) {
            table.setCompareDisplay(Table.COMPARE_ABSOLUTE);

        } else if (e.getSource() == comparePercent) {
            table.setCompareDisplay(Table.COMPARE_PERCENT);

        }
    }
}