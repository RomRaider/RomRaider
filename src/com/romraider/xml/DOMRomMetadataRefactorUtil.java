package com.romraider.xml;

import static org.w3c.dom.Node.ELEMENT_NODE;

import javax.naming.directory.BasicAttribute;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import com.romraider.xml.DOMHelper;
import com.romraider.metadata.RomIndexID;
import com.sun.org.apache.xerces.internal.dom.AttrImpl;

public abstract class DOMRomMetadataRefactorUtil {

	public static Document refactorDocument(Document d) {
		Node root = d.getDocumentElement();
		NodeList children = root.getChildNodes();
		
		// Iterate through document nodes and find romids
		for (int i = 0; i < children.getLength(); i++) {
			Node c = children.item(i);
			if (c.getNodeType() == ELEMENT_NODE && c.getNodeName().equalsIgnoreCase("romid")) {
				refactorRomid(root, c);
				root.removeChild(c);
			}
		}
		return d;
	}
	
	
	public static Node refactorRomid (Node root, Node romid) {
		NodeList children = romid.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node c = children.item(i);

			if (c.getNodeType() == ELEMENT_NODE && c.getNodeName().equalsIgnoreCase("xmlid")) {
				DOMHelper.setAttribute(root, "name", DOMHelper.unmarshallText(c));
			} else if (c.getNodeType() == ELEMENT_NODE && c.getNodeName().equalsIgnoreCase("xmlid")) {
				
			}
		}
		// DOMHelper.setAttribute(node, value, attribute)
		return root;
	}
}