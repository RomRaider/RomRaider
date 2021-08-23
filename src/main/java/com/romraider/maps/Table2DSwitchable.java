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

import com.romraider.Settings.Endian;
import com.romraider.util.ByteUtil;

@SuppressWarnings("serial")
public class Table2DSwitchable extends Table2D {	
	private LinkedList<PresetEntry> presets = new LinkedList<PresetEntry>();
	private final List<PresetButton> buttonGroup = new ArrayList<PresetButton>();

	//Struct for saving Preset values
	private class PresetEntry {
		String name;
		LinkedList<Integer> data;
	}
	   
	public Table2DSwitchable() {
		super();
	}
	
	public void setValues(String name, String data) {
		PresetEntry entry = new PresetEntry();
		entry.data = new LinkedList<Integer>();
	
		data =  data.trim();
		String seperator = data.contains(",") ? "," : " ";
		
		for (String s : data.split(seperator)) {	
			Integer i = ByteUtil.parseUnsignedInt(s, 16);
			
			if (getStorageType() > 1 && getEndian() == Endian.LITTLE)
			{
				if(getStorageType() == 2) {
					i = Short.reverseBytes((short)(i&0xFFFF))&0xFFFF;
				}
					
				else if(getStorageType() == 4)
					i = Integer.reverseBytes(i);
				
			}
				
			entry.data.add(i);
		}
		
		entry.name = name;

		presets.add(entry);
	}

	@Override
	public void populateTable(byte[] input, int romRamOffset) throws ArrayIndexOutOfBoundsException, IndexOutOfBoundsException {
		super.populateTable(input, romRamOffset);
		
		JLabel axisLabel = getAxisLabel();
		
		if(getAxis().isStaticDataTable()) {
			axisLabel.setText(" " + axisLabel.getText() + " ");
			Font f = axisLabel.getFont();
			axisLabel.setFont(f.deriveFont(f.getStyle() | Font.BOLD));
		}
		
		JPanel radioPanel = new JPanel(new GridLayout(0, 1));
		
		// Add presets
		if(presets.size() > 0) {
			JLabel optionLabel = new JLabel(" Presets");
			
			Font f = optionLabel.getFont();
			optionLabel.setFont(f.deriveFont(f.getStyle() | Font.BOLD));		
			radioPanel.add(optionLabel);
		}
		
		//Setup button for each preset
		for (PresetEntry entry : presets) {
			PresetButton button = new PresetButton();

			button.setText(entry.name);
			button.setPresetData(entry.data);

			button.addActionListener(new PresetListener());
			
			buttonGroup.add(button);
			radioPanel.add(button);
		}

		add(radioPanel, BorderLayout.SOUTH);
		repaint();
	}

	//New values, check if any presets are active
	@Override
	public void repaint() {
		super.repaint();
	
		if (buttonGroup != null) {
			for (PresetButton button: buttonGroup) {
					button.checkIfActive();
			}
		}
	} 
	
	@Override
	public TableType getType() {
		return Table.TableType.TABLE_2D_SWITCHABLE;
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
				for (int i = 0; i < getDataSize(); i++) {
					if(getDataSize() == values.size()) {
						if ((int) data[i].getDataCell().getBinValue() != values.get(i)) {
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
			
			if(getDataSize() == button.values.size()) {
				for (int i = 0; i < getDataSize(); i++) {
					data[i].getDataCell().setBinValue(button.values.get(i));
				}
			}
			calcCellRanges();
			repaint();
		}
	}
}
