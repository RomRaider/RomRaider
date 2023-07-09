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

import static com.romraider.editor.ecu.ECUEditorManager.getECUEditor;
import static com.romraider.swing.LookAndFeelManager.initLookAndFeel;
import static com.romraider.util.LogManager.initDebugLogging;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.romraider.Settings;
import com.romraider.editor.ecu.ECUEditor;
import com.romraider.editor.ecu.OpenImageWorker;
import com.romraider.util.ByteUtil;
import com.romraider.util.ResourceUtil;
import com.romraider.util.SettingsManager;

public class BMWCodingConversionLayer extends ConversionLayer {
	protected static final ResourceBundle rb = new ResourceUtil().getBundle(BMWCodingConversionLayer.class.getName());

	private int splitAddress = 0;
	boolean guessChecksums = false;

	HashMap<Integer, String> fswMap;
	HashMap<Integer, String> pswMap;
	HashMap<Integer, String> aswMap;
	HashMap<Integer, String> csvMap;
	HashMap<String, String> transMap;

	BMWConversionRomNodeManager[] romManagers;
	ByteBuffer dataBuffer;
	int dataIndex;

	String currentCategory = "";
	int currentFSW;
	Node currentTable;

	String memoryLayout = "uint8";
	String endian = "little";

	int unusedCounter = 0;

	// Naming stays german, because the file is also german
	static final int TYPE_DATEINAME = 0x0000; // Filename
	static final int SGID_CODIERINDEX = 0x0001; // Codingindex
	static final int SGID_HARDWARENUMMER = 0x0002; // Hardware number
	static final int SGID_SWNUMMER = 0x0003; // Software number
	static final int SPEICHERORG = 0x0004; // Memory layout
	static final int ANLIEFERZUSTAND = 0x0005; // State of delivery when new (?)
	static final int CODIERDATENBLOCK = 0x0006; // Coding data block (like a group)
	static final int HERSTELLERDATENBLOCK = 0x0007; // Manufacturer data block (like a group)
	static final int RESERVIERTDATENBLOCK = 0x0008; // Reserved data (like a group)
	static final int UNBELEGT1 = 0x0009; // Unused (Not actually sometimes)
	static final int UNBELEGT2 = 0x000A; // Unused (Not actually sometimes)
	static final int KENNUNG_K = 0x000B; // ?
	static final int KENNUNG_D = 0x000C; // ?
	static final int KENNUNG_X = 0x000D; // ?
	static final int KENNUNG_ALL = 0x000E; // ?
	static final int PARZUWEISUNG_PSW2 = 0x000F; // Coding data preset information
	static final int PARZUWEISUNG_PSW1 = 0x0010; // Coding data preset information
	static final int PARZUWEISUNG_DIR = 0x0011; // ?
	static final int PARZUWEISUNG_FSW = 0x0012; // Coding data memory storage information

	// Gets called when ROM is opened directly
	public BMWCodingConversionLayer() {
		this(0, false);
	}

	// Gets called from the toolbar with more options
	public BMWCodingConversionLayer(int splitAddress, boolean guessChecksums) {
		this.splitAddress = splitAddress;
		this.guessChecksums = guessChecksums;
	}

	@Override
	public String getRegexFileNameFilter() {
		return "^.*\\.C\\d\\d$";
	}

	// Reads a string in an array until zero byte
	private static String readString(byte[] input, int offset) {
		StringBuilder s = new StringBuilder();

		while ((char) (input[offset]) != 0) {
			s.append((char) input[offset]);
			offset++;
		}

		return s.toString();
	}

	// Reads the optional translation file from NCS Dummy
	private HashMap<String, String> readTranslationFile(File transF) {
		HashMap<String, String> map = new HashMap<String, String>();

		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(transF));
			String line;

