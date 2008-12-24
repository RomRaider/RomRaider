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

package com.romraider.NewGUI.tree;

import com.romraider.NewGUI.data.ApplicationStateManager;
import com.romraider.NewGUI.data.TableMetaData;
import static javax.swing.BorderFactory.createLineBorder;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;

public class ETreeCellRenderer implements TreeCellRenderer {

    DefaultTreeCellRenderer defaultRenderer = new DefaultTreeCellRenderer();

    public Component getTreeCellRendererComponent(JTree tree, Object value,
                                                  boolean selected, boolean expanded, boolean leaf, int row,
                                                  boolean hasFocus) {

        Component returnValue = null;

        if (value != null && value instanceof ETreeNode) {

            ETreeNode eTreeNode = (ETreeNode) value;

            JPanel namedJPanel = new JPanel(new GridLayout(1, 1));
            namedJPanel.setBorder(createLineBorder(Color.WHITE));
            JLabel nodeName = new JLabel("");
            namedJPanel.setBackground(Color.WHITE);

            // Define appropriate ICON to use for node
            if (eTreeNode.getTableMetaData().getNodeType() == TableMetaData.DATA_1D) {
                nodeName = new JLabel(eTreeNode.getNodeName() + " ", new ImageIcon("./graphics/1d.gif"), JLabel.LEFT);
            } else if (eTreeNode.getTableMetaData().getNodeType() == TableMetaData.DATA_2D) {
                nodeName = new JLabel(eTreeNode.getNodeName() + " ", new ImageIcon("./graphics/2d.gif"), JLabel.LEFT);
            } else if (eTreeNode.getTableMetaData().getNodeType() == TableMetaData.DATA_3D) {
                nodeName = new JLabel(eTreeNode.getNodeName() + " ", new ImageIcon("./graphics/3d.gif"), JLabel.LEFT);
            }


            if (eTreeNode.getTableMetaData().getNodeType() == TableMetaData.MAP_SET_ROOT) {

                nodeName = new JLabel(eTreeNode.getNodeName(), JLabel.LEFT);
                nodeName.setFont(new Font("Tahoma", Font.PLAIN, 11));
                namedJPanel.add(nodeName);

                if (selected) {
                    namedJPanel.setBackground(new Color(220, 220, 255));
                    namedJPanel.setBorder(createLineBorder(new Color(0, 0, 225)));

                } else {
                    namedJPanel.setBorder(createLineBorder(new Color(220, 0, 0)));
                    namedJPanel.setBackground(new Color(255, 210, 210));
                }

                namedJPanel.setPreferredSize(new Dimension(tree.getParent().getWidth(), 30));
                namedJPanel.setMaximumSize(new Dimension(tree.getParent().getWidth(), 30));
                namedJPanel.setEnabled(tree.isEnabled());
                returnValue = namedJPanel;

            } else if (eTreeNode.getTableMetaData().getNodeType() == TableMetaData.DATA_3D || eTreeNode.getTableMetaData().getNodeType() == TableMetaData.DATA_2D || eTreeNode.getTableMetaData().getNodeType() == TableMetaData.DATA_1D) {

                namedJPanel.add(nodeName);
                nodeName.setFont(new Font("Tahoma", Font.PLAIN, 11));

                if (selected) {
                    namedJPanel.setBackground(new Color(220, 220, 255));
                    namedJPanel.setBorder(createLineBorder(new Color(0, 0, 225)));
                }

                if (eTreeNode.getUserLevel() == 5) {
                    nodeName.setForeground(new Color(255, 150, 150));
                    nodeName.setFont(new Font("Tahoma", Font.ITALIC, 11));

                } else if (eTreeNode.getUserLevel() > ApplicationStateManager.getCurrentUserLevel()) {
                    //tableName.setForeground(new Color(185, 185, 185));
                    nodeName.setFont(new Font("Tahoma", Font.ITALIC, 11));

                }

                returnValue = namedJPanel;
            }
        }


        if (returnValue == null) {
            returnValue = defaultRenderer.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);

        }

        return returnValue;
    }
}
