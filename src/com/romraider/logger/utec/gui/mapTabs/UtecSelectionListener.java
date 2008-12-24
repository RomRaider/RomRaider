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

package com.romraider.logger.utec.gui.mapTabs;

import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class UtecSelectionListener implements ListSelectionListener {
    private JTable parentTable = null;

    public UtecSelectionListener(JTable parentTable) {
        this.parentTable = parentTable;
    }

    public void valueChanged(ListSelectionEvent event) {
        //System.out.println("1: "+ event.getFirstIndex()+"     2: "+event.getLastIndex());

        int selRow[] = parentTable.getSelectedRows();
        int selCol[] = parentTable.getSelectedColumns();


        for (int i = 0; i < selRow.length; i++) {
            //System.out.println("Row Value: "+selRow[i]);
        }

        for (int i = 0; i < selCol.length; i++) {
            //System.out.println("Col Value: "+selCol[i]);
        }

        //System.out.println("---------------------------");
        Object[] selectedCells = new Object[selRow.length * selCol.length];


    }

}
