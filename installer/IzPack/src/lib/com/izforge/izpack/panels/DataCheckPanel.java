/*
 * IzPack - Copyright 2001-2007 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://developer.berlios.de/projects/izpack/
 *
 * Copyright 2002 Jan Blok
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
 *
 * This panel written by Hal Vaughan
 * http://thresholddigital.com
 * hal@thresholddigital.com
 * 
 * And updated by Fabrice Mirabile
 * miraodb@hotmail.com
 */

package com.izforge.izpack.panels;

import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

import javax.swing.BoxLayout;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JScrollPane;

import com.izforge.izpack.installer.InstallData;
import com.izforge.izpack.installer.InstallerFrame;
import com.izforge.izpack.installer.IzPanel;
import com.izforge.izpack.Pack;

/**
 * 
 * DataCheckPanel: Provide a lot of debugging information.  Print a simple header of our 
 * instance number and a line to separate output from other instances, then print all
 * the InstallData variables and list all the packs and selected packs.  I hope this will
 * be expanded by others to provide needed debugging information by those developing panels
 * for IzPack.
 * @author Hal Vaughan
 * @author Fabrice Mirabile
 */
public class DataCheckPanel extends IzPanel
{
	
	private static final long serialVersionUID = 3257848774955905587L;
	
	static int instanceCount = 0;
	
	protected int instanceNumber = 0;
	
	private InstallData iData;
	
	JEditorPane staticText;
			
	/**
	 * The constructor.
	 *
	 * @param parent The parent.
	 * @param id The installation data.
	 */
	public DataCheckPanel(InstallerFrame parent, InstallData id)
	{
		super(parent, id);
		
		iData = id;
		instanceNumber = instanceCount++;
		
		String sInfo = "Debugging data.  All InstallData variables and all packs (selected packs are marked).";
		BoxLayout bLayout = new BoxLayout(this, BoxLayout.Y_AXIS);
		setLayout (bLayout);
//		setLayout(new GridLayout(3,1));
		JLabel lInfo = new JLabel(sInfo);
		add(lInfo);
        staticText = new JEditorPane();
        staticText.setEditable(false);
        JScrollPane scrollText = new JScrollPane(staticText);
        add(new JLabel("  "));
        add(scrollText);
		
	}
	
	/**
	 * When the panel is made active, call the printDebugInfo method.
     * 
	 * @see com.izforge.izpack.installer.IzPanel#panelActivate()
     * @param none
     * @return void
	 */
	public void panelActivate()
	{
        printDebugInfo();
		return;
	}	
	
    /**
     * Get and return the list of pack names.
     * 
     * @param packList
     * @return String
     */
	private String getPackNames(List packList)
	{
		int i;
		String pStatus;
		String sOutput = "";
		Pack iPack;
		for (i = 0; i < packList.size(); i++)
		{
			iPack = (Pack) packList.get(i);
			if (iData.selectedPacks.indexOf(iPack) != -1)
				pStatus = "Selected";
			else
				pStatus = "Unselected";
			sOutput = sOutput + "\t" + i + ": " + iPack.name + " (" + pStatus + ")\n";
		}
		return sOutput;
	}

    /**
     * Print list of variables names and value, as well as the list
     * of packages and their status (selected or not).
     * 
     * @param none
     * @return void
     */
    private void printDebugInfo()
    {
        int i = 0;
        String sInfo = "InstallData Variables:\n";
        System.out.println("------------------------Data Check Panel Instance " + 
                instanceNumber + "------------------------");
        System.out.println("InstallData Variables:");
        Properties varList = iData.getVariables();
        String[] alphaName = new String[varList.size()];
        Enumeration varNames = varList.propertyNames();
        while (varNames.hasMoreElements())
            alphaName[i++] = (String) varNames.nextElement();
        java.util.Arrays.sort(alphaName);
        for (i = 0; i < alphaName.length; i++)
            sInfo = sInfo + "\tName: " + alphaName[i] + ", Value: " + varList.getProperty(alphaName[i]) + "\n";
        sInfo = sInfo + "\nAvailable Packs: \n" + getPackNames(iData.allPacks) + "\n";
        System.out.println(sInfo);
        staticText.setText(sInfo);
    }

	/**
	 * By nature, always true.
	 *
	 * @return True
	 */
	public boolean isValidated()
	{
		return true;
	}
}
