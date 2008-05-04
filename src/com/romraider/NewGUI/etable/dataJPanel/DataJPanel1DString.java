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

package com.romraider.NewGUI.etable.dataJPanel;

import com.romraider.NewGUI.data.TableMetaData;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import java.awt.BorderLayout;

public class DataJPanel1DString extends JPanel implements DataJPanelInterface {

    private TableMetaData tableMetaData;
    private String initialStringValue;
    private JTextArea dataTextArea;

    public DataJPanel1DString(TableMetaData tableMetaData, Object[][] data) {
        this.tableMetaData = tableMetaData;
        this.initialStringValue = (String) data[0][0];

        this.setLayout(new BorderLayout());

        dataTextArea = new JTextArea((String) data[0][0]);
        JScrollPane dataScrollPane = new JScrollPane(dataTextArea);
        dataScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        dataScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        this.add(dataScrollPane, BorderLayout.CENTER);
    }

    public JToolBar getToolBar() {
        // TODO Auto-generated method stub
        return new JToolBar();
    }

    public JMenuBar getMenuBar() {
        // TODO Auto-generated method stub
        return new JMenuBar();
    }

    public boolean dataChanged() {
        // TODO Auto-generated method stub
        return this.initialStringValue.equals(this.dataTextArea.getText());
    }

    public void copySelectedTableData() {
        // TODO Auto-generated method stub

    }

    public void copyEntireTable() {
        // TODO Auto-generated method stub

    }

    public void pasteTableData() {
        // TODO Auto-generated method stub

    }

    public void setClosed(boolean value) {
        // TODO Auto-generated method stub

    }

    public void revertDataState() {
        // TODO Auto-generated method stub

    }

    public void saveDataState() {
        // TODO Auto-generated method stub

    }

    public void replaceData(Object[][] newData) {
        // TODO Auto-generated method stub
        this.dataTextArea.setText((String) newData[0][0]);
    }

    public Object[][] getData() {
        Object[][] temp = new Object[1][1];
        temp[0][0] = this.dataTextArea.getText();
        return temp;
    }

}
