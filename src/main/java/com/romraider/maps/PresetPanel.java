/*
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2021 RomRaider.com
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

package com.romraider.maps;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;

import com.romraider.maps.PresetManager.PresetEntry;

public class PresetPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private final List<PresetButton> buttonGroup = new ArrayList<PresetButton>();
	private PresetManager manager;
	private TableView table;
	private final int minimumWidth = 500;
	
	public PresetPanel(TableView t, PresetManager manager) {
		this.manager = manager;
		this.table = t;
		setBorder(new EmptyBorder(2, 2, 2, 2));
		setLayout(new GridBagLayout());
	}
	
	public void populatePanel() {		
		//If this is an axis within another table dont show the panel
		if(table.getTable() instanceof Table1D) {
			if(((Table1D) (table.getTable())).getAxisParent() != null) {
				return;
			}
		}
				
		JPanel radioPanel = new JPanel(new GridLayout(0, 1));		
		radioPanel.setBorder(new EmptyBorder(0, 2, 7, 0));
		boolean isSwitchTable = table.getTable() instanceof TableSwitch;

		JLabel optionLabel = new JLabel();			
		String s = isSwitchTable ? table.getName(): "Presets";
		optionLabel.setText(s);
		optionLabel.setPreferredSize(new Dimension(minimumWidth, 20));		
		
		Font f = optionLabel.getFont();
		if (isSwitchTable)
			optionLabel.setFont(f.deriveFont(f.getStyle() | Font.BOLD, 15));
		else
			optionLabel.setFont(f.deriveFont(f.getStyle() | Font.BOLD));
	
		radioPanel.add(optionLabel);
		
		//Setup button for each preset
		for (PresetEntry entry : manager.getPresets()) {
			PresetButton button = new PresetButton();

			button.setText(entry.name);
			button.setPresetData(entry.data);

			if (isSwitchTable) {
				Font x = button.getFont();
				button.setFont(x.deriveFont(x.getStyle(), 15));
			}
				
			button.addActionListener(new PresetListener());			
			buttonGroup.add(button);
			radioPanel.add(button);
		}
		
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.anchor = GridBagConstraints.FIRST_LINE_START;
		c.gridx = 0;       
		c.gridy = 0;	
		add(radioPanel, c);
		
		//Add description if its a switch
		String stringDesc = table.getTable().getDescription();
		
		if(isSwitchTable && stringDesc != null && stringDesc.trim().length() > 0) {
			JTextArea desc = new JTextArea(stringDesc);
			desc.setLineWrap(true);
			desc.setWrapStyleWord(true);
			desc.setOpaque(false);
	
			Font x = optionLabel.getFont();
			desc.setFont(x.deriveFont(x.getStyle(), 12));
				
			c.gridx = 0;        
			c.gridy = 1;			
			c.anchor = GridBagConstraints.LAST_LINE_START; 
			add(desc, c);			
		}
		
		//Move it to the bottom and left
		//For sure better way to do this...
		JPanel temp = new JPanel();
		temp.setLayout(new BorderLayout());
		temp.add(this, BorderLayout.WEST);
		
		table.add(temp, BorderLayout.SOUTH);
		repaint();
	}	
	
	@Override
	public void repaint() {	
		if (buttonGroup != null) {
			for (PresetButton button: buttonGroup) {
					button.checkIfActive();
			}
		}
		
		super.repaint();
	} 
		
	/*
	 * Custom Button and Actionlistener
	 */
	class PresetButton extends JCheckBox{
		private static final long serialVersionUID = 1L;
		LinkedList<Integer> values; //Pointer to PresetEntry.data
		
		public void setPresetData(LinkedList<Integer> list) {
			values = list;
		}
		
		public void checkIfActive() {
			// Check if the radio button is current selected
			boolean found = true;
			
			if (values != null) {
				for (int i = 0; i < table.getTable().getDataSize(); i++) {
					if(table.getTable().getDataSize() == values.size()) {
						DataCell[] data = table.getTable().data;
						if ((int) data[i].getBinValue() != values.get(i)) {
							found = false;
							break;
						}
					}
				}				
				setSelected(found);
			}	
		}
	}
	
	class PresetListener implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent event) {
			PresetButton button = (PresetButton)event.getSource();
			
			if(table.getTable().getDataSize() == button.values.size()) {
				for (int i = 0; i < table.getTable().getDataSize(); i++) {
					try {
						table.getTable().data[i].setBinValue(button.values.get(i));
					} catch (UserLevelException e) {
						e.printStackTrace();
					}
				}
			}
			
			table.getTable().calcCellRanges();
			repaint();
		}
	}
}