			while ((line = br.readLine()) != null) {
				final String[] values = line.split(",");
				if (values.length == 2) {
					map.put(values[0], values[1]);
				}
			}

		} catch (final FileNotFoundException e) {
			return null;
		} catch (final IOException e) {
			return null;
		} finally {
			try {
				if (br != null) {
					br.close();
				}
			} catch (final IOException e) {
				e.printStackTrace();
			}
		}

		return map;
	}

	private static HashMap<Integer, String> createMapFromNCSDict(File f) {
		// Parse name dictionary
		HashMap<Integer, String> map = new HashMap<Integer, String>();
		byte[] fswInput;

		try {
			fswInput = ECUEditor.readFile(f);
		} catch (IOException e) {
			return null;
		}

		ByteBuffer fswBuffer = ByteBuffer.wrap(fswInput);
		fswBuffer = fswBuffer.order(ByteOrder.LITTLE_ENDIAN);

		for (int i = getStartOfFile(fswBuffer); i < fswInput.length;) {
			int oldIndex = i;

			int length = fswBuffer.get(i);
			int frameType = fswBuffer.getShort(i + 1);
			i += 3;

			switch (frameType) {
			// Name
			case 0x0000:
				break;
			// SWT Entry
			case 0x0001:
				int keyId = fswBuffer.getShort(i);
				String name = readString(fswInput, i + 2);
				map.put(keyId, name);
				break;
			}

			i = oldIndex + length + 4;
		}

		return map;
	}

	private static HashMap<Integer, String> createMapFromCVT(File f, HashMap<Integer, String> aswMap) {
		// Parse name dictionary
		HashMap<Integer, String> map = new HashMap<Integer, String>();
		byte[] fswInput;

		try {
			fswInput = ECUEditor.readFile(f);
		} catch (IOException e) {
			return null;
		}

		ByteBuffer cvtBuffer = ByteBuffer.wrap(fswInput);
		cvtBuffer = cvtBuffer.order(ByteOrder.LITTLE_ENDIAN);

		/*
		 * 0000 - DATEINAME - S - NAME 0001 - GRUPPE - {S} - NAME 0002 - INDIVID - {S} -
		 * NAME 0003 - AUFTRAGSAUSDRUCK - A - AUFTRAGSAUSDRUCK 0004 - FSW_PSW - WW -
		 * FSWINDEX,PSWINDEX 0005 - FSW - W - FSWINDEX
		 */

		String currentOption = "";
		for (int i = getStartOfFile(cvtBuffer); i < fswInput.length;) {
			int oldIndex = i;

			int length = cvtBuffer.get(i);
			int frameType = cvtBuffer.getShort(i + 1);
			i += 3;

			switch (frameType) {
			// Name
			case 0x0000:
				break;
			// Option/Auftrag
			case 0x0003:
				int lengthOption = cvtBuffer.get(i);
				i++;
				String optionCode = "";

				for (int j = 0; j < lengthOption;) {
					if (cvtBuffer.get(j + i) == 0x53) {
						int keyId = cvtBuffer.getShort(j + i + 1);
						currentOption = aswMap.get(keyId);
						optionCode = optionCode + currentOption;
						j += 3;
					} else {
						optionCode += (char) cvtBuffer.get(j + i);
						j++;
					}
				}

				currentOption = optionCode;
				break;

			// Link FSW/PSW combination to option
			case 0x0004:
				int fsw = cvtBuffer.getShort(i);
				int psw = cvtBuffer.getShort(i + 2);
				map.put(fsw << 16 | psw, currentOption);
				break;
			}

			i = oldIndex + length + 4;
		}

		return map;
	}

	// Look for 0xFFFF in the file to skip the header
	private static int getStartOfFile(ByteBuffer b) {
		for (int i = 0; i < b.capacity(); i++) {
			int value = b.getShort(i);
			if (value == -1) {
				return i + 2;
			}
		}

		return -1;
	}

	public Document convertToDocumentTree(File f) throws SAXException {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = null;

		try {
			builder = dbf.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}

		Document doc = builder.newDocument();

		// Check first if naming conversion files are there
		File[] listOfFiles = f.getParentFile().listFiles();

		File fswF = new File("");
		File pswF = new File("");
		File aswF = new File("");
		File csvF = new File(f.getParent(), f.getParentFile().getName() + "CVT.000");

		for (int i = 0; i < listOfFiles.length; i++) {
			File fList = listOfFiles[i];
			if (fList.isFile()) {
				if (fList.getName().matches("(?i)SWTFSW\\d\\d\\.dat"))
					fswF = fList;
				else if (fList.getName().matches("(?i)SWTPSW\\d\\d\\.dat"))
					pswF = fList;
				else if (fList.getName().matches("(?i)SWTASW\\d\\d\\.dat"))
					aswF = fList;
			}
		}

		if (!fswF.exists()) {
			throw new SAXException(rb.getString("MISSINGFILE") + "SWTFSW.dat. " + rb.getString("NCSHINT"));
		}
		if (!pswF.exists()) {
			throw new SAXException(rb.getString("MISSINGFILE") + "SWTPSW.dat. " + rb.getString("NCSHINT"));
		}
		if (!aswF.exists()) {
			throw new SAXException(rb.getString("MISSINGFILE") + "SWTASW.dat. " + rb.getString("NCSHINT"));
		}
		if (!csvF.exists()) {
			throw new SAXException(rb.getString("MISSINGFILE") + "CVT.000. " + rb.getString("NCSHINT"));
		}

		fswMap = createMapFromNCSDict(fswF);
		pswMap = createMapFromNCSDict(pswF);
		aswMap = createMapFromNCSDict(aswF);
		csvMap = createMapFromCVT(csvF, aswMap);

		// Optional translation file that has to be in the DATEN folder
		// Created from NCSDummy developers
		File transF = new File(f, "../../Translations.csv");
		if (transF.exists())
			transMap = readTranslationFile(transF);

		byte[] input;

		try {
			input = ECUEditor.readFile(f);
		} catch (IOException e) {
			throw new SAXException(rb.getString("ERRORFILE") + f);
		}

		dataBuffer = ByteBuffer.wrap(input);
		dataBuffer = dataBuffer.order(ByteOrder.LITTLE_ENDIAN);

		Node roms = doc.createElement("roms");
		doc.appendChild(roms);

		// Create one manager if no splitting
		// Create two otherwise
		if (splitAddress == 0)
			romManagers = new BMWConversionRomNodeManager[] { new BMWConversionRomNodeManager(0, doc, roms) };
		else
			romManagers = new BMWConversionRomNodeManager[] { new BMWConversionRomNodeManager(0, doc, roms),
					new BMWConversionRomNodeManager(splitAddress, doc, roms) };

		/*
		 * 0000 - DATEINAME - S - NAME 0001 - SGID_CODIERINDEX - B(B) - WERT,WERT2 0002
		 * - SGID_HARDWARENUMMER - S(S) - WERT,WERT2 0003 - SGID_SWNUMMER - S(S) -
		 * WERT,WERT2 0004 - SPEICHERORG - SS - STRUKTUR,TYP 0005 - ANLIEFERZUSTAND -
		 * (B) - WERT 0006 - CODIERDATENBLOCK - {L}LWS -
		 * BLOCKNR,WORTADR,BYTEADR,BEZEICHNUNG 0007 - HERSTELLERDATENBLOCK - {L}LWS -
		 * BLOCKNR,WORTADR,BYTEADR,BEZEICHNUNG 0008 - RESERVIERTDATENBLOCK - {L}LWS -
		 * BLOCKNR,WORTADR,BYTEADR,BEZEICHNUNG 0009 - UNBELEGT1 - {L}LW{B}(B) -
		 * BLOCKNR,WORTADR,BYTEADR,INDEX,MASKE 000A - UNBELEGT2 - (B) - WERT 000B -
		 * KENNUNG_K - SS(S) - IDENT,WERT1,WERTN 000C - KENNUNG_D - WW(WW) -
		 * HEXWERT1,HEXWERT2,HEXWERTN1,HEXWERTN2 000D - KENNUNG_X - WW(WW) -
		 * HEXWERT1,HEXWERT2,HEXWERTN1,HEXWERTN2 000E - KENNUNG_ALL - SW(W) -
		 * KENNUNG,HEXWERT1,HEXWERTN 000F - PARZUWEISUNG_PSW2 - (B) - DATUM 0010 -
		 * PARZUWEISUNG_PSW1 - W(B) - PSW,DATUM 0011 - PARZUWEISUNG_DIR -
		 * {L}LWW{B}(B)(A)B - BLOCKNR,WORTADR,BYTEADR,FSW,INDEX,MASKE,OPERATION,EINHEIT
		 * 0012 - PARZUWEISUNG_FSW - {L}LWW{B}(B){B}{B} -
		 * BLOCKNR,WORTADR,BYTEADR,FSW,INDEX,MASKE,EINHEIT,INDIVID
		 */

		// Look for 0xFFFF in the file to skip the header
		dataIndex = getStartOfFile(dataBuffer);

		if (dataIndex == 0) {
			throw new SAXException(rb.getString("ERRORFILESTART") + f.toString());
		}

		while (dataIndex < input.length) {
			int oldIndex = dataIndex;

			int length = 0xFFFF & dataBuffer.get(dataIndex);
			int frameType = 0xFFFF & dataBuffer.getShort(dataIndex + 1);
			dataIndex += 2;

			switch (frameType) {
			case SPEICHERORG:
				parseMemoryOrg();
				break;
			case TYPE_DATEINAME:
				// String fileNameInFile = readString(input, i+1);
				break;
			// "Unused" Data, which often contains hidden data
			case UNBELEGT1:
				parseUnused();
				break;
			// Not sure what to do with this
			case UNBELEGT2:
				break;
			// Coding Block (=category)
			// Fall-Through!
			case CODIERDATENBLOCK:
			case HERSTELLERDATENBLOCK:
			case RESERVIERTDATENBLOCK:
				parseGroup();
				break;
			case PARZUWEISUNG_DIR:
				parseFSWValues(false);
				break;
			case PARZUWEISUNG_FSW:
				parseFSWValues(false);
				break;

			case PARZUWEISUNG_PSW1:
				parsePresetValues(true);
				break;
			case PARZUWEISUNG_PSW2:
				parsePresetValues(false);
				break;
			}

			// No matter what the index points to, always go by length
			// Skip frameType, checksum and go to next
			dataIndex = oldIndex + length + 4;
		}

		for (BMWConversionRomNodeManager man : romManagers) {
			man.calculateRomID(f, "BMW");
		}
				
		return doc;
	}

	private void parseMemoryOrg() {
		String layout = readString(dataBuffer.array(), dataIndex + 1);

		/*
		 * if(layout.equalsIgnoreCase("byte")) memoryLayout = "uint8"; else
		 * if(layout.equalsIgnoreCase("wordmsb")) { memoryLayout = "uint16"; endian =
		 * "big"; } else if(layout.equalsIgnoreCase("wordlsb")) { memoryLayout =
		 * "uint16"; endian = "little"; }
		 */
		dataIndex += layout.length() + 2;
		// String blockType = readString(input, i); //? What does it do?
		// isBlock = blockType.equals("BLOCK");
	}

	private void parseUnused() {
		short blockU = dataBuffer.getShort(dataIndex);
		dataIndex += 2;

		if (blockU != 0) {
			// int blockNumber = dataBuffer.getInt(i); //Whats it for?
			dataIndex += 4;
		}

		int storageAddressU = dataBuffer.getInt(dataIndex);
		int byteCountU = dataBuffer.getShort(dataIndex + 4);

		byte indexU = dataBuffer.get(dataIndex + 6);
		dataIndex += indexU;

		int maskLengthU = 0xFFFF & dataBuffer.getShort(dataIndex + 7);
		byte[] maskU = new byte[maskLengthU];

		for (int j = 0; j < maskLengthU; j++) {
			maskU[j] = dataBuffer.get(dataIndex + 9 + j);
		}

		dataIndex = dataIndex + 9 + maskLengthU;

		// Create actual node in rom
		for (BMWConversionRomNodeManager man : romManagers) {
			Element table = man.createTable("UNUSED_" + unusedCounter, currentCategory, memoryLayout, endian,
					storageAddressU, byteCountU, maskU);

			if (table != null)
				currentTable = table;
		}

		unusedCounter++;
	}

	private void parseGroup() {
		short blockR = dataBuffer.getShort(dataIndex);
		dataIndex += 2;

		if (blockR != 0) {
			// int blockNumber = dataBuffer.getInt(i); //Whats it for?
			dataIndex += 4;
		}

		// Not used in our case, since only used as category
		// int storageAddressBlock = dataBuffer.getInt(i);
		// int byteCountBlock = dataBuffer.getShort(i+4);

		currentCategory = readString(dataBuffer.array(), dataIndex + 6);

		// Add optional translation
		if (transMap != null && transMap.containsKey(currentCategory)) {
			currentCategory = currentCategory + " | " + transMap.get(currentCategory);
		}
	}

	private void parseFSWValues(boolean isDIR) {
		short blockD = dataBuffer.getShort(dataIndex);
		dataIndex += 2;

		if (blockD != 0) {
			// int blockNumber = dataBuffer.getInt(i); //Whats it for?
			dataIndex += 4;
		}

		int storageAddressD = dataBuffer.getInt(dataIndex);
		int byteCountD = dataBuffer.getShort(dataIndex + 4);

		int functionKeywordD = dataBuffer.getShort(dataIndex + 6);
		currentFSW = functionKeywordD;

		String nameFSWD = fswMap.get(functionKeywordD);

		// Add optional translation
		if (transMap != null && transMap.containsKey(nameFSWD)) {
			nameFSWD = nameFSWD + " | " + transMap.get(nameFSWD);
		}

		byte indexD = dataBuffer.get(dataIndex + 8);
		dataIndex += indexD;

		int maskLengthD = 0xFFFF & dataBuffer.getShort(dataIndex + 9);
		byte[] maskD = new byte[maskLengthD];

		for (int j = 0; j < maskLengthD; j++) {
			maskD[j] = dataBuffer.get(dataIndex + 11 + j);
		}

		dataIndex = dataIndex + 11 + maskLengthD;

		if (isDIR) {
			// Whats operation?
			int operationLen = 0xFFFF & dataBuffer.getShort(dataIndex);
			dataIndex += operationLen;
			// byte unit = dataBuffer.get(i+2);
		}

		// Create actual node in rom
		for (BMWConversionRomNodeManager man : romManagers) {
			Element table = man.createTable(nameFSWD, currentCategory, memoryLayout, endian, storageAddressD,
					byteCountD, maskD);

			if (table != null)
				currentTable = table;
		}
	}

	private void parsePresetValues(boolean parsePSW) {
		dataIndex += 1;

		int functionKeywordPSW = 0;

		if (parsePSW) {
			functionKeywordPSW = dataBuffer.getShort(dataIndex);
			dataIndex += 2;
		}

		int numValuesPSW1 = dataBuffer.getShort(dataIndex);
		dataIndex += 2;

		String namePSW = "Unamed";
		String PSW1_s = "";

		byte[] presetValuesPSW1 = new byte[numValuesPSW1];

		for (int j = 0; j < numValuesPSW1; j++) {
			presetValuesPSW1[j] = dataBuffer.get(dataIndex + j);
			PSW1_s += " " + String.format("%02X", (ByteUtil.asUnsignedInt(presetValuesPSW1[j])));
		}

		if (parsePSW) {
			namePSW = pswMap.get(functionKeywordPSW);

			// Add optional translation
			if (transMap != null && transMap.containsKey(namePSW)) {
				namePSW += " | " + transMap.get(namePSW);
			}

			// Add option combinations
			int key = currentFSW << 16 | functionKeywordPSW;
			if (csvMap.containsKey(key))
				namePSW += " | " + csvMap.get(key);
		}

		for (BMWConversionRomNodeManager man : romManagers) {
			man.addPreset(PSW1_s.trim(), namePSW.trim(), currentTable);
		}
	}

	// Used for test code only
	// Gets all files within folder
	private static Collection<File> listFileTree(File dir) {
		Set<File> fileTree = new HashSet<File>();
		if (dir == null || dir.listFiles() == null) {
			return fileTree;
		}
		for (File entry : dir.listFiles()) {
			if (entry.isFile())
				fileTree.add(entry);
			else
				fileTree.addAll(listFileTree(entry));
		}
		return fileTree;
	}

	// Test Code
	public static void main(String args[]) {
		initDebugLogging();
		initLookAndFeel();
		ECUEditor editor = getECUEditor();
		editor.initializeEditorUI();
		editor.checkDefinitions();

		// Make sure we dont override any settings
		SettingsManager.setTesting(true);
		Settings settings = SettingsManager.getSettings();

		settings.getEcuDefinitionFiles().clear();
		// settings.getEcuDefinitionFiles().add(new
		// File("C:\\NCSEXPER\\DATEN\\E46\\KMB_E46.C08"));
		// settings.getEcuDefinitionFiles().add(new
		// File("C:\\NCSEXPER\\DATEN\\E46\\IHK_E46.C17"));
		// settings.getEcuDefinitionFiles().add(new
		// File("C:\\NCSEXPER\\DATEN\\E46\\GM5.C04"));

		File folder = new File("C:\\NCSEXPER\\DATEN\\");
		Collection<File> listOfFiles = listFileTree(folder);

		ConversionLayer l = new BMWCodingConversionLayer();

		for (File f : listOfFiles) {
			if (l.isFileSupported(f)) {
				settings.getEcuDefinitionFiles().add(f);
			}
		}

		// settings.getEcuDefinitionFiles().add(new
		// File("C:\\NCSEXPER\\DATEN\\E36\\KMB_E36.C25"));
		OpenImageWorker w = new OpenImageWorker(
				new File("E:\\google_drive\\ECU_Tuning\\maps\\Tacho\\Tacho Grau\\C25_352k_248_oil_6Cyl.hex"));
		// OpenImageWorker w = new OpenImageWorker(new
		// File("E:\\Downloads\\ZKE_eep.bin"));
		// OpenImageWorker w = new OpenImageWorker(new
		// File("E:\\Downloads\\A-C_eep.bin"));
		// OpenImageWorker w = new OpenImageWorker(new
		// File("E:\\Downloads\\MFL_0000-1000.bin"));
		// OpenImageWorker w = new OpenImageWorker(new
		// File("E:\\Downloads\\IKE_eep.bin"));

		w.execute();
	}

	@Override
	public String getDefinitionPickerInfo() {
		return rb.getString("LOADINGWARNING");
	}
}
