package com.romraider.xml;

import static org.w3c.dom.Node.ELEMENT_NODE;
import static com.romraider.xml.DOMHelper.unmarshallAttribute;
import static com.romraider.xml.DOMHelper.unmarshallText;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Vector;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import com.romraider.metadata.exception.ScalingMetadataNotFoundException;
import com.romraider.metadata.exception.TableMetadataNotFoundException;
import com.romraider.metadata.index.RomIndexID;
import com.romraider.metadata.index.RomMetadata;
import com.romraider.metadata.rom.AbstractTableMetadata;
import com.romraider.metadata.rom.ScalingMetadata;
import com.romraider.metadata.rom.ScalingMetadataList;
import com.romraider.metadata.rom.Table1DMetadata;
import com.romraider.metadata.rom.Table2DMetadata;
import com.romraider.metadata.rom.Table3DMetadata;
import com.romraider.metadata.rom.TableAxisMetadata;
import com.romraider.metadata.rom.TableMetadataList;
import com.sun.org.apache.xerces.internal.impl.xpath.regex.ParseException;
import com.sun.org.apache.xerces.internal.parsers.DOMParser;

public class DOMRomMetadataUnmarshaller {
	
	private RomMetadata rom;
	private TableMetadataList tables;
	private ScalingMetadataList scalings;
	
	public DOMRomMetadataUnmarshaller() {
		rom = new RomMetadata();
		tables = new TableMetadataList();
		scalings = new ScalingMetadataList();
	}
	
	public RomMetadata getRomMetadata() {
		rom.setScalingMetadata(scalings);
		rom.setTableMetadata(tables);
		return rom;
	}
	
	public void unmarshallRomMetadata(RomIndexID romid) throws SAXException, IOException, TransformerFactoryConfigurationError, TransformerException {
		System.out.println("Unmarshalling " + romid.getXmlid() + " ...");

		DOMParser parser = new DOMParser();
		parser.parse(new InputSource(romid.getDefinitionFile().getAbsolutePath()));
		Transformer t = TransformerFactory.newInstance().newTransformer(new StreamSource("./metadata.xsl"));
		DOMResult result = new DOMResult();
		t.transform(new DOMSource(parser.getDocument().getDocumentElement()), result);
		
		Node root = result.getNode().getFirstChild();
		NodeList children = root.getChildNodes();
		
		rom.setId(unmarshallAttribute(root, "id", rom.getId()));
		rom.setInternalidstring(unmarshallAttribute(root, "internalidstring", rom.getInternalidstring()));
		rom.setInternalidaddress(Integer.parseInt(unmarshallAttribute(root, "internalidaddress", rom.getInternalidaddress()+"")));
		rom.setYear(unmarshallAttribute(root, "year", rom.getYear()));
		rom.setMarket(unmarshallAttribute(root, "market", rom.getMarket()));
		rom.setMake(unmarshallAttribute(root, "make", rom.getMake()));
		rom.setModel(unmarshallAttribute(root, "model", rom.getModel()));
		rom.setSubmodel(unmarshallAttribute(root, "submodel", rom.getSubmodel()));
		rom.setTransmission(unmarshallAttribute(root, "transmission", rom.getTransmission()));
		rom.setMemmodel(unmarshallAttribute(root, "memmodel", rom.getMemmodel()));
		rom.setFlashmethod(unmarshallAttribute(root, "flashmethod", rom.getFlashmethod()));
		
		for (int i = 0; i < children.getLength(); i++) {
			Node n = children.item(i);
			if (n.getNodeType() == ELEMENT_NODE && n.getNodeName().equalsIgnoreCase("scaling")) {
				unmarshallScalingMetadata(n);
			} else if (n.getNodeType() == ELEMENT_NODE && n.getNodeName().equalsIgnoreCase("table")) {
				try {
					unmarshallTableMetadata(n);
				} catch (ScalingMetadataNotFoundException x) {
					// TODO omit tables with invalid scaling
				}
			}
		}	
	}
	
