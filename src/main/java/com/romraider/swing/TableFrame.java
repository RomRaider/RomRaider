/*
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2020 RomRaider.com
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

import static javax.swing.BorderFactory.createBevelBorder;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.MessageFormat;
import java.util.ResourceBundle;

import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JOptionPane;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

import com.romraider.Settings;
import com.romraider.editor.ecu.ECUEditor;
import com.romraider.editor.ecu.ECUEditorManager;
import com.romraider.logger.ecu.ui.handler.table.TableUpdateHandler;
import com.romraider.maps.Rom;
import com.romraider.maps.Table;
import com.romraider.maps.TableView;
import com.romraider.maps.UserLevelException;
import com.romraider.util.ResourceUtil;

public class TableFrame extends JInternalFrame implements InternalFrameListener, ActionListener {

    private static final long serialVersionUID = -2651279694660392351L;
    private static final ResourceBundle rb = new ResourceUtil().getBundle(
            TableFrame.class.getName());
    private final TableView tableView;
    private TableMenuBar tableMenuBar = null;

    public TableFrame(String title, TableView tableView) {
        super(title, true, true);
        this.tableView = tableView;
        Table t = tableView.getTable();
        t.setTableFrame(this);
        
        add(tableView);
        tableView.repaint();
        
        setFrameIcon(null);
        setBorder(createBevelBorder(0));
        if (System.getProperty("os.name").startsWith("Mac OS"))
            putClientProperty("JInternalFrame.isPalette", true);
        setVisible(false);
        tableMenuBar = new TableMenuBar(this);
        setJMenuBar(tableMenuBar);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        addInternalFrameListener(this);
    }

    @Override
    public void internalFrameActivated(InternalFrameEvent e) {
        ECUEditor parent = getEditor();
        parent.getTableToolBar().updateTableToolBar();
        parent.getToolBar().updateButtons();
        parent.getEditorMenuBar().updateMenu();
    }

    @Override
    public void internalFrameOpened(InternalFrameEvent e) {
        RegisterTable();
    }

    @Override
    public void internalFrameClosing(InternalFrameEvent e) {
        TableUpdateHandler.getInstance().deregisterTable(this.getTable());
    }

    @Override
    public void internalFrameClosed(InternalFrameEvent e) {
        getEditor().getTableToolBar().updateTableToolBar();
    }

    @Override
    public void internalFrameIconified(InternalFrameEvent e) {
        ;
    }

    @Override
    public void internalFrameDeiconified(InternalFrameEvent e) {
        ;
    }

    @Override
    public void internalFrameDeactivated(InternalFrameEvent e) {
        getEditor().getTableToolBar().updateTableToolBar();
    }

    public void RegisterTable() {
        TableUpdateHandler.getInstance().registerTable(this.getTable());
    }

    public Table getTable() {
        return tableView.getTable();
    }
    public TableView getTableView() {
        return tableView;
    }

    public ECUEditor getEditor() {
        return ECUEditorManager.getECUEditor();
    }

    public TableMenuBar getTableMenuBar() {
        return this.tableMenuBar;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        TableMenuBar menu = getTableMenuBar();
        Table t = getTable();
        
        try {
        if (e.getSource() == menu.getUndoAll()) {
            t.undoAll();

        } else if (e.getSource() == menu.getRevert()) {
            t.setRevertPoint();

        } else if (e.getSource() == menu.getUndoSel()) {
            getTableView().undoSelected();

        } else if (e.getSource() == menu.getClose()) {
            ECUEditorManager.getECUEditor().removeDisplayTable(this);

        } else if (e.getSource() == menu.getTableProperties()) {
            JOptionPane.showMessageDialog(getTableView(),
                    new TablePropertyPanel(t),
                    MessageFormat.format(
                            rb.getString("TBLPROP"), getTable().getName()),
                    JOptionPane.INFORMATION_MESSAGE);

        } else if (e.getSource() == menu.getCopySel()) {
            getTableView().copySelection();

        } else if (e.getSource() == menu.getCopyTable()) {
            getTableView().copyTable();

        } else if (e.getSource() == menu.getPaste()) {
            getTableView().paste();

        } else if (e.getSource() == menu.getCompareOff()) {
            t.setCompareTable(null);
            t.setCompareValueType(Settings.DataType.BIN);
            getTableMenuBar().getCompareToBin().setSelected(true);

        } else if (e.getSource() == menu.getCompareAbsolute()) {
            getTableView().setCompareDisplay(Settings.CompareDisplay.ABSOLUTE);

        } else if (e.getSource() == menu.getComparePercent()) {
            getTableView().setCompareDisplay(Settings.CompareDisplay.PERCENT);

        } else if (e.getSource() == menu.getCompareOriginal()) {
            t.setCompareValueType(Settings.DataType.ORIGINAL);
            getTableMenuBar().getCompareToOriginal().setSelected(true);
            compareByTable(t);

        } else if (e.getSource() == menu.getCompareMap()) {
            JTableChooser chooser = new JTableChooser();
            Table selectedTable = chooser.showChooser(t);
            if(null != selectedTable) {
                compareByTable(selectedTable);
            }

        } else if (e.getSource() instanceof TableMenuItem) {
            Table selectedTable = findSimilarTable((TableMenuItem)e.getSource());
            if(null != e.getSource()) {
                compareByTable(selectedTable);
            }

        } else if (e.getSource() == menu.getCompareToOriginal()) {
            t.setCompareValueType(Settings.DataType.ORIGINAL);
            t.refreshCompare();

        } else if (e.getSource() == menu.getCompareToBin()) {
            t.setCompareValueType(Settings.DataType.BIN);
            t.refreshCompare();

        } else if (e.getSource() == menu.getInterp()) {
            getTableView().interpolate();

        } else if (e.getSource() == menu.getVertInterp()) {
            getTableView().verticalInterpolate();

        } else if (e.getSource() == menu.getHorizInterp()) {
            getTableView().horizontalInterpolate();
        }
        }
        catch(UserLevelException ex) {
        	TableView.showInvalidUserLevelPopup(ex);
        }
    }

    public void compareByTable(Table selectedTable) {
    	Table t = getTable();
    	
        if(null == selectedTable) {
            return;
        }
        
        t.setCompareTable(selectedTable);
        ECUEditorManager.getECUEditor().getTableToolBar().updateTableToolBar(t);
        t.populateCompareValues(selectedTable);
    }

    public void refreshSimilarOpenTables() {
        JMenu similarTables =  getTableMenuBar().getSimilarOpenTables();
        similarTables.removeAll();

        for(Rom rom : ECUEditorManager.getECUEditor().getImages()) {
            for(TableTreeNode tableNode : rom.getTableNodes()) {
                if(tableNode.getTable().getName().equalsIgnoreCase(getTable().getName())) {
                    JRadioButtonMenuItem similarTable = new TableMenuItem(rom.getFileName());
                    similarTable.setToolTipText(tableNode.getTable().getName());
                    similarTable.addActionListener(this);
                    similarTables.add(similarTable);
                    break;
                }
            }
        }

        getTableMenuBar().initCompareGroup();
        getTableMenuBar().repaint();
    }

    private Table findSimilarTable(TableMenuItem menuItem) {
        for(Rom rom : ECUEditorManager.getECUEditor().getImages()) {
            if(menuItem.getText().equalsIgnoreCase(rom.getFileName())) {
                for(TableTreeNode treeNode : rom.getTableNodes()) {
                    if(menuItem.getToolTipText().equalsIgnoreCase(treeNode.getFrame().getTable().getName())) {
                        return treeNode.getFrame().getTable();
                    }
                }
            }
        }
        return null;
    }
}