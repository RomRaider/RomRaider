/*
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2023 RomRaider.com
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

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.romraider.Settings.Endian;
import com.romraider.xml.RomAttributeParser;

public class VDFConversionLayer extends ConversionLayer {
	// protected static final ResourceBundle rb = new ResourceUtil().getBundle(
	// VDFConversionLayer.class.getName());
	private static final Logger LOGGER = Logger.getLogger(VDFConversionLayer.class);
	Node romIDNode;
	Node romNode;

	final private static byte TYPE_SWITCH = 0x1; // 0x230 Size
	final private static byte TYPE_SCALAR = 0x2; // 0x279 Size
	final private static byte TYPE_3D_TABLE = 0x4; // 0x2BC Size, Short Unsigned?
	final private static byte TYPE_3D_TABLE_BYTE_SIGNED = 0x6; // 0x2BC Size
	final private static byte TYPE_3D_TABLE_SHORT_SIGNED = 0x7; // 0x2BC Size
	final private static byte TYPE_3D_TABLE_FLOAT = 0xE; // 0x2BC Size
	final private static byte TYPE_3D_TABLE_BYTE_UNSIGNED = 0x3; // 0x2BC Size
	final private static byte TYPE_DTC = 0x9; // 0x234

	private static int START_FILE_OFFSET = 0x1ED;
	private static int START_OFFSET = 0x25E0;
	private static int START_OFFSET_TTFNAME = 0x20E;
	private static int SWITCHES_CATEGORY_START_OFFSET = 0x222;
	private static int CONSTANT_CATEGORY_START_OFFSET = 0x60A;
	private static int TABLE_CATEGORY_START_OFFSET = 0x9F2;
	private static int DIAGNOSTICS_CATEGORY_START_OFFSET = 0xDDA;

	private static int START_CATEGORY_COUNT = 0x11C2;

	ArrayList<String> allAxis = new ArrayList<String>();
	private LinkedList<String> switchCategories = new LinkedList<String>();
	private LinkedList<String> constantCategories = new LinkedList<String>();
	private LinkedList<String> tableCategories = new LinkedList<String>();
	private LinkedList<String> diagnosticCategories = new LinkedList<String>();

	private ArrayList<Integer> switchCategoriesCount = new ArrayList<Integer>();
	private ArrayList<Integer> constantCategoriesCount = new ArrayList<Integer>();
	private ArrayList<Integer> diagnosticCategoriesCount = new ArrayList<Integer>();

	int countSwitches = 0;
	int countScalars = 0;
	int countDTCs = 0;
	int lastTableAddress = 0;

	private LinkedList<Element> createdTables = new LinkedList<Element>();

	@Override
	public String getDefinitionPickerInfo() {
		return "";
		// return rb.getString("LOADINGWARNING");
	}

	@Override
	public String getRegexFileNameFilter() {
		return "^.*(vdf|jdf)";
	}

	@Override
	public Document convertToDocumentTree(File f) throws Exception {
		// Read file into byte array
		byte[] fileData = new byte[(int) f.length()];
		DataInputStream dis = new DataInputStream(new FileInputStream(f));
		dis.readFully(fileData);
		dis.close();

		// Create new RR document
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = factory.newDocumentBuilder();
		Document doc = docBuilder.newDocument();
		FileInputStream fileStream = null;

		try {
			fileStream = new FileInputStream(f);
			doc = parseVDFFile(f, fileData, doc);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (fileStream != null)
					fileStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return doc;
	}

	private Node createStartDocument(byte[] data, Document doc) throws IOException {
		Node roms = doc.createElement("roms");
		romNode = doc.createElement("rom");
		romIDNode = doc.createElement("romid");

		Node idAddress = doc.createElement("internalidaddress");
		Node idString = doc.createElement("internalidstring");
		idString.setTextContent("force");
		idAddress.setTextContent("-1");

		Node offset = doc.createElement("offset");
		offset.setTextContent("0x" + Integer.toHexString(
				(int) RomAttributeParser.parseByteValue(data, Endian.LITTLE, START_FILE_OFFSET, 2, false)));

		// Read the definition name
		byte[] nameArray = Arrays.copyOfRange(data, 0x8, 0x8 + 0xF);
		String definitionName = new String(nameArray).trim();
		Node xmlID = doc.createElement("xmlid");
		xmlID.setTextContent(definitionName);

		romIDNode.appendChild(idAddress);
		romIDNode.appendChild(idString);
		romIDNode.appendChild(offset);
		romIDNode.appendChild(xmlID);

		romNode.appendChild(romIDNode);
		roms.appendChild(romNode);
		doc.appendChild(roms);

		return romNode;
	}

	private String getCurrentCategory(LinkedList<String> categories, ArrayList<Integer> categoryCount, int index) {

		for (int i = 0; i < categoryCount.size(); i++) {
			if (index < categoryCount.get(i)) {
				return categories.get(i);
			}
		}
		return "";

	}

	private Node parseSwitch(Document doc, byte[] data) {
		long adr = RomAttributeParser.parseByteValue(data, Endian.LITTLE, 54, 0x4, false);
		// long mask = RomAttributeParser.parseByteValue(data, Endian.LITTLE, 58, 0x1,
		// false);
		// long offValue = RomAttributeParser.parseByteValue(data, Endian.LITTLE, 59,
		// 0x1, false);
		String name = new String(data, 4, 0x32).trim();

		if (name.isEmpty())
			return null;

		Element table = doc.createElement("table");

		table.setAttribute("name", name);
		table.setAttribute("category",
				"Switches//" + getCurrentCategory(switchCategories, switchCategoriesCount, countSwitches));
		table.setAttribute("storageaddress", "0x" + Long.toHexString(adr));
		table.setAttribute("endian", "big");
		table.setAttribute("storagetype", "uint8");
		table.setAttribute("sizey", "1");
		table.setAttribute("type", "Switch");
		// table.setAttribute("mask", Long.toHexString(mask));

		Element enable = doc.createElement("state");
		enable.setAttribute("name", "On");
		enable.setAttribute("data", "1");

		Element disable = doc.createElement("state");
		disable.setAttribute("name", "0ff");
		disable.setAttribute("data", "0");

		table.appendChild(enable);
		table.appendChild(disable);

		countSwitches++;
		return table;

	}

	private String constructExpression(float scalar, float offset) {
		String scalarString = (scalar > 0.99 && scalar < 1.01) ? "" : (" * " + scalar);
		String offsetString = offset == 0.0 ? "" : (" + " + offset);

		return "x" + scalarString + offsetString;
	}

	private Node parseScalar(Document doc, byte[] data) {
		String name = new String(data, 4, 0x32).trim();
		String unit = new String(data, 52, 0xF).trim();

		if (name.isEmpty())
			return null;

		long adr = RomAttributeParser.parseByteValue(data, Endian.LITTLE, 81, 0x4, false);
		// float exponential = RomAttributeParser.byteToFloat(Arrays.copyOfRange(data,
		// 69, 73), Endian.LITTLE, Endian.LITTLE);
		float scalar = RomAttributeParser.byteToFloat(Arrays.copyOfRange(data, 73, 77), Endian.LITTLE, Endian.LITTLE);
		float offset = RomAttributeParser.byteToFloat(Arrays.copyOfRange(data, 77, 81), Endian.LITTLE, Endian.LITTLE);
		String expression = constructExpression(scalar, offset);

		int type = (int) RomAttributeParser.parseByteValue(data, Endian.LITTLE, 2, 1, false);

		String datatype = "";
		switch (type) {
		case 0x0:
			datatype = "uint8";
			break;
		case 0xF:
			datatype = "float";
			break;
		case 0x1:
		case 0x2:
		case 0x4:
			datatype = "uint16";
			break;
		case 11:
			datatype = "uint32";
			break;
		default:
			System.out.println("Received unknown data scalar type " + type);
			return null;
		}
		Element table = doc.createElement("table");

		table.setAttribute("name", name);
		table.setAttribute("category",
				"Scalars//" + getCurrentCategory(constantCategories, constantCategoriesCount, countScalars));
		table.setAttribute("storagetype", datatype);
		table.setAttribute("storageaddress", "0x" + Long.toHexString(adr));
		table.setAttribute("endian", "big");
		table.setAttribute("sizey", "1");
		table.setAttribute("type", "1D");

		Element scaling = doc.createElement("scaling");
		scaling.setAttribute("expression", expression);
		scaling.setAttribute("units", unit);
		scaling.setAttribute("format", "0.##");
		table.appendChild(scaling);

		countScalars++;
		return table;
	}

	private Node parseDTC(Document doc, byte[] data) {
		long adr = RomAttributeParser.parseByteValue(data, Endian.LITTLE, 0x36, 0x4, false);
		String name = new String(data, 4, 0x32).trim();

		if (name.isEmpty())
			return null;

		// For easy of life: Cut out the P-Code and put it in the front so its sorted
		// correctly
		Pattern pattern = Pattern.compile("(\\(\\w?.*\\))");
		Matcher matcher = pattern.matcher(name);
		if (matcher.find()) {
			name = matcher.group(1) + " " + name.replace(matcher.group(1), "");
			name = name.trim();
		}

		Element table = doc.createElement("table");

		table.setAttribute("name", name);
		table.setAttribute("category",
				"DTC//" + getCurrentCategory(diagnosticCategories, diagnosticCategoriesCount, countDTCs));
		table.setAttribute("storageaddress", "0x" + Long.toHexString(adr));
		table.setAttribute("endian", "big");
		table.setAttribute("storagetype", "uint8");
		table.setAttribute("sizey", "1");
		table.setAttribute("type", "Switch");

		// How is the following encoded in the file? Is it?
		Element state0 = doc.createElement("state");
		state0.setAttribute("name", "Not reported / No MIL");
		state0.setAttribute("data", "0");

		Element state1 = doc.createElement("state");
		state1.setAttribute("name", "Type A / No MIL");
		state1.setAttribute("data", "1");

		Element state2 = doc.createElement("state");
		state2.setAttribute("name", "Type B / No MIL");
		state2.setAttribute("data", "2");

		Element state3 = doc.createElement("state");
		state3.setAttribute("name", "Type C / No MIL");
		state3.setAttribute("data", "3");

		Element state4 = doc.createElement("state");
		state4.setAttribute("name", "Not reported / MIL");
		state4.setAttribute("data", "4");

		Element state5 = doc.createElement("state");
		state5.setAttribute("name", "Type A / MIL");
		state5.setAttribute("data", "5");

		Element state6 = doc.createElement("state");
		state6.setAttribute("name", "Type B / MIL");
		state6.setAttribute("data", "6");

		Element state7 = doc.createElement("state");
		state7.setAttribute("name", "Type C / MIL");
		state7.setAttribute("data", "7");

		table.appendChild(state0);
		table.appendChild(state1);
		table.appendChild(state2);
		table.appendChild(state3);
		table.appendChild(state4);
		table.appendChild(state5);
		table.appendChild(state6);
		table.appendChild(state7);

		countDTCs++;
		return table;
	}

	private void fillAxisData(Document doc, Node parentTable, long size, int axisRef) {
		String[] splitValues = { " " };
		if (axisRef > 0) {
			// If the TTF failed loading add empty cells
			if (allAxis.size() == 0) {
				splitValues = new String[(int) size];
				Collections.nCopies((int) size, String.valueOf(" ")).toArray(splitValues);
			} else {
				splitValues = allAxis.get(axisRef - 1).split(",");
			}
		}

		for (int i = 0; i < size; i++) {
			Element data = doc.createElement("data");
			data.setTextContent(splitValues[i]);
			parentTable.appendChild(data);
		}
	}

	private Node parse3DTable(Document doc, byte[] data, int type) {
		String name = new String(data, 0x40, 0x3C);
		name = name.trim();

		if (name.isEmpty())
			return null;

		String nameAxis1 = new String(data, 0x7C, 0x28);
		nameAxis1 = nameAxis1.trim();
		String nameAxis2 = new String(data, 0xA4, 0x12);
		nameAxis2 = nameAxis2.trim();

		String datatype = "";

		// Whats uint32_t?
		switch (type) {
		case TYPE_3D_TABLE_BYTE_SIGNED:
			datatype = "int8";
			break;
		case TYPE_3D_TABLE_FLOAT:
			datatype = "float";
			break;
		case TYPE_3D_TABLE_BYTE_UNSIGNED:
			datatype = "uint8";
			break;
		case TYPE_3D_TABLE_SHORT_SIGNED:
			datatype = "int16";
			break;
		default:
			datatype = "uint16";
		}
		
		long adr = RomAttributeParser.parseByteValue(data, Endian.LITTLE, 6, 0x4, false);
		long sizeX = RomAttributeParser.parseByteValue(data, Endian.LITTLE, 10, 0x1, false);
		long sizeY = RomAttributeParser.parseByteValue(data, Endian.LITTLE, 12, 0x1, false);

		// float exponential =
		// RomAttributeParser.byteToFloat(Arrays.copyOfRange(data,14, 18),
		// Endian.LITTLE, Endian.LITTLE);
		float scalar = RomAttributeParser.byteToFloat(Arrays.copyOfRange(data, 18, 22), Endian.LITTLE, Endian.LITTLE);
		float offset = RomAttributeParser.byteToFloat(Arrays.copyOfRange(data, 22, 26), Endian.LITTLE, Endian.LITTLE);
		String expression = constructExpression(scalar, offset);

		boolean swapXY = RomAttributeParser.parseByteValue(data, Endian.LITTLE, 62, 0x1, false) > 0;
		int axisRefX = (int) RomAttributeParser.parseByteValue(data, Endian.LITTLE, 0xB8, 0x2, false);
		int axisRefY = (int) RomAttributeParser.parseByteValue(data, Endian.LITTLE, 0xBA, 0x2, false);
		int skipCells = (int) RomAttributeParser.parseByteValue(data, Endian.LITTLE, 0xC8, 0x2, false);
		skipCells = skipCells / (datatype.contains("int8") ? 1 : 2); // I assume the offset is in bytes?
		
		Element table = doc.createElement("table");

		table.setAttribute("name", name);
		table.setAttribute("storagetype", datatype);
		table.setAttribute("storageaddress", "0x" + Long.toHexString(adr));
		table.setAttribute("endian", "big");
		table.setAttribute("swapxy", swapXY ? "true" : "false");
		
		boolean is2DTable = sizeY == 1 || sizeX == 1;
		long table2DSize = sizeY > sizeX ? sizeY : sizeX;
		
		if(is2DTable)
		{
			table.setAttribute("sizey", Long.toString(table2DSize));
			table.setAttribute("type", "2D");
		}
		else
		{
			table.setAttribute("sizey", Long.toString(sizeY));
			table.setAttribute("sizex", Long.toString(sizeX));	
			table.setAttribute("type", "3D");
		}

		if (skipCells > 0)
			table.setAttribute("skipCells", Integer.toString(skipCells));

		Element scaling = doc.createElement("scaling");
		scaling.setAttribute("expression", expression);
		scaling.setAttribute("units", is2DTable ? (sizeX == 1 ? nameAxis1 : nameAxis2) : "");
		scaling.setAttribute("format", "0.##");
		table.appendChild(scaling);
		
		if(!is2DTable)
		{
			Element tableX = doc.createElement("table");
			tableX.setAttribute("name", nameAxis1);
			tableX.setAttribute("sizex", Long.toString(sizeX));
			tableX.setAttribute("type", "Static X Axis");
			fillAxisData(doc, tableX, sizeX, axisRefX);
			table.appendChild(tableX);
		}
		
		// If its a 2D table pick the larger size, otherwise use the normal X
		long YSizeToLoad =  is2DTable ? table2DSize : sizeY;
		
		Element tableY = doc.createElement("table");
		tableY.setAttribute("name", nameAxis2);
		tableY.setAttribute("sizey", Long.toString(YSizeToLoad));
		tableY.setAttribute("type", "Static Y Axis");
		fillAxisData(doc, tableY, YSizeToLoad, axisRefY);
		table.appendChild(tableY);

		createdTables.add(table);
		return table;
	}

	private void createTables(byte[] data, Document doc, Node romNode) throws IOException {
		boolean dataToRead = true;
		int currentAdr = START_OFFSET;

		while (dataToRead) {
			Node newTable = null;
			int type = data[currentAdr];
			switch (type) {
			case TYPE_SWITCH:
				newTable = parseSwitch(doc, Arrays.copyOfRange(data, currentAdr, currentAdr + 0x230));
				currentAdr += 0x230;
				break;
			case TYPE_SCALAR:
				newTable = parseScalar(doc, Arrays.copyOfRange(data, currentAdr, currentAdr + 0x279));
				currentAdr += 0x279;
				break;
			case TYPE_3D_TABLE:
				newTable = parse3DTable(doc, Arrays.copyOfRange(data, currentAdr, currentAdr + 0x2BC), type);
				currentAdr += 0x2BC;
				break;
			case TYPE_3D_TABLE_BYTE_SIGNED:
				newTable = parse3DTable(doc, Arrays.copyOfRange(data, currentAdr, currentAdr + 0x2BC), type);
				currentAdr += 0x2BC;
				break;
			case TYPE_3D_TABLE_SHORT_SIGNED:
				newTable = parse3DTable(doc, Arrays.copyOfRange(data, currentAdr, currentAdr + 0x2BC), type);
				currentAdr += 0x2BC;
				break;
			case TYPE_3D_TABLE_FLOAT:
				newTable = parse3DTable(doc, Arrays.copyOfRange(data, currentAdr, currentAdr + 0x2BC), type);
				currentAdr += 0x2BC;
				break;
			case TYPE_3D_TABLE_BYTE_UNSIGNED:
				newTable = parse3DTable(doc, Arrays.copyOfRange(data, currentAdr, currentAdr + 0x2BC), type);
				currentAdr += 0x2BC;
				break;
			case TYPE_DTC:
				newTable = parseDTC(doc, Arrays.copyOfRange(data, currentAdr, currentAdr + 0x234));
				currentAdr += 0x234;
				break;
			default:
				dataToRead = false;
			}

			if (newTable != null) {
				romNode.appendChild(newTable);
			}
		}
		lastTableAddress = currentAdr;
	}

	private void parsePackedStrings(byte[] data, int start, LinkedList<String> output) {
		int currentOffset = start;
		while (true) {
			String newCategory = new String(data, currentOffset, 0x28).trim();
			currentOffset += 0x28;

			if (!newCategory.isEmpty()) {
				output.add(newCategory);
			} else {
				break;
			}
		}
	}

	private void parseCategories(byte[] data) {
		parsePackedStrings(data, SWITCHES_CATEGORY_START_OFFSET, switchCategories);
		parsePackedStrings(data, CONSTANT_CATEGORY_START_OFFSET, constantCategories);
		parsePackedStrings(data, TABLE_CATEGORY_START_OFFSET, tableCategories);
		parsePackedStrings(data, DIAGNOSTICS_CATEGORY_START_OFFSET, diagnosticCategories);

		for (int i = 0; i < switchCategories.size(); i++) {
			switchCategoriesCount.add((int) RomAttributeParser.parseByteValue(data, Endian.LITTLE,
					START_CATEGORY_COUNT + i * 2, 0x2, false));
			if (i > 0) {
				switchCategoriesCount.set(i, switchCategoriesCount.get(i) + switchCategoriesCount.get(i - 1));
			}
		}

		for (int i = 0; i < constantCategories.size(); i++) {
			constantCategoriesCount.add((int) RomAttributeParser.parseByteValue(data, Endian.LITTLE,
					START_CATEGORY_COUNT + 0x32 + i * 2, 0x2, false));
			if (i > 0) {
				constantCategoriesCount.set(i, constantCategoriesCount.get(i) + constantCategoriesCount.get(i - 1));
			}
		}

		for (int i = 0; i < diagnosticCategories.size(); i++) {
			diagnosticCategoriesCount.add((int) RomAttributeParser.parseByteValue(data, Endian.LITTLE,
					START_CATEGORY_COUNT + 0x32 * 2 + i * 2, 0x2, false));
			if (i > 0) {
				diagnosticCategoriesCount.set(i,
						diagnosticCategoriesCount.get(i) + diagnosticCategoriesCount.get(i - 1));
			}
		}
	}

	private void updateTableCategories(byte[] data) {
		// Find the last empty block of 32 times 0x20 from the end of the file
		int currentOffset = data.length - 1;
		while (true) {
			boolean allEmpty = true;
			for (int i = 0; i < 32; i++) {
				if (data[currentOffset - i] != 0x20) {
					allEmpty = false;
					break;
				}
			}

			if (allEmpty)
				break;
			else
				currentOffset -= 32;
		}

		// Go forward until we find the start of the mapping table
		while (data[currentOffset] == 0x20) {
			currentOffset++;
		}

		int tableCount = 0;
		for (Element n : createdTables) {
			String cat = tableCategories.get(data[currentOffset + tableCount * 2] - 1);
			n.setAttribute("category", "Tables//" + cat);
			tableCount++;
		}

	}

	private void loadAxisData(File f, byte[] data) {
		String ttfName = new String(data, START_OFFSET_TTFNAME, 0x14).trim() + ".tff";
		File ttfFile = new File(f.getParent(), ttfName);

		LOGGER.info("Trying to load TTF file " + ttfFile.getAbsolutePath());

		byte[] fileData = new byte[(int) ttfFile.length()];
		DataInputStream dis = null;

		try {
			dis = new DataInputStream(new FileInputStream(ttfFile));
			dis.readFully(fileData);
			dis.close();

			// Length is 0x200
			for (int i = 0; i < fileData.length; i += 0x200) {
				allAxis.add(new String(fileData, i, 0x200).trim());
			}

		} catch (IOException e) {
			LOGGER.warn("Failed to load TTF file!");
		}
	}

	private Document parseVDFFile(File f, byte[] data, Document doc) throws IOException {
		romNode = createStartDocument(data, doc);
		loadAxisData(f, data);
		parseCategories(data);
		createTables(data, doc, romNode);
		updateTableCategories(data);

		return doc;
	}
}