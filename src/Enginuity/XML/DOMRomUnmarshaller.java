//DOM XML parser for ROMs

package Enginuity.XML;

import Enginuity.Maps.*;
import javax.management.modelmbean.XMLParseException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class DOMRomUnmarshaller {
    
    public DOMRomUnmarshaller() { }
    
    public Rom unmarshallXMLDefinition (Node rootNode, byte[] input) throws RomNotFoundException, XMLParseException, StackOverflowError {
        try {
            Node n;
            NodeList nodes = rootNode.getChildNodes();

            for (int i = 0; i < nodes.getLength(); i++) { 
                n = nodes.item(i);
                
                if (n.getNodeType() == Node.ELEMENT_NODE && n.getNodeName().equalsIgnoreCase("rom")) {  
                    Node n2;
                    NodeList nodes2 = n.getChildNodes();
                    
                    for (int z = 0; z < nodes2.getLength(); z++) {
                        n2 = nodes2.item(z);
                        if (n2.getNodeType() == Node.ELEMENT_NODE && n2.getNodeName().equalsIgnoreCase("romid")) {

                            RomID romID = unmarshallRomID(n2, new RomID());
                            String ecuID = new String(input, romID.getInternalIdAddress(), romID.getInternalIdString().length());
                            
                            if (romID.getInternalIdString().equalsIgnoreCase(ecuID) && !ecuID.equalsIgnoreCase("")) {
                                Rom output = unmarshallRom(n, new Rom());
                                
                                //set ram offset
                                output.getRomID().setRamOffset(output.getRomID().getFileSize() - input.length);
                                return output;
                            }
                        }
                    }
                }
            }
        } catch (NullPointerException ex) {
            System.out.println(ex);            
        } catch (StackOverflowError ex) { //endless loop in includes
            throw new StackOverflowError();
        }
        throw new RomNotFoundException();
    }                   
    
    public Rom unmarshallRom (Node rootNode, Rom rom) throws XMLParseException, RomNotFoundException, StackOverflowError {
	Node n;
	NodeList nodes = rootNode.getChildNodes();
        
        if (!unmarshallAttribute(rootNode, "base", "none").equalsIgnoreCase("none")) {
            rom = getBaseRom(rootNode.getParentNode(), unmarshallAttribute(rootNode, "base", "none"), rom);
            rom.getRomID().setObsolete(false);
        }           
	
	for (int i = 0; i < nodes.getLength(); i++) {
	    n = nodes.item(i);

	    if (n.getNodeType() == Node.ELEMENT_NODE) {              
		if (n.getNodeName().equalsIgnoreCase("romid")) {
		    rom.setRomID(unmarshallRomID(n, rom.getRomID()));
		    
		} else if (n.getNodeName().equalsIgnoreCase("table")) { 
                    Table table = null;          
                    try {
                        table = rom.getTable(unmarshallAttribute(n, "name", "unknown"));
                    } catch (TableNotFoundException e) { /* table does not already exist (do nothing) */ }                                  
                    
                    try {
                        table = unmarshallTable(n, table, rom);  
                        table.setContainer(rom);     
                        rom.addTable(table);
                    } catch (TableIsOmittedException ex) { // table is not supported in inherited def (skip)
                        rom.removeTable(table.getName());
                    }
                    
                } else { /* unexpected element in Rom (skip)*/ }
	    } else { /* unexpected node-type in Rom (skip)*/ }
	}
	return rom;
    }    
    
    public Rom getBaseRom(Node rootNode, String xmlID, Rom rom) throws XMLParseException, RomNotFoundException {
        Node n;
        NodeList nodes = rootNode.getChildNodes();  
        
        try {
            for (int i = 0; i < nodes.getLength(); i++) { 
                n = nodes.item(i);

                if (n.getNodeType() == Node.ELEMENT_NODE && n.getNodeName().equalsIgnoreCase("rom")) {  
                    Node n2;
                    NodeList nodes2 = n.getChildNodes();

                    for (int z = 0; z < nodes2.getLength(); z++) {
                        n2 = nodes2.item(z);
                        if (n2.getNodeType() == Node.ELEMENT_NODE && n2.getNodeName().equalsIgnoreCase("romid")) {

                            RomID romID = unmarshallRomID(n2, new RomID());
                            if (romID.getXmlid().equalsIgnoreCase(xmlID)) {
                                Rom returnrom = unmarshallRom(n, rom);
                                return returnrom;
                            }
                        }
                    }
                }
            }                 
        } catch (StackOverflowError ex) {
            throw new StackOverflowError();
        }
        throw new RomNotFoundException();
    }

    public RomID unmarshallRomID (Node romIDNode, RomID romID) {
	Node n;
	NodeList nodes = romIDNode.getChildNodes();
	
	for (int i=0; i < nodes.getLength(); i++){
	    n = nodes.item(i);

	    if (n.getNodeType() == Node.ELEMENT_NODE) {

		if (n.getNodeName().equalsIgnoreCase("xmlid")){
		    romID.setXmlid(unmarshallText(n));
		} else if (n.getNodeName().equalsIgnoreCase("internalidaddress")) {
		    romID.setInternalIdAddress(RomAttributeParser.parseHexString(unmarshallText(n)));
		} else if (n.getNodeName().equalsIgnoreCase("internalidstring")) {
		    romID.setInternalIdString(unmarshallText(n));
                    if (romID.getInternalIdString() == null) romID.setInternalIdString("");
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
		} else if (n.getNodeName().equalsIgnoreCase("filesize")) {
		    romID.setFileSize(RomAttributeParser.parseFileSize(unmarshallText(n)));
		} else if (n.getNodeName().equalsIgnoreCase("obsolete")) {
		    romID.setObsolete(Boolean.parseBoolean(unmarshallText(n)));
		} else {
		    // unexpected element in RomID (skip)
		}
	    } else {
		// unexpected node-type in RomID (skip)
	    }
	}
	return romID;
    }
    
    private Table copyTable(Table input) {
        Table output = input;
        return output;
    }
   
    private Table unmarshallTable(Node tableNode, Table table, Rom rom) throws XMLParseException, TableIsOmittedException {
        
        if (unmarshallAttribute(tableNode, "omit", "false").equalsIgnoreCase("true")) {
            throw new TableIsOmittedException();
        }
        
        if (!unmarshallAttribute(tableNode, "base", "none").equalsIgnoreCase("none")) {
            
            try {
                table = (Table)ObjectCloner.deepCopy((Object)rom.getTable(unmarshallAttribute(tableNode, "base", "none")));
                
            } catch (TableNotFoundException ex) {
                // table not found
            } catch (NullPointerException ex) {
                ex.printStackTrace();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        
        try {
            if (table.getType() < 1) { }
        } catch (NullPointerException ex) { // if type is null or less than 0, create new instance (otherwise it is inherited)
            if (unmarshallAttribute(tableNode, "type", "unknown").equalsIgnoreCase("3D")) {
                table = new Table3D();
            } else if (unmarshallAttribute(tableNode, "type", "unknown").equalsIgnoreCase("2D")) {
                table = new Table2D();
            } else if (unmarshallAttribute(tableNode, "type", "unknown").equalsIgnoreCase("1D")) {
                table = new Table1D();
            } else if (unmarshallAttribute(tableNode, "type", "unknown").equalsIgnoreCase("X Axis") ||
                       unmarshallAttribute(tableNode, "type", "unknown").equalsIgnoreCase("Y Axis")) {
                table = new Table1D();
            } else if (unmarshallAttribute(tableNode, "type", "unknown").equalsIgnoreCase("Static Y Axis") ||
                       unmarshallAttribute(tableNode, "type", "unknown").equalsIgnoreCase("Static X Axis")) {
                table = new Table1D();
            } else {
                // huh?
                System.out.println(table);
            }            
        }
            
        table.setName(unmarshallAttribute(tableNode, "name", table.getName()));
        table.setType(RomAttributeParser.parseTableType(unmarshallAttribute(tableNode, "type", table.getType())));
        if (unmarshallAttribute(tableNode, "beforeram", "false").equalsIgnoreCase("true")) table.setBeforeRam(true);
        
        if (unmarshallAttribute(tableNode, "type", "unknown").equalsIgnoreCase("Static X Axis") ||
                unmarshallAttribute(tableNode, "type", "unknown").equalsIgnoreCase("Static Y Axis")) {
            table.setIsStatic(true);
            ((Table1D)table).setIsAxis(true);
        } else if (unmarshallAttribute(tableNode, "type", "unknown").equalsIgnoreCase("X Axis") ||
                unmarshallAttribute(tableNode, "type", "unknown").equalsIgnoreCase("Y Axis")) {
            ((Table1D)table).setIsAxis(true);
        }
        
        table.setCategory(unmarshallAttribute(tableNode, "category", table.getCategory()));
        table.setStorageType(RomAttributeParser.parseStorageType(unmarshallAttribute(tableNode, "storagetype", table.getStorageType())));
        table.setEndian(RomAttributeParser.parseEndian(unmarshallAttribute(tableNode, "endian", table.getEndian())));
        table.setStorageAddress(RomAttributeParser.parseHexString(unmarshallAttribute(tableNode, "storageaddress", table.getStorageAddress())));
        table.setDescription(unmarshallAttribute(tableNode, "description", table.getDescription()));
        table.setDataSize(Integer.parseInt(unmarshallAttribute(tableNode, "sizey", unmarshallAttribute(tableNode, "sizex", table.getDataSize()))));
        table.setFlip(Boolean.parseBoolean(unmarshallAttribute(tableNode, "flipy", unmarshallAttribute(tableNode, "flipx", table.getFlip()+""))));
        
        if (table.getType() == Table.TABLE_3D) {
            ((Table3D)table).setFlipX(Boolean.parseBoolean(unmarshallAttribute(tableNode, "flipx", ((Table3D)table).getFlipX()+"")));
            ((Table3D)table).setFlipY(Boolean.parseBoolean(unmarshallAttribute(tableNode, "flipy", ((Table3D)table).getFlipY()+"")));  
            ((Table3D)table).setSizeX(Integer.parseInt(unmarshallAttribute(tableNode, "sizex", ((Table3D)table).getSizeX())));
            ((Table3D)table).setSizeY(Integer.parseInt(unmarshallAttribute(tableNode, "sizey", ((Table3D)table).getSizeY())));          
        }
	
	Node n;
	NodeList nodes = tableNode.getChildNodes();
	
	for (int i = 0; i < nodes.getLength(); i++) {
	    n = nodes.item(i);

	    if (n.getNodeType() == Node.ELEMENT_NODE) {
		if (n.getNodeName().equalsIgnoreCase("table")) {
                    
		    if (table.getType() == Table.TABLE_2D) {
                        
                        if (RomAttributeParser.parseTableType(unmarshallAttribute(n, "type", "unknown")) == Table.TABLE_Y_AXIS ||                            
                                RomAttributeParser.parseTableType(unmarshallAttribute(n, "type", "unknown")) == Table.TABLE_X_AXIS) {                         
                            
                            Table1D tempTable = (Table1D)unmarshallTable(n, ((Table2D)table).getAxis(), rom);
                            if (tempTable.getDataSize() != table.getDataSize()) tempTable.setDataSize(table.getDataSize());
                            tempTable.setData(((Table2D)table).getAxis().getData());
                            tempTable.setAxisParent(table);
                            ((Table2D)table).setAxis(tempTable);                            
                            
                        }
                    }  else if (table.getType() == Table.TABLE_3D) {
                        if (RomAttributeParser.parseTableType(unmarshallAttribute(n, "type", "unknown")) == Table.TABLE_X_AXIS) {  
                            
                            Table1D tempTable = (Table1D)unmarshallTable(n, ((Table3D)table).getXAxis(), rom);
                            if (tempTable.getDataSize() != ((Table3D)table).getSizeX()) tempTable.setDataSize(((Table3D)table).getSizeX());        
                            tempTable.setData(((Table3D)table).getXAxis().getData());               
                            tempTable.setAxisParent(table);
                            ((Table3D)table).setXAxis(tempTable);
                            
                        } else if (RomAttributeParser.parseTableType(unmarshallAttribute(n, "type", "unknown")) == Table.TABLE_Y_AXIS) { 
                            
                            Table1D tempTable = (Table1D)unmarshallTable(n, ((Table3D)table).getYAxis(), rom);
                            if (tempTable.getDataSize() != ((Table3D)table).getSizeY()) tempTable.setDataSize(((Table3D)table).getSizeY());     
                            tempTable.setData(((Table3D)table).getYAxis().getData());                 
                            tempTable.setAxisParent(table);
                            ((Table3D)table).setYAxis(tempTable);                            
                        }             
                    }
                } else if (n.getNodeName().equalsIgnoreCase("scaling")) {
                    table.setScale(unmarshallScale(n, table.getScale()));
		} else if (n.getNodeName().equalsIgnoreCase("data")) {
                    // parse and add data to table
                    DataCell dataCell = new DataCell();
                    dataCell.setDisplayValue(unmarshallText(n));
                    dataCell.setTable(table);
                    ((Table1D)table).addStaticDataCell(dataCell);
                } else {
		    // unexpected element in Table (skip)
		}
	    } else {
		// unexpected node-type in Table (skip)
	    }
	}
	return table;
    }   
    
    private Scale unmarshallScale (Node scaleNode, Scale scale) {        
        scale.setUnit(unmarshallAttribute(scaleNode, "units", scale.getUnit()));
        scale.setExpression(unmarshallAttribute(scaleNode, "expression", scale.getExpression()));
        scale.setByteExpression(unmarshallAttribute(scaleNode, "to_byte", scale.getByteExpression()));
        scale.setFormat(unmarshallAttribute(scaleNode, "format", "#"));
        scale.setIncrement(Integer.parseInt(unmarshallAttribute(scaleNode, "increment", scale.getIncrement())));
        
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
    
    private String unmarshallAttribute(Node node, String name, int defaultValue) {
        return unmarshallAttribute(node, name, defaultValue+"");
    }
}