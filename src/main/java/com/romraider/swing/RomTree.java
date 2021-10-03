/*
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2015 RomRaider.com
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

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import com.romraider.editor.ecu.ECUEditor;
import com.romraider.editor.ecu.ECUEditorManager;
import com.romraider.maps.Rom;
import com.romraider.util.SettingsManager;

public class RomTree extends JTree implements MouseListener {

    private static final long serialVersionUID = 1630446543383498886L;

    public RomTree(DefaultMutableTreeNode input) {
        super(input);
        setRootVisible(false);
        setRowHeight(0);
        addMouseListener(this);
        setCellRenderer(new RomCellRenderer());
        setFont(new Font("Tahoma", Font.PLAIN, 11));
        setToggleClickCount(SettingsManager.getSettings().getTableClickCount());

        // key binding actions
        Action tableSelectAction = new AbstractAction() {
            private static final long serialVersionUID = -6007532264821746092L;

            @Override
            public void actionPerformed(ActionEvent e) {
                Object selectedRow = getLastSelectedPathComponent();
                /* if nothing is selected */
                if (selectedRow == null) {
                    return;
                }

                if (selectedRow instanceof TableTreeNode) {
                    showTable((TableTreeNode)selectedRow);
                }
                setLastSelectedRom(selectedRow);
            }
        };

        this.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "enter");
        this.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), "space");
        this.getActionMap().put("enter", tableSelectAction);
        this.getActionMap().put("space", tableSelectAction);
    }

    public ECUEditor getEditor() {
        return ECUEditorManager.getECUEditor();
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        TreePath treePath = getPathForLocation(e.getX(), e.getY());
        if (treePath == null)
            return; // this happens if we click in the empty area
        Object selectedRow = treePath.getLastPathComponent();
        /* if nothing is selected */
        if (selectedRow == null) {
            return;
        }

        if (e.getClickCount() >= SettingsManager.getSettings().getTableClickCount()
                && selectedRow instanceof TableTreeNode && getRomNode((TableTreeNode)selectedRow) != null) {
            showTable((TableTreeNode)selectedRow);
        }

        setLastSelectedRom(selectedRow);
    }

    private void showTable(TableTreeNode selectedRow) {
        getEditor().displayTable(selectedRow);
    }

    private void setLastSelectedRom(Object selectedNode) {
        if (selectedNode == null || selectedNode instanceof RomTreeRootNode) {
            return;
        }

        Rom romNode = getRomNode(selectedNode);
        if (romNode == null) {
            return;
        }
        getEditor().setLastSelectedRom(romNode);
        getEditor().refreshUI();
    }

    public static Rom getRomNode(Object currentNode){
        if (currentNode == null) {
            return null;
        } else if(currentNode instanceof Rom) {
            return (Rom)currentNode;
        } else if(currentNode instanceof TableTreeNode) {
            return getRomNode(((TableTreeNode)currentNode).getParent());
        } else if(currentNode instanceof CategoryTreeNode) {
            return getRomNode(((CategoryTreeNode)currentNode).getParent());
        } else {
            return null;
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

}