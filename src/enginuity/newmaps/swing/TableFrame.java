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

import javax.swing.*;
import java.awt.*;

class TableFrame extends JFrame {
    // Instance attributes used in this example
    private	JPanel		topPanel;
    private	Table		table;
    private	JScrollPane     scrollPane;

    private	String		columnNames[];
    private	String		dataValues[][];

    // Constructor of main frame
    public TableFrame() {
        // Set the frame characteristics
        setTitle("Advanced Table Application");
        setSize(300, 200);
        setBackground(Color.gray);
        // remove when i make a JInternalFrame
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // Create a panel to hold all other components
        topPanel = new JPanel();
        topPanel.setLayout(new BorderLayout());
        getContentPane().add(topPanel);

        // Create columns
        CreateColumns();
        CreateData();

        // Create a new table instance
        table = new Table(dataValues, columnNames);

        // Configure some of Table's paramters
        table.setColumnSelectionAllowed(false);
        table.setRowSelectionAllowed(false);

        // Change the selection colour
        table.setSelectionForeground(Color.white);
        table.setSelectionBackground(Color.red);

        // Add the table to a scrolling pane
        scrollPane = new JScrollPane(table);
        topPanel.add(scrollPane, BorderLayout.CENTER);
        setVisible(true);
    }

    public void CreateColumns() {
        // Create column string labels
        columnNames = new String[8];

        for( int iCtr = 0; iCtr < 8; iCtr++ ) {
            columnNames[iCtr] = "Col:" + iCtr;
        }
    }

    public void CreateData() {
        // Create data for each element
        dataValues = new String[100][8];

        for (int iY = 0; iY < 100; iY++) {
            for(int iX = 0; iX < 8; iX++) {
                    dataValues[iY][iX] = "" + iX + "," + iY;
            }
        }
    }
      
    public static void main( String args[] ) {
        TableFrame mainFrame = new TableFrame();
    } 
}
