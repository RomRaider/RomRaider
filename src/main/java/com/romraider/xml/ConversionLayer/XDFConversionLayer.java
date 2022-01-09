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
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.romraider.Settings;
import com.romraider.editor.ecu.ECUEditor;
import com.romraider.editor.ecu.OpenImageWorker;
import com.romraider.util.HexUtil;
import com.romraider.util.SettingsManager;;

public class XDFConversionLayer extends ConversionLayer {
   private static final Logger LOGGER = Logger.getLogger(XDFConversionLayer.class);    
   private HashMap<Integer, String> categoryMap = new HashMap<Integer,String>();
   private HashMap<Integer, Element> tableMap = new HashMap<Integer, Element>();
   private LinkedList <EmbedInfoData> embedsToSolve = new LinkedList<EmbedInfoData>();
   
   int bitCount;
   private boolean signed;
   private int offset;
   private int numDigits;
   //private String dataType;
   private boolean lsbFirst;
   
   //Defaults
   String defaultDataType;
   
  
	@Override
	public String getRegexFileNameFilter() {
		 return "^.*xdf";
	}
	
	@Override
	public Document convertToDocumentTree(File f) throws Exception {
	    Document doc = null;
	    FileInputStream fileStream = null; 
	    BufferedReader br = null;
	    
		try {
			//Check first if its an older definition
			//which is not xml based
		    br = new BufferedReader(new FileReader(f));	
		    String firstLine = br.readLine();
		    
		    if(firstLine.equalsIgnoreCase("XDF")) {
		    	br.close();
		    	//TODO: Add i18n
		    	throw new SAXException("Sorry, only XML XDFs are currently supported!");
		    }
		    else {
		    	br.close();
		    }
		    
		    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		    factory.setNamespaceAware(true);
		    factory.setXIncludeAware(true);
		    DocumentBuilder docBuilder = factory.newDocumentBuilder();
		    
		    fileStream = new FileInputStream(f); 
	    	Document XMLdoc = docBuilder.parse(fileStream, f.getAbsolutePath());
	    	doc = convertXDFDocument(XMLdoc);
	    } catch (ParserConfigurationException e) {
			e.printStackTrace();
	    } catch (IOException e) {
			e.printStackTrace();
		}
	    finally {
			try {
				if(fileStream != null)
					fileStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
	    }
		return doc;
	}
	
	private Element solveLinkObj(int key, Node targetTable) {
		if(tableMap.containsKey(key)) {
			Element sourceTableRR = tableMap.get(key);
			return (Element) sourceTableRR.cloneNode(true);
		}
		else {
			return null;
		}
	}
	
	private class EmbedInfoData{
		Element tableNodeRR;
		Node axisNode;
		Node flagsNodeTable;
	}
	
	private Element parseAxis(Document doc, Element tableNodeRR, Node axisNode, Node flagsNodeTable) {	
    	Node idNode = axisNode.getAttributes().getNamedItem("id");
    	String id = "z";
    	
    	if(idNode != null) {
    		id = idNode.getNodeValue();
    	}
    	
		Element targetTable = null;		
		Element scaling = null;
	
		boolean hasEmbedInfo = false;
		String staticTable = "";
		
    	int nodeCountAxis = axisNode.getChildNodes().getLength();
    	Node n;
    	
		//Check first if we need to copy attributes from the base table
    	for(int i=0; i < nodeCountAxis ; i++) {
    		n = axisNode.getChildNodes().item(i);
    		
    		if(n.getNodeName().equalsIgnoreCase("embedinfo") && 
    				n.getAttributes().getNamedItem("linkobjid") != null){   			
    			Integer key = HexUtil.hexToInt(n.getAttributes().getNamedItem("linkobjid").getNodeValue());
    						
    			Element refTable = solveLinkObj(key, targetTable);
    			
    			//May needs to be solved later
    			if(refTable != null) {
    				targetTable = refTable;
    				
    		    	int nodeCountRefTable = targetTable.getChildNodes().getLength();
    		    	targetTable.removeAttribute("type");
    		    	targetTable.removeAttribute("category");
    		    	
    				//Find scaling child and delete child tables
    		    	for(int j=0; j < nodeCountRefTable; j++) {
    		    		Node tN = targetTable.getChildNodes().item(j);
    		    		if(tN == null) continue;
    		    		
    		    		if(tN.getNodeName().equalsIgnoreCase("table") ||
    		    		   tN.getNodeName().equalsIgnoreCase("description")) 
    		    		{
    		    			targetTable.removeChild(tN);
    		    		}
    		    		else if(tN.getNodeName().equalsIgnoreCase("scaling")) {
    		    			scaling = (Element) tN;
    		    		}
    		    	}
    			}
    			else {
    				EmbedInfoData e = new EmbedInfoData();
    				e.axisNode = axisNode;
    				e.flagsNodeTable = flagsNodeTable;
    				e.tableNodeRR = tableNodeRR;
    				embedsToSolve.add(e);
    				
    				//Return an empty table so we count as an axis...
    				return doc.createElement("table");
    			}
    			
    			hasEmbedInfo = true;
    			break;
    		}
    	}
    	
    	if(targetTable == null) {
    		targetTable = id.equalsIgnoreCase("z") ? tableNodeRR : doc.createElement("table");		
    	}
    	
    	if(scaling == null) {
    		scaling = doc.createElement("scaling");
    		targetTable.appendChild(scaling);
    	}
    	
    	Node addressNode = null;
    	int staticCells = 0;
    	int indexCount = -1;
    	String lastStaticValue = "";

    	for(int i=0; i < nodeCountAxis ; i++) {
    		n = axisNode.getChildNodes().item(i);
    		
    		if(n.getNodeName().equalsIgnoreCase("indexcount")){
    			indexCount = Integer.parseInt(n.getTextContent());
    		}		
    		else if(!hasEmbedInfo && n.getNodeName().equalsIgnoreCase("embeddeddata")){

    			addressNode = n.getAttributes().getNamedItem("mmedaddress");
    			
    			if(addressNode != null) {  					
	    			String address = addressNode.getNodeValue();
	    			targetTable.setAttribute("storageaddress", address);
	    		}    			
    			
    			Node flagsNode = n.getAttributes().getNamedItem("mmedtypeflags");
    			Node sizeBitsNode = n.getAttributes().getNamedItem("mmedelementsizebits");
    			//Node majorStrideBitsNode = n.getAttributes().getNamedItem("mmedmajorstridebits");
    			//Node minorStrideBitsNode = n.getAttributes().getNamedItem("mmedminorstridebits");
    			Node rowCountNode = n.getAttributes().getNamedItem("mmedrowcount");
    			Node colCountNode = n.getAttributes().getNamedItem("mmedcolcount");
    			
    			boolean signedLocal = signed;
    			boolean lsbFirstLocal = lsbFirst;
    			int flags = 0;
    			int sizeBits = 0; 
    			//int majorStrideBits = 0; HexUtil.hexToInt(majorStrideBitsNode.getNodeValue());
    			//int minorStrideBits = 0; HexUtil.hexToInt(minorStrideBitsNode.getNodeValue());
    			
    			if(flagsNode == null)
    				flagsNode = flagsNodeTable;
    				    			
    			if(flagsNode != null) {
    				try {
    					flags =  HexUtil.hexToInt(flagsNode.getNodeValue());
    					
	    				if((flags &(0x01)) > 0) {
	    					signedLocal = true;
	    				}
	    				else if((flags & 0x01) == 0) {
	    					signedLocal = false;
	    				}
	    				
	    				if((flags & 0x02) > 0) {
	    					lsbFirstLocal = false;
	    				}
	    				else {
	    					lsbFirstLocal = true;
	    				}
	    				
	    				if((flags & 0x04) > 0) {
	    					targetTable.setAttribute("swapxy", "true");
	    				}
    				}
    				catch(NumberFormatException e) {
    					//TODO: Not sure how to handle this yet...
    					LOGGER.error("Failed to parse flag " + flagsNode.getNodeValue());	    					
    				}	    		
    			}
    			
    			if(sizeBitsNode != null) {
    				sizeBits = Integer.parseInt(sizeBitsNode.getNodeValue());
    			}
    			else {
    				sizeBits = bitCount;
    			}
    			
    			if(colCountNode!=null) {
    				targetTable.setAttribute("sizex", colCountNode.getNodeValue());
    			}
    			
    			if(rowCountNode!=null) {
    				targetTable.setAttribute("sizey", rowCountNode.getNodeValue());
    			}
   		
    			if(!hasEmbedInfo) {
        			targetTable.setAttribute("storagetype", (signedLocal ? "" : "u") + "int" + sizeBits);
	    			targetTable.setAttribute("endian", lsbFirstLocal ? "big" : "little");
    			}
    		}
    		else if(!hasEmbedInfo && n.getNodeName().equalsIgnoreCase("units")) {
    			scaling.setAttribute("units", n.getTextContent());
    			
    			/*if(!id.equalsIgnoreCase("z")) {
    				targetTable.setAttribute("name", " ");
    			}*/
    		}
    		else if(!hasEmbedInfo && n.getNodeName().equalsIgnoreCase("decimalpl")) {
    			String format = "0";
    			
    			if(!n.getTextContent().equals("0")) {
    				int count = Math.abs(Integer.parseInt(n.getTextContent()));
    				format = "0." + new String(new char[count]).replace("\0", "0");

    			}  			
				scaling.setAttribute("format", format);
    		}
    		else if(!hasEmbedInfo && n.getNodeName().equalsIgnoreCase("math")) {
    			String formula = n.getAttributes().getNamedItem("equation").getNodeValue();	
    			formula = formula.replace("X", "x");
    			scaling.setAttribute("expression", formula);
    		}

    		else if(n.getNodeName().equalsIgnoreCase("label")) {   			
    			String label = n.getAttributes().getNamedItem("value").getNodeValue();	
    			Element data = doc.createElement("data");
    			data.setTextContent(label);
    			targetTable.appendChild(data);
    			lastStaticValue = label;
    			staticCells++;	
    		}
    	}
    	   	
    	if(staticCells == indexCount && indexCount > 1 && !lastStaticValue.equalsIgnoreCase("0.00")) {
    		staticTable = "Static "; 		
			targetTable.setAttribute("size" + id, "" + staticCells);
    		targetTable.removeAttribute("endian");
    		targetTable.removeAttribute("storagetype");
    	}
    	
    	if(!scaling.hasAttribute("format")) {
			String format = "0." + new String(new char[numDigits]).replace("\0", "0");
			scaling.setAttribute("format", format);
		}

		if(!targetTable.hasAttribute("storageaddress") && staticTable.isEmpty()) return null;
		
    	if(id.equalsIgnoreCase("z")) return null;
    	else {
    		targetTable.setAttribute("type", staticTable + id.toUpperCase() + " Axis");
    		return targetTable;   
    	}
	}
	
	private Element parseTable(Document doc, Node romNode, Node tableNode) {
    	int nodeCountTable = tableNode.getChildNodes().getLength();
    	Node n;
    	Element tableNodeRR = doc.createElement("table");
    	
    	Node uniqueIDNode = tableNode.getAttributes().getNamedItem("uniqueid");
    	Node flagsNode = tableNode.getAttributes().getNamedItem("flags"); 
    	
    	if(uniqueIDNode != null) {
    		tableMap.put(HexUtil.hexToInt(uniqueIDNode.getNodeValue()), tableNodeRR);
    	}
    	
    	LinkedList<String> categories = new LinkedList<String>();
		int numAxis = 0;
				
    	for(int i=0; i < nodeCountTable ; i++) {
    		n = tableNode.getChildNodes().item(i);
    		
    		if(n.getNodeName().equalsIgnoreCase("title")){
    			//TunerPro can currently not edit axis directly, but we can
    			//These tables contain the axis, which we can skip
    			if(n.getTextContent().endsWith("(autogen)")){
    				return null;
    			}
    			
    			tableNodeRR.setAttribute("name", n.getTextContent());   				
    		}
    		if(n.getNodeName().equalsIgnoreCase("description")){
    			Element desc = doc.createElement("description");
    			desc.setTextContent(n.getTextContent());
    			tableNodeRR.appendChild(desc);
    		}
    		else if(n.getNodeName().equalsIgnoreCase("categorymem")) {
    			//int index = Integer.parseInt(n.getAttributes().getNamedItem("index").getNodeValue());
    			int category = Integer.parseInt(n.getAttributes().getNamedItem("category").getNodeValue());
    			
    			if(categoryMap.containsKey(category-1)) {
    				categories.add(categoryMap.get(category-1));
    			}
    		}
    		else if(n.getNodeName().equalsIgnoreCase("xdfaxis")) {
    			Element axis = parseAxis(doc, tableNodeRR, n, flagsNode);
    			
    			if(axis != null) {
    				numAxis++;
    				
    				//This checks if we have embed info
    				if(axis.hasAttribute("type"))
    					tableNodeRR.appendChild(axis);				
    			}
    		}
    	}
  	 	
    	String category = "";
    	
    	for(int i = 0; i < categories.size(); i++) {
    		String cat = categories.get(i);   
    			category+=cat;
    			
				if(i < categories.size() - 1)
					category+="//";
    	}
  	
    	tableNodeRR.setAttribute("category", category);
    	tableNodeRR.setAttribute("type", (numAxis + 1) + "D");
    	
    	return tableNodeRR;
	}
	
	private Node parseXDFHeader(Document doc, Node romNode, Node header) {
    	int nodeCountHeader = header.getChildNodes().getLength();
    	Node n;
    	Node romIDNode = doc.createElement("romid");
    	
    	for(int i=0; i < nodeCountHeader ; i++) {
    		n = header.getChildNodes().item(i);
    		
    		if(n.getNodeName().equalsIgnoreCase("CATEGORY")){
    			categoryMap.put(HexUtil.hexToInt(n.getAttributes().getNamedItem("index").getNodeValue()),
    					n.getAttributes().getNamedItem("name").getNodeValue());
    		}   
    		else if(n.getNodeName().equalsIgnoreCase("flags")){
    			//TODO
    		}  
    		else if(n.getNodeName().equalsIgnoreCase("deftitle")){
    			String title = n.getTextContent();
    	    	Node ecuID = doc.createElement("ecuid");
    	    	ecuID.setTextContent(title);
    	    	romIDNode.appendChild(ecuID);
    		}
    		else if(n.getNodeName().equalsIgnoreCase("description")){
    			//TODO
    		}
    		else if(n.getNodeName().equalsIgnoreCase("BASEOFFSET")){
    			offset = Integer.parseInt(n.getAttributes().getNamedItem("offset").getNodeValue());
    			
    			if(!n.getAttributes().getNamedItem("subtract").getNodeValue().equals("0")) {
    				offset*=-1;
    			}
    		}
    		else if(n.getNodeName().equalsIgnoreCase("DEFAULTS")){
    			if(!n.getAttributes().getNamedItem("float").getNodeValue().equalsIgnoreCase("0")) {
    				//dataType = "float";
    			}
    			else {   				
    				bitCount = Integer.parseInt(n.getAttributes().getNamedItem("datasizeinbits").getNodeValue());
    				signed = !n.getAttributes().getNamedItem("signed").getNodeValue().equalsIgnoreCase("0");
    				
    				//dataType = (signed ? "" : "u") + "int" + bitCount;
    				lsbFirst = !n.getAttributes().getNamedItem("lsbfirst").getNodeValue().equalsIgnoreCase("0");   				
    				numDigits = HexUtil.hexToInt(n.getAttributes().getNamedItem("sigdigits").getNodeValue());
    			}
    		}
    		else if(n.getNodeName().equalsIgnoreCase("REGION")){
    			//Ignored currently: type, startAddress, regionFlags, name, desc
    			//TODO: Start address probably matters....
    			int fileSize = HexUtil.hexToInt(n.getAttributes().getNamedItem("size").getNodeValue());  
    	    	Node fileSizeN = doc.createElement("filesize");
    	    	fileSizeN.setTextContent(fileSize + "b");
    		}		
    	}
    		
    	//XDFs dont have an identification component
    	//So we just load it, no questions asked
    	Node idAddress = doc.createElement("internalidaddress");
    	Node idString = doc.createElement("internalidstring");
    	idString.setTextContent("force");
    	idAddress.setTextContent("-1");
    	
    	Element offsetNode = doc.createElement("offset");
    	offsetNode.setTextContent("0x" + Integer.toHexString(offset));
	  	
    	romIDNode.appendChild(offsetNode);
    	romIDNode.appendChild(idAddress);
    	romIDNode.appendChild(idString);
    	   	
    	return romIDNode;
	}
	
	private Document convertXDFDocument(Document xdfDoc) throws SAXException {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = null;
                
		try {
			builder = dbf.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		
		//New RomRaider document
        Document doc = builder.newDocument();     
        
        Node baseNode = xdfDoc;
        Node romNode = null;
        int nodeCount = 0;
    	int nodeCountBase = baseNode.getChildNodes().getLength();
    	
    	for(int i=0; i < nodeCountBase; i++) {
    		Node n = baseNode.getChildNodes().item(i);
    		 if(n.getNodeName().equalsIgnoreCase("XDFFORMAT")) {
    			 baseNode = n;
    			 break;
    		 }
    	}
    	
    	if(baseNode == xdfDoc) {
    		throw new SAXException("XDF file does not have an XDFFORMAT element!");
        }
    	     	
    	nodeCount = baseNode.getChildNodes().getLength();       	
    	Node header = null;
    	
    	//Find XDF Header first
    	for(int i=0; i < nodeCount; i++) {
    		Node n = baseNode.getChildNodes().item(i);
           	if(n.getNodeName().equalsIgnoreCase("XDFHEADER")){
           		
            	//Create the initial document
                Node roms = doc.createElement("roms");                   
        		romNode = doc.createElement("rom");	
        		
        		doc.appendChild(roms);
        		roms.appendChild(romNode);
           		           		
           		header = n;
           		Node headerNode = parseXDFHeader(doc, romNode, header);
           		romNode.appendChild(headerNode);
           		break;
        	}
        }
    	
    	if(header == null) {
    		throw new SAXException("XDF file does not have an XDFHEADER element!");
    	}
    	    	
    	//Go through all tables and create RR tables
    	for(int i=0; i < nodeCount; i++) {
    		Node n = baseNode.getChildNodes().item(i);
           	if(n.getNodeName().equalsIgnoreCase("XDFTABLE")){
           		Element table = parseTable(doc, romNode, n);
           		
           		if(table != null)
           			romNode.appendChild(table);
        	}
        } 	
    
    	//Some references could not be solved because we didnt parse the table yet
    	//So we have to do these now
    	for(EmbedInfoData e: embedsToSolve) {
    		Element axis = parseAxis(doc, e.tableNodeRR, e.axisNode, e.flagsNodeTable);
			
			if(axis != null)
				e.tableNodeRR.appendChild(axis);	
    	}
    	
    	categoryMap.clear();
    	tableMap.clear();
    	embedsToSolve.clear();
    	
        return doc;
	}
	
	private static LinkedList<File> listFileTree(File dir) {
	    LinkedList<File> fileTree = new LinkedList<File>();
	    if(dir==null||dir.listFiles()==null){
	        return fileTree;
	    }
	    for (File entry : dir.listFiles()) {
	        if (entry.isFile()) fileTree.add(entry);
	        else if(entry.isDirectory()) fileTree.addAll(listFileTree(entry));
	    }
	    return fileTree;
	}
	
	//Test Code
	public static void main(String args[]) {
        initDebugLogging();
        initLookAndFeel();
        ECUEditor editor = getECUEditor();
        editor.initializeEditorUI();
        editor.checkDefinitions();
        
		//Make sure we dont override any settings
		SettingsManager.setTesting(true);
		Settings settings = SettingsManager.getSettings();
		
		List<File> listOfFiles = new LinkedList<File>();
		//File folder = new File("C:\\Users\\User\\Downloads\\BMW-XDFs-master\\");
				
		listOfFiles.add(new File("E:\\Downloads\\MS430069_64K.xdf"));
		//listOfFiles.add(new File("C:\\Users\\User\\Downloads\\BMW-XDFs-master\\F G series B58\\00003076501103.xdf"));
		//Collection<File> listOfFiles =  listFileTree(folder);
		//Collections.shuffle((List<?>) listOfFiles);
	
		for(File f: listOfFiles) {
			ConversionLayer l = new XDFConversionLayer();
			if (l.isFileSupported(f)) {
				settings.getEcuDefinitionFiles().clear();
				settings.getEcuDefinitionFiles().add(f);
				//File bin = new File(f.getAbsolutePath().replace(".xdf", "_original.bin"));
				File bin = new File("E:\\google_drive\\ECU_Tuning\\maps\\MS43_430069_64KB_.bin");
				
				if(bin.exists()) {
					System.out.println(f);
					OpenImageWorker w = new OpenImageWorker(bin);
					w.execute();
					break;
				}
			}
		}
	}
}
