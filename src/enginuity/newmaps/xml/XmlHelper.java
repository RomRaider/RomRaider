/*
 *
 * Enginuity Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006 Enginuity.org
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
 *
 */

package enginuity.newmaps.xml;

import enginuity.logger.ecu.exception.ConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

final public class XmlHelper extends Object {
 
	/////////////////////
	// Class Variables //
	/////////////////////
 
	/** TODO Consider moving this to an interface with constants */
	static final public String XML_TAG_ROOT = "document";
 
	//////////////////
	// Constructors //
	//////////////////
 
	/**
	 * Default constructor.
	 */
 
	public XmlHelper() {
 
		super();
	}
 
	////////////////////
	// Static Methods //
	////////////////////
 
	static public Document newDocument()
		throws ConfigurationException {
 
		try {
			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			return builder.newDocument();
		}
		catch (ParserConfigurationException configure) {
			throw new ConfigurationException("Error instantiating DOM XML parser", configure);
		}
	}
 
	static public Document parse(InputStream in)
		throws ConfigurationException, IOException, Exception {
 
		try {
			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			return builder.parse(in);
		}
		catch (ParserConfigurationException configure) {
			throw new ConfigurationException("Error instantiating DOM XML parser", configure);
		}
		catch (SAXException parse) {
			throw new Exception("Error SAX parsing XML input stream", parse);
		}
	}
 
	static public Document parse(String uri)
		throws ConfigurationException, IOException, Exception {
 
		try {
			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			return builder.parse(uri);
		}
		catch (ParserConfigurationException configure) {
			throw new ConfigurationException("Error instantiating DOM XML parser", configure);
		}
		catch (SAXException parse) {
			throw new Exception("Error SAX parsing XML uri '" + uri + "'", parse);
		}
	}
 
	static public String serialize(Document target)
		throws IOException, Exception {
 
		ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
		serialize(target, byteOut, true);
		return byteOut.toString();
	}
	
	static public void serialize(Document document, OutputStream target)
		throws IOException, Exception {
			
		serialize(document, target, false);		
	}
 
	static public void serialize(Document document, OutputStream target, boolean indent)
		throws IOException, Exception {
 
		try {
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			if (indent)
				transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.transform(new DOMSource(document), new StreamResult(target));
		}
		catch (TransformerConfigurationException configure) {
			throw new ConfigurationException("Error instantiating XML transformer", configure);
		}
		catch (TransformerException transform) {
			/** @todo Need to write a generic transformation or serialization exception */
			throw new Exception("Error transforming XML", transform);
		}
	}
 
	static public Element getChildElement(Element parent, String name) {
 
		Element child = null;
		if (parent != null) {
			NodeList childNodes = parent.getElementsByTagName(name);
			switch (childNodes.getLength()) {
				case 0:
					System.err.println("XML element <" + parent.getNodeName() + ">" +
						" has zero child nodes <" + name + "> (expecting one node)");
				case 1:
					child = (Element) childNodes.item(0);
					break;
				default:
					child = (Element) childNodes.item(0);
					/** @todo Consider implementing a "Warning Log" for errors such as these rather than sys.err */
					System.err.println("XML element <" + parent.getNodeName() + ">" +
						" has " + childNodes.getLength() + " child nodes <" + name + "> (expecting only one)");
			}
		}
		return child;
	}
 
	static public List getAllChildElements(Element parent, String name) {
 
		List elements = null;
		if (parent != null) {
			elements = Collections.synchronizedList(new ArrayList());
			NodeList childNodes = parent.getChildNodes();
			for (int current = 0; current < childNodes.getLength(); current++) {
				Node node = childNodes.item(current);
				if ((node instanceof Element) && node.getNodeName().equals(name)) {
					elements.add(node);
				}
			}
		}
		return elements;
	}
 
	static public String getChildElementValue(Element parent, String childName) {
 
		Element child = getChildElement(parent, childName);
		return child == null ? null : getElementValue(child);
	}
 
	static public String getElementValue(Element parent) {
 
		NodeList nodes = parent.getChildNodes();
		int current = 0;
		int length = nodes.getLength();
		while (current < length) {
			Node node = nodes.item(current);
			if (node instanceof Text) {
				String value = node.getNodeValue();
				if (value != null)
					return value.trim();
			}
			current++;
		}
		return "";
	}
}