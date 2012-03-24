/*
* RomRaider Open-Source Tuning, Logging and Reflashing
* Copyright (C) 2006-2012 RomRaider.com
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
 
import static com.romraider.maps.RomChecksum.validateRomChecksum;
import static com.romraider.util.ByteUtil.indexOfBytes;
import static com.romraider.util.HexUtil.asBytes;
import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.INFORMATION_MESSAGE;
import static javax.swing.JOptionPane.WARNING_MESSAGE;
import static javax.swing.JOptionPane.showMessageDialog;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;

import com.romraider.Settings;

public class TableSwitch extends Table {
 
    private static final long serialVersionUID = -4887718305447362308L;
    private ButtonGroup buttonGroup = new ButtonGroup();
    private Map<String, byte[]> switchStates = new HashMap<String, byte[]>();
    private int dataSize = 0;
 
    public TableSwitch(Settings settings) {
        super(settings);
        storageType = 1;
        type = TABLE_SWITCH;
        locked = true;
        removeAll();
        setLayout(new BorderLayout());
    }
 
    public void setDataSize(int size) {
        if (dataSize == 0) dataSize = size;
    }
 
    public int getDataSize() {
        return dataSize;
    }

    public void populateTable(byte[] input) {
        JPanel radioPanel = new JPanel(new GridLayout(0, 1));
        radioPanel.add(new JLabel("  " + name));
        for (String stateName : switchStates.keySet()) {
            JRadioButton button = new JRadioButton(stateName);
            buttonGroup.add(button);
            radioPanel.add(button);
        }
        add(radioPanel, BorderLayout.CENTER);
       
        // Validate the ROM image checksums.
        // if the result is >0: position of failed checksum
        // if the result is  0: all the checksums matched
        // if the result is -1: all the checksums have been previously disabled
        if (super.getName().equalsIgnoreCase("Checksum Fix")) {
        	int result = validateRomChecksum(input, storageAddress, dataSize);
	        String message = String.format(
	        		"Checksum No. %d is invalid.%n" +
	        		"The ROM image may be corrupt.%n" +
	        		"Use of this ROM image is not advised!", result);
        	if (result > 0) {
    	        showMessageDialog(this,
    	        		message,
    	        		"ERROR - Checksums Failed",
    	        		WARNING_MESSAGE);
    	        setButtonsUnselected(buttonGroup);
        	}
        	else if (result == -1){
        		message = "All Checksums are disabled.";
    	        showMessageDialog(this,
    	        		message,
    	        		"Warning - Checksum Status",
    	        		INFORMATION_MESSAGE);
            	getButtonByText(buttonGroup, "on").setSelected(true);        	}
        	else {
        		getButtonByText(buttonGroup, "off").setSelected(true);
        		locked = false;
        	}
        	return;
        }

        // Validate XML switch definition data against the ROM data to select
        // the appropriate switch setting or throw an error if there is a
        // mismatch and disable this table's editing ability.
        if (!beforeRam) ramOffset = container.getRomID().getRamOffset();
        Map<String, Integer> sourceStatus = new HashMap<String, Integer>();
        for (String stateName : switchStates.keySet()) {
            byte[] sourceData = new byte[dataSize];
            System.arraycopy(
            		input,
            		storageAddress - ramOffset,
            		sourceData,
            		0,
            		dataSize);
            int compareResult = indexOfBytes(sourceData, getValues(stateName));
            if (compareResult == -1) {
                getButtonByText(buttonGroup, stateName).setSelected(false);
            }
            else {
                getButtonByText(buttonGroup, stateName).setSelected(true);
            }
            sourceStatus.put(stateName, compareResult);
        }

        for (String source : sourceStatus.keySet()) {
            if (sourceStatus.get(source) != -1) {
            	locked = false;
                break;
            }
        }

        if (locked) {
	        String mismatch = String.format("Table: %s%nTable editing has been disabled.%nDefinition file or ROM image may be corrupt.", super.getName());
	        showMessageDialog(this,
	                          mismatch,
	                          "ERROR - Data Mismatch",
	                          ERROR_MESSAGE);
            setButtonsUnselected(buttonGroup);
        }
    }
 
    public void setName(String name) {
        super.setName(name);
    }
 
    public int getType() {
        return TABLE_SWITCH;
    }
 
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
 
    public byte[] saveFile(byte[] input) {
    	if (!super.getName().equalsIgnoreCase("Checksum Fix")) {
	    	if (!locked) {
	    		JRadioButton selectedButton = getSelectedButton(buttonGroup);
	    		System.arraycopy(
	    				switchStates.get(selectedButton.getText()),
	    				0,
	    				input,
	    				storageAddress - ramOffset,
	    				dataSize);
	    	}
    	}
        return input;
    }
 
    public void setValues(String name, String input) {
    	switchStates.put(name, asBytes(input));
    }
 
    public byte[] getValues(String key) {
            return switchStates.get(key);
    }
 
    public Dimension getFrameSize() {
        int height = verticalOverhead + 75;
        int width = horizontalOverhead;
        if (height < minHeight) {
            height = minHeight;
        }
        int minWidth = isLiveDataSupported() ? minWidthOverlay : minWidthNoOverlay;
        if (width < minWidth) {
            width = minWidth;
        }
        return new Dimension(width, height);
    }
 
    public void colorize() {
    }
 
    public void cursorUp() {
    }
 
    public void cursorDown() {
    }
 
    public void cursorLeft() {
    }
 
    public void cursorRight() {
    }
 
    public void setAxisColor(Color color) {
    }
 
    public boolean isLiveDataSupported() {
        return false;
    }
 
    public boolean isButtonSelected() {
    	if (buttonGroup.getSelection() == null) {
    		return false;
    	}
    	else {
    		return true;
    	}
    }

    // returns the selected radio button in the specified group
    private static JRadioButton getSelectedButton(ButtonGroup group) {
        for (Enumeration<AbstractButton> e = group.getElements(); e.hasMoreElements(); ) {
            JRadioButton b = (JRadioButton)e.nextElement();
            if (b.getModel() == group.getSelection()) {
                return b;
            }
        }
        return null;
    }
 
    // Unselects & disables all radio buttons in the specified group
    private static void setButtonsUnselected(ButtonGroup group) {
        for (Enumeration<AbstractButton> e = group.getElements(); e.hasMoreElements(); ) {
            JRadioButton b = (JRadioButton)e.nextElement();
            b.setSelected(false);
            b.setEnabled(false);
        }
    }
 
    // returns the radio button based on its display text
    private static JRadioButton getButtonByText(ButtonGroup group, String text) {
        for (Enumeration<AbstractButton> e = group.getElements(); e.hasMoreElements(); ) {
            JRadioButton b = (JRadioButton)e.nextElement();
            if (b.getText().equalsIgnoreCase(text)) {
                return b;
            }
        }
        return null;
    }
}
