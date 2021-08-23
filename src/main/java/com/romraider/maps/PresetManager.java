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

import java.util.LinkedList;

import com.romraider.Settings.Endian;
import com.romraider.util.ByteUtil;

public class PresetManager {
	private LinkedList<PresetEntry> presets = new LinkedList<PresetEntry>();
	private Table table;
	
	public PresetManager(Table t){
		table = t;
	}
	
	//Struct for saving Preset values
	public class PresetEntry {
		String name;
		LinkedList<Integer> data;
	}
	
	public void setValues(String name, String data) {
		PresetEntry entry = new PresetEntry();
		entry.data = new LinkedList<Integer>();
	
		data =  data.trim();
		String seperator = data.contains(",") ? "," : " ";
		
		for (String s : data.split(seperator)) {	
			Integer i = ByteUtil.parseUnsignedInt(s, 16);
			
			if (table.getStorageType() > 1 && table.getEndian() == Endian.LITTLE)
			{
				if(table.getStorageType() == 2) {
					i = Short.reverseBytes((short)(i&0xFFFF))&0xFFFF;
				}
					
				else if(table.getStorageType() == 4)
					i = Integer.reverseBytes(i);
				
			}
				
			entry.data.add(i);
		}
		
		entry.name = name;
		presets.add(entry);
	}
	
	public LinkedList<PresetEntry> getPresets(){
		return this.presets;
	}
}
