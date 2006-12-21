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

import enginuity.ECUEditor;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Enumeration;

public class RomTree extends JTree implements MouseListener {

    public static ECUEditor container;

    public RomTree(DefaultMutableTreeNode input) {
        super(input);
        setRootVisible(false);
        setRowHeight(0);
        addMouseListener(this);
        setCellRenderer(new RomCellRenderer());
        setFont(new Font("Tahoma", Font.PLAIN, 11));
    }

    public ECUEditor getContainer() {
        return container;
    }

    public void setContainer(ECUEditor container) {
        this.container = container;
    }

    public void mouseClicked(MouseEvent e) {

        try {

            Object selectedRow = getPathForLocation(e.getX(), e.getY()).getLastPathComponent();

            if (e.getClickCount() >= container.getSettings().getTableClickCount() &&
                    selectedRow instanceof TableTreeNode) {

                TableTreeNode node = (TableTreeNode) selectedRow;

                //if (!(node.getTable().getUserLevel() > container.getSettings().getUserLevel())) {
                    container.displayTable(node.getFrame());
                //}

            }

            if (selectedRow instanceof TableTreeNode) {
                TableTreeNode node = (TableTreeNode) getLastSelectedPathComponent();
                container.setLastSelectedRom(node.getTable().getRom());

            } else if (selectedRow instanceof CategoryTreeNode) {
                CategoryTreeNode node = (CategoryTreeNode) getLastSelectedPathComponent();
                container.setLastSelectedRom(node.getRom());

            } else if (selectedRow instanceof RomTreeNode) {
                RomTreeNode node = (RomTreeNode) getLastSelectedPathComponent();
                container.setLastSelectedRom(node.getRom());
            }
        } catch (NullPointerException ex) {
        }
    }

    public void mousePressed(MouseEvent e) {
    }

    public void mouseReleased(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void removeDescendantToggledPaths(Enumeration<TreePath> toRemove) {
        super.removeDescendantToggledPaths(toRemove);
    }
}