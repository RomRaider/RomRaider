/*
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2012 RomRaider.com
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
import java.util.Enumeration;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import com.romraider.editor.ecu.ECUEditor;
import com.romraider.editor.ecu.ECUEditorManager;

public class RomTree extends JTree implements MouseListener {

    private static final long serialVersionUID = 1630446543383498886L;

    public RomTree(DefaultMutableTreeNode input) {
        super(input);
        setRootVisible(false);
        setRowHeight(0);
        addMouseListener(this);
        setCellRenderer(new RomCellRenderer());
        setFont(new Font("Tahoma", Font.PLAIN, 11));

        // key binding actions
        Action tableSelectAction = new AbstractAction() {
            private static final long serialVersionUID = -6008026264821746092L;

            @Override
            public void actionPerformed(ActionEvent e) {
                try{
                    Object selectedRow = getSelectionPath().getLastPathComponent();
                    showHideTable(selectedRow);
                }catch(NullPointerException ex) {
                }
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
        try{
            Object selectedRow = getPathForLocation(e.getX(), e.getY()).getLastPathComponent();

            if (e.getClickCount() >= getEditor().getSettings().getTableClickCount()) {
                showHideTable(selectedRow);
            }
            setLastSelectedRom(selectedRow);
        }catch(NullPointerException ex) {
        }
    }

    private void showHideTable(Object selectedRow) {
        try{
            if(selectedRow instanceof TableTreeNode) {
                TableTreeNode node = (TableTreeNode) selectedRow;
                getEditor().displayTable(node.getFrame());
            }
            setLastSelectedRom(selectedRow);
        } catch (NullPointerException ex) {
        }
    }

    private void setLastSelectedRom(Object selectedNode) {
        if (selectedNode instanceof TableTreeNode || selectedNode instanceof CategoryTreeNode || selectedNode instanceof RomTreeNode)
        {
            Object lastSelectedPathComponent = getLastSelectedPathComponent();
            if(lastSelectedPathComponent instanceof TableTreeNode) {
                TableTreeNode node = (TableTreeNode) lastSelectedPathComponent;
                getEditor().setLastSelectedRom(node.getTable().getRom());
            } else if(lastSelectedPathComponent instanceof CategoryTreeNode) {
                CategoryTreeNode node = (CategoryTreeNode) lastSelectedPathComponent;
                getEditor().setLastSelectedRom(node.getRom());
            } else if(lastSelectedPathComponent instanceof RomTreeNode) {
                RomTreeNode node = (RomTreeNode) lastSelectedPathComponent;
                getEditor().setLastSelectedRom(node.getRom());
            }
        }
        getEditor().refreshUI();
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

    @Override
    public void removeDescendantToggledPaths(Enumeration<TreePath> toRemove) {
        super.removeDescendantToggledPaths(toRemove);
    }
}