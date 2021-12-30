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
import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.showMessageDialog;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;

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
import com.romraider.editor.ecu.ECUEditorManager;
import com.romraider.editor.ecu.OpenImageWorker;
import com.romraider.util.HexUtil;
import com.romraider.util.SettingsManager;;

public class XDFConversionLayer extends ConversionLayer {
   private static final Logger LOGGER = Logger.getLogger(XDFConversionLayer.class);    
   private HashMap<Integer, String> categoryMap = new HashMap<Integer,String>();
   
   int bitCount;
   private boolean signed;
   private int offset;
   private int numDigits;
   private String dataType;
   private boolean lsbFirst;
   
   //Defaults
   String defaultDataType;
   
  
	@Override
	public String getRegexFileNameFilter() {
		 return "^.*xdf";
	}
	
	@Override
	public Document convertToDocumentTree(File f) {
        ECUEditor editor = ECUEditorManager.getECUEditor();
	    Document doc = null;
	    FileInputStream fileStream = null; 
	    
		try {
		    fileStream = new FileInputStream(f); 
		    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		    factory.setNamespaceAware(true);
		    factory.setXIncludeAware(true);
		    DocumentBuilder docBuilder = factory.newDocumentBuilder();
	    	Document XMLdoc = docBuilder.parse(fileStream, f.getAbsolutePath());
	    	doc = convertXDFDocument(XMLdoc);
	    	System.out.println(doc);
	   }
	    catch (SAXException spe) {	    		
	        showMessageDialog(editor,
	        		spe.getMessage(),
	        		//TODO: Add i18n
	                "Error in loading XDF",
	                ERROR_MESSAGE);
	    } catch (ParserConfigurationException e) {
			e.printStackTrace();
	    } catch (IOException e) {
			e.printStackTrace();
		}
	    finally {
			try {
				fileStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
	    }
		return doc;
	}
	
	private Element parseAxis(Document doc, Element tableNode, Node axisNode) {
    	int nodeCountAxis = axisNode.getChildNodes().getLength();
    	Node n;
    	Element axisNodeRR = doc.createElement("table");
    	
    	String id = axisNode.getAttributes().getNamedItem("id").getNodeValue();
		Element targetTable = id.equalsIgnoreCase("z") ? tableNode : axisNodeRR;
		
    	for(int i=0; i < nodeCountAxis ; i++) {
    		n = axisNode.getChildNodes().item(i);
    		
    		if(n.getNodeName().equalsIgnoreCase("embeddeddata")){

    			Node addressNode = n.getAttributes().getNamedItem("mmedaddress");
    			
    			if(addressNode != null) {  					
	    			String address = addressNode.getNodeValue();
	    			Node flagsNode = axisNode.getAttributes().getNamedItem("mmedtypeflags");
	    			Node sizeBitsNode = axisNode.getAttributes().getNamedItem("mmedelementsizebits");
	    			Node majorStrideBitsNode = axisNode.getAttributes().getNamedItem("mmedmajorstridebits");
	    			Node minorStrideBitsNode = axisNode.getAttributes().getNamedItem("mmedminorstridebits");
	    				    			
	    			int flags = 0;
	    			int sizeBits = 0; 
	    			//int majorStrideBits = 0; HexUtil.hexToInt(majorStrideBitsNode.getNodeValue());
	    			//int minorStrideBits = 0; HexUtil.hexToInt(minorStrideBitsNode.getNodeValue());
	    			
	    			if(flagsNode != null) {
	    				flags =  HexUtil.hexToInt(flagsNode.getNodeValue());
	    			}
	    			
	    			if(sizeBitsNode != null) {
	    				sizeBits = Integer.parseInt(sizeBitsNode.getNodeValue());
	    			}
	    			else {
	    				sizeBits = bitCount;
	    			}
	    			
	    			targetTable.setAttribute("storagetype", (signed ? "" : "u") + "int" + sizeBits);
	    			targetTable.setAttribute("endian", lsbFirst ? "little" : "big");
	    			targetTable.setAttribute("storageaddress", address);
	    			
	    			if(!id.equalsIgnoreCase("z")) {
	    				targetTable.setAttribute("type", id.toUpperCase() + " Axis");
	    			}
    			}
    			else {
    				return null;
    			}
    		}
    		else if(n.getNodeName().equalsIgnoreCase("indexcount")) {
    			String indexCount = n.getTextContent();
    			tableNode.setAttribute("size"  + (id.equalsIgnoreCase("x") ? "x" : "y"), indexCount);
    		}
    		
    	}
    	
    	if(id.equalsIgnoreCase("z")) return null;
    	else
    		return axisNodeRR;   	
	}
	
	private Element parseTable(Document doc, Node romNode, Node tableNode) {
    	int nodeCountTable = tableNode.getChildNodes().getLength();
    	Node n;
    	Element tableNodeRR = doc.createElement("table");
    	
		LinkedList<String> categories = new LinkedList<String>();
		int numAxis = 0;
				
    	for(int i=0; i < nodeCountTable ; i++) {
    		n = tableNode.getChildNodes().item(i);
    		
    		if(n.getNodeName().equalsIgnoreCase("title")){
    			tableNodeRR.setAttribute("name", n.getTextContent());
    		}
    		else if(n.getNodeName().equalsIgnoreCase("categorymem")) {
    			int index = HexUtil.hexToInt(n.getAttributes().getNamedItem("index").getNodeValue());
    			int category = HexUtil.hexToInt(n.getAttributes().getNamedItem("category").getNodeValue());
    			
    			if(categoryMap.containsKey(category)) {
    				categories.add(index, categoryMap.get(category));
    			}
    		}
    		else if(n.getNodeName().equalsIgnoreCase("xdfaxis")) {
    			Element axis = parseAxis(doc, tableNodeRR, n);
    			
    			if(axis != null) {
    				numAxis++;
    				tableNodeRR.appendChild(axis);
    			}
    		}
    		
    		//TODO
    		
    	}
    	 	
    	String category = "";
    	Collections.sort(categories);
    	
    	for(int i = 0; i < categories.size(); i++) {
    		category+=categories.get(i);
    		
    		if(i < categories.size() - 1)
    			category+="//";
    	}
    	
    	tableNodeRR.setAttribute("type", (numAxis + 1) + "D");
    	tableNodeRR.setAttribute("category", category);
    	tableNodeRR.setAttribute("userlevel", "1");
    	
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
    		}
    		else if(n.getNodeName().equalsIgnoreCase("description")){
    			//TODO
    		}
    		else if(n.getNodeName().equalsIgnoreCase("BASEOFFSET")){
    			offset = HexUtil.hexToInt(n.getAttributes().getNamedItem("offset").getNodeValue());
    			
    			if(!n.getAttributes().getNamedItem("subtract").getNodeValue().equals("0")) {
    				offset*=-1;
    			}
    		}
    		else if(n.getNodeName().equalsIgnoreCase("DEFAULTS")){
    			if(!n.getAttributes().getNamedItem("float").getNodeValue().equalsIgnoreCase("0")) {
    				dataType = "float";
    			}
    			else {   				
    				bitCount = Integer.parseInt(n.getAttributes().getNamedItem("datasizeinbits").getNodeValue());
    				signed = !n.getAttributes().getNamedItem("signed").getNodeValue().equalsIgnoreCase("0");
    				
    				dataType = (signed ? "" : "u") + "int" + bitCount;
    				lsbFirst = !n.getAttributes().getNamedItem("lsbfirst").getNodeValue().equalsIgnoreCase("0");   				
    				numDigits = HexUtil.hexToInt(n.getAttributes().getNamedItem("sigdigits").getNodeValue());
    			}
    		}
    		else if(n.getNodeName().equalsIgnoreCase("REGION")){
    			//type
    			//startAddress
    			//regionFlags
    			//name
    			//desc
    			int fileSize = HexUtil.hexToInt(n.getAttributes().getNamedItem("size").getNodeValue());  
    	    	Node fileSizeN = doc.createElement("filesize");
    	    	fileSizeN.setTextContent(fileSize + "b");
    		}		
    	}
    		
    	Node idAddress = doc.createElement("internalidaddress");
    	//idAddress.setTextContent("0x" + Integer.toHexString(bestIDFitAddress));

    	Node idString = doc.createElement("internalidstring");
    	//idString.setTextContent("0x" + bestIDFitData.replace(" ", ""));
    	
    	//This can be used to force a definition file for a bin
    	idString.setTextContent("force");
    	idAddress.setTextContent("-1");
    	
    	Node ramoffset = doc.createElement("noramoffset"); 	
	  	
    	romIDNode.appendChild(ramoffset);
    	romIDNode.appendChild(idAddress);
    	romIDNode.appendChild(idString);
    	romIDNode.appendChild(doc.createElement("year"));
    	romIDNode.appendChild(doc.createElement("market"));
    	romIDNode.appendChild(doc.createElement("transmission"));
    	romIDNode.appendChild(doc.createElement("xmlid"));
    	   	
    	return romIDNode;
	}
	
	private Document convertXDFDocument(Document xdfDoc) {
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
        
        if(baseNode.getFirstChild() != null && baseNode.getFirstChild().getNodeName().equalsIgnoreCase("XDFFORMAT")){
        	baseNode = baseNode.getFirstChild();
        	
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
        		LOGGER.error("XDF file does not have an XDFHEADER element!");
        		return null;
        	}
        	
        	//Go through all tables
        	for(int i=0; i < nodeCount; i++) {
        		Node n = baseNode.getChildNodes().item(i);
               	if(n.getNodeName().equalsIgnoreCase("XDFTABLE")){
               		Element table = parseTable(doc, romNode, n);
                	romNode.appendChild(table);
            	}
            } 	
        }
        else {
        	LOGGER.error("XDF file does not have an XDFFORMAT element!");
        	return null;
        }	
        
        System.out.println(convertDocumentToString(doc));
        return doc;
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
		
		settings.getEcuDefinitionFiles().clear();
		settings.getEcuDefinitionFiles().add(new File("C:\\Users\\User\\Downloads\\BMW-XDFs-master\\9E60B.xdf"));
	
		OpenImageWorker w = new OpenImageWorker(new File("C:\\Users\\User\\Downloads\\BMW-XDFs-master\\9E60B_original.bin"));

		w.execute();
	}
}



