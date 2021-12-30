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

package com.romraider.xml.ConversionLayer;

import java.io.File;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.romraider.util.ByteUtil;

class BMWConversionRomNodeManager{
	int offsetAddress = 0;
	
	Document doc;
	Element romNode;
	
	Element lastTable;
	int lastStorageAddress;
	int maxStorageAddress;
	
	//Variables to find the best ID settings
	int bestIDFitAddress;
	String bestIDFitData = "";
	int bestIDFitAddressTemp;
	String bestIDFitDataTemp = "";
	int lastPresetCount = 0;
	
	BMWConversionRomNodeManager(int offsetAddress, Document doc, Node root){
		this.doc = doc;
		this.offsetAddress = offsetAddress;
		this.romNode = doc.createElement("rom");			
		root.appendChild(romNode);
	}
	
	public Element getRomNode() {
		return romNode;
	}
		
	public Element createTable(String name, String category, String storageType, String endian,
			int storageAddress, int byteCount, byte[] mask) {
		
		//This handles the correct rom splitting
		if(offsetAddress == 0 || storageAddress >= offsetAddress) {	
			
			if(!bestIDFitDataTemp.equals("") && lastPresetCount == 1) {
				bestIDFitData = bestIDFitDataTemp;
				bestIDFitAddress = bestIDFitAddressTemp;
			}				
			
			lastPresetCount = 0;
						
			int divisor = 1;
			if(storageType.contains("int16")) divisor = 2;
			else if (storageType.contains("int32")) divisor = 4;
			
			int sizey = byteCount / divisor;
			
			Element table = doc.createElement("table");
			table.setAttribute("name", name);
			table.setAttribute("category", category);
			table.setAttribute("storagetype", storageType);
			table.setAttribute("storageaddress", "0x" + 
					Integer.toHexString(storageAddress - offsetAddress));
			table.setAttribute("endian", endian);
			table.setAttribute("sizey", Integer.toString(sizey));
			table.setAttribute("type", "1D");
			table.setAttribute("mask", Integer.toHexString(ByteUtil.asUnsignedInt(mask[0])));
			
			romNode.appendChild(table);
			lastTable = table;
			
			lastStorageAddress = storageAddress - offsetAddress;							
			if(lastStorageAddress > maxStorageAddress) maxStorageAddress = lastStorageAddress;
										
			return table;
		}
		else {
			return null;
		}
	}
	
	public Element addPreset(String data, String name, Node table) {
		if(offsetAddress == 0 || this.lastTable == table) {	
			Element statePSW1 = doc.createElement("state");      		      			
			statePSW1.setAttribute("data", data);
			statePSW1.setAttribute("name", name);
			lastTable.appendChild(statePSW1);
			
			data = data.replace(" ", "");
			
			
			//Try to find the best fitting preset that only appears once
			//and has the longest memory footprint for best identification
			if(lastPresetCount >= 1) {
				bestIDFitDataTemp = "";
			}									
			else if(data.length() >= 2 && bestIDFitData.length() <= data.length() &&
					!data.replace("0", "").equals("") && !data.replace("F", "").equals("")){					
				bestIDFitDataTemp = data;
				bestIDFitAddressTemp = lastStorageAddress;
			}
			
			lastPresetCount++;
			
			return statePSW1;
		}
		else {
			return null;
		}	
	}
	
	public void calculateRomID(File f, String make) {
    	Element romIDNode = doc.createElement("romid");
		
    	Node idAddress = doc.createElement("internalidaddress");
    	idAddress.setTextContent("0x" + Integer.toHexString(bestIDFitAddress));

    	Node idString = doc.createElement("internalidstring");
    	idString.setTextContent("0x" + bestIDFitData.replace(" ", ""));
    	
    	//This can be used to force a definition file for a bin
    	//idString.setTextContent("force");
    	//idAddress.setTextContent("-1");
    	
    	Node ramoffset = doc.createElement("noramoffset");
    	
    	//Set filesize based on largest address and round up to a power of 2
    	Node fileSize = doc.createElement("filesize");
    	String fileS = ((int)Math.pow(2, 32 - Integer.numberOfLeadingZeros(lastStorageAddress - 1)) + "b");
    	fileSize.setTextContent(fileS);
    	
    	Node makeN = doc.createElement("make");
    	makeN.setTextContent(make);
    	
    	Node model = doc.createElement("model");
    	model.setTextContent(f.getParentFile().getName());
    	
    	Node ecuID = doc.createElement("ecuid");
    	String[] nameSplit = f.getName().split("\\.");
    	ecuID.setTextContent(nameSplit[1]);
    	
    	Node subModel = doc.createElement("submodel");
    	subModel.setTextContent(f.getName());
    	
    	romIDNode.appendChild(makeN);
    	romIDNode.appendChild(ecuID);
    	romIDNode.appendChild(model);
    	romIDNode.appendChild(subModel);
    	romIDNode.appendChild(ramoffset);
    	romIDNode.appendChild(fileSize);
    	romIDNode.appendChild(idAddress);
    	romIDNode.appendChild(idString);
    	romIDNode.appendChild(doc.createElement("year"));
    	romIDNode.appendChild(doc.createElement("market"));
    	romIDNode.appendChild(doc.createElement("transmission"));
    	romIDNode.appendChild(doc.createElement("xmlid"));
    	romNode.appendChild(romIDNode); 	         	   	
	}
}