	public void unmarshallScalingMetadata(Node n) {
		ScalingMetadata s;
		try {
			s = scalings.get(unmarshallAttribute(n, "name", "<null>"));
		} catch (ScalingMetadataNotFoundException x) {
			s = new ScalingMetadata();
			scalings.add(s);
		}
		s.setId(unmarshallAttribute(n, "id", s.getId()));
		s.setUnits(unmarshallAttribute(n, "units", s.getUnits()));
		s.setToexpr(unmarshallAttribute(n, "toexpr", s.getToexpr()));
		s.setFrexpr(unmarshallAttribute(n, "frexpr", s.getFrexpr()));
		s.setFormat(unmarshallAttribute(n, "format", s.getFormat()));
		s.setMin(Double.parseDouble(unmarshallAttribute(n, "min", s.getMin()+"")));
		s.setMax(Double.parseDouble(unmarshallAttribute(n, "max", s.getMax()+"")));
		s.setStorageType(unmarshallAttribute(n, "storagetype", s.getStorageType()));
		String scalingEndian;
		if (s.getEndian() == ScalingMetadata.ENDIAN_LITTLE) scalingEndian = "little";
		else scalingEndian = "big";			
		scalingEndian = unmarshallAttribute(n, "endian", scalingEndian);
		if (scalingEndian.equalsIgnoreCase("little")) s.setEndian(ScalingMetadata.ENDIAN_LITTLE);
		else s.setEndian(ScalingMetadata.ENDIAN_BIG);
	}
	
	public void unmarshallTableMetadata(Node n) throws ScalingMetadataNotFoundException {	
		AbstractTableMetadata t;
		String tableType = unmarshallAttribute(n, "type", "<null>");
		String tableId = unmarshallAttribute(n, "id","<null>");	
		try { 
			 t = tables.get(tableId);
		} catch (TableMetadataNotFoundException e) {
			
			if (tableType.equalsIgnoreCase("3d")) 		t = new Table3DMetadata();
			else if (tableType.equalsIgnoreCase("2d")) 	t = new Table2DMetadata();
			else if (tableType.equalsIgnoreCase("1d")) 	t = new Table1DMetadata();
			else {
				System.out.println(tableId);
				System.out.println(tableType);
				throw new ParseException(tableId, 0);
			}
			tables.add(t);
		}
		t.setId(unmarshallAttribute(n, "id", t.getId()));
		t.setCategory(unmarshallAttribute(n, "category", t.getCategory()));
		t.setDescription(unmarshallAttribute(n, "description", t.getDescription()));
		if (!unmarshallAttribute(n, "scaling", "<null>").equals("<null>")) {
			t.setScalingMetadata(scalings.get(unmarshallAttribute(n, "scaling", "<null>")));
		}				
		NodeList children = n.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node c = children.item(i);
			if (c.getNodeType() == ELEMENT_NODE && n.getNodeName().equalsIgnoreCase("table")) {
				unmarshallTableAxis(c, t);
			}
		}
	}
	
	public void unmarshallTableAxis(Node n, AbstractTableMetadata t) throws ScalingMetadataNotFoundException {
		TableAxisMetadata a;
		String tableAxis = unmarshallAttribute(n, "axis", "<null>");
		String parentType = t.getClass().getName();
		if (parentType.equals("com.romraider.metadata.rom.Table3DMetadata")) {
			if (tableAxis.equalsIgnoreCase("x")) a = ((Table3DMetadata)t).getXaxis();
			else a = ((Table3DMetadata)t).getYaxis();
		}
		else a = ((Table2DMetadata)t).getYaxis();

		a.setId(unmarshallAttribute(n, "id", a.getId()));
		a.setCategory(unmarshallAttribute(n, "category", a.getCategory()));
		a.setDescription(unmarshallAttribute(n, "description", a.getDescription()));
		a.setStatic(Boolean.valueOf(unmarshallAttribute(n, "static", a.isStatic())));
		if (!unmarshallAttribute(n, "scaling", "<null>").equals("<null>")) {
			a.setScalingMetadata(scalings.get(unmarshallAttribute(n, "scaling", "<null>")));
		}		
	}

	public static Vector<RomIndexID> unmarshallRomIDIndex(File file) throws SAXException, IOException {		
		Vector<RomIndexID> romVector = new Vector<RomIndexID>();
		InputSource src = new InputSource(new FileInputStream(file));
		DOMParser parser = new DOMParser();
		parser.parse(src);
		Node root = parser.getDocument().getDocumentElement();
		RomIndexID romid = new RomIndexID();
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
	
	
	public static RomIndexID unmarshallRomID(Node src, RomIndexID romid) {
		NodeList children = src.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node n = children.item(i);						
			if (n.getNodeType() == ELEMENT_NODE) {
				if 		(n.getNodeName().equalsIgnoreCase("xmlid")) 			
					romid.setXmlid(unmarshallText(n));
				else if (n.getNodeName().equalsIgnoreCase("internalidstring")) 	
					romid.setInternalIDString(unmarshallText(n));
				else if (n.getNodeName().equalsIgnoreCase("internalidaddress")) 
					romid.setInternalIDAddress(Integer.parseInt(unmarshallText(n), 16));
			}			
		}				
		return romid;
	}
}