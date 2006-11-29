//DOM XML parser for ROMs

package enginuity.xml;

import enginuity.ECUEditor;
import enginuity.Settings;
import enginuity.maps.DataCell;
import enginuity.maps.Rom;
import enginuity.maps.RomID;
import enginuity.maps.Scale;
import enginuity.maps.Table;
import enginuity.maps.Table1D;
import enginuity.maps.Table2D;
import enginuity.maps.Table3D;
import enginuity.maps.TableSwitch;
import enginuity.swing.DebugPanel;
import enginuity.swing.JProgressPane;
import enginuity.util.ObjectCloner;
import static enginuity.xml.DOMHelper.unmarshallAttribute;
import static enginuity.xml.DOMHelper.unmarshallText;
import org.w3c.dom.Node;
import static org.w3c.dom.Node.ELEMENT_NODE;
import org.w3c.dom.NodeList;

import javax.management.modelmbean.XMLParseException;
import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public final class DOMRomUnmarshaller {

    private JProgressPane progress = null;
    private List<Scale> scales = new ArrayList<Scale>();
    private Settings settings;
    private ECUEditor parent;

    public DOMRomUnmarshaller(Settings settings, ECUEditor parent) {
        this.settings = settings;
        this.parent = parent;
    }

    public Rom unmarshallXMLDefinition(Node rootNode, byte[] input, JProgressPane progress) throws RomNotFoundException, XMLParseException, StackOverflowError, Exception {

        this.progress = progress;
        Node n;
        NodeList nodes = rootNode.getChildNodes();

        // unmarshall scales first
        for (int i = 0; i < nodes.getLength(); i++) {
            n = nodes.item(i);

            if (n.getNodeType() == ELEMENT_NODE && n.getNodeName().equalsIgnoreCase("scalingbase")) {
                scales.add(unmarshallScale(n, new Scale()));
            }
        }

        // now unmarshall roms
        for (int i = 0; i < nodes.getLength(); i++) {
            n = nodes.item(i);

            if (n.getNodeType() == ELEMENT_NODE && n.getNodeName().equalsIgnoreCase("rom")) {
                Node n2;
                NodeList nodes2 = n.getChildNodes();

                for (int z = 0; z < nodes2.getLength(); z++) {
                    n2 = nodes2.item(z);
                    if (n2.getNodeType() == ELEMENT_NODE && n2.getNodeName().equalsIgnoreCase("romid")) {

                        RomID romID = unmarshallRomID(n2, new RomID());

                        if (romID.getInternalIdString().length() > 0 && foundMatch(romID, input)) {
                            Rom output = unmarshallRom(n, new Rom());

                            //set ram offset
                            output.getRomID().setRamOffset(output.getRomID().getFileSize() - input.length);
                            return output;
                        }
                    }
                }
            }
        }
        throw new RomNotFoundException();
    }

    public static boolean foundMatch(RomID romID, byte[] file) {

        String idString = romID.getInternalIdString();

        // romid is hex string
        if (idString.length() > 2 && idString.substring(0, 2).equalsIgnoreCase("0x")) {

            try {
                // put romid in to byte array to check for match
                idString = idString.substring(2); // remove "0x"
                int[] romIDBytes = new int[idString.length() / 2];

                for (int i = 0; i < romIDBytes.length; i++) {
                    // check to see if each byte matches

                    if ((file[romID.getInternalIdAddress() + i] & 0xff) !=
                            Integer.parseInt(idString.substring(i * 2, i * 2 + 2), 16)) {

                        return false;
                    }
                }
                // if no mismatched bytes found, return true
                return true;
            } catch (Exception ex) {
                // if any exception is encountered, names do not match
                ex.printStackTrace();
                return false;
            }

            // else romid is NOT hex string
        } else {
            try {
                String ecuID = new String(file, romID.getInternalIdAddress(), romID.getInternalIdString().length());
                return foundMatchByString(romID, ecuID);
            } catch (Exception ex) {
                // if any exception is encountered, names do not match
                return false;
            }
        }
    }

    public static boolean foundMatchByString(RomID romID, String ecuID) {

        try {
            if (ecuID.equalsIgnoreCase(romID.getInternalIdString())) {
                return true;
            } else {
                return false;
            }
        } catch (Exception ex) {
            // if any exception is encountered, names do not match
            return false;
        }
    }

    public static void main(String args[]) {
        DOMRomUnmarshaller um = new DOMRomUnmarshaller(new Settings(), new ECUEditor());
        um.parent.dispose();
        RomID romID = new RomID();
        romID.setInternalIdString("Asdfd");

        byte[] file = "Asdfd".getBytes();
        System.out.println(foundMatch(romID, file));

        file[0] = 1;
        file[1] = 1;
        file[2] = 1;
        file[3] = 1;
        System.out.println(foundMatch(romID, file));

        romID.setInternalIdString("0x010101");
        System.out.println(foundMatch(romID, file));
    }

    public Rom unmarshallRom(Node rootNode, Rom rom) throws XMLParseException, RomNotFoundException, StackOverflowError, Exception {
        Node n;
        NodeList nodes = rootNode.getChildNodes();

        progress.update("Creating tables...", 15);

        if (!unmarshallAttribute(rootNode, "base", "none").equalsIgnoreCase("none")) {
            rom = getBaseRom(rootNode.getParentNode(), unmarshallAttribute(rootNode, "base", "none"), rom);
            rom.getRomID().setObsolete(false);
        }

        for (int i = 0; i < nodes.getLength(); i++) {
            n = nodes.item(i);

            // update progress
            int currProgress = (int) ((double) i / (double) nodes.getLength() * 40);
            progress.update("Creating tables...", 10 + currProgress);

            if (n.getNodeType() == ELEMENT_NODE) {
                if (n.getNodeName().equalsIgnoreCase("romid")) {
                    rom.setRomID(unmarshallRomID(n, rom.getRomID()));

                } else if (n.getNodeName().equalsIgnoreCase("table")) {
                    Table table = null;
                    try {
                        table = rom.getTable(unmarshallAttribute(n, "name", "unknown"));
                    } catch (TableNotFoundException e) { /* table does not already exist (do nothing) */ }

                    try {
                        table = unmarshallTable(n, table, rom);
                        table.setRom(rom);
                        rom.addTable(table);
                    } catch (TableIsOmittedException ex) {
                        // table is not supported in inherited def (skip)
                        if (table != null) {
                            rom.removeTable(table.getName());
                        }
                    } catch (XMLParseException ex) {
                        ex.printStackTrace();
                    }

                } else { /* unexpected element in Rom (skip)*/ }
            } else { /* unexpected node-type in Rom (skip)*/ }
        }
        return rom;
    }

    public Rom getBaseRom(Node rootNode, String xmlID, Rom rom) throws XMLParseException, RomNotFoundException, StackOverflowError, Exception {
        Node n;
        NodeList nodes = rootNode.getChildNodes();

        for (int i = 0; i < nodes.getLength(); i++) {
            n = nodes.item(i);

            if (n.getNodeType() == ELEMENT_NODE && n.getNodeName().equalsIgnoreCase("rom")) {
                Node n2;
                NodeList nodes2 = n.getChildNodes();

                for (int z = 0; z < nodes2.getLength(); z++) {
                    n2 = nodes2.item(z);
                    if (n2.getNodeType() == ELEMENT_NODE && n2.getNodeName().equalsIgnoreCase("romid")) {

                        RomID romID = unmarshallRomID(n2, new RomID());
                        if (romID.getXmlid().equalsIgnoreCase(xmlID)) {
                            Rom returnrom = unmarshallRom(n, rom);
                            returnrom.getRomID().setObsolete(false);
                            return returnrom;
                        }
                    }
                }
            }
        }
        throw new RomNotFoundException();
    }

    public RomID unmarshallRomID(Node romIDNode, RomID romID) {
        Node n;
        NodeList nodes = romIDNode.getChildNodes();

        for (int i = 0; i < nodes.getLength(); i++) {
            n = nodes.item(i);

            if (n.getNodeType() == ELEMENT_NODE) {

                if (n.getNodeName().equalsIgnoreCase("xmlid")) {
                    romID.setXmlid(unmarshallText(n));

                } else if (n.getNodeName().equalsIgnoreCase("internalidaddress")) {
                    romID.setInternalIdAddress(RomAttributeParser.parseHexString(unmarshallText(n)));

                } else if (n.getNodeName().equalsIgnoreCase("internalidstring")) {
                    romID.setInternalIdString(unmarshallText(n));
                    if (romID.getInternalIdString() == null) {
                        romID.setInternalIdString("");
                    }

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
                    romID.setYear(unmarshallText(n));

                } else if (n.getNodeName().equalsIgnoreCase("flashmethod")) {
                    romID.setFlashMethod(unmarshallText(n));

                } else if (n.getNodeName().equalsIgnoreCase("memmodel")) {
                    romID.setMemModel(unmarshallText(n));

                } else if (n.getNodeName().equalsIgnoreCase("filesize")) {
                    romID.setFileSize(RomAttributeParser.parseFileSize(unmarshallText(n)));

                } else if (n.getNodeName().equalsIgnoreCase("obsolete")) {
                    romID.setObsolete(Boolean.parseBoolean(unmarshallText(n)));

                } else { /* unexpected element in RomID (skip) */ }
            } else { /* unexpected node-type in RomID (skip) */ }
        }
        return romID;
    }

    private Table unmarshallTable(Node tableNode, Table table, Rom rom) throws XMLParseException, TableIsOmittedException, Exception {

        if (unmarshallAttribute(tableNode, "omit", "false").equalsIgnoreCase("true")) { // remove table if omitted
            throw new TableIsOmittedException();
        }

        if (!unmarshallAttribute(tableNode, "base", "none").equalsIgnoreCase("none")) { // copy base table for inheritance            
            try {
                table = (Table) ObjectCloner.deepCopy((Object) rom.getTable(unmarshallAttribute(tableNode, "base", "none")));

            } catch (TableNotFoundException ex) { /* table not found, do nothing */

            } catch (NullPointerException ex) {
                JOptionPane.showMessageDialog(parent, new DebugPanel(ex,
                        parent.getSettings().getSupportURL()), "Exception", JOptionPane.ERROR_MESSAGE);

            }
        }

        try {
            if (table.getType() < 1) {
            }
        } catch (NullPointerException ex) { // if type is null or less than 0, create new instance (otherwise it is inherited)
            if (unmarshallAttribute(tableNode, "type", "unknown").equalsIgnoreCase("3D")) {
                table = new Table3D(settings);

            } else if (unmarshallAttribute(tableNode, "type", "unknown").equalsIgnoreCase("2D")) {
                table = new Table2D(settings);

            } else if (unmarshallAttribute(tableNode, "type", "unknown").equalsIgnoreCase("1D")) {
                table = new Table1D(settings);

            } else if (unmarshallAttribute(tableNode, "type", "unknown").equalsIgnoreCase("X Axis") ||
                    unmarshallAttribute(tableNode, "type", "unknown").equalsIgnoreCase("Y Axis")) {
                table = new Table1D(settings);

            } else if (unmarshallAttribute(tableNode, "type", "unknown").equalsIgnoreCase("Static Y Axis") ||
                    unmarshallAttribute(tableNode, "type", "unknown").equalsIgnoreCase("Static X Axis")) {
                table = new Table1D(settings);

            } else if (unmarshallAttribute(tableNode, "type", "unknown").equalsIgnoreCase("Switch")) {
                table = new TableSwitch(settings);

            } else {
                throw new XMLParseException("Error loading table.");
            }
        }

        // unmarshall table attributes                    
        table.setName(unmarshallAttribute(tableNode, "name", table.getName()));
        table.setType(RomAttributeParser.parseTableType(unmarshallAttribute(tableNode, "type", String.valueOf(table.getType()))));
        if (unmarshallAttribute(tableNode, "beforeram", "false").equalsIgnoreCase("true")) {
            table.setBeforeRam(true);
        }

        if (unmarshallAttribute(tableNode, "type", "unknown").equalsIgnoreCase("Static X Axis") ||
                unmarshallAttribute(tableNode, "type", "unknown").equalsIgnoreCase("Static Y Axis")) {
            table.setIsStatic(true);
            ((Table1D) table).setIsAxis(true);
        } else if (unmarshallAttribute(tableNode, "type", "unknown").equalsIgnoreCase("X Axis") ||
                unmarshallAttribute(tableNode, "type", "unknown").equalsIgnoreCase("Y Axis")) {
            ((Table1D) table).setIsAxis(true);
        }

        table.setCategory(unmarshallAttribute(tableNode, "category", table.getCategory()));
        table.setStorageType(RomAttributeParser.parseStorageType(unmarshallAttribute(tableNode, "storagetype", String.valueOf(table.getStorageType()))));
        table.setEndian(RomAttributeParser.parseEndian(unmarshallAttribute(tableNode, "endian", String.valueOf(table.getEndian()))));
        table.setStorageAddress(RomAttributeParser.parseHexString(unmarshallAttribute(tableNode, "storageaddress", String.valueOf(table.getStorageAddress()))));
        table.setDescription(unmarshallAttribute(tableNode, "description", table.getDescription()));
        table.setDataSize(unmarshallAttribute(tableNode, "sizey", unmarshallAttribute(tableNode, "sizex", table.getDataSize())));
        table.setFlip(unmarshallAttribute(tableNode, "flipy", unmarshallAttribute(tableNode, "flipx", table.getFlip())));
        table.setUserLevel(unmarshallAttribute(tableNode, "userlevel", table.getUserLevel()));
        table.setLocked(unmarshallAttribute(tableNode, "locked", table.isLocked()));
        table.setLogParam(unmarshallAttribute(tableNode, "logparam", table.getLogParam()));

        if (table.getType() == Table.TABLE_3D) {
            ((Table3D) table).setFlipX(unmarshallAttribute(tableNode, "flipx", ((Table3D) table).getFlipX()));
            ((Table3D) table).setFlipY(unmarshallAttribute(tableNode, "flipy", ((Table3D) table).getFlipY()));
            ((Table3D) table).setSizeX(unmarshallAttribute(tableNode, "sizex", ((Table3D) table).getSizeX()));
            ((Table3D) table).setSizeY(unmarshallAttribute(tableNode, "sizey", ((Table3D) table).getSizeY()));
        }

        Node n;
        NodeList nodes = tableNode.getChildNodes();

        for (int i = 0; i < nodes.getLength(); i++) {
            n = nodes.item(i);

            if (n.getNodeType() == ELEMENT_NODE) {
                if (n.getNodeName().equalsIgnoreCase("table")) {

                    if (table.getType() == Table.TABLE_2D) { // if table is 2D, parse axis

                        if (RomAttributeParser.parseTableType(unmarshallAttribute(n, "type", "unknown")) == Table.TABLE_Y_AXIS ||
                                RomAttributeParser.parseTableType(unmarshallAttribute(n, "type", "unknown")) == Table.TABLE_X_AXIS) {

                            Table1D tempTable = (Table1D) unmarshallTable(n, ((Table2D) table).getAxis(), rom);
                            if (tempTable.getDataSize() != table.getDataSize()) {
                                tempTable.setDataSize(table.getDataSize());
                            }
                            tempTable.setData(((Table2D) table).getAxis().getData());
                            tempTable.setAxisParent(table);
                            ((Table2D) table).setAxis(tempTable);

                        }
                    } else if (table.getType() == Table.TABLE_3D) { // if table is 3D, populate axiis
                        if (RomAttributeParser.parseTableType(unmarshallAttribute(n, "type", "unknown")) == Table.TABLE_X_AXIS) {

                            Table1D tempTable = (Table1D) unmarshallTable(n, ((Table3D) table).getXAxis(), rom);
                            if (tempTable.getDataSize() != ((Table3D) table).getSizeX()) {
                                tempTable.setDataSize(((Table3D) table).getSizeX());
                            }
                            tempTable.setData(((Table3D) table).getXAxis().getData());
                            tempTable.setAxisParent(table);
                            ((Table3D) table).setXAxis(tempTable);

                        } else if (RomAttributeParser.parseTableType(unmarshallAttribute(n, "type", "unknown")) == Table.TABLE_Y_AXIS) {

                            Table1D tempTable = (Table1D) unmarshallTable(n, ((Table3D) table).getYAxis(), rom);
                            if (tempTable.getDataSize() != ((Table3D) table).getSizeY()) {
                                tempTable.setDataSize(((Table3D) table).getSizeY());
                            }
                            tempTable.setData(((Table3D) table).getYAxis().getData());
                            tempTable.setAxisParent(table);
                            ((Table3D) table).setYAxis(tempTable);

                        }
                    }

                } else if (n.getNodeName().equalsIgnoreCase("scaling")) {
                    // check whether scale already exists. if so, modify, else use new instance
                    Scale baseScale = new Scale();
                    try {
                        baseScale = table.getScaleByName(unmarshallAttribute(n, "name", "x"));
                    } catch (Exception ex) {
                    }

                    table.setScale(unmarshallScale(n, baseScale));

                } else if (n.getNodeName().equalsIgnoreCase("data")) {
                    // parse and add data to table
                    DataCell dataCell = new DataCell();
                    dataCell.setDisplayValue(unmarshallText(n));
                    dataCell.setTable(table);
                    table.addStaticDataCell(dataCell);

                } else if (n.getNodeName().equalsIgnoreCase("description")) {
                    table.setDescription(unmarshallText(n));

                } else if (n.getNodeName().equalsIgnoreCase("state")) {
                    // set on/off values for switch type
                    if (unmarshallAttribute(n, "name", "").equalsIgnoreCase("on")) {
                        ((TableSwitch) table).setOnValues(unmarshallAttribute(n, "data", "0"));

                    } else if (unmarshallAttribute(n, "name", "").equalsIgnoreCase("off")) {
                        ((TableSwitch) table).setOffValues(unmarshallAttribute(n, "data", "0"));

                    }

                } else { /*unexpected element in Table (skip) */ }
            } else { /* unexpected node-type in Table (skip) */ }
        }

        return table;
    }

    private Scale unmarshallScale(Node scaleNode, Scale scale) {

        // look for base scale first
        String base = unmarshallAttribute(scaleNode, "base", "none");
        if (!base.equalsIgnoreCase("none")) {
            for (Scale scaleItem : scales) {

                // check whether name matches base and set scale if so
                if (scaleItem.getName().equalsIgnoreCase(base)) {
                    try {
                        scale = (Scale) ObjectCloner.deepCopy(scaleItem);

                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(parent, new DebugPanel(ex,
                                parent.getSettings().getSupportURL()), "Exception", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        }

        // set remaining attributes
        scale.setName(unmarshallAttribute(scaleNode, "name", scale.getName()));
        scale.setUnit(unmarshallAttribute(scaleNode, "units", scale.getUnit()));
        scale.setExpression(unmarshallAttribute(scaleNode, "expression", scale.getExpression()));
        scale.setByteExpression(unmarshallAttribute(scaleNode, "to_byte", scale.getByteExpression()));
        scale.setFormat(unmarshallAttribute(scaleNode, "format", "#"));
        scale.setMax(unmarshallAttribute(scaleNode, "max", 0.0));
        scale.setMin(unmarshallAttribute(scaleNode, "min", 0.0));

        // get coarse increment with new attribute name (coarseincrement), else look for old (increment)
        scale.setCoarseIncrement(unmarshallAttribute(scaleNode, "coarseincrement",
                unmarshallAttribute(scaleNode, "increment", scale.getCoarseIncrement())));

        scale.setFineIncrement(unmarshallAttribute(scaleNode, "fineincrement", scale.getFineIncrement()));

        return scale;
    }
}