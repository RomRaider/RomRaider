/*
 *
 * Enginuity Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006 Enginuity.org
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

package enginuity.swing;

import enginuity.maps.Rom;
import enginuity.maps.Table;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.Enumeration;
import java.util.Vector;

public class RomTreeNode extends DefaultMutableTreeNode {

    private Rom rom = new Rom();

    public RomTreeNode(Rom rom, int userLevel, boolean isDisplayHighTables) {
        setRom(rom);
        refresh(userLevel, isDisplayHighTables);
        updateFileName();
    }

    public void refresh(int userLevel, boolean isDisplayHighTables) {

        removeAllChildren();
        Vector<Table> tables = rom.getTables();

        for (int i = 0; i < tables.size(); i++) {
            Table table = tables.get(i);
            add(table);

            if (isDisplayHighTables || userLevel >= table.getUserLevel()) {

                boolean categoryExists = false;

                for (int j = 0; j < getChildCount(); j++) {
                    if (getChildAt(j).toString().equals(table.getCategory())) {

                        // add to appropriate category
                        TableTreeNode tableNode = new TableTreeNode(table);
                        getChildAt(j).add(tableNode);
                        categoryExists = true;
                        break;
                    }
                }

                if (!categoryExists) { // if category does not already exist, create it
                    add(new CategoryTreeNode(table.getCategory(), table.getRom()));
                    TableTreeNode tableNode = new TableTreeNode(table);

                    getLastChild().add(tableNode);
                }
            }
        }
    }

    public void removeAllChildren() {

        // close all table windows
        // loop through categories first
        for (int i = 0; i < getChildCount(); i++) {
            DefaultMutableTreeNode category = getChildAt(i);

            // loop through tables in each category
            for (Enumeration j = category.children(); j.hasMoreElements();) {
                ((TableTreeNode) j.nextElement()).getFrame().dispose();

            }
        }

        // removeAllChildren
        super.removeAllChildren();
    }

    public void updateFileName() {
        setUserObject(rom);
    }

    public void add(Table table) {
        TableFrame frame = new TableFrame(table);
        table.setFrame(frame);
    }

    public DefaultMutableTreeNode getChildAt(int i) {
        return (DefaultMutableTreeNode) super.getChildAt(i);
    }

    public DefaultMutableTreeNode getLastChild() {
        return (DefaultMutableTreeNode) super.getLastChild();
    }

    public Rom getRom() {
        return rom;
    }

    public void setRom(Rom rom) {
        this.rom = rom;
    }

}