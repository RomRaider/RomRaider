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
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSeparator;

import com.romraider.maps.Rom;
import com.romraider.maps.Table;

public class TableMenuBar extends JMenuBar implements ActionListener {

    private static final long serialVersionUID = -695692646459410510L;
    private final Table table;
    private JMenu fileMenu;
    private JMenuItem graph;
    //private JRadioButtonMenuItem overlay = new JRadioButtonMenuItem("Overlay Log");

    private JMenu compareMenu;
    private JRadioButtonMenuItem compareOriginal;
    private JRadioButtonMenuItem compareMap;
    private JMenu similarOpenTables;
    private JRadioButtonMenuItem compareOff;
    private JMenu compareDisplay;
    private JRadioButtonMenuItem comparePercent;
    private JRadioButtonMenuItem compareAbsolute;
    private JMenu compareToValue;
    private JRadioButtonMenuItem compareToOriginal;
    private JRadioButtonMenuItem compareToBin;

    private JMenuItem close;
    private JMenu editMenu;
    private JMenuItem undoSel;
    private JMenuItem undoAll;
    private JMenuItem revert;
    private JMenuItem copySel;
    private JMenuItem copyTable;
    private JMenuItem paste;
    private JMenu viewMenu;
    private JMenuItem tableProperties;

    private ButtonGroup compareGroup;
    private ButtonGroup compareDisplayGroup;
    private ButtonGroup compareToGroup;

    public TableMenuBar(Table table) {
        this.table = table;
        initTableMenuBar();
    }

    public void initTableMenuBar() {
        initFileMenu();
        initEditMenu();
        initViewMenu();
    }

    public void refreshTableMenuBar() {
        refreshSimilarOpenTables();
        initCompareGroup();
    }

    private void initFileMenu() {
        fileMenu = new JMenu("Table");
        graph = new JMenuItem("View Graph");
        compareMenu = new JMenu("Compare");
        close = new JMenuItem("Close Table");

        initCompareMenu();
        close.setText("Close " + table.getName());

        graph.addActionListener(this);
        close.addActionListener(this);

        graph.setMnemonic('G');
        close.setMnemonic('X');
        graph.setEnabled(false);

        fileMenu.add(graph);
        fileMenu.add(compareMenu);
        fileMenu.add(new JSeparator());
        fileMenu.add(close);

        this.add(fileMenu);
    }

    private void initEditMenu() {
        editMenu = new JMenu("Edit");
        undoSel = new JMenuItem("Undo Selected Changes");
        undoAll = new JMenuItem("Undo All Changes");
        revert = new JMenuItem("Set Revert Point");
        copySel = new JMenuItem("Copy Selection");
        copyTable = new JMenuItem("Copy Table");
        paste = new JMenuItem("Paste");

        editMenu.add(undoSel);
        editMenu.add(undoAll);
        editMenu.add(revert);
        editMenu.add(new JSeparator());
        editMenu.add(copySel);
        editMenu.add(copyTable);
        editMenu.add(new JSeparator());
        editMenu.add(paste);

        editMenu.setMnemonic('E');
        undoSel.setMnemonic('U');
        undoAll.setMnemonic('A');
        revert.setMnemonic('R');
        copySel.setMnemonic('C');
        copyTable.setMnemonic('T');
        paste.setMnemonic('P');

        undoSel.addActionListener(this);
        undoAll.addActionListener(this);
        revert.addActionListener(this);
        copySel.addActionListener(this);
        copyTable.addActionListener(this);
        paste.addActionListener(this);
        this.add(editMenu);
    }

    private void initViewMenu() {
        tableProperties = new JMenuItem("Table Properties");

        viewMenu = new JMenu("View");
        viewMenu.add(tableProperties);
        viewMenu.setMnemonic('V');
        tableProperties.setMnemonic('P');
        tableProperties.addActionListener(this);

        fileMenu.setMnemonic('F');
        fileMenu.setMnemonic('T');
        this.add(viewMenu);
    }

