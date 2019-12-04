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

package com.romraider.maps;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Enumeration;
import java.util.LinkedList;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import com.romraider.Settings;
import com.romraider.util.ByteUtil;
import com.romraider.xml.RomAttributeParser;

@SuppressWarnings("serial")
public class Table2DMaskedSwitchable extends Table2D {

	
	private int bitMask;
	private LinkedList<PresetEntry> defaultEntries = new LinkedList<PresetEntry>();
	private final ButtonGroup buttonGroup = new ButtonGroup();

	//Struct for saving Preset values
	class PresetEntry {
		String name;
		LinkedList<Integer> data;
	}
	   
	public Table2DMaskedSwitchable() {
		super();
	}

	public void setBitMask(int mask) {
		
		//Clamp mask to max size
		bitMask = (int) Math.min(mask, Math.pow(2,getStorageType()*8)-1);
		calcValueRange();
	}

	public int getBitMask() {
		return bitMask;
	}

	public void setStringMask(String stringMask) {	
		int mask = Integer.parseUnsignedInt(stringMask, 16); 		
		setBitMask(mask);
	}

	public void setPredefinedOption(String name, String data) {
		PresetEntry entry = new PresetEntry();
		entry.data = new LinkedList<Integer>();

		for (String s : data.split(",")) {
			entry.data.add(Integer.parseUnsignedInt(s, 16));
		}
		entry.name = name;

		defaultEntries.add(entry);
	}

	@Override
	public void populateTable(byte[] input, int romRamOffset) throws ArrayIndexOutOfBoundsException, IndexOutOfBoundsException {
		super.populateTable(input, romRamOffset);

		// temporarily remove lock
		boolean tempLock = locked;
		locked = false;
		
		// Saves the masked value in dataCell
		for (int i = 0; i < getDataSize(); i++) {
			// populate data cells
			if (storageType == Settings.STORAGE_TYPE_FLOAT) { // float storage type
				LOGGER.error("Float is not supported for Table2DMaskedSwitchable!");
				return;

			} else if (storageType == Settings.STORAGE_TYPE_MOVI20 || storageType == Settings.STORAGE_TYPE_MOVI20S) {
				LOGGER.error("MOVI20(S) is not supported for Table2DMaskedSwitchable!");
				return;

			} else {	
				double binValue = RomAttributeParser.parseByteValueMasked(input, endian, getStorageAddress() + i * storageType - ramOffset, storageType, signed, bitMask);
				data[i].setBinValue(binValue);
			}

			// show locked cell
			if (tempLock) {
				data[i].setForeground(Color.GRAY);
			}
		}
		
		JLabel axisLabel = getAxisLabel();
		
		if(getAxis().isStaticDataTable()) {
			axisLabel.setText(" " + axisLabel.getText() + " ");
			Font f = axisLabel.getFont();
			axisLabel.setFont(f.deriveFont(f.getStyle() | Font.BOLD));
		}
		
		JPanel radioPanel = new JPanel(new GridLayout(0, 1));
		
		// Add presets
		if(defaultEntries.size() > 0) {
			JLabel optionLabel = new JLabel(" Predefined Options");
			
			Font f = optionLabel.getFont();
			optionLabel.setFont(f.deriveFont(f.getStyle() | Font.BOLD));		
			radioPanel.add(optionLabel);
		}
		
		//Setup radio button for each preset
		for (PresetEntry entry : defaultEntries) {
			PresetJRadioButton button = new PresetJRadioButton();

			button.setText(entry.name);
			button.setPresetData(entry.data);
			button.checkIfActive();
			button.addActionListener(new PresetListener());
			
			buttonGroup.add(button);
			radioPanel.add(button);
		}

		add(radioPanel, BorderLayout.SOUTH);

		// reset locked status
		locked = tempLock;
		
		calcValueRange();
		calcCellRanges();

		repaint();
	}

	//New values, check if any presets are active
	@Override
	public void repaint() {
		super.repaint();
	
		if (buttonGroup != null) {
			Enumeration<AbstractButton> buttons = buttonGroup.getElements();
			
			if (buttons != null) {
				buttonGroup.clearSelection();
				
				while(buttons.hasMoreElements()) {
					PresetJRadioButton button = (PresetJRadioButton)buttons.nextElement();
					button.checkIfActive();
				}
			}
		}
	} 
	
	@Override
	protected void calcValueRange() {
		if (getStorageType() != Settings.STORAGE_TYPE_FLOAT) {
			if (!isSignedData()) {				
					maxAllowedBin =(int)(Math.pow(2,ByteUtil.lengthOfMask(bitMask)) - 1);
					minAllowedBin = 0.0;
			}
		}
	}

	@Override
	public byte[] saveFile(byte[] binData) {
		if (userLevel <= getSettings().getUserLevel() && (userLevel < 5 || getSettings().isSaveDebugTables())) {

	        binData = getAxis().saveFile(binData);
			
			for (int i = 0; i < data.length; i++) {				
				byte[] output = null;
				
				if (this.isStaticDataTable() && storageType > 0) {
					LOGGER.warn("Static data table: " + this.getName() + ", storageType: " + storageType);
				}
				if (storageType != Settings.STORAGE_TYPE_FLOAT) {

					if (!this.isStaticDataTable() && storageType > 0) {
						// Shift left again
						int tempData = (int) data[i].getBinValue() << ByteUtil.firstOneOfMask(bitMask);

						output = RomAttributeParser.parseIntegerValue(tempData, endian, storageType);
					}

					int byteLength = storageType;
					
					for (int z = 0; z < byteLength; z++) { // insert into file							
						// Trim mask depending on bit
						bitMask &= 0xFF << (8 * z);

						// Delete old bits
						binData[i * byteLength + z + getStorageAddress() - ramOffset] &= ~bitMask;

						// Overwrite
						binData[i * byteLength + z + getStorageAddress() - ramOffset] |= output[z];
					}
				}
			}
		}
		return binData;
	}


	@Override
	public TableType getType() {
		return Table.TableType.TABLE_2D_MASKED_SWITCHABLE;
	}

	@Override
	public boolean equals(Object other) {
		try {
			if (null == other) {
				return false;
			}

			if (other == this) {
				return true;
			}

			if (!(other instanceof TableBitwiseSwitch)) {
				return false;
			}

			Table2DMaskedSwitchable otherTable = (Table2DMaskedSwitchable) other;

			if ((null == this.getName() && null == otherTable.getName())
					|| (this.getName().isEmpty() && otherTable.getName().isEmpty())) {
				;// Skip name compare if name is null or empty.
			} else if (!this.getName().equalsIgnoreCase(otherTable.getName())) {
				return false;
			}

			if (this.getDataSize() != otherTable.getDataSize()) {
				return false;
			}

			if (this.bitMask == otherTable.bitMask) {
				return true;
			}
			return false;
		} catch (Exception ex) {
			// TODO: Log Exception.
			return false;
		}
	}

	/*
	 * Custom JRadioButton and Actionlistener
	 */
	class PresetJRadioButton extends JRadioButton{
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
			PresetJRadioButton button = (PresetJRadioButton)event.getSource();
			
			if(getDataSize() == button.values.size()) {
				for (int i = 0; i < getDataSize(); i++) {
					data[i].setBinValue(button.values.get(i));
				}
			}
			calcCellRanges();
			repaint();
		}
	}
}
