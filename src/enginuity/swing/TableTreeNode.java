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

public class TableTreeNode extends DefaultMutableTreeNode {

    private String type;
    private Rom rom;
    private Table table;
    private String toolTip;
    private TableFrame frame;

    public TableTreeNode(Table table) {
        //super(table.getName() + " (" + table.getType() + "D)");
        super(table);
        this.table = table;
        this.frame = table.getFrame();
    }

    public String getType() {
        return type;
    }

    public Rom getRom() {
        return rom;
    }

    public void setRom(Rom rom) {
        this.rom = rom;
    }

    public Table getTable() {
        return table;
    }

    public void setTable(Table table) {
        this.table = table;
    }

    public void setToolTipText(String input) {
        toolTip = input;
    }

    public String getToolTipText() {
        return toolTip;
    }

    public TableFrame getFrame() {
        return frame;
    }

    public void setFrame(TableFrame frame) {
        this.frame = frame;
    }
}