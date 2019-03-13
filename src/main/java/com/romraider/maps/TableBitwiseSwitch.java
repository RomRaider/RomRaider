/*
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2018 RomRaider.com
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
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import com.romraider.Settings;
import com.romraider.maps.Table.TableType;
import com.romraider.util.ByteUtil;

import static com.romraider.util.ByteUtil.isBitSet;
import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.showMessageDialog;

public class TableBitwiseSwitch extends Table {

    private static final long serialVersionUID = -4887718305447362308L;
    private final Map<String, Integer> controlBits = new HashMap<String, Integer>();
    private ArrayList<JCheckBox> checkboxes;
    
    public TableBitwiseSwitch() {
        super();
        storageType = 1;
        removeAll();
        setLayout(new BorderLayout());
        checkboxes = new ArrayList<JCheckBox>();
    }

    @Override
    public void populateTable(byte[] input, int romRamOffset) throws ArrayIndexOutOfBoundsException, IndexOutOfBoundsException  {
        byte currentValue = input[storageAddress];
    	JPanel radioPanel = new JPanel(new GridLayout(0, 1));
        radioPanel.add(new JLabel("  " + getName()));
        for (Entry<String, Integer> entry: controlBits.entrySet()) {
        	if (entry.getValue() > 7) {
        		String mismatch = String.format("Table: %s%nDefinition file specified an out of range bit switch! Only values 0-7 are valid!", super.getName());
        		showMessageDialog(this,
                        mismatch,
                        "ERROR - Incorrect definition",
                        ERROR_MESSAGE);
        		continue;
			}
            JCheckBox cb = new JCheckBox(entry.getKey());
            cb.setSelected(isBitSet(currentValue, entry.getValue()));
            checkboxes.add(cb);
            radioPanel.add(cb);
        }
        add(radioPanel, BorderLayout.CENTER);
    }

    @Override
    public void setName(String name) {
        super.setName(name);
    }

    @Override
    public TableType getType() {
        return Table.TableType.SWITCH;
    }

    @Override
    public void setDescription(String description) {
        super.setDescription(description);
        JTextArea descriptionArea = new JTextArea(description);
        descriptionArea.setOpaque(false);
        descriptionArea.setEditable(false);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setLineWrap(true);
        descriptionArea.setMargin(new Insets(0,5,5,5));

        add(descriptionArea, BorderLayout.SOUTH);
    }

    @Override
    public byte[] saveFile(byte[] input) {
    	// get original values
    	boolean[] bools = ByteUtil.byteToBoolArr(input[storageAddress]);
    	
    	// set to new state from checkboxes, in reverse order
    	for (Entry<String, Integer> entry: controlBits.entrySet()) {
    		JCheckBox cb = getButtonByText(entry.getKey());
    		bools[7 -entry.getValue()] = cb.isSelected();
    	}
    	
    	input[storageAddress] = ByteUtil.booleanArrayToBit(bools);
        return input;
    }
    
    public void setValues(String name, String input) {
        controlBits.put(name, Integer.parseInt(input));
    }

    public int getValues(String key) {
        return controlBits.get(key);
    }

    public Map<String, Integer> getSwitchStates() {
        return this.controlBits;
    }

    @Override
    public void cursorUp() {
    }

    @Override
    public void cursorDown() {
    }

    @Override
    public void cursorLeft() {
    }

    @Override
    public void cursorRight() {
    }

	@Override
	public void shiftCursorUp() {
	}

	@Override
	public void shiftCursorDown() {
	}

	@Override
	public void shiftCursorLeft() {
	}

	@Override
	public void shiftCursorRight() {
	}

    @Override
    public boolean isLiveDataSupported() {
        return false;
    }

    @Override
    public boolean equals(Object other) {
    	try {
            if(null == other) {
                return false;
            }

            if(other == this) {
                return true;
            }

            if(!(other instanceof TableBitwiseSwitch)) {
                return false;
            }

            TableBitwiseSwitch otherTable = (TableBitwiseSwitch) other;

            if( (null == this.getName() && null == otherTable.getName())
                    || (this.getName().isEmpty() && otherTable.getName().isEmpty()) ) {
                ;// Skip name compare if name is null or empty.
            } else if(!this.getName().equalsIgnoreCase(otherTable.getName())) {
                return false;
            }

            if(this.getDataSize() != otherTable.getDataSize()) {
                return false;
            }

            if(this.getSwitchStates().keySet().equals(otherTable.getSwitchStates().keySet())) {
                return true;
            }
            return false;
    	} catch(Exception ex) {
            // TODO: Log Exception.
            return false;
        }
    }

    @Override
    public void populateCompareValues(Table compareTable) {
        return; // Do nothing.
    }

    @Override
    public void calcCellRanges() {
        return; // Do nothing.
    }

    @Override
    public void drawTable()
    {
        return; // Do nothing.
    }

    @Override
    public void updateTableLabel() {
        return; // Do nothing.
    }

    @Override
    public void setCurrentScale(Scale curScale) {
        return; // Do nothing.
    }

	@Override
	public boolean isButtonSelected() {
		// TODO Auto-generated method stub
		return false;
	}
	
    private JCheckBox getButtonByText(String text) {
    	for (JCheckBox cb : checkboxes) {
    		if (cb.getText().equalsIgnoreCase(text)) {
    			return cb;
    		}
    	}
        return null;
    }
}
