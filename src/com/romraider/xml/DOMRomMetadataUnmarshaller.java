package com.romraider.xml;

import static org.w3c.dom.Node.ELEMENT_NODE;
import static com.romraider.xml.DOMHelper.unmarshallAttribute;
import static com.romraider.xml.DOMHelper.unmarshallText;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Vector;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.sun.org.apache.xerces.internal.parsers.DOMParser;

public class DOMRomMetadataUnmarshaller {

	public static Vector<RomID> getRomID(File file) throws SAXException, IOException {
		Vector<RomID> romVector = new Vector<RomID>();
		DOMParser parser = new DOMParser();
		InputSource src = new InputSource(new FileInputStream(file));
		parser.parse(src);
		Node root = parser.getDocument().getDocumentElement();
		RomID romid = new RomID();
		
		NodeList children = root.getChildNodes();
		
		//
		// Iterate through document nodes and find romids
		//
		for (int i = 0; i < children.getLength(); i++) {
			Node n = children.item(i);
			if (n.getNodeType() == ELEMENT_NODE && n.getNodeName().equalsIgnoreCase("romid")) {
				romid = unmarshallRomID(n);
				romid.setFile(file);
				romVector.add(romid);
			} else if (n.getNodeType() == ELEMENT_NODE && n.getNodeName().equalsIgnoreCase("include")) {
				romid.setInclude(unmarshallText(n));
			}
		}
	
		return romVector;
	}
	
	
	public static RomID unmarshallRomID(Node src) {
		RomID romid = new RomID(); 
		NodeList children = src.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node n = children.item(i);
						
			if (n.getNodeType() == ELEMENT_NODE) {
				if 		(n.getNodeName().equalsIgnoreCase("xmlid")) 			romid.setXmlID(unmarshallText(n));
				else if (n.getNodeName().equalsIgnoreCase("internalidstring")) 	romid.setInternalIDString(unmarshallText(n));
				else if (n.getNodeName().equalsIgnoreCase("internalidaddress")) romid.setInternalIDAddress(Integer.parseInt(unmarshallText(n)));
			}
			
			// TODO: Deal with inheritance
		}		
		
		return romid;
	}
}
