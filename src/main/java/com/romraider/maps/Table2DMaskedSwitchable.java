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
import java.util.LinkedList;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import com.romraider.Settings;
import com.romraider.util.ByteUtil;
import com.romraider.xml.RomAttributeParser;

public class Table2DMaskedSwitchable extends Table {
	private static final long serialVersionUID = 1L;

	int bitMask;
	private LinkedList<DefaultEntry> defaultEntries = new LinkedList<DefaultEntry>();
	private final ButtonGroup buttonGroup = new ButtonGroup();

	class DefaultEntry {
		String name;
		LinkedList<Integer> data;
	}

	public Table2DMaskedSwitchable() {
		super();
		super.setDataSize(1);
	}

	public void setBitMask(int mask) {
		bitMask = mask;
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
		DefaultEntry entry = new DefaultEntry();
		System.out.println(super.getDataSize());

		entry.data = new LinkedList<Integer>();

		for (String s : data.split(",")) {
			entry.data.add(Integer.parseUnsignedInt(s, 16));
		}
		entry.name = name;

		defaultEntries.add(entry);
	}

	@Override
	public void populateTable(byte[] input, int romRamOffset) throws ArrayIndexOutOfBoundsException, IndexOutOfBoundsException {
		centerLayout.setRows(1);
		centerLayout.setColumns(this.getDataSize());
		super.populateTable(input, romRamOffset);

		// temporarily remove lock
		boolean tempLock = locked;
		locked = false;


		// Saves the masked value in dataCell
		for (int i = 0; i < getDataSize(); i++) {

			if (isSignedData()) {
				LOGGER.error("Single Cell Table only works on unsigned data!");
				return;
			}

			// populate data cells
			if (storageType == Settings.STORAGE_TYPE_FLOAT) { // float storage type
				LOGGER.error("Float is not supported for Table2DMaskedSwitchable!");
				return;

			} else if (storageType == Settings.STORAGE_TYPE_MOVI20 || storageType == Settings.STORAGE_TYPE_MOVI20S) {
				LOGGER.error("MOVI20(S) is not supported for Table2DMaskedSwitchable!");
				return;

			} else {
				data[i].setBinValue(RomAttributeParser.parseByteValueMasked(input, endian,
						getStorageAddress() + i * storageType - ramOffset, storageType, signed, bitMask));
			}

			// System.out.println(dataValue);

			// show locked cell
			if (tempLock) {
				data[i].setForeground(Color.GRAY);
			}

		}
		
		// Setup Top Text
		JLabel descriptionArea = new JLabel(" " + getName() +" | " + description);
		descriptionArea.setBorder(BorderFactory.createEmptyBorder(5, 0, 5,0));
		Font f = descriptionArea.getFont();
		descriptionArea.setFont(f.deriveFont(f.getStyle() | Font.BOLD));
		descriptionArea.setOpaque(false);
		add(descriptionArea, BorderLayout.NORTH);
		
		JPanel radioPanel = new JPanel(new GridLayout(0, 1));
		// Add default values
		if(defaultEntries.size() > 0) {
			JLabel optionLabel = new JLabel(" Predefined Options");
			
			f = optionLabel.getFont();
			optionLabel.setFont(f.deriveFont(f.getStyle() | Font.BOLD));		
			radioPanel.add(optionLabel);
			//radioPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
	
			// System.out.println(defaultEntries.size());
		}
		
		for (DefaultEntry entry : defaultEntries) {
			JRadioButton button = new JRadioButton(entry.name);

			// Check if the radio button is current selected
			boolean found = true;
			for (int i = 0; i < getDataSize(); i++) {
				if ((int) data[i].getBinValue() != entry.data.get(i)) {
					found = false;
					break;
				}
			}
			button.setSelected(found);

			button.addActionListener(new ActionListener() {
				LinkedList<Integer> values = entry.data;

				@Override
				public void actionPerformed(ActionEvent event) {
					for (int i = 0; i < getDataSize(); i++) {
						data[i].setBinValue(values.get(i));
					}

					calcCellRanges();
					repaint();
				}
			});

			buttonGroup.add(button);
			radioPanel.add(button);
		}

		add(radioPanel, BorderLayout.SOUTH);

		// reset locked status
		locked = tempLock;
		calcCellRanges();

		repaint();
	}

	@Override
	protected void calcValueRange() {
		if (getStorageType() != Settings.STORAGE_TYPE_FLOAT) {
			if (!isSignedData()) {
				maxAllowedBin = bitMask >> ByteUtil.firstOneOfMask(bitMask);
				minAllowedBin = 0.0;
			}
		}
	}

	@Override
	public byte[] saveFile(byte[] binData) {
		if (userLevel <= getSettings().getUserLevel() && (userLevel < 5 || getSettings().isSaveDebugTables())) {

			for (int i = 0; i < data.length; i++) {
				// determine output byte values

				byte[] output = null;
				if (this.isStaticDataTable() && storageType > 0) {
					LOGGER.warn("Static data table: " + this.getName() + ", storageType: " + storageType);
				}
				if (storageType != Settings.STORAGE_TYPE_FLOAT) {

					// Convert byte values
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
	public void setName(String name) {
		super.setName(name);
	}

	@Override
	public TableType getType() {
		return Table.TableType.TABLE_2D;
	}

	@Override
	public void setDescription(String description) {
		super.setDescription(description);

		return;
		/*
		 * JTextArea descriptionArea = new JTextArea(description);
		 * descriptionArea.setOpaque(false); descriptionArea.setEditable(false);
		 * descriptionArea.setWrapStyleWord(true); descriptionArea.setLineWrap(true);
		 * descriptionArea.setMargin(new Insets(0, 5, 5, 5));
		 * 
		 * add(descriptionArea, BorderLayout.NORTH);
		 */
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

	@Override
	public void cursorUp() {
		// TODO Auto-generated method stub

	}

	@Override
	public void cursorDown() {
		// TODO Auto-generated method stub

	}

	@Override
	public void cursorLeft() {
		// TODO Auto-generated method stub

	}

	@Override
	public void cursorRight() {
		// TODO Auto-generated method stub

	}

	@Override
	public void shiftCursorUp() {
		// TODO Auto-generated method stub

	}

	@Override
	public void shiftCursorDown() {
		// TODO Auto-generated method stub

	}

	@Override
	public void shiftCursorLeft() {
		// TODO Auto-generated method stub

	}

	@Override
	public void shiftCursorRight() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isLiveDataSupported() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isButtonSelected() {
		// TODO Auto-generated method stub
		return false;
	}

}
