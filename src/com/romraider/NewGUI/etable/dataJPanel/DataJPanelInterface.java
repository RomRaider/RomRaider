/*
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
 */

package com.romraider.NewGUI.etable.dataJPanel;

import javax.swing.JMenuBar;
import javax.swing.JToolBar;

public interface DataJPanelInterface {
    public JToolBar getToolBar();

    public JMenuBar getMenuBar();

    public boolean dataChanged();

    public void copySelectedTableData();

    public void copyEntireTable();

    public void pasteTableData();

    public void setClosed(boolean value);

    public void revertDataState();

    public void saveDataState();

    public void replaceData(Object[][] newData);

    public Object[][] getData();
}
