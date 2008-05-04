/*
 *
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2008 RomRaider.com
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
 *
 */

package com.romraider.NewGUI.interfaces;

import javax.swing.JMenu;
import javax.swing.JToolBar;
import java.awt.event.ActionListener;
import java.util.Vector;


public interface TuningEntity extends ActionListener {

    // Return name of tuning entity
    public String getName();

    // Return all the menu items relevant to tuning entity
    public Vector<JMenu> getMenuItems();

    // Return the toolbar
    public JToolBar getToolBar();

    // Return object data based on passed table name
    public Object[][] getTableData(String tableName);

    // Remove tuning group
    public void removeTuningGroup(String tuningGroup);

    // Push back modified data to the tuning entity
    public void setTableData(String tableIdentifier, Object[][] data);

    // Control methods
    public void init(TuningEntityListener listener);

    // Notify of system exit. Tuning entity must reply to parent GUI that they are in fac
    // ready for shutdown
    public void notifySystemExit();
}
