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

import static javax.swing.BorderFactory.createBevelBorder;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.MessageFormat;
import java.util.ResourceBundle;
import java.util.Vector;

import javax.swing.Icon;
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
    private TableView tableView;
    private final TableMenuBar tableMenuBar;

    public TableFrame(String title, TableView tableView) {
        super(title, true, true);
        this.tableView = tableView;
        Table t = tableView.getTable();

        Icon icon = RomCellRenderer.getIconForTable(t);
        setFrameIcon(icon);

        t.setTableFrame(this);
        add(tableView);

        setBorder(createBevelBorder(0));
        if (System.getProperty("os.name").startsWith("Mac OS"))
            putClientProperty("JInternalFrame.isPalette", true);
        setVisible(false);
        tableMenuBar = new TableMenuBar(this);
        setJMenuBar(tableMenuBar);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        addInternalFrameListener(this);
    }

    public void RegisterTable() {
        TableUpdateHandler.getInstance().registerTable(this.getTable());
    }

    public void DeregisterTable() {
        TableUpdateHandler.getInstance().deregisterTable(this.getTable());
    }

    private void updateToolbar(Table t) {
        ECUEditor parent = getEditor();
        parent.getTableToolBar().updateTableToolBar(t);
        parent.getToolBar().updateButtons();
        parent.getEditorMenuBar().updateMenu();
    }

    @Override
    public void internalFrameActivated(InternalFrameEvent e) {
    	updateToolbar(getTable());
    }

    @Override
    public void internalFrameDeactivated(InternalFrameEvent e) {
    	updateToolbar(null);
    }

    @Override
    public void internalFrameClosing(InternalFrameEvent e) {}

    @Override
    public void internalFrameOpened(InternalFrameEvent e) {}

    @Override
    public void internalFrameClosed(InternalFrameEvent e) {}
    @Override
    public void internalFrameIconified(InternalFrameEvent e) {}
    @Override
    public void internalFrameDeiconified(InternalFrameEvent e) {}

    public Table getTable() {
    	if(tableView == null) return null;

        return tableView.getTable();
    }
    public TableView getTableView() {
        return tableView;
    }

    public void setTableView(TableView v) {
    	tableView = v;
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
            else {
                // User closed/cancelled Chooser window
                menu.getCompareOff().setSelected(true);
            }

        } else if (e.getSource() instanceof TableMenuItem) {
            Table selectedTable = ((TableMenuItem) e.getSource()).getTable();
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
            getTable().interpolate();

        } else if (e.getSource() == menu.getVertInterp()) {
        	getTable().verticalInterpolate();

        } else if (e.getSource() == menu.getHorizInterp()) {
        	getTable().horizontalInterpolate();
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

        Vector<Rom> images = ECUEditorManager.getECUEditor().getImages();
        boolean addedTable = false;

        if(images.size() > 1) {
	        for(Rom rom : images) {
	        	if (rom == getTable().getRom()) continue;
	        	if(rom.getTableNodes().containsKey(getTable().getName().toLowerCase())) {
	        		TableTreeNode tableNode = rom.getTableNodes().get(getTable().getName().toLowerCase());
                    JRadioButtonMenuItem similarTable = new TableMenuItem(tableNode.getTable());
                    similarTable.setToolTipText(tableNode.getTable().getName());
                    similarTable.addActionListener(this);
                    similarTables.add(similarTable);
                    addedTable = true;
                    continue;
	                }
	            }
        }

        if(addedTable)
        	similarTables.setEnabled(true);
        else
        	similarTables.setEnabled(false);

        getTableMenuBar().initCompareGroup(this);
        getTableMenuBar().repaint();
    }
}
