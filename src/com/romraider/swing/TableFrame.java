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
import com.romraider.maps.Table;

public class TableFrame extends JInternalFrame implements InternalFrameListener {

    private static final long serialVersionUID = -2651279694660392351L;
    private Table table;
    private final ECUEditor parent;

    public TableFrame(Table table, ECUEditor parent) {
        super(table.getRom().getFileName() + " - " + table.getName(), true, true);

        this.parent = parent;

        setTable(table);
        add(table);
        setFrameIcon(null);
        setBorder(createBevelBorder(0));
        if (System.getProperty("os.name").startsWith("Mac OS"))
            putClientProperty("JInternalFrame.isPalette", true);
        setVisible(false);
        setJMenuBar(new TableMenuBar(table));
        setDefaultCloseOperation(HIDE_ON_CLOSE);
        table.setFrame(this);
        addInternalFrameListener(this);
    }

    public TableToolBar getToolBar() {
        return parent.getTableToolBar();
    }

    @Override
    public void internalFrameActivated(InternalFrameEvent e) {
        getTable().getEditor().setLastSelectedRom(getTable().getRom());
        parent.updateTableToolBar(this.table);
        parent.getToolBar().updateButtons();
        parent.getEditorMenuBar().updateMenu();
    }


    @Override
    public void internalFrameOpened(InternalFrameEvent e) {
        ;
    }

    @Override
    public void internalFrameClosing(InternalFrameEvent e) {
        getTable().getEditor().removeDisplayTable(this);
    }

    @Override
    public void internalFrameClosed(InternalFrameEvent e) {
        parent.updateTableToolBar(null);
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
        parent.updateTableToolBar(null);
    }

    public Table getTable() {
        return table;
    }

    public void setTable(Table table) {
        this.table = table;
    }

    public void updateFileName() {
        setTitle(table.getRom().getFileName() + " - " + table.getName());
    }
}