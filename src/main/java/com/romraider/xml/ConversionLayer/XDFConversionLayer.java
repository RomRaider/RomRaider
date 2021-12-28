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
import java.util.HashMap;
import java.util.LinkedList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
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
   
   private String title;
   private int fileSize;
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
	
	private void parseTable(Document doc, Node baseNode) {
    	int nodeCountTable = baseNode.getChildNodes().getLength();
    	Node n;
    	
		String title;
		LinkedList<String> categories = new LinkedList<String>();
		
    	for(int i=0; i < nodeCountTable ; i++) {
    		n = baseNode.getChildNodes().item(i);
    		
    		if(n.getNodeName().equalsIgnoreCase("title")){
    			title = n.getTextContent();
    		}
    		else if(n.getNodeName().equalsIgnoreCase("categorymem")) {
    			int index = HexUtil.hexToInt(n.getAttributes().getNamedItem("index").getNodeValue());
    			int category = HexUtil.hexToInt(n.getAttributes().getNamedItem("category").getNodeValue());
    			categories.add(index, categoryMap.get(category));
    		}
    		
    		//TODO
    		
    	}

	}
	
	private void parseXDFHeader(Document doc, Node header) {
    	int nodeCountHeader = header.getChildNodes().getLength();
    	Node n;
    	
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
    			title = n.getTextContent();
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
    				String bitCount = n.getAttributes().getNamedItem("datasizeinbits").getNodeValue();
    				boolean signed = !n.getAttributes().getNamedItem("signed").getNodeValue().equalsIgnoreCase("0");
    				
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
    			fileSize = HexUtil.hexToInt(n.getAttributes().getNamedItem("size").getNodeValue());   			
    		}		
    	}
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
        int nodeCount = 0;
        
        if(baseNode.getFirstChild() != null && baseNode.getFirstChild().getNodeName().equalsIgnoreCase("XDFFORMAT")){
        	baseNode = baseNode.getFirstChild();
        	
        	nodeCount = baseNode.getChildNodes().getLength();       	
        	Node header = null;
        	
        	//Find XDF Header first
        	for(int i=0; i < nodeCount; i++) {
        		Node n = baseNode.getChildNodes().item(i);
               	if(n.getNodeName().equalsIgnoreCase("XDFHEADER")){
               		header = n;
               		parseXDFHeader(doc, header);
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
               		parseTable(doc, header);
            	}
            } 	
        }
        else {
        	LOGGER.error("XDF file does not have an XDFFORMAT element!");
        	return null;
        }	
        
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



