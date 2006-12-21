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

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;
import java.util.Vector;

public class JTableChooser extends JOptionPane {

    JPanel displayPanel = new JPanel();
    DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("Open Images");
    JTree displayTree = new JTree(rootNode);

    public boolean showChooser(Vector<Rom> roms, Component parent, Table targetTable) {

        for (int i = 0; i < roms.size(); i++) {
            Rom rom = roms.get(i);
            DefaultMutableTreeNode romNode = new DefaultMutableTreeNode(rom.getFileName());
            rootNode.add(romNode);

            for (int j = 0; j < rom.getTables().size(); j++) {
                Table table = rom.getTables().get(j);
                TableChooserTreeNode tableNode = new TableChooserTreeNode(table.getName(), table);

                // categories
                boolean categoryExists = false;
                for (int k = 0; k < romNode.getChildCount(); k++) {
                    if (romNode.getChildAt(k).toString().equalsIgnoreCase(table.getCategory())) {
                        ((DefaultMutableTreeNode) romNode.getChildAt(k)).add(tableNode);
                        categoryExists = true;
                        break;
                    }
                }

                if (!categoryExists) {
                    DefaultMutableTreeNode categoryNode = new DefaultMutableTreeNode(table.getCategory());
                    romNode.add(categoryNode);
                    categoryNode.add(tableNode);
                }
            }
        }

        displayPanel.setPreferredSize(new Dimension(350, 400));
        displayPanel.setMinimumSize(new Dimension(350, 400));
        displayTree.setPreferredSize(new Dimension(330, 400));
        displayTree.setMinimumSize(new Dimension(330, 400));

        displayTree.setRootVisible(true);
        displayTree.updateUI();
        displayPanel.add(new JScrollPane(displayTree));

        Object[] values = {"Compare", "Cancel"};

        if ((showOptionDialog(parent, displayPanel, "Select a Map", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, values, values[0]) == 0 &&
                (displayTree.getLastSelectedPathComponent() instanceof TableChooserTreeNode))) {
            ((TableChooserTreeNode) displayTree.getLastSelectedPathComponent()).getTable().copyTable();
            return true;
        } else {
            return false;
        }
    }
}