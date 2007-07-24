/*
 * IzPack - Copyright 2001-2007 Julien Ponge, All Rights Reserved.
 * 
 * http://izpack.org/
 * http://developer.berlios.de/projects/izpack/
 * 
 * Copyright 2001 Johannes Lehtinen 
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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.izforge.izpack.gui.LabelFactory;
import com.izforge.izpack.installer.InstallData;
import com.izforge.izpack.installer.InstallerFrame;
import com.izforge.izpack.installer.IzPanel;
import com.izforge.izpack.installer.ResourceManager;
import com.izforge.izpack.util.VariableSubstitutor;

/**
 * The XInfo panel class - shows some adaptative text (ie by parsing for some variables.
 * 
 * @author Julien Ponge
 */
public class XInfoPanel extends IzPanel
{

    /**
     * 
     */
    private static final long serialVersionUID = 3257009856274970416L;

    /** The text area. */
    private JTextArea textArea;

    /** The info to display. */
    private String info;

    /**
     * The constructor.
     * 
     * @param parent The parent window.
     * @param idata The installation data.
     */
    public XInfoPanel(InstallerFrame parent, InstallData idata)
    {
        super(parent, idata);

        // We initialize our layout
        GridBagLayout layout = new GridBagLayout();
        GridBagConstraints gbConstraints = new GridBagConstraints();
        setLayout(layout);

        // We add the components

        JLabel infoLabel = LabelFactory.create(parent.langpack.getString("InfoPanel.info"), parent.icons
                .getImageIcon("edit"), JLabel.TRAILING);
        parent.buildConstraints(gbConstraints, 0, 0, 1, 1, 1.0, 0.0);
        gbConstraints.insets = new Insets(5, 5, 5, 5);
        gbConstraints.fill = GridBagConstraints.BOTH;
        gbConstraints.anchor = GridBagConstraints.SOUTHWEST;
        layout.addLayoutComponent(infoLabel, gbConstraints);
        add(infoLabel);

        textArea = new JTextArea();
        textArea.setEditable(false);
        JScrollPane scroller = new JScrollPane(textArea);
        parent.buildConstraints(gbConstraints, 0, 1, 1, 1, 1.0, 0.9);
        gbConstraints.anchor = GridBagConstraints.CENTER;
        layout.addLayoutComponent(scroller, gbConstraints);
        add(scroller);
    }

    /** Loads the info text. */
    private void loadInfo()
    {
        try
        {
            // We read it
            info = ResourceManager.getInstance().getTextResource("XInfoPanel.info");
        }
        catch (Exception err)
        {
            info = "Error : could not load the info text !";
        }
    }

    /** Parses the text for special variables. */
    private void parseText()
    {
        try
        {
            // Initialize the variable substitutor
            VariableSubstitutor vs = new VariableSubstitutor(idata.getVariables());

            // Parses the info text
            info = vs.substitute(info, null);
        }
        catch (Exception err)
        {
            err.printStackTrace();
        }
    }

    /** Called when the panel becomes active. */
    public void panelActivate()
    {
        // Text handling
        loadInfo();
        parseText();

        // UI handling
        textArea.setText(info);
        textArea.setCaretPosition(0);
    }

    /**
     * Indicates wether the panel has been validated or not.
     * 
     * @return Always true.
     */
    public boolean isValidated()
    {
        return true;
    }
}
