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
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.romraider.maps.PresetManager.PresetEntry;

public class PresetPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private final List<PresetButton> buttonGroup = new ArrayList<PresetButton>();
	private PresetManager manager;
	private TableView table;
	
	public PresetPanel(TableView t, PresetManager manager) {
		this.manager = manager;
		this.table = t;
	}
	
	public void populatePanel() {
		
		//If this is an axis within another table dont show the panel
		if(table.getTable() instanceof Table1D) {
			if(((Table1D) (table.getTable())).getAxisParent() != null) {
				return;
			}
		}
				
		JPanel radioPanel = new JPanel(new GridLayout(0, 1));
		boolean isSwitchTable = table.getTable() instanceof TableSwitch;
		
		// Add presets
		if(manager.getPresets().size() > 0) {
			String s = isSwitchTable ? "Switch States    ": "Presets";
			
			JLabel optionLabel = new JLabel(s);
			Font f = optionLabel.getFont();
			if (isSwitchTable)
				optionLabel.setFont(f.deriveFont(f.getStyle() | Font.BOLD, 15));
			else
				optionLabel.setFont(f.deriveFont(f.getStyle() | Font.BOLD));
			radioPanel.add(optionLabel);
		}
		
		//Setup button for each preset
		for (PresetEntry entry : manager.getPresets()) {
			PresetButton button = new PresetButton();

			button.setText(entry.name);
			button.setPresetData(entry.data);

			if (isSwitchTable) {
				Font f = button.getFont();
				button.setFont(f.deriveFont(f.getStyle(), 15));
			}
				
			button.addActionListener(new PresetListener());
			
			buttonGroup.add(button);
			radioPanel.add(button);
		}

		table.add(radioPanel, BorderLayout.SOUTH);
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
			table.getTable().calcValueRange();
			repaint();
		}
	}
}
