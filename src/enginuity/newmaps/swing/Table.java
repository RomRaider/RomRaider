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

package enginuity.newmaps.swing;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionListener;

public class Table extends JTable implements ListSelectionModel {
    
    private int width = 0;
    private int height = 0;
    
    public Table (Object[][] dataValues, Object[] columnNames) {
        super(dataValues, columnNames);
    }
    
    public static void main( String args[] ) {
        TableFrame mainFrame = new TableFrame();
    }    

    public void setSelectionInterval(int index0, int index1) {    }
    public void addSelectionInterval(int index0, int index1) {    }
    public void removeSelectionInterval(int index0, int index1) {    }
    public int getMinSelectionIndex() { return 0;   }
    public int getMaxSelectionIndex() { return 0;   }
    public boolean isSelectedIndex(int index) { return false;   }
    public int getAnchorSelectionIndex() { return 0;   }
    public void setAnchorSelectionIndex(int index) {    }
    public int getLeadSelectionIndex() { return 0;   }
    public void setLeadSelectionIndex(int index) {    }
    public boolean isSelectionEmpty() { return false;   }
    public void insertIndexInterval(int index, int length, boolean before) {    }
    public void removeIndexInterval(int index0, int index1) {    }
    public void setValueIsAdjusting(boolean valueIsAdjusting) {    }
    public boolean getValueIsAdjusting() { return false;   }
    public int getSelectionMode() { return 0;   }
    public void addListSelectionListener(ListSelectionListener x) {    }
    public void removeListSelectionListener(ListSelectionListener x) {    }
    
}