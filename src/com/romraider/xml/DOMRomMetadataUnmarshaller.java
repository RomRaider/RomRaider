package com.romraider.xml;

import static org.w3c.dom.Node.ELEMENT_NODE;
import static com.romraider.xml.DOMHelper.unmarshallAttribute;
import static com.romraider.xml.DOMHelper.unmarshallText;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Vector;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.romraider.metadata.RomID;
import com.romraider.metadata.RomMetadata;
import com.sun.org.apache.xerces.internal.parsers.DOMParser;

public class DOMRomMetadataUnmarshaller {
	
	public static RomMetadata unmarshallRomMetadata(RomID romid, RomMetadata romMetadata) throws SAXException, IOException {
		
		InputSource src = new InputSource(new FileInputStream(romid.getDefinitionFile()));
		DOMParser parser = new DOMParser();
		parser.parse(src);
		Node root = parser.getDocument().getDocumentElement();
		
		
		return null;
		
	}

	
	public static Vector<RomID> unmarshallRomIDIndex(File file) throws SAXException, IOException {
		
		Vector<RomID> romVector = new Vector<RomID>();
		InputSource src = new InputSource(new FileInputStream(file));
		DOMParser parser = new DOMParser();
		parser.parse(src);
		Node root = parser.getDocument().getDocumentElement();
		RomID romid = new RomID();
		NodeList children = root.getChildNodes();
		
		// Iterate through document nodes and find romids
		for (int i = 0; i < children.getLength(); i++) {
			Node n = children.item(i);
			if (n.getNodeType() == ELEMENT_NODE && n.getNodeName().equalsIgnoreCase("romid")) {
				romid = unmarshallRomID(n, romid);
				romid.setDefinitionFile(file);
				romVector.add(romid);
			} else if (n.getNodeType() == ELEMENT_NODE && n.getNodeName().equalsIgnoreCase("include")) {
				romid.setInclude(unmarshallText(n));
			}
		}
	
		return romVector;
	}
	
	
	public static RomID unmarshallRomID(Node src, RomID romid) {
		NodeList children = src.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node n = children.item(i);
						
			if (n.getNodeType() == ELEMENT_NODE) {
				if 		(n.getNodeName().equalsIgnoreCase("xmlid")) 			
					romid.setXmlID(unmarshallText(n));
				else if (n.getNodeName().equalsIgnoreCase("internalidstring")) 	
					romid.setInternalIDString(unmarshallText(n));
				else if (n.getNodeName().equalsIgnoreCase("internalidaddress")) 
					romid.setInternalIDAddress(Integer.parseInt(unmarshallText(n)));
				else if (n.getNodeName().equalsIgnoreCase("ecuid")) 			
					romid.setEcuid(unmarshallText(n));
				else if (n.getNodeName().equalsIgnoreCase("year")) 			
					romid.setYear(unmarshallText(n));
				else if (n.getNodeName().equalsIgnoreCase("market")) 			
					romid.setMarket(unmarshallText(n));
				else if (n.getNodeName().equalsIgnoreCase("make")) 			
					romid.setMake(unmarshallText(n));
				else if (n.getNodeName().equalsIgnoreCase("model")) 			
					romid.setModel(unmarshallText(n));
				else if (n.getNodeName().equalsIgnoreCase("submodel")) 			
					romid.setSubmodel(unmarshallText(n));
				else if (n.getNodeName().equalsIgnoreCase("transmission")) 			
					romid.setTransmission(unmarshallText(n));
				else if (n.getNodeName().equalsIgnoreCase("memmodel")) 			
					romid.setMemmodel(unmarshallText(n));
				else if (n.getNodeName().equalsIgnoreCase("flashmethod")) 			
					romid.setFlashMethod(unmarshallText(n));
			}
			
		}		
		
		return romid;
	}
}
