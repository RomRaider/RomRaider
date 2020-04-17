/*
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2019 RomRaider.com
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

package com.romraider.logger.ecu.ui.swing.menubar.action;

import static javax.swing.JOptionPane.QUESTION_MESSAGE;
import static javax.swing.JOptionPane.YES_NO_OPTION;
import static javax.swing.JOptionPane.showConfirmDialog;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.ToolTipManager;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import com.romraider.logger.ecu.EcuLogger;
import com.romraider.logger.ecu.definition.Module;
import com.romraider.logger.ecu.definition.Transport;
import com.romraider.swing.menubar.action.AbstractAction;

public final class SelectProtocolAction extends AbstractAction {

    public SelectProtocolAction(EcuLogger logger) {
        super(logger);
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        try {        	
            logger.stopLogging();
            new CommSettings();
        } catch (Exception e) {
            logger.reportError(e);
        }
    }

    private class CommSettings extends JDialog {
        private static final long serialVersionUID = -6226102628115868401L;
        private final JButton selectButton = new JButton(rb.getString("SPRABTN"));
        private final Map<String, Map<Transport, Collection<Module>>> protocolList;
        private TreePath selectedPath;

        private CommSettings() {
            this.protocolList = logger.getProtocolList();
            setTitle(rb.getString("SPRATITLE"));
            setModalityType(ModalityType.APPLICATION_MODAL);
            setIconImage(logger.getIconImage());
            setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            setBounds(
                    logger.getX() + (logger.getWidth() / 2) - 250,
                    logger.getY() + 90,
                    300,
                    300);
            getContentPane().setLayout(new BorderLayout());

            final JPanel contentPanel = new JPanel();
            contentPanel.setLayout(new BorderLayout());
            contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
            contentPanel.add(buildTree(), BorderLayout.CENTER);
            getContentPane().add(contentPanel, BorderLayout.CENTER);

            final JPanel buttonPane = new JPanel();
            buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
            getContentPane().add(buttonPane, BorderLayout.SOUTH);

            selectButton.setEnabled(false);
            selectButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    confirmSelection();
                }
            });
            buttonPane.add(selectButton);
            getRootPane().setDefaultButton(selectButton);

            final JButton cancelButton = new JButton(rb.getString("SPRACANCEL"));
            cancelButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    closeDialog();
                }
            });
            buttonPane.add(cancelButton);
            setVisible(true);
        }

        private Component buildTree() {
            final JTree tree = new JTree(buildNodeTree()) {
                private static final long serialVersionUID = 4718749407995133513L;
                
                @Override
                protected void setExpandedState(TreePath path, boolean state) {
                    if (state) {
                        super.setExpandedState(path, state);
                    }
                }

                @Override
                public String getToolTipText(MouseEvent e) {
                    if (e != null) {
                        final TreePath path = super.getPathForLocation(
                                e.getX(), e.getY());
                        if (path != null) {
                            final DefaultMutableTreeNode node = getTreeNode(path);
                            if (node != null) {
                                final Object o = node.getUserObject();
                                if (o instanceof Transport) {
                                    return String.format("%s [%s]",
                                            ((Transport) o).getDescription(),
                                            ((Transport) o).getId());
                                }
                            }
                        }
                        else {
                            return rb.getString("SPRATRANSPORT");
                        }
                    }
                    return null;
                }
            };
            ToolTipManager.sharedInstance().registerComponent(tree);

            final DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
            renderer.setOpenIcon(null);
            renderer.setLeafIcon(null);
            tree.setCellRenderer(renderer);

            tree.setEditable(true);
            tree.setRootVisible(true);
            tree.setShowsRootHandles(false);
            tree.getSelectionModel().setSelectionMode(
                    TreeSelectionModel.SINGLE_TREE_SELECTION);
            
            final String currentProtocol = logger.getSettings().getLoggerProtocol();
            final String currentTransport = logger.getSettings().getTransportProtocol();
            for (int i = 0; i < tree.getRowCount(); i++) {
                tree.expandRow(i);
                final TreePath path = tree.getPathForRow(i);
                final DefaultMutableTreeNode node = getTreeNode(path);
                final Object o = node.getUserObject();

                if (o instanceof Transport) {
                    final DefaultMutableTreeNode parent =
                            (DefaultMutableTreeNode) node.getParent();
                    final String parentName = (String) parent.getUserObject();
                    final String transportId = ((Transport) o).getId();

                    if (currentTransport.equalsIgnoreCase(transportId) &&
                            currentProtocol.equalsIgnoreCase(parentName)) {
                        tree.setSelectionPath(path);
                    }
                }
            }

            tree.addTreeSelectionListener(new TreeSelectionListener() {
                @Override
                public void valueChanged(TreeSelectionEvent e) {
                    
                    final DefaultMutableTreeNode node =
                            (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
                    if (node != null) {
                        if (node.isLeaf()) {
                            selectButton.setEnabled(true);
                            selectedPath = tree.getSelectionPath();
                        }
                        else {
                            selectButton.setEnabled(false);
                        }
                    }
                }
            });

            final JScrollPane scrollPane = new JScrollPane(tree);
            return scrollPane;
        }

        private MutableTreeNode buildNodeTree() {
            final DefaultMutableTreeNode root;
            root = new DefaultMutableTreeNode(rb.getString("SPRATREE"));
            for (String protocol : protocolList.keySet()) {
                final DefaultMutableTreeNode protocolNode =
                        new DefaultMutableTreeNode(protocol);
                root.add(protocolNode);
                final Map<Transport, Collection<Module>> transportMap =
                        protocolList.get(protocol);
                final Set<Transport> trasnports = transportMap.keySet();
                    for (Transport transport : trasnports) {
                        final DefaultMutableTreeNode transportNode =
                                new DefaultMutableTreeNode(transport);
                        protocolNode.add(transportNode);
                }
            }
            return root;
        }

        private DefaultMutableTreeNode getTreeNode(TreePath path) {
          return (DefaultMutableTreeNode) path.getLastPathComponent();
        }

        private final void confirmSelection() {
            final int result = showConfirmDialog(logger,
                    rb.getString("SPRACONFIRM"),
                    rb.getString("SPRACONFIRMTITLE"),
                    YES_NO_OPTION,
                    QUESTION_MESSAGE);

            if (result == 0) {
                final DefaultMutableTreeNode node = getTreeNode(selectedPath);
                final Object o = node.getUserObject();
                if (o instanceof Transport) {
                    final DefaultMutableTreeNode parent =
                            (DefaultMutableTreeNode) node.getParent();
                    final String parentName = (String) parent.getUserObject();
                    logger.getSettings().setLoggerProtocol(parentName);
                    logger.getSettings().setTransportProtocol(((Transport) o).getId());

                }
                
                logger.updateElmSelectable();                          	
                logger.loadLoggerParams();
                closeDialog();
            }
        }

        private final void closeDialog() {
            setVisible(false);
            dispose();
            logger.startLogging();
        }
    }
}
