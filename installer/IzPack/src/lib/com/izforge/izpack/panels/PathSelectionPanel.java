/*
 * IzPack - Copyright 2001-2007 Julien Ponge, All Rights Reserved.
 * 
 * http://izpack.org/
 * http://developer.berlios.de/projects/izpack/
 * 
 * Copyright 2004 Klaus Bartz
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 *     
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.izforge.izpack.panels;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.izforge.izpack.gui.ButtonFactory;
import com.izforge.izpack.gui.IzPanelConstraints;
import com.izforge.izpack.gui.IzPanelLayout;
import com.izforge.izpack.gui.LayoutConstants;
import com.izforge.izpack.installer.InstallData;
import com.izforge.izpack.installer.IzPanel;
import com.izforge.izpack.installer.LayoutHelper;

/**
 * This is a sub panel which contains a text field and a browse button for path selection. This is
 * NOT an IzPanel, else it is made to use in an IzPanel for any path selection. If the IzPanel
 * parent implements ActionListener, the ActionPerformed method will be called, if
 * PathSelectionPanel.ActionPerformed was called with a source other than the browse button. This
 * can be used to perform parentFrame.navigateNext in the IzPanel parent. An example implementation
 * is done in com.izforge.izpack.panels.PathInputPanel.
 * 
 * @author Klaus Bartz
 * 
 */
public class PathSelectionPanel extends JPanel implements ActionListener, LayoutConstants
{

    /**
     * 
     */
    private static final long serialVersionUID = 3618700794577105718L;

    /** The text field for the path. */
    private JTextField textField;

    /** The 'browse' button. */
    private JButton browseButton;

    /** IzPanel parent (not the InstallerFrame). */
    private IzPanel parent;

    /**
     * The installer internal data.
     */
    private InstallData idata;

    /**
     * The constructor. Be aware, parent is the parent IzPanel, not the installer frame.
     * 
     * @param parent The parent IzPanel.
     * @param idata The installer internal data.
     */
    public PathSelectionPanel(IzPanel parent, InstallData idata)
    {
        super();
        this.parent = parent;
        this.idata = idata;
        createLayout();
    }

    /**
     * Creates the layout for this sub panel.
     */
    protected void createLayout()
    {
        // We woulduse the IzPanelLayout also in this "sub"panel.
        // In an IzPanel there are support of this layout manager at
        // more than one places. In this panel not, therefore we have
        // to make all things needed.
        // First create a layout helper.
        LayoutHelper layoutHelper = new LayoutHelper(this);
        // Start the layout.
        layoutHelper.startLayout(new IzPanelLayout());
        // One of the rare points we need explicit a constraints.
        IzPanelConstraints ipc = IzPanelLayout.getDefaultConstraint(TEXT_CONSTRAINT);
        // The text field should be stretched.
        ipc.setXStretch(1.0);
        textField = new JTextField(idata.getInstallPath(), 10);
        textField.addActionListener(this);
        parent.setInitialFocus(textField);
        add(textField,ipc);
        // We would have place between text field and button.
        add(IzPanelLayout.createHorizontalFiller(3));
        // No explicit constraints for the button (else implicit) because
        // defaults are OK.
        browseButton = ButtonFactory.createButton(parent.getInstallerFrame().langpack
                .getString("TargetPanel.browse"), parent.getInstallerFrame().icons
                .getImageIcon("open"), idata.buttonsHColor);
        browseButton.addActionListener(this);
        add(browseButton);
    }

    // There are problems with the size if no other component needs the
    // full size. Sometimes directly, somtimes only after a back step.

    public Dimension getMinimumSize()
    {
        Dimension ss = super.getPreferredSize();
        Dimension retval = parent.getSize();
        retval.height = ss.height;
        return (retval);
    }

    /**
     * Actions-handling method.
     * 
     * @param e The event.
     */
    public void actionPerformed(ActionEvent e)
    {
        Object source = e.getSource();

        if (source == browseButton)
        {
            // The user wants to browse its filesystem

            // Prepares the file chooser
            JFileChooser fc = new JFileChooser();
            fc.setCurrentDirectory(new File(textField.getText()));
            fc.setMultiSelectionEnabled(false);
            fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            fc.addChoosableFileFilter(fc.getAcceptAllFileFilter());

            // Shows it
            if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
            {
                String path = fc.getSelectedFile().getAbsolutePath();
                textField.setText(path);
            }

        }
        else
        {
            if (parent instanceof ActionListener) ((ActionListener) parent).actionPerformed(e);
        }
    }

    /**
     * Returns the chosen path.
     * 
     * @return the chosen path
     */
    public String getPath()
    {
        return (textField.getText());
    }

    /**
     * Sets the contents of the text field to the given path.
     * 
     * @param path the path to be set
     */
    public void setPath(String path)
    {
        textField.setText(path);
    }

    /**
     * Returns the text input field for the path. This methode can be used to differ in a
     * ActionPerformed method of the parent between the browse button and the text field.
     * 
     * @return the text input field for the path
     */
    public JTextField getPathInputField()
    {
        return textField;
    }

    /**
     * Returns the browse button object for modification or for use with a different ActionListener.
     * 
     * @return the browse button to open the JFileChooser
     */
    public JButton getBrowseButton()
    {
        return browseButton;
    }

}
