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

import static javax.swing.BorderFactory.createBevelBorder;

import javax.swing.JInternalFrame;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

import com.romraider.editor.ecu.ECUEditor;
import com.romraider.editor.ecu.ECUEditorManager;
import com.romraider.logger.ecu.ui.handler.table.TableUpdateHandler;
import com.romraider.maps.Table;

public class TableFrame extends JInternalFrame implements InternalFrameListener {

    private static final long serialVersionUID = -2651279694660392351L;
    private final Table table;
    private TableMenuBar tableMenuBar = null;

    public TableFrame(String title, Table table) {
        super(title, true, true);
        this.table = table;
        add(table);
        setFrameIcon(null);
        setBorder(createBevelBorder(0));
        if (System.getProperty("os.name").startsWith("Mac OS"))
            putClientProperty("JInternalFrame.isPalette", true);
        setVisible(false);
        tableMenuBar = new TableMenuBar(this);
        setJMenuBar(tableMenuBar);
        setDefaultCloseOperation(HIDE_ON_CLOSE);
        addInternalFrameListener(this);
    }

    @Override
    public void internalFrameActivated(InternalFrameEvent e) {
        ECUEditor parent = getEditor();
        parent.updateTableToolBar(getTable());
        parent.getToolBar().updateButtons();
        parent.getEditorMenuBar().updateMenu();
    }


    @Override
    public void internalFrameOpened(InternalFrameEvent e) {
        TableUpdateHandler.getInstance().registerTable(this.getTable());
    }

    @Override
    public void internalFrameClosing(InternalFrameEvent e) {
        TableUpdateHandler.getInstance().deregisterTable(this.getTable());
        getEditor().removeDisplayTable(this);
    }

    @Override
    public void internalFrameClosed(InternalFrameEvent e) {
        getEditor().updateTableToolBar(null);
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
        getEditor().updateTableToolBar(null);
    }

    public Table getTable() {
        return table;
    }

    public ECUEditor getEditor() {
        return ECUEditorManager.getECUEditor();
    }

    public TableMenuBar getTableMenuBar() {
        return this.tableMenuBar;
    }
}