/*
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2020 RomRaider.com
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

//DOM XML parser for ROMs

package com.romraider.xml;

import static com.romraider.xml.DOMHelper.unmarshallAttribute;
import static com.romraider.xml.DOMHelper.unmarshallText;
import static org.w3c.dom.Node.ELEMENT_NODE;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.management.modelmbean.XMLParseException;
import javax.swing.JOptionPane;

import com.romraider.Settings;
import org.apache.log4j.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.romraider.editor.ecu.ECUEditorManager;
import com.romraider.maps.DataCell;
import com.romraider.maps.Rom;
import com.romraider.maps.RomID;
import com.romraider.maps.Scale;
import com.romraider.maps.Table;
import com.romraider.maps.Table1D;
import com.romraider.maps.Table2D;
import com.romraider.maps.Table3D;
import com.romraider.maps.TableBitwiseSwitch;
import com.romraider.maps.Table2DMaskedSwitchable;
import com.romraider.maps.TableSwitch;
import com.romraider.maps.checksum.ChecksumFactory;
import com.romraider.maps.checksum.ChecksumManager;
import com.romraider.swing.DebugPanel;
import com.romraider.swing.JProgressPane;
import com.romraider.util.ObjectCloner;
import com.romraider.util.SettingsManager;

public final class DOMRomUnmarshaller {
    private static final Logger LOGGER = Logger
            .getLogger(DOMRomUnmarshaller.class);
    private JProgressPane progress = null;
    private final List<Scale> scales = new ArrayList<Scale>();
    private String memModelEndian = null;
    private final Scale rawScale = new Scale();
    private final Map<String, Integer> tableNames = new HashMap<String, Integer>();
    private ChecksumManager checksumManager = null;

    public DOMRomUnmarshaller() {
    }

    public Rom unmarshallXMLDefinition(Node rootNode, byte[] input,
            JProgressPane progress) throws RomNotFoundException,
            XMLParseException, StackOverflowError, Exception {

        this.progress = progress;
        Node n;
        NodeList nodes = rootNode.getChildNodes();

        // unmarshall scales first
        for (int i = 0; i < nodes.getLength(); i++) {
            n = nodes.item(i);

            if (n.getNodeType() == ELEMENT_NODE
                    && n.getNodeName().equalsIgnoreCase("scalingbase")) {
                scales.add(unmarshallScale(n, new Scale()));
            }
        }

        // now unmarshall roms
        for (int i = 0; i < nodes.getLength(); i++) {
            n = nodes.item(i);

            if (n.getNodeType() == ELEMENT_NODE
                    && n.getNodeName().equalsIgnoreCase("rom")) {
                Node n2;
                NodeList nodes2 = n.getChildNodes();

                for (int z = 0; z < nodes2.getLength(); z++) {
                    n2 = nodes2.item(z);
                    if (n2.getNodeType() == ELEMENT_NODE
                            && n2.getNodeName().equalsIgnoreCase("romid")) {

                        RomID romID = unmarshallRomID(n2, new RomID());

                        if (romID.getInternalIdString().length() > 0
                                && foundMatch(romID, input)) {
                            Rom output = unmarshallRom(n, new Rom());

                            // set ram offset
                            output.getRomID().setRamOffset(
                                    output.getRomID().getFileSize()
                                    - input.length);
                            //output.addChecksumManager(checksumManager);
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
        if (idString.length() > 2
                && idString.substring(0, 2).equalsIgnoreCase("0x")) {

            try {
                // put romid in to byte array to check for match
                idString = idString.substring(2); // remove "0x"
                int[] romIDBytes = new int[idString.length() / 2];

                for (int i = 0; i < romIDBytes.length; i++) {
                    // check to see if each byte matches

                    if ((file[romID.getInternalIdAddress() + i] & 0xff) != Integer
                            .parseInt(idString.substring(i * 2, i * 2 + 2), 16)) {

                        return false;
                    }
                }
                // if no mismatched bytes found, return true
                return true;
            } catch (Exception ex) {
                // if any exception is encountered, names do not match
                LOGGER.warn("Error finding match", ex);
                return false;
            }

            // else romid is NOT hex string
        } else {
            try {
                String ecuID = new String(file, romID.getInternalIdAddress(),
                        romID.getInternalIdString().length());
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

    public Rom unmarshallRom(Node rootNode, Rom rom) throws XMLParseException,
    RomNotFoundException, StackOverflowError, Exception {
        Node n;
        NodeList nodes = rootNode.getChildNodes();
        filterFoundRomTables(nodes);

        progress.update("Creating " + rom.getRomID().getXmlid() + " tables...", 0);

        if (!unmarshallAttribute(rootNode, "base", "none").equalsIgnoreCase(
                "none")) {
            rom = getBaseRom(rootNode.getParentNode(),
                    unmarshallAttribute(rootNode, "base", "none"), rom);
            rom.getRomID().setObsolete(false);
        }

        for (int i = 0; i < nodes.getLength(); i++) {
            n = nodes.item(i);

            // update progress
            int currProgress = (int) (i / (double) nodes.getLength() * 100);
            progress.update("Creating " + rom.getRomID().getXmlid() + " tables...", currProgress);

            if (n.getNodeType() == ELEMENT_NODE) {
                if (n.getNodeName().equalsIgnoreCase("romid")) {
                    rom.setRomID(unmarshallRomID(n, rom.getRomID()));

                } else if (n.getNodeName().equalsIgnoreCase("table")) {
                    Table table = null;
                    try {
                        table = rom.getTableByName(unmarshallAttribute(n, "name",
                                null));
                    } catch (TableNotFoundException e) {
                        /*
                         * table does not
                         * already exist (do
                         * nothing)
                         */
                    } catch (InvalidTableNameException iex) {
                        // Table name is null or empty.  Do nothing.
                    }

                    try {
                        table = unmarshallTable(n, table, rom);
                        //rom.addTableByName(table);
                        if (table != null) {
                            //rom.removeTableByName(table);
                            rom.addTable(table);
                        }
                    } catch (TableIsOmittedException ex) {
                        // table is not supported in inherited def (skip)
                        if (table != null) {
                            //rom.removeTableByName(table);
                            rom.removeTable(table);
                        }
                    } catch (XMLParseException ex) {
                        LOGGER.error("Error unmarshalling rom", ex);
                    }
                } else if (n.getNodeName().equalsIgnoreCase("checksum")) {
                    rom.getRomID().setChecksum(unmarshallAttribute(n, "type", ""));
                    checksumManager = unmarshallChecksum(n);
                    rom.addChecksumManager(checksumManager);

                } else { /* unexpected element in Rom (skip) */
                }
            } else { /* unexpected node-type in Rom (skip) */
            }
        }
        return rom;
    }

    public Rom getBaseRom(Node rootNode, String xmlID, Rom rom)
            throws XMLParseException, RomNotFoundException, StackOverflowError,
            Exception {
        Node n;
        NodeList nodes = rootNode.getChildNodes();

        for (int i = 0; i < nodes.getLength(); i++) {
            n = nodes.item(i);

            if (n.getNodeType() == ELEMENT_NODE
                    && n.getNodeName().equalsIgnoreCase("rom")) {
                Node n2;
                NodeList nodes2 = n.getChildNodes();

                for (int z = 0; z < nodes2.getLength(); z++) {
                    n2 = nodes2.item(z);
                    if (n2.getNodeType() == ELEMENT_NODE
                            && n2.getNodeName().equalsIgnoreCase("romid")) {

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

                } else if (n.getNodeName()
                        .equalsIgnoreCase("internalidaddress")) {
                    romID.setInternalIdAddress(RomAttributeParser
                            .parseHexString(unmarshallText(n)));

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
                    memModelEndian = unmarshallAttribute(n, "endian", null);
                } else if (n.getNodeName().equalsIgnoreCase("filesize")) {
                    romID.setFileSize(RomAttributeParser
                            .parseFileSize(unmarshallText(n)));

                } else if (n.getNodeName().equalsIgnoreCase("obsolete")) {
                    romID.setObsolete(Boolean.parseBoolean(unmarshallText(n)));

                } else { /* unexpected element in RomID (skip) */
                }
            } else { /* unexpected node-type in RomID (skip) */
            }
        }
        return romID;
    }

    private Table unmarshallTable(Node tableNode, Table table, Rom rom)
            throws XMLParseException, TableIsOmittedException, Exception {

        if (unmarshallAttribute(tableNode, "omit", "false").equalsIgnoreCase(
                "true")) { // remove table if omitted
            throw new TableIsOmittedException();
        }

        if (!unmarshallAttribute(tableNode, "base", "none").equalsIgnoreCase(
                "none")) { // copy base table for inheritance
            try {
                table = (Table) ObjectCloner
                        .deepCopy(rom.getTableByName(unmarshallAttribute(tableNode,
                                "base", "none")));

            } catch (TableNotFoundException ex) { /* table not found, do nothing */

            } catch (InvalidTableNameException ex) { // Table name is invalid, do nothing.

            } catch (NullPointerException ex) {
                JOptionPane.showMessageDialog(ECUEditorManager.getECUEditor(),
                        new DebugPanel(ex, SettingsManager.getSettings().getSupportURL()), "Exception",
                        JOptionPane.ERROR_MESSAGE);

            }
        }

        if (table == null) {
            // create new instance (otherwise it
            // is inherited)
            final String tn = unmarshallAttribute(tableNode, "name", "unknown");
            final String type = unmarshallAttribute(tableNode, "type", "unknown");
            if (tableNames.containsKey(tn) || type.contains("xis")) {
                if (unmarshallAttribute(tableNode, "type", "unknown")
                        .equalsIgnoreCase("3D")) {
                    table = new Table3D();
                    table.getScales().add(rawScale);
                    ((Table3D) table).getXAxis().getScales().add(rawScale);
                    ((Table3D) table).getYAxis().getScales().add(rawScale);

                } else if (unmarshallAttribute(tableNode, "type", "unknown")
                        .equalsIgnoreCase("2D")) {
                    table = new Table2D();
                    table.getScales().add(rawScale);
                    ((Table2D) table).getAxis().getScales().add(rawScale);

                } else if (unmarshallAttribute(tableNode, "type", "unknown")
                        .equalsIgnoreCase("1D")) {
                    table = new Table1D(Table.TableType.TABLE_1D);

                } else if (unmarshallAttribute(tableNode, "type", "unknown")
                        .equalsIgnoreCase("X Axis")
                        || unmarshallAttribute(tableNode, "type", "unknown")
                        .equalsIgnoreCase("Static X Axis"))  {
                    table = new Table1D(Table.TableType.X_AXIS);

                } else if (unmarshallAttribute(tableNode, "type", "unknown")
                        .equalsIgnoreCase("Y Axis")
                        || unmarshallAttribute(tableNode, "type", "unknown")
                        .equalsIgnoreCase("Static Y Axis")) {
                    table = new Table1D(Table.TableType.Y_AXIS);
                } else if (unmarshallAttribute(tableNode, "type", "unknown")
                        .equalsIgnoreCase("Switch")) {
                    table = new TableSwitch();

                } else if (unmarshallAttribute(tableNode, "type", "unknown")
                        .equalsIgnoreCase("BitwiseSwitch")) {
                    table = new TableBitwiseSwitch();
                }
                else if (unmarshallAttribute(tableNode, "type", "unknown")
                            .equalsIgnoreCase("2DMaskedSwitchable")) {
                        table = new Table2DMaskedSwitchable();
                } else {
                    throw new XMLParseException("Error loading table, "
                            + tableNode.getAttributes().getNamedItem("name"));
                }
            }
            else {
                return table;
            }
        }

        // unmarshall table attributes
        final String tn = unmarshallAttribute(tableNode, "name", table.getName());
        table.setName(tn);
        if (unmarshallAttribute(tableNode, "beforeram", "false")
                .equalsIgnoreCase("true")) {
            table.setBeforeRam(true);
        }

        table.setCategory(unmarshallAttribute(tableNode, "category",
                table.getCategory()));
        if (table.getStorageType() < 1) {
            table.setSignedData(RomAttributeParser
                    .parseStorageDataSign(unmarshallAttribute(tableNode,
                            "storagetype",
                            String.valueOf(table.getStorageType()))));
        }
        table.setStorageType(RomAttributeParser
                .parseStorageType(unmarshallAttribute(tableNode, "storagetype",
                        String.valueOf(table.getStorageType()))));
        if (memModelEndian == null) {
            table.setEndian(RomAttributeParser.parseEndian(unmarshallAttribute(
                    tableNode, "endian", table.getEndian().getMarshallingString())));
        }
        else {
            final Settings.Endian endian = memModelEndian.equalsIgnoreCase("little") ? Settings.Endian.LITTLE : Settings.Endian.BIG;
            table.setMemModelEndian(endian);
            table.setEndian(endian);
        }
        if (tableNames.containsKey(tn)) {
            table.setStorageAddress(tableNames.get(tn));
        }
        else {
            table.setStorageAddress(RomAttributeParser
                .parseHexString(unmarshallAttribute(tableNode,
                        "storageaddress",
                        String.valueOf(table.getStorageAddress()))));
        }
        
        table.setDescription(unmarshallAttribute(tableNode, "description",
                table.getDescription()));
        table.setDataSize(unmarshallAttribute(tableNode, "sizey",
                unmarshallAttribute(tableNode, "sizex", table.getDataSize())));
        table.setFlip(unmarshallAttribute(tableNode, "flipy",
                unmarshallAttribute(tableNode, "flipx", table.getFlip())));
        table.setUserLevel(unmarshallAttribute(tableNode, "userlevel",
                table.getUserLevel()));
        table.setLocked(unmarshallAttribute(tableNode, "locked",
                table.isLocked()));
        table.setLogParam(unmarshallAttribute(tableNode, "logparam",
                table.getLogParam()));
        

        if (table.getType() == Table.TableType.TABLE_3D) {
            ((Table3D) table).setSwapXY(unmarshallAttribute(tableNode,
                    "swapxy", ((Table3D) table).getSwapXY()));
            ((Table3D) table).setFlipX(unmarshallAttribute(tableNode, "flipx",
                    ((Table3D) table).getFlipX()));
            ((Table3D) table).setFlipY(unmarshallAttribute(tableNode, "flipy",
                    ((Table3D) table).getFlipY()));
            ((Table3D) table).setSizeX(unmarshallAttribute(tableNode, "sizex",
                    ((Table3D) table).getSizeX()));
            ((Table3D) table).setSizeY(unmarshallAttribute(tableNode, "sizey",
                    ((Table3D) table).getSizeY()));
        }
        
        if (table.getType() == Table.TableType.TABLE_2D_MASKED_SWITCHABLE) {
        ((Table2DMaskedSwitchable) table).setStringMask(
                unmarshallAttribute(tableNode, "mask", "FFFFFFFF"));
        }
        
        Node n;
        NodeList nodes = tableNode.getChildNodes();

        for (int i = 0; i < nodes.getLength(); i++) {
            n = nodes.item(i);

            if (n.getNodeType() == ELEMENT_NODE) {
                if (n.getNodeName().equalsIgnoreCase("table")) {

                    if (table.getType() == Table.TableType.TABLE_2D || table.getType() == Table.TableType.TABLE_2D_MASKED_SWITCHABLE) { // if table is 2D,
                        // parse axis

                        if (RomAttributeParser
                                .parseTableType(unmarshallAttribute(n, "type",
                                        "unknown")) == Table.TableType.Y_AXIS
                                        || RomAttributeParser
                                        .parseTableType(unmarshallAttribute(n,
                                                "type", "unknown")) == Table.TableType.X_AXIS) {

                            Table1D tempTable = (Table1D) unmarshallTable(n,
                                    ((Table2D) table).getAxis(), rom);
                            if (tempTable.getDataSize() != table.getDataSize()) {
                                tempTable.setDataSize(table.getDataSize());
                            }
                            tempTable.setData(((Table2D) table).getAxis()
                                    .getData());
                            ((Table2D) table).setAxis(tempTable);

                        }
                    } else if (table.getType() == Table.TableType.TABLE_3D) { // if table
                        // is 3D,
                        // populate
                        // xAxis
                        if (RomAttributeParser
                                .parseTableType(unmarshallAttribute(n, "type",
                                        "unknown")) == Table.TableType.X_AXIS) {

                            Table1D tempTable = (Table1D) unmarshallTable(n,
                                    ((Table3D) table).getXAxis(), rom);
                            if (tempTable.getDataSize() != ((Table3D) table)
                                    .getSizeX()) {
                                tempTable.setDataSize(((Table3D) table)
                                        .getSizeX());
                            }
                            tempTable.setData(((Table3D) table).getXAxis()
                                    .getData());
                            ((Table3D) table).setXAxis(tempTable);

                        } else if (RomAttributeParser
                                .parseTableType(unmarshallAttribute(n, "type",
                                        "unknown")) == Table.TableType.Y_AXIS) {

                            Table1D tempTable = (Table1D) unmarshallTable(n,
                                    ((Table3D) table).getYAxis(), rom);
                            if (tempTable.getDataSize() != ((Table3D) table)
                                    .getSizeY()) {
                                tempTable.setDataSize(((Table3D) table)
                                        .getSizeY());
                            }
                            tempTable.setData(((Table3D) table).getYAxis()
                                    .getData());
                            ((Table3D) table).setYAxis(tempTable);

                        }
                    }

                } else if (n.getNodeName().equalsIgnoreCase("scaling")) {
                    // check whether scale already exists. if so, modify, else
                    // use new instance
                    Scale baseScale = table.getScale(unmarshallAttribute(n,"name", "Default"));
                    table.addScale(unmarshallScale(n, baseScale));

                } else if (n.getNodeName().equalsIgnoreCase("data")) {
                    // parse and add data to table
                    DataCell dataCell = new DataCell(table, unmarshallText(n));
                    if(table instanceof Table1D) {
                        ((Table1D)table).addStaticDataCell(dataCell);
                    } else {
                        // Why would this happen.  Static should only be for axis.
                        LOGGER.error("Error adding static data cell.");
                    }

                } else if (n.getNodeName().equalsIgnoreCase("description")) {
                    table.setDescription(unmarshallText(n));

                } else if (n.getNodeName().equalsIgnoreCase("state")) {
                    ((TableSwitch) table).setValues(
                            unmarshallAttribute(n, "name", ""),
                            unmarshallAttribute(n, "data", "0.0"));
                                      
                } else if (n.getNodeName().equalsIgnoreCase("bit")) {
                    ((TableBitwiseSwitch) table).setValues(
                            unmarshallAttribute(n, "name", ""),
                            unmarshallAttribute(n, "position", "0"));

                } else if (n.getNodeName().equalsIgnoreCase("maskedPreset")) {
                    ((Table2DMaskedSwitchable) table).setPredefinedOption(
                            unmarshallAttribute(n, "presetName", ""),
                            unmarshallAttribute(n, "maskedData", "0")
                           
                            );
                } else { /* unexpected element in Table (skip) */
                }
            } else { /* unexpected node-type in Table (skip) */
            }
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
                        JOptionPane.showMessageDialog(
                                ECUEditorManager.getECUEditor(),
                                new DebugPanel(ex, SettingsManager.getSettings()
                                        .getSupportURL()), "Exception",
                                        JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        }

        // set remaining attributes
        scale.setName(unmarshallAttribute(scaleNode, "name", "Default"));
        scale.setUnit(unmarshallAttribute(scaleNode, "units", scale.getUnit()));
        scale.setExpression(unmarshallAttribute(scaleNode, "expression",
                scale.getExpression()));
        scale.setByteExpression(unmarshallAttribute(scaleNode, "to_byte",
                scale.getByteExpression()));
        scale.setFormat(unmarshallAttribute(scaleNode, "format", "#"));
        scale.setMax(unmarshallAttribute(scaleNode, "max", 0.0));
        scale.setMin(unmarshallAttribute(scaleNode, "min", 0.0));

        // get coarse increment with new attribute name (coarseincrement), else
        // look for old (increment)
        scale.setCoarseIncrement(unmarshallAttribute(
                scaleNode,
                "coarseincrement",
                unmarshallAttribute(scaleNode, "increment",
                        scale.getCoarseIncrement())));

        scale.setFineIncrement(unmarshallAttribute(scaleNode, "fineincrement",
                scale.getFineIncrement()));
        for (Scale s : scales) {
            if (s.equals(scale)) {
                return s;
            }
        }
        scales.add(scale);
        return scale;
    }

    /**
     * Create a list of table names to be used as a filter on the inherited
     * tables to reduce unnecessary table object creation.
     * @param nodes -  the NodeList to filter
     * @throws XMLParseException
     * @throws TableIsOmittedException
     * @throws Exception
     */
    private void filterFoundRomTables (NodeList nodes) {
        Node n;

        for (int i = 0; i < nodes.getLength(); i++) {
            n = nodes.item(i);
            if (n.getNodeType() == ELEMENT_NODE
                    && n.getNodeName().equalsIgnoreCase("table")) {

                final String name = unmarshallAttribute(n, "name", "unknown");
                final int address = RomAttributeParser
                        .parseHexString(unmarshallAttribute(n,
                            "storageaddress", "-1"));

                if (unmarshallAttribute(n, "omit", "false").equalsIgnoreCase(
                        "true")) {
                    return;
                }

                //Why cant the address not be zero?
                if (!tableNames.containsKey(name) && address >= 0) {
                    tableNames.put(name, address);
                }
                else if (tableNames.containsKey(name)) {
                    if (tableNames.get(name) < 1 && address >= 0) {
                        tableNames.put(name, address);
                        }
                }
            }
        }
    }

    /**
     * Unmarshall the attributes of the checksum element and populate a
     * CheckSumManager object to be assigned to the ROM.
     * @param node -  the checksum element node to process
     * @return CheckSumManager object
     */
    private ChecksumManager unmarshallChecksum(Node node) {
        final Map<String, String> attrs = new HashMap<String, String>();

        for (int i = 0; i < node.getAttributes().getLength(); i++) {
            attrs.put(node.getAttributes().item(i).getNodeName().toLowerCase(),
                    node.getAttributes().item(i).getNodeValue());
        }
           return ChecksumFactory.getManager(attrs);
    }
}
