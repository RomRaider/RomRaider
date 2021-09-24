/*
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2020 RomRaider.com
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
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.Map.Entry;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import com.romraider.util.ResourceUtil;

import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.showMessageDialog;

public class TableBitwiseSwitchView extends TableView {

	private static final long serialVersionUID = -4887718305447362308L;
    private static final ResourceBundle rb = new ResourceUtil().getBundle(
            TableBitwiseSwitch.class.getName());
	private ArrayList<JCheckBox> checkboxes;
	private TableBitwiseSwitch table;


	public TableBitwiseSwitchView(TableBitwiseSwitch table) {
		super(table);
		removeAll();
		setLayout(new BorderLayout());
		checkboxes = new ArrayList<JCheckBox>();
	}

	@Override
	public void populateTableVisual()
			throws ArrayIndexOutOfBoundsException, IndexOutOfBoundsException {
		int maxBitPosition = ((table.getDataSize() * 8) - 1);
		JPanel radioPanel = new JPanel(new GridLayout(0, 1));
		radioPanel.add(new JLabel("  " + getName()));

		for (Entry<String, Integer> entry : TableBitwiseSwitch.sortByValue(table.getControlBits()).entrySet()) {
			if (entry.getValue() > maxBitPosition) {
				String mismatch = MessageFormat.format(
						rb.getString("OUTOFRANGE"), super.getName());
				showMessageDialog(this, mismatch,
				        rb.getString("DEFERROR"), ERROR_MESSAGE);
				break;
			} else {
				JCheckBox cb = new JCheckBox(entry.getKey());
				cb.setSelected(table.getBitsArray()[entry.getValue()]);
				checkboxes.add(cb);
				radioPanel.add(cb);
			}
		}
		add(radioPanel, BorderLayout.CENTER);
		
		JTextArea descriptionArea = new JTextArea(table.getDescription());
		descriptionArea.setOpaque(false);
		descriptionArea.setEditable(false);
		descriptionArea.setWrapStyleWord(true);
		descriptionArea.setLineWrap(true);
		descriptionArea.setMargin(new Insets(0, 5, 5, 5));

		add(descriptionArea, BorderLayout.SOUTH);
	}

	public ArrayList<JCheckBox> getCheckboxes() {
		return checkboxes;
	}
	
	@Override
	public void setName(String name) {
		super.setName(name);
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
	public void drawTable() {
		return; // Do nothing.
	}

	@Override
	public void updateTableLabel() {
		return; // Do nothing.
	}
}