    private void initCompareMenu() {
        compareOriginal = new JRadioButtonMenuItem("Show Changes");
        compareOriginal.setToolTipText("Compares the current values to the original or revert point values.");
        compareMap = new JRadioButtonMenuItem("Compare to Another Map");
        compareMap.setToolTipText("Compares this table and a selected table.");
        similarOpenTables = new JMenu("Compare to Table");
        similarOpenTables.setToolTipText("Compares this table to a similar table.");

        compareOff = new JRadioButtonMenuItem("Off");

        comparePercent = new JRadioButtonMenuItem("Percent Difference");
        compareAbsolute = new JRadioButtonMenuItem("Absolute Difference");
        compareDisplayGroup = new ButtonGroup();
        compareDisplayGroup.add(comparePercent);
        compareDisplayGroup.add(compareAbsolute);
        compareDisplay = new JMenu("Display");
        compareDisplay.add(comparePercent);
        compareDisplay.add(compareAbsolute);

        compareToOriginal = new JRadioButtonMenuItem("Compre to Original Value");
        compareToOriginal.setToolTipText("Compares this table to the selected table's original or revert point values.");
        compareToBin = new JRadioButtonMenuItem("Compare to Bin Value");
        compareToBin.setToolTipText("Compares this table to the selected table's current values.");
        compareToGroup = new ButtonGroup();
        compareToGroup.add(compareToOriginal);
        compareToGroup.add(compareToBin);
        compareToValue = new JMenu("Compare to");
        compareToValue.add(compareToOriginal);
        compareToValue.add(compareToBin);

        compareMenu.add(compareOriginal);
        compareMenu.add(compareMap);
        compareMenu.add(similarOpenTables);
        compareMenu.add(compareOff);
        compareMenu.add(new JSeparator());
        compareMenu.add(compareDisplay);
        compareMenu.add(new JSeparator());
        compareMenu.add(compareToValue);

        compareMenu.setMnemonic('C');
        compareOriginal.setMnemonic('C');
        compareMap.setMnemonic('M');
        compareOff.setMnemonic('O');
        compareDisplay.setMnemonic('D');
        comparePercent.setMnemonic('P');
        compareAbsolute.setMnemonic('A');
        similarOpenTables.setMnemonic('S');
        compareToValue.setMnemonic('T');
        compareToOriginal.setMnemonic('R');
        compareToOriginal.setMnemonic('B');

        compareOff.setSelected(true);
        compareAbsolute.setSelected(true);
        compareToOriginal.setSelected(true);

        initCompareGroup();

        compareOriginal.addActionListener(this);
        compareMap.addActionListener(this);
        compareOff.addActionListener(this);
        comparePercent.addActionListener(this);
        compareAbsolute.addActionListener(this);
        compareToOriginal.addActionListener(this);
        compareToBin.addActionListener(this);
    }

    private void initCompareGroup() {
        compareGroup = new ButtonGroup();

        compareGroup.add(compareOriginal);
        compareGroup.add(compareMap);
        compareGroup.add(compareOff);

        for(int i = 0; i< similarOpenTables.getItemCount(); i++) {
            compareGroup.add(similarOpenTables.getItem(i));
        }
    }

    private void refreshSimilarOpenTables() {
        similarOpenTables.removeAll();
        String currentTableName = table.getName();
        Vector<Rom> roms = table.getEditor().getImages();

        for(Rom rom : roms) {
            Vector<Table> tables = rom.getTables();
            for(Table table : tables) {
                if(table.getName().equalsIgnoreCase(currentTableName)) {
                    JRadioButtonMenuItem similarTable = new TableMenuItem(table);
                    similarTable.addActionListener(this);
                    similarOpenTables.add(similarTable);
                }
            }
        }
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
            compareByDisplay(Table.COMPARE_DISPLAY_OFF);

        } else if (e.getSource() == compareAbsolute) {
            compareByDisplay(Table.COMPARE_DISPLAY_ABSOLUTE);

        } else if (e.getSource() == comparePercent) {
            compareByDisplay(Table.COMPARE_DISPLAY_PERCENT);

        } else if (e.getSource() == compareOriginal) {
            table.setCompareType(Table.COMPARE_TYPE_ORIGINAL);
            compareToOriginal.setSelected(true);
            compareByTable(table);

        } else if (e.getSource() == compareMap) {
            JTableChooser chooser = new JTableChooser();
            Table selectedTable = chooser.showChooser(table);
            if(null != selectedTable) {
                compareByTable(selectedTable);
            }

        } else if (e.getSource() instanceof TableMenuItem) {
            Table selectedTable = ((TableMenuItem) e.getSource()).getTable();
            compareByTable(selectedTable);

        } else if (e.getSource() == compareToOriginal) {
            compareByType(Table.COMPARE_TYPE_ORIGINAL);

        } else if (e.getSource() == compareToBin) {
            compareByType(Table.COMPARE_TYPE_BIN);

        }
    }

    private void compareByType(int compareType) {
        table.setCompareType(compareType);
        if(table.fillCompareValues()) {
            table.refreshCellDisplay();
        }
    }

    private void compareByTable(Table selectedTable) {
        if(null == selectedTable) {
            return;
        }

        if(table.getCompareDisplay() == Table.COMPARE_DISPLAY_OFF) {
            // Default to absolute if none selected.
            this.compareAbsolute.setSelected(true);
            table.setCompareDisplay(Table.COMPARE_DISPLAY_ABSOLUTE);
        }

        selectedTable.addComparedToTable(table);

        table.setCompareTable(selectedTable);
        if(table.fillCompareValues()) {
            table.refreshCellDisplay();
        }
    }

    public void compareByDisplay(int compareDisplay) {
        table.setCompareDisplay(compareDisplay);
        table.refreshCellDisplay();
    }
}