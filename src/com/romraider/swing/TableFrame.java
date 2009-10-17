/*
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2009 RomRaider.com
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

import com.romraider.maps.Table;
import static javax.swing.BorderFactory.createBevelBorder;
import javax.swing.JInternalFrame;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;
import java.awt.BorderLayout;

public class TableFrame extends JInternalFrame implements InternalFrameListener {

    private static final long serialVersionUID = -2651279694660392351L;
    private Table table;
    private TableToolBar toolBar;

    public TableFrame(Table table) {
        super(table.getRom().getFileName() + " - " + table.getName(), true, true);
        setTable(table);
        add(table);
        setFrameIcon(null);
        setBorder(createBevelBorder(0));
        setVisible(false);
        setJMenuBar(new TableMenuBar(table));
        toolBar = new TableToolBar(table, this);
        add(toolBar, BorderLayout.NORTH);
        setDefaultCloseOperation(HIDE_ON_CLOSE);
        table.setFrame(this);
        addInternalFrameListener(this);
    }

    public TableToolBar getToolBar() {
        return toolBar;
    }

    public void internalFrameActivated(InternalFrameEvent e) {
        getTable().getRom().getContainer().setLastSelectedRom(getTable().getRom());
    }


    public void internalFrameOpened(InternalFrameEvent e) {
    }

    public void internalFrameClosing(InternalFrameEvent e) {
        getTable().getRom().getContainer().removeDisplayTable(this);
    }

    public void internalFrameClosed(InternalFrameEvent e) {
    }

    public void internalFrameIconified(InternalFrameEvent e) {
    }

    public void internalFrameDeiconified(InternalFrameEvent e) {
    }

    public void internalFrameDeactivated(InternalFrameEvent e) {
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