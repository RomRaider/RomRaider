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

package com.romraider.xml;

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
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.romraider.Settings;
import com.romraider.editor.ecu.ECUEditor;
import com.romraider.editor.ecu.OpenImageWorker;
import com.romraider.util.SettingsManager;

public class BMWCodingConversionLayer implements ConversionLayer {
	private int splitAddress = 0;
	boolean guessChecksums = false;
	
	//Naming stays german, because the file is also german
	static final int TYPE_DATEINAME = 0x0000;			//Filename
	static final int SGID_CODIERINDEX = 0x0001;			//Codingindex
	static final int SGID_HARDWARENUMMER = 0x0002;		//Hardware number		
	static final int SGID_SWNUMMER = 0x0003;			//Software number
	static final int SPEICHERORG = 0x0004;				//Memory layout
	static final int ANLIEFERZUSTAND = 0x0005;			//State of delivery when new (?)
	static final int CODIERDATENBLOCK = 0x0006;			//Coding data block (like a group)
	static final int HERSTELLERDATENBLOCK = 0x0007;		//Manufacturer data block (like a group)
	static final int RESERVIERTDATENBLOCK = 0x0008;		//Reserved data	(like a group)
	static final int UNBELEGT1 = 0x0009;				//Unused (Not actually sometimes)
	static final int UNBELEGT2 = 0x000A;				//Unused (Not actually sometimes)
	static final int KENNUNG_K = 0x000B;				//?
	static final int KENNUNG_D = 0x000C;				//?
	static final int KENNUNG_X = 0x000D;				//?
	static final int KENNUNG_ALL = 0x000E;				//?
	static final int PARZUWEISUNG_PSW2 = 0x000F;		//Coding data preset information
	static final int PARZUWEISUNG_PSW1 = 0x0010;		//Coding data preset information
	static final int PARZUWEISUNG_DIR = 0x0011;			//?
	static final int PARZUWEISUNG_FSW = 0x0012;			//Coding data memory storage information
	
	//Gets called when ROM is opened directly
	public BMWCodingConversionLayer() {
		 this(0, false);
	}
	
	//Gets called from the toolbar with more options
	public BMWCodingConversionLayer(int splitAddress, boolean guessChecksums) {
		this.splitAddress = splitAddress;
		this.guessChecksums = guessChecksums;
	}
		
	public boolean isFileSupported(File f) {
		return f.getName().matches("^[\\w,\\s-]+\\.C\\d\\d");
	}
	
	private static String readString(byte[] input, int offset) {
		StringBuilder s = new StringBuilder();
		
		while((char)(input[offset])!=0) {
			s.append((char)input[offset]);
			offset++;
		}
		
		return s.toString();
	}
	
    private HashMap<String, String> readTranslationFile(File transF) {
    	HashMap<String, String> map = new HashMap<String, String>();
    	
    	try (BufferedReader br = new BufferedReader(new FileReader(transF))) {
    	    String line;
   	        	    
    	    while ((line = br.readLine()) != null) {
    	        String[] values = line.split(",");
    	        if(values.length == 2) map.put(values[0], values[1]);
    	    }
    	    
    	} catch (FileNotFoundException e) {
			return null;
		} catch (IOException e) {
			return null;
		}
    	
		return map;
	}

	private static HashMap<Integer, String> createMapFromNCSDict(File f) {
		//Parse name dictionary
		HashMap <Integer, String> map= new HashMap<Integer, String>();
		byte[] fswInput;
		
		try {
			fswInput = ECUEditor.readFile(f);
		} catch (IOException e) {
			return null;
		}
		
		ByteBuffer fswBuffer = ByteBuffer.wrap(fswInput);
		fswBuffer = fswBuffer.order(ByteOrder.LITTLE_ENDIAN);
				
		/*
		 * 	0000 - DATEINAME - S - NAME
			0001 - SWT_EINTRAG - WS - KEYID,KEYWORD
		 */
		
        for(int i=0x5E; i < fswInput.length;) {
        	int oldIndex = i;
        	
        	int length = fswBuffer.get(i);
        	int frameType = fswBuffer.getShort(i+1);
        	i+=3;
       	       	
        	switch(frameType) {
        		//Name
        		case 0x0000:
        			break;
        		//SWT Entry
        		case 0x0001:      			
        			int keyId = fswBuffer.getShort(i);
        			String name = readString(fswInput, i+2);
        			map.put(keyId, name);
        			break;       			
        	}
        	
        	i = oldIndex+length + 4;
        }
        
        return map;
	}
		
	private static HashMap<Integer, String> createMapFromCVT(File f, HashMap<Integer, String> aswMap) {
		//Parse name dictionary
		HashMap <Integer, String> map= new HashMap<Integer, String>();
		byte[] fswInput;
		
		try {
			fswInput = ECUEditor.readFile(f);
		} catch (IOException e) {
			return null;
		}
		
		ByteBuffer cvtBuffer = ByteBuffer.wrap(fswInput);
		cvtBuffer = cvtBuffer.order(ByteOrder.LITTLE_ENDIAN);
				
		/*
			0000 - DATEINAME - S - NAME
			0001 - GRUPPE - {S} - NAME
			0002 - INDIVID - {S} - NAME
			0003 - AUFTRAGSAUSDRUCK - A - AUFTRAGSAUSDRUCK
			0004 - FSW_PSW - WW - FSWINDEX,PSWINDEX
			0005 - FSW - W - FSWINDEX
		 */
		String currentOption = "";
		

        for(int i=0xEA; i < fswInput.length;) {
        	int oldIndex = i;
        	
        	int length = cvtBuffer.get(i);
        	int frameType = cvtBuffer.getShort(i+1);
       	    i+=3;
       	     
        	switch(frameType) {
        		//Name
        		case 0x0000:
        			break;     			
        		//Option/Auftrag
        		case 0x0003:      	       		       			
        			int lengthOption = cvtBuffer.get(i);
        			i++;
        			String optionCode = "";
        			
        			for(int j = 0; j < lengthOption;) {
        				if(cvtBuffer.get(j + i) == 0x53) {
		        			int keyId = cvtBuffer.getShort(j+i+1);
		        			currentOption = aswMap.get(keyId);
		        			optionCode = optionCode + currentOption;
		        			j+=3;
        				}
        				else {
        					optionCode+=(char) cvtBuffer.get(j+i);
        					j++; 
        				} 		
        			}    
        			
    				currentOption = optionCode;      			
        			break;
        		//Link FSW/PSW combination to option
        		case 0x0004:      			
        			int fsw = cvtBuffer.getShort(i);
        			int psw = cvtBuffer.getShort(i+2);
        			map.put(fsw << 16 | psw, currentOption);
        			break; 
        	}
        	
        	i = oldIndex+length + 4;
        }
        
        return map;
	}
		
	public Document convertToDocumentTree(File f) {		
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = null;;
        
		try {
			builder = dbf.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		
        Document doc = builder.newDocument();
        
		//Check first if naming conversion files are there
		File fswF = new File(f.getParent(), "SWTFSW01.DAT");		
		File pswF = new File(f.getParent(), "SWTPSW01.DAT");
		File aswF = new File(f.getParent(), "SWTASW01.DAT");
		File csvF = new File(f.getParent(), f.getParentFile().getName() + "CVT.000");
		
		if(!fswF.exists() || !pswF.exists() || !aswF.exists() || !csvF.exists()) return null;
		
		HashMap <Integer, String> fswMap= createMapFromNCSDict(fswF);
		HashMap <Integer, String> pswMap= createMapFromNCSDict(pswF);
		HashMap <Integer, String> aswMap= createMapFromNCSDict(aswF);
		HashMap <Integer, String> csvMap= createMapFromCVT(csvF, aswMap);
		
		//Optional translation file that has to be in the DATEN folder
		//Created from NCSDummy developers
		HashMap <String, String> transMap = null;
		
		File transF = new File(f, "../../Translations.csv");		
		if(transF.exists()) transMap = readTranslationFile(transF);
		    			        			        
        byte[] input;
        
		try {
			input = ECUEditor.readFile(f);
		} catch (IOException e) {
			return null;
		}
		
		ByteBuffer dataBuffer = ByteBuffer.wrap(input);
		dataBuffer = dataBuffer.order(ByteOrder.LITTLE_ENDIAN);
       		
        Node roms = doc.createElement("roms");
        doc.appendChild(roms);
        
        BMWRomManager[] romManagers = null;
        
        if(splitAddress == 0) romManagers= new BMWRomManager[] {new BMWRomManager(0, doc, roms)};
        else
        	romManagers= new BMWRomManager[] {
        			new BMWRomManager(0, doc, roms),
        			new BMWRomManager(splitAddress, doc, roms)};

        
        String currentCategory = "";
        int currentFSW = 0;       
        Node currentTable = null;
        
        /*
         *  0000 - DATEINAME - S - NAME
			0001 - SGID_CODIERINDEX - B(B) - WERT,WERT2
			0002 - SGID_HARDWARENUMMER - S(S) - WERT,WERT2
			0003 - SGID_SWNUMMER - S(S) - WERT,WERT2
			0004 - SPEICHERORG - SS - STRUKTUR,TYP
			0005 - ANLIEFERZUSTAND - (B) - WERT
			0006 - CODIERDATENBLOCK - {L}LWS - BLOCKNR,WORTADR,BYTEADR,BEZEICHNUNG
			0007 - HERSTELLERDATENBLOCK - {L}LWS - BLOCKNR,WORTADR,BYTEADR,BEZEICHNUNG
			0008 - RESERVIERTDATENBLOCK - {L}LWS - BLOCKNR,WORTADR,BYTEADR,BEZEICHNUNG
			0009 - UNBELEGT1 - {L}LW{B}(B) - BLOCKNR,WORTADR,BYTEADR,INDEX,MASKE
			000A - UNBELEGT2 - (B) - WERT
			000B - KENNUNG_K - SS(S) - IDENT,WERT1,WERTN
			000C - KENNUNG_D - WW(WW) - HEXWERT1,HEXWERT2,HEXWERTN1,HEXWERTN2
			000D - KENNUNG_X - WW(WW) - HEXWERT1,HEXWERT2,HEXWERTN1,HEXWERTN2
			000E - KENNUNG_ALL - SW(W) - KENNUNG,HEXWERT1,HEXWERTN
			000F - PARZUWEISUNG_PSW2 - (B) - DATUM
			0010 - PARZUWEISUNG_PSW1 - W(B) - PSW,DATUM
			0011 - PARZUWEISUNG_DIR - {L}LWW{B}(B)(A)B - BLOCKNR,WORTADR,BYTEADR,FSW,INDEX,MASKE,OPERATION,EINHEIT
			0012 - PARZUWEISUNG_FSW - {L}LWW{B}(B){B}{B} - BLOCKNR,WORTADR,BYTEADR,FSW,INDEX,MASKE,EINHEIT,INDIVID
         */
               
        for(int i=0x481; i < input.length;) {
        	int oldIndex = i;
        	
        	int length = dataBuffer.get(i);
        	int frameType = dataBuffer.getShort(i+1);
        	i+=2;
       	       	
        	switch(frameType) {
        		case TYPE_DATEINAME:
        			//fileNameInFile = readString(input, i+1);
        		//Coding Block (=category)
        		case CODIERDATENBLOCK:
        		case HERSTELLERDATENBLOCK:
        		case RESERVIERTDATENBLOCK:
        			//Skip blocknumber
        			i+=1;
        			
        			//Not used in our case, since only used as category
        			//int storageAddressBlock = dataBuffer.getInt(i);
        			//int byteCountBlock = dataBuffer.getShort(i+4);        			
        		     			
        			currentCategory = readString(input, i+7);
        			
        			//Add optional translation
        			if(transMap != null && transMap.containsKey(currentCategory)) {
        				currentCategory = currentCategory + " | " + transMap.get(currentCategory);
        			}
        			
        			break;
        		case PARZUWEISUNG_FSW:
           			//Skip blocknumber
        			i+=2;
        			int storageAddress = dataBuffer.getInt(i);
        			int byteCount = dataBuffer.getShort(i+4);
        			int functionKeyword = dataBuffer.getShort(i+6);
        			currentFSW = functionKeyword;
        			
        			String nameFSW = fswMap.get(functionKeyword);
        					
        			//Add optional translation
        			if(transMap != null && transMap.containsKey(nameFSW)) {
        				nameFSW = nameFSW + " | " + transMap.get(nameFSW);
        			}        			
        			
        		//	byte index = dataBuffer.get(i+8);
        			
        			int maskLength =  dataBuffer.getShort(i+9);
        			byte[] mask = new byte[maskLength];
        			
        			for(int j=0;j<maskLength;j++) {
        				mask[j] = dataBuffer.get(i+11+j);   			
        			}
        	
        			i = i + 11 + maskLength;
        			//byte optData = dataBuffer.get(i);
        			//byte unit = dataBuffer.get(i+1);
        			//byte individual = dataBuffer.get(i+2);
        			
        			//Create actual node in rom
        	        for(BMWRomManager man: romManagers){
        	        	Element table = man.createTable(fswMap.get(functionKeyword),
        	        			currentCategory, storageAddress, byteCount, mask);
        	        	
        	        	if(table != null) currentTable = table;
        	        }
        	        
        			break;

        		case PARZUWEISUNG_PSW1:
        			i+=1;
        			int functionKeywordPSW = dataBuffer.getShort(i);
        			int numValuesPSW1 = dataBuffer.getShort(i+2);
        			
        			byte[] presetValuesPSW1 = new byte[numValuesPSW1];
        			String PSW1_s = "";
        			
        			for(int j=0;j<numValuesPSW1;j++) {
        				presetValuesPSW1[j] = dataBuffer.get(i+4+j);  	
        				PSW1_s+=" " + String.format("%02X", (Byte.toUnsignedInt(presetValuesPSW1[j])));
        			}
        			
        			String namePSW = pswMap.get(functionKeywordPSW);
					
        			//Add optional translation
        			if(transMap != null && transMap.containsKey(namePSW)) {
        				namePSW += " | " + transMap.get(namePSW);
        			}
        			
        			//Add option combinations
        			int key = currentFSW << 16 | functionKeywordPSW;
        			if(csvMap.containsKey(key))namePSW += " | " + csvMap.get(key);
        	        
        			for(BMWRomManager man: romManagers){
        	        	man.addPreset(PSW1_s, namePSW, currentTable);
        	        }
        			
        			break;
        		case PARZUWEISUNG_PSW2:
        			i+=1;
        			int numValuesPSW = dataBuffer.getShort(i+2);     			
        			byte[] presetValuesPSW2 = new byte[numValuesPSW];
        			
        			String PSW2_s = "";
        			for(int j = 0 ; j < numValuesPSW; j++) {
        				presetValuesPSW2[j] = dataBuffer.get(i+4+j);  
        				PSW2_s+= " " + Integer.toHexString(Byte.toUnsignedInt(presetValuesPSW2[j]));
        			}
        			
        			for(BMWRomManager man: romManagers){
        	        	man.addPreset(PSW2_s, "Unnamed", currentTable);
        	        }
        			
        			break;
        	}        	  	        	
        	
        	//No matter what the index points to, always go by length
        	//Skip frameType, checksum and go to next
        	i = oldIndex+length + 4;
        }
        
		for(BMWRomManager man: romManagers){
        	man.calculateRomID(f);
        }
           
        /*
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer trans = tf.newTransformer();
        StringWriter sw = new StringWriter();
        trans.transform(new DOMSource(doc), new StreamResult(sw));
        System.out.println(sw.toString());*/
        
        return doc;
	}
		
	
	class BMWRomManager{
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
		
		BMWRomManager(int offsetAddress, Document doc, Node root){
			this.doc = doc;
			this.offsetAddress = offsetAddress;
			this.romNode = doc.createElement("rom");			
			root.appendChild(romNode);
		}
		
		public Element getRomNode() {
			return romNode;
		}
		
		
		public Element createTable(String name, String category, int storageAddress, int byteCount, byte[] mask) {
			
			//This handles the correct rom splitting
			if(offsetAddress == 0 || storageAddress >= offsetAddress) {	
				
				if(!bestIDFitDataTemp.equals("") && lastPresetCount == 1) {
					bestIDFitData = bestIDFitDataTemp;
					bestIDFitAddress = bestIDFitAddressTemp;
				}				
				
				lastPresetCount = 0;
				
				Element table = doc.createElement("table");
				table.setAttribute("name", name);
				table.setAttribute("category", category);
				table.setAttribute("storagetype", "uint8");
				table.setAttribute("storageaddress", "0x" + 
						Integer.toHexString(storageAddress - offsetAddress));
				table.setAttribute("endian", "little");
				table.setAttribute("sizey", Integer.toString(byteCount));
				table.setAttribute("type", "1D");
				table.setAttribute("mask", Integer.toHexString(Byte.toUnsignedInt(mask[0])));
				
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
				
				if(lastPresetCount >= 1) {
					bestIDFitDataTemp = "";
				}			
				else if(bestIDFitData.length() == 0 || (bestIDFitData.length() <= data.length() &&
						!data.replace("0", "").equals("x") && !data.replace("F", "").equals("0x"))){
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
		
		public void calculateRomID(File f) {
        	Element romIDNode = doc.createElement("romid");
			
        	//romNode.setTextContent("Test");
        	Node idAddress = doc.createElement("internalidaddress");
        	idAddress.setTextContent("0x" + Integer.toHexString(bestIDFitAddress));
        	Node idString = doc.createElement("internalidstring");
        	idString.setTextContent("0x" + bestIDFitData.replace(" ", ""));
        	Node ramoffset = doc.createElement("noramoffset");
        	
        	//Set filesize based on largest address and round up to a power of 2
        	Node fileSize = doc.createElement("filesize");
        	String fileS = ((int)Math.pow(2, 32 - Integer.numberOfLeadingZeros(lastStorageAddress - 1)) + "b");
        	fileSize.setTextContent(fileS);
        	
        	Node make = doc.createElement("make");
        	make.setTextContent("BMW");
        	
        	Node model = doc.createElement("model");
        	model.setTextContent(f.getParentFile().getName());
        	
        	Node ecuID = doc.createElement("ecuid");
        	String[] nameSplit = f.getName().split("\\.");
        	ecuID.setTextContent(nameSplit[1]);
        	
        	Node subModel = doc.createElement("submodel");
        	subModel.setTextContent(nameSplit[0]);
        	
        	romIDNode.appendChild(make);
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
	
	public static void main(String args[]) {
        initDebugLogging();
        initLookAndFeel();
        ECUEditor editor = getECUEditor();
        editor.initializeEditorUI();
        editor.checkDefinitions();
        
		//Make sure we dont override any settings
		SettingsManager.setTesting(true);
		Settings settings = SettingsManager.getSettings();	
		
		settings.getEcuDefinitionFiles().clear();
		settings.getEcuDefinitionFiles().add(new File("C:\\NCSEXPER\\DATEN\\E36\\KMB_E36.C25"));
		OpenImageWorker w = new OpenImageWorker(new File("E:\\google_drive\\ECU_Tuning\\maps\\Tacho\\Tacho Grau\\C25_352k_248_oil_6Cyl.hex"));
		w.execute();
	}
}

