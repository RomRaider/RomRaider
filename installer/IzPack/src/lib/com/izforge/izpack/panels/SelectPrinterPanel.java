/*
 * IzPack - Copyright 2001-2007 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://developer.berlios.de/projects/izpack/
 *
 * Copyright 2006 Hal Vaughan
 * http://thresholddigital.com
 * hal@thresholddigital.com
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
 *
 * Updated by Fabrice Mirabile the 06/01/2006 
 *
 */

package com.izforge.izpack.panels;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.print.PrintServiceLookup;
import javax.print.PrintService;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.izforge.izpack.gui.LabelFactory;
import com.izforge.izpack.installer.InstallData;
import com.izforge.izpack.installer.InstallerFrame;
import com.izforge.izpack.installer.IzPanel;

/**
 * The SelectPrinter panel class.
 *
 * @author Hal Vaughan
 */
public class SelectPrinterPanel extends IzPanel implements ActionListener
{

    /**
     *
     */
    private static final long serialVersionUID = 3257848774955905587L;

    /** The ComboBox to list the printers. */
     private JComboBox cbPrinters;

    /** Install data variables. */
     private InstallData iData;

    /**
     * The constructor.
     *
     * @param parent The parent.
     * @param id The installation data.
     */
    public SelectPrinterPanel(InstallerFrame parent, InstallData id)
    {
        super(parent, id);
        
        iData = id;

        // The 'super' layout
        GridBagLayout superLayout = new GridBagLayout();
        setLayout(superLayout);
        GridBagConstraints gbConstraints = new GridBagConstraints();
        gbConstraints.insets = new Insets(0, 0, 0, 0);
        gbConstraints.fill = GridBagConstraints.NONE;
        gbConstraints.anchor = GridBagConstraints.CENTER;

        // We initialize our 'real' layout
        JPanel centerPanel = new JPanel();
        BoxLayout layout = new BoxLayout(centerPanel, BoxLayout.Y_AXIS);
        centerPanel.setLayout(layout);
        superLayout.addLayoutComponent(centerPanel, gbConstraints);
        add(centerPanel);

        cbPrinters = new JComboBox();
    	PrintService[] pServices = PrintServiceLookup.lookupPrintServices(null, null);
    	iData.setVariable("SELECTED_PRINTER", pServices[0].getName());
    	for (int i = 0; i < pServices.length; i++)
    	{
    		cbPrinters.addItem(pServices[i].getName());
    	}
        cbPrinters.addActionListener(this);

        // We create and put the labels
        String str;

        centerPanel.add(Box.createVerticalStrut(10));
        
        str = parent.langpack.getString("PrinterSelectPanel.select_printer");
        JLabel selectLabel = LabelFactory.create(str, JLabel.LEADING);
        selectLabel.setAlignmentX(JLabel.LEADING);
        centerPanel.add(selectLabel);

        centerPanel.add(Box.createVerticalStrut(20));
        
        centerPanel.add(cbPrinters);

        
    }

    public void actionPerformed(ActionEvent event)
    {
    	String sPrinter = (String) cbPrinters.getSelectedItem();
    	iData.setVariable("SELECTED_PRINTER", sPrinter);
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
