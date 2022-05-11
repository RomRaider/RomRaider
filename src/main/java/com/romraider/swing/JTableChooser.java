/*
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2022 RomRaider.com
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

package com.romraider.swing;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ResourceBundle;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import com.romraider.Settings;
import com.romraider.editor.ecu.ECUEditorManager;
import com.romraider.maps.Rom;
import com.romraider.maps.Table;
import com.romraider.util.ResourceUtil;
import com.romraider.util.SettingsManager;

public class JTableChooser extends JOptionPane implements MouseListener {

    private static final long serialVersionUID = 5611729002131147882L;
    private static final ResourceBundle rb = new ResourceUtil().getBundle(
            JTableChooser.class.getName());

    final JPanel displayPanel = new JPanel();
    DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("Open Images");
    final JTree displayTree = new JTree(rootNode);
    final JButton compareButton = new JButton(rb.getString("COMPARE"));
    JScrollPane displayScrollPane;
    private final Settings settings = SettingsManager.getSettings();

    public JTableChooser() {
    }

    public Table showChooser(Table targetTable) {
        final Vector<Rom> roms = ECUEditorManager.getECUEditor().getImages();
        int nameLength = 0;

        for (int i = 0; i < roms.size(); i++) {
            final Rom rom = roms.get(i);
            rom.getTableNodes().values();
            final DefaultMutableTreeNode romNode = new DefaultMutableTreeNode(rom.getFileName());
            rootNode.add(romNode);

            for (TableTreeNode tableTreeNode : rom.getTableNodes().values()) {
                final Table table = tableTreeNode.getTable();
                // use the length of the table name to set the width of the displayTree
                // so the entire name can be read without being cut off on the right
                if (table.getName().length() > nameLength) {
                    nameLength = table.getName().length();
                }
                final TableChooserTreeNode tableNode = new TableChooserTreeNode(table.getName(), table);

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
                    final DefaultMutableTreeNode categoryNode = new DefaultMutableTreeNode(table.getCategory());
                    romNode.add(categoryNode);
                    categoryNode.add(tableNode);
                }
            }
        }

        if (settings.isTableTreeSorted())
            rootNode = sortTableChooser(rootNode);

        displayTree.setPreferredSize(new Dimension(nameLength * 9, 400));
        displayTree.setMinimumSize(new Dimension(nameLength * 9, 400));

        displayTree.expandPath(new TreePath(rootNode.getPath()));

        displayTree.addMouseListener(this);
        displayScrollPane = new JScrollPane(displayTree);
        displayScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        displayPanel.add(displayScrollPane);

        Object[] values = {compareButton, rb.getString("CANCEL")};
        compareButton.setEnabled(false);
        compareButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                final JOptionPane pane = (JOptionPane)((JButton) (e.getSource())).getParent().getParent();
                pane.setValue(compareButton);
            }
        });

        int result = showOptionDialog(SwingUtilities.windowForComponent(targetTable.getTableView()),
                displayPanel,
                rb.getString("SELECT"), JOptionPane.DEFAULT_OPTION,
                JOptionPane.PLAIN_MESSAGE, null, values, values[0]);

        if (result == 0 && displayTree.getLastSelectedPathComponent() instanceof TableChooserTreeNode) {
            return ((TableChooserTreeNode) displayTree.getLastSelectedPathComponent()).getTable();
        } else {
            return null;
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        displayTree.setPreferredSize(new Dimension(displayTree.getWidth(),
                (displayTree.getRowCount()*displayTree.getRowHeight())));
        displayTree.revalidate();

        if(displayTree.getLastSelectedPathComponent() instanceof TableChooserTreeNode) {
        	compareButton.setEnabled(true);
        }
        else {
        	compareButton.setEnabled(false);
        }
    }
    @Override
    public void mouseClicked(MouseEvent e){}
    @Override
    public void mouseEntered(MouseEvent e){}
    @Override
    public void mouseExited(MouseEvent e){}
    @Override
    public void mousePressed(MouseEvent e){}

    /**
     * Sort the ROMs, categories and tables in the Compare table chooser JTree.
     * @param root - the current root node
     * @return - the sorted node tree
     */
    public DefaultMutableTreeNode sortTableChooser(DefaultMutableTreeNode root) {
        for (int i = 0; i < root.getChildCount(); i++) {
            DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) root.getChildAt(i);
            final String nodeString = childNode.getUserObject().toString();
            for (int j = 0; j < i; j++) {
                final DefaultMutableTreeNode prevNode = (DefaultMutableTreeNode) root.getChildAt(j);
                final String prevNodeString = prevNode.getUserObject().toString();
                if (nodeString.compareToIgnoreCase(prevNodeString) < 0) {
                    root.insert(childNode, j);
                    break;
                }
            }
            if (childNode.getChildCount() > 0) {
                childNode = sortTableChooser(childNode);
            }
        }
        return root;
    }
}
