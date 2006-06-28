//DOM XML parser for ROMs

package Enginuity.Maps.XML;

import Enginuity.Maps.*;
import Enginuity.Maps.ECUDefinitionCollection;
import javax.management.modelmbean.XMLParseException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class DOMRomUnmarshaller {
    
    ECUDefinitionCollection roms = new ECUDefinitionCollection();
    
    public DOMRomUnmarshaller() { }
    
    public ECUDefinitionCollection unmarshallXMLDefinition (Node rootNode) throws XMLParseException {
	Node n;
	NodeList nodes = rootNode.getChildNodes();
	
	for (int i = 0; i<nodes.getLength(); i++) {
	    n = nodes.item(i);

	    if (n.getNodeType() == Node.ELEMENT_NODE) {              
		if (n.getNodeName().equalsIgnoreCase("rom")) {
		    roms.addRom(unmarshallRom(n));
		    
		} else {
		    // unexpected element in XMLDefinition (skip)
		}
	    } else {
		// unexpected node-type in XMLDefinition (skip)
	    }
	}
	return roms;        
    }
    
    public Rom unmarshallRom (Node rootNode) throws XMLParseException {
	Rom rom = new Rom();
        
	Node n;
	NodeList nodes = rootNode.getChildNodes();
	
	for (int i = 0; i < nodes.getLength(); i++) {
	    n = nodes.item(i);

	    if (n.getNodeType() == Node.ELEMENT_NODE) {              
		if (n.getNodeName().equalsIgnoreCase("romid")) {
		    rom.setRomID(unmarshallRomID(n));
		    
		} else if (n.getNodeName().equalsIgnoreCase("table")) { 
                    Table tempTable = unmarshallTable(n);
                    tempTable.setContainer(rom);
                    rom.addTable(tempTable);
                    
                } else {
		    // unexpected element in Rom (skip)
		}
	    } else {
		// unexpected node-type in Rom (skip)
	    }
	}
	return rom;
    }    

    private RomID unmarshallRomID (Node romIDNode) {
	RomID romID = new RomID();

	Node n;
	NodeList nodes = romIDNode.getChildNodes();
	
	for (int i=0; i < nodes.getLength(); i++){
	    n = nodes.item(i);

	    if (n.getNodeType() == Node.ELEMENT_NODE) {

		if (n.getNodeName().equalsIgnoreCase("xmlid")){
		    romID.setXmlid( unmarshallText( n ) );
		} else if (n.getNodeName().equalsIgnoreCase("internalidaddress")) {
		    romID.setInternalIdAddress(RomAttributeParser.parseHexString(unmarshallText( n )) );
		} else if (n.getNodeName().equalsIgnoreCase("internalidstring")) {
		    romID.setInternalIdString(unmarshallText( n ) );
		} else if (n.getNodeName().equalsIgnoreCase("caseid")) {
		    romID.setCaseId(unmarshallText(n));
		} else if (n.getNodeName().equalsIgnoreCase("ecuid")) {
		    romID.setEcuId(unmarshallText(n));
		} else if (n.getNodeName().equalsIgnoreCase("make")) {
		    romID.setMake(unmarshallText(n));
		} else if (n.getNodeName().equalsIgnoreCase("market")) {
		    romID.setMarket(unmarshallText(n));
		} else if (n.getNodeName().equalsIgnoreCase("model")) {
		    romID.setModel(unmarshallText(n));
		} else if (n.getNodeName().equalsIgnoreCase("submodel")) {
		    romID.setSubModel(unmarshallText(n));
		} else if (n.getNodeName().equalsIgnoreCase("transmission")) {
		    romID.setTransmission(unmarshallText(n));
		} else if (n.getNodeName().equalsIgnoreCase("year")) {
		    romID.setYear(Integer.parseInt(unmarshallText(n)));
		} else if (n.getNodeName().equalsIgnoreCase("flashmethod")) {
		    romID.setFlashMethod(unmarshallText(n));
		} else if (n.getNodeName().equalsIgnoreCase("memmodel")) {
		    romID.setMemModel(unmarshallText(n));
		} else {
		    // unexpected element in RomID (skip)
		}
	    } else {
		// unexpected node-type in RomID (skip)
	    }
	}
	return romID;
    }
   
    private Table unmarshallTable(Node tableNode) throws XMLParseException {
	Table table;    
        
        if (unmarshallAttribute(tableNode, "type", "unknown").equalsIgnoreCase("3D")) {
            table = new Table3D();
        } else if (unmarshallAttribute(tableNode, "type", "unknown").equalsIgnoreCase("2D")) {
            table = new Table2D();
        } else if (unmarshallAttribute(tableNode, "type", "unknown").equalsIgnoreCase("1D")) {
            table = new Table1D();
        } else if (unmarshallAttribute(tableNode, "type", "unknown").equalsIgnoreCase("X Axis") ||
                   unmarshallAttribute(tableNode, "type", "unknown").equalsIgnoreCase("Y Axis")) {
            table = new Table1D();
            ((Table1D)table).setIsAxis(true);
        } else if (unmarshallAttribute(tableNode, "type", "unknown").equalsIgnoreCase("Static Y Axis") ||
                   unmarshallAttribute(tableNode, "type", "unknown").equalsIgnoreCase("Static X Axis")) {
            table = new Table1D();
            table.setIsStatic(true);
            ((Table1D)table).setIsAxis(true);
        } else {
            //exception
            throw new XMLParseException();
        }
            
        table.setName(unmarshallAttribute(tableNode, "name", "unknown"));
        table.setType(RomAttributeParser.parseTableType(unmarshallAttribute(tableNode, "type", "1D")));
        table.setCategory(unmarshallAttribute(tableNode, "category", "unknown"));
        table.setStorageType(RomAttributeParser.parseStorageType(unmarshallAttribute(tableNode, "storagetype", "uint8")));
        table.setEndian(RomAttributeParser.parseEndian(unmarshallAttribute(tableNode, "endian", "unknown")));
        table.setStorageAddress(RomAttributeParser.parseHexString(unmarshallAttribute(tableNode, "storageaddress", "0")));
        table.setDescription(unmarshallAttribute(tableNode, "description", "unknown"));
        table.setDataSize(Integer.parseInt(unmarshallAttribute(tableNode, "sizey", unmarshallAttribute(tableNode, "sizex", "0"))));
        table.setFlip(Boolean.parseBoolean(unmarshallAttribute(tableNode, "flipy", unmarshallAttribute(tableNode, "flipx", "false"))));
        
        if (table.getType() == Table.TABLE_3D) {
            ((Table3D)table).setFlipX(Boolean.parseBoolean(unmarshallAttribute(tableNode, "flipx", "unknown")));
            ((Table3D)table).setFlipY(Boolean.parseBoolean(unmarshallAttribute(tableNode, "flipy", "unknown")));  
            ((Table3D)table).setSizeX(Integer.parseInt(unmarshallAttribute(tableNode, "sizex", "1")));
            ((Table3D)table).setSizeY(Integer.parseInt(unmarshallAttribute(tableNode, "sizey", "1")));          
        }
	
	Node n;
	NodeList nodes = tableNode.getChildNodes();
	
	for (int i = 0; i < nodes.getLength(); i++) {
	    n = nodes.item(i);

	    if (n.getNodeType() == Node.ELEMENT_NODE) {

		if (n.getNodeName().equalsIgnoreCase("table")) {
		    if (table.getType() == Table.TABLE_2D) {
                        if ((unmarshallTable(n).getType() == Table.TABLE_Y_AXIS) ||                            
                                (unmarshallTable(n).getType() == Table.TABLE_X_AXIS)) {
                            Table tempTable = (Table1D)(unmarshallTable(n));
                            tempTable.setAxisParent(table);
                            ((Table2D)table).setAxis((Table1D)tempTable);
                        }
                    }  else if (table.getType() == Table.TABLE_3D) {
                        if (unmarshallTable(n).getType() == Table.TABLE_X_AXIS) {
                            Table tempTable = (Table1D)(unmarshallTable(n));
                            tempTable.setAxisParent(table);
                            tempTable.setDataSize(((Table3D)table).getSizeX());
                            ((Table3D)table).setXAxis((Table1D)tempTable);
                        } else if (unmarshallTable(n).getType() == Table.TABLE_Y_AXIS) {
                            Table tempTable = (Table1D)(unmarshallTable(n));
                            tempTable.setAxisParent(table);                         
                            ((Table3D)table).setYAxis((Table1D)tempTable);
                        }             
                    }
                } else if (n.getNodeName().equalsIgnoreCase("scaling")) {
                    table.setScale(unmarshallScale(n));
		} else if (n.getNodeName().equalsIgnoreCase("data")) {
                    // parse and add data to table
                    DataCell dataCell = new DataCell();
                    dataCell.setRealValue(unmarshallText(n));
                    dataCell.setTable(table);
                    ((Table1D)table).addStaticDataCell(dataCell);
                } else {
		    // unexpected element in Table (skip)
		}
	    } else {
		// unexpected node-type in Table (skip)
	    }
	}
        
        // set axis sizes
        if (table.getType() == Table.TABLE_2D) {
            if (((Table2D)table).getAxis().isStatic() == false) ((Table2D)table).getAxis().setDataSize(table.getDataSize());            
        } else if (table.getType() == Table.TABLE_3D) {
            if (((Table3D)table).getXAxis().isStatic() == false) ((Table3D)table).getXAxis().setDataSize(((Table3D)table).getSizeX());
            if (((Table3D)table).getYAxis().isStatic() == false) ((Table3D)table).getYAxis().setDataSize(((Table3D)table).getSizeY());
        }
	return table;
    }   
    
    private Scale unmarshallScale (Node scaleNode) {
	Scale scale = new Scale();
        
        scale.setUnit(unmarshallAttribute(scaleNode, "units", ""));
        scale.setExpression(unmarshallAttribute(scaleNode, "expression", "x"));
        scale.setFormat(unmarshallAttribute(scaleNode, "format", "#"));
        scale.setIncrement(Integer.parseInt(unmarshallAttribute(scaleNode, "increment", "1")));
        
	return scale;
    }  
    
    private String unmarshallText(Node textNode) {
	StringBuffer buf = new StringBuffer();

	Node n;
	NodeList nodes = textNode.getChildNodes();

	for (int i = 0; i < nodes.getLength(); i++){
	    n = nodes.item(i);

	    if (n.getNodeType() == Node.TEXT_NODE) {
		buf.append(n.getNodeValue());
	    } else {
		// expected a text-only node (skip)
	    }
	}
	return buf.toString();
    }    
    
    private String unmarshallAttribute(Node node, String name, String defaultValue) {
	Node n = node.getAttributes().getNamedItem(name);
	return (n!=null)?(n.getNodeValue()):(defaultValue);
    }    
}