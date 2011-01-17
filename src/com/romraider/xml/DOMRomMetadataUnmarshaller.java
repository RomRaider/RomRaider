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

import com.romraider.metadata.AbstractMultiDimensionTableMetadata;
import com.romraider.metadata.RomID;
import com.romraider.metadata.RomMetadata;
import com.romraider.metadata.ScalingMetadata;
import com.romraider.metadata.AbstractTableMetadata;
import com.romraider.metadata.ScalingMetadataNotFoundException;
import com.romraider.metadata.Table1DMetadata;
import com.romraider.metadata.Table2DMetadata;
import com.romraider.metadata.Table3DMetadata;
import com.romraider.metadata.TableMetadataNotFoundException;
import com.sun.org.apache.xerces.internal.impl.xpath.regex.ParseException;
import com.sun.org.apache.xerces.internal.parsers.DOMParser;

public class DOMRomMetadataUnmarshaller {
	
	public static RomMetadata unmarshallRomMetadata(RomID romid, RomMetadata r) throws SAXException, IOException {
		
		System.out.println("Unmarshalling " + romid.getXmlid() + " ...");
		
		InputSource src = new InputSource(new FileInputStream(romid.getDefinitionFile()));
		DOMParser parser = new DOMParser();
		parser.parse(src);
		Node root = parser.getDocument().getDocumentElement();
		NodeList children = root.getChildNodes();
		
		for (int i = 0; i < children.getLength(); i++) {
			Node n = children.item(i);
			if (n.getNodeType() == ELEMENT_NODE && n.getNodeName().equalsIgnoreCase("romid")) {
				RomID t;
				if (r.getRomid() == null) t = new RomID();
				else t = r.getRomid();
				r.setRomID(unmarshallRomID(n, t));
			} else if (n.getNodeType() == ELEMENT_NODE && n.getNodeName().equalsIgnoreCase("scaling")) {
				r.add(unmarshallScalingMetadata(n, r));
			} else if (n.getNodeType() == ELEMENT_NODE && n.getNodeName().equalsIgnoreCase("table")) {
				r.add(unmarshallTableMetadata(n, r, null));
			}
		}
		return r;	
	}
	
	
	public static ScalingMetadata unmarshallScalingMetadata(Node n, RomMetadata r) {
		
		ScalingMetadata s;
		s = r.getScalingMetadata(unmarshallAttribute(n, "name", "<null>"));		
		s.setName(unmarshallAttribute(n, "name", s.getName()));
		s.setUnits(unmarshallAttribute(n, "units", s.getUnits()));
		s.setToexpr(unmarshallAttribute(n, "toexpr", s.getToexpr()));
		s.setFrexpr(unmarshallAttribute(n, "frexpr", s.getFrexpr()));
		s.setFormat(unmarshallAttribute(n, "format", s.getFormat()));
		s.setMin(Double.parseDouble(unmarshallAttribute(n, "min", s.getMin()+"")));
		s.setMax(Double.parseDouble(unmarshallAttribute(n, "max", s.getMax()+"")));
		s.setStorageType(unmarshallAttribute(n, "storagetype", s.getStorageType()));
		
		// Convert endian to string
		String scalingEndian;
		if (s.getEndian() == ScalingMetadata.ENDIAN_LITTLE) scalingEndian = "little";
		else scalingEndian = "big";
			
		scalingEndian = unmarshallAttribute(n, "endian", scalingEndian);
		if (scalingEndian.equalsIgnoreCase("little")) s.setEndian(ScalingMetadata.ENDIAN_LITTLE);
		else s.setEndian(ScalingMetadata.ENDIAN_BIG);
		
		return s;
	}
	
	
	public static AbstractTableMetadata unmarshallTableMetadata(Node n, RomMetadata r, AbstractMultiDimensionTableMetadata p) {
		
		// Try to find table from parent rom or create new
		AbstractTableMetadata t;
		String tableType = unmarshallAttribute(n, "type", "<null>");
	
		try { 
			//System.out.println(unmarshallAttribute(n, "name", "<null>"));
			if (p == null) t = r.getTableMetadata(unmarshallAttribute(n, "name", "<null>"));
			else t = p.getAxisByName(unmarshallAttribute(n, "name", "<null>"));
		} catch (TableMetadataNotFoundException e) {
			
			if (tableType.equalsIgnoreCase("3d")) 		t = new Table3DMetadata();
			else if (tableType.equalsIgnoreCase("2d")) 	t = new Table2DMetadata();
			else if (tableType.equalsIgnoreCase("1d")) 	t = new Table1DMetadata();
			else if (tableType.toLowerCase().contains("axis")) t = new Table1DMetadata();
			else {
				throw new ParseException(tableType, 0);
			}
		}

		
		// Unmarshall attributes
		t.setName(unmarshallAttribute(n, "name", t.getName()));
		t.setCategory(unmarshallAttribute(n, "name", t.getCategory()));
		t.setDescription(unmarshallAttribute(n, "name", t.getDescription()));
		
		if (!unmarshallAttribute(n, "scaling", "<null>").equals("<null>")) {
			t.setScalingMetadata(r.getScalingMetadata(unmarshallAttribute(n, "scaling", "<null>")));
		}		
		
		NodeList children = n.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node c = children.item(i);
			if (c.getNodeType() == ELEMENT_NODE && n.getNodeName().equalsIgnoreCase("scaling")) {
				t.setDescription(unmarshallText(c));
			}
		}
		
		// Unmarshall type-specific attributes and elements
		if (tableType.equalsIgnoreCase("3d")) 		t = unmarshallTable3DMetadata(n, r, t);
		else if (tableType.equalsIgnoreCase("2d")) 	t = unmarshallTable2DMetadata(n, r, t);
		
		return t;
	}


	public static Table3DMetadata unmarshallTable3DMetadata(Node n, RomMetadata r, AbstractTableMetadata t) {
		Table3DMetadata output = (Table3DMetadata)t;
		NodeList children = n.getChildNodes();
		
		for (int i = 0; i < children.getLength(); i++) {
			Node c = children.item(i);			
			
			if (c.hasAttributes() || c.hasChildNodes()) {				
				// TODO: The line above should not be needed. What are the elements that have no attributes or child nodes??
				String tableType = unmarshallAttribute(c, "type", "<null>");
				if (!tableType.equalsIgnoreCase("<null>")) {
					Table1DMetadata axis = (Table1DMetadata) unmarshallTableMetadata(c, r, (Table3DMetadata) t);				
					if (axis.getType() == AbstractTableMetadata.TABLEMETADATA_TYPE_XAXIS) output.setXaxis(axis);
					else if (axis.getType() == AbstractTableMetadata.TABLEMETADATA_TYPE_YAXIS) output.setYaxis(axis);
				}
			}
		}
		return output;
	}
	
	
	public static Table2DMetadata unmarshallTable2DMetadata(Node n, RomMetadata r, AbstractTableMetadata t) {
		Table2DMetadata output = (Table2DMetadata)t;
		NodeList children = n.getChildNodes();
		//System.out.println(t.getName());
		
		for (int i = 0; i < children.getLength(); i++) {
			Node c = children.item(i);
			if (c.hasAttributes() || c.hasChildNodes()) {
				String tableType = unmarshallAttribute(c, "type", "<null>");
				if (!tableType.equalsIgnoreCase("<null>")) {
					Table1DMetadata axis = (Table1DMetadata) unmarshallTableMetadata(c, r, (Table2DMetadata) t);			
					if (axis.getType() == AbstractTableMetadata.TABLEMETADATA_TYPE_AXIS) output.setAxis(axis);
				}
			}
		}
		return output;
	}
	
	
	// TODO: Is this method needed?
	/*public static Table1DMetadata unmarshallTable1DMetadata(Node n, RomMetadata r, AbstractTableMetadata t) {
		Table1DMetadata output = (Table1DMetadata)t;
		return output;
	}*/
	
	
	public static Table1DMetadata unmarshallTableAxisMetadata(Node n, RomMetadata r, AbstractTableMetadata t) {
		AbstractMultiDimensionTableMetadata abs = (AbstractMultiDimensionTableMetadata)t;
		Table1DMetadata axis = abs.getAxisByType(unmarshallAttribute(n, "type", "<null>"));
		
		return axis;
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
					romid.setInternalIDAddress(Integer.parseInt(unmarshallText(n), 16));
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