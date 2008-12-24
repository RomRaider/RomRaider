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

package com.romraider.NewGUI.interfaces;

import com.romraider.NewGUI.tree.ETreeNode;
import javax.swing.JMenu;
import javax.swing.JToolBar;
import java.util.Vector;

public interface TuningEntityListener {

    /**
     * Change the tree structure base on the passed root node
     *
     * @param rootNodeOfNewTree
     */
    public void addNewTuningGroup(ETreeNode rootNodeOfNewTree);

    /**
     * Removes a tuning group from the tree
     *
     * @param tableGroup
     */
    public void removeTuningGroup(String tableGroup);

    /**
     * Prepends list of menu items to the menu bae
     *
     * @param items
     */
    public void rebuildJMenuBar(Vector<JMenu> items);

    /**
     * If a tuning entity has a custom tool bar to add, call this method.
     *
     * @param theToolBar
     */
    public void setNewToolBar(JToolBar theToolBar);

    /**
     * Return the number of maps that have changed belonging to the targeted tuning entity of the defined tableGroup
     *
     * @param tuningEntity
     * @param tableGroup
     * @return
     */
    public int getMapChangeCount(TuningEntity tuningEntity, String tableGroup);

    /**
     * Has the main GUI kick off the process of saving table data.
     */
    public void saveMaps();

    /**
     * Tuning entity calls back to main gui when its prepared for exit.
     */
    public void readyForExit();
}
