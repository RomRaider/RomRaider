/*
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2022 RomRaider.com
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

import java.util.LinkedList;

import com.romraider.Settings.Endian;
import com.romraider.util.ByteUtil;

public class PresetManager {
	private LinkedList<PresetEntry> presets = new LinkedList<PresetEntry>();
	private Table table;

	public PresetManager(Table t){
		table = t;
	}

	public class PresetEntry {
		int dataCellOffset = 0;
		String name;
		boolean isBitMask;
		LinkedList<Integer> data;
	}

	private int parseStringToInt(String s) {
		Integer i = ByteUtil.parseUnsignedInt(s, 16);

		if (table.getStorageType() > 1 && table.getEndian() == Endian.LITTLE)
		{
			if(table.getStorageType() == 2) {
				i = Short.reverseBytes((short)(i&0xFFFF))&0xFFFF;
			}
			else if(table.getStorageType() == 4) {
				i = Integer.reverseBytes(i);
			}
		}
		return i;
	}

	public boolean isPresetActive(PresetEntry entry) {
		if (entry.data != null && table.getDataSize() >= entry.data.size() + entry.dataCellOffset) {
			for (int i = 0; i < entry.data.size(); i++) {
					DataCell[] data = table.data;
					if ((int) data[i + entry.dataCellOffset].getBinValue() != entry.data.get(i)) {
						return false;
					}
			}
			return true;
		}
		return false;
	}

	public void applyPreset(PresetEntry entry) {
		if (entry.data != null && table.getDataSize() >= entry.data.size() + entry.dataCellOffset) {
			for (int i = 0; i < entry.data.size(); i++) {
				try {
					table.data[i + entry.dataCellOffset].setBinValue(entry.data.get(i));
				} catch (UserLevelException e) {
					TableView.showInvalidUserLevelPopup(e);
				}
			}
		}
	}

	public void clearPreset(PresetEntry entry) {
		if (entry.data != null && table.getDataSize() >= entry.data.size() + entry.dataCellOffset) {
			for (int i = 0; i < entry.data.size(); i++) {
				try {
					table.data[i+ entry.dataCellOffset].setBinValue(0);
				} catch (UserLevelException e) {
					TableView.showInvalidUserLevelPopup(e);
				}
			}
		}
	}

	private PresetEntry createPresetEntryValue(String name, String data, int dataCellOffset, boolean isBitMask) {
		PresetEntry entry = new PresetEntry();
		entry.name = name;
		entry.data = new LinkedList<Integer>();
		entry.dataCellOffset = dataCellOffset;
		entry.isBitMask = isBitMask;

		data =  data.trim();

		for (String s : data.split(data.contains(",") ? "," : " ")) {
			if(!s.isEmpty()){
				entry.data.add(parseStringToInt(s));
			}
		}

		return entry;
	}

	//Don't check for duplicate names, just add
	public void setPresetValues(String name, String data, int dataCellOffset, boolean isBitMask) {
		PresetEntry entry = createPresetEntryValue(name, data, dataCellOffset, isBitMask);

		presets.add(entry);
	}

	//Check for duplicate names, then replace if exist or add otherwise
	public void addPresetValue(String name, String data, int dataCellOffset, boolean isBitMask) {
		PresetEntry oldEntry = null;
		PresetEntry newEntry = createPresetEntryValue(name, data, dataCellOffset, isBitMask);

		for (int i = 0; i < presets.size(); i++) {
			if (presets.get(i).name.equalsIgnoreCase(newEntry.name)){
				oldEntry = presets.set(i, newEntry);
				break;
			}
		}

		if (oldEntry == null) {
			presets.add(newEntry);
		}
	}

	public LinkedList<PresetEntry> getPresets(){
		return this.presets;
	}
}
