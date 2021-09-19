/*
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2021 RomRaider.com
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

import java.util.HashMap;
import java.util.Map;

import javax.management.modelmbean.XMLParseException;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.romraider.maps.Rom;
import com.romraider.maps.RomID;
import com.romraider.maps.Table;
import com.romraider.maps.checksum.ChecksumFactory;
import com.romraider.maps.checksum.ChecksumManager;
import com.romraider.swing.JProgressPane;

public final class DOMRomUnmarshaller {
    private static final Logger LOGGER = Logger
            .getLogger(DOMRomUnmarshaller.class);
    private JProgressPane progress = null; 
    private ChecksumManager checksumManager = null;
    private TableScaleAttributeHandler tableScaleHandler = new TableScaleAttributeHandler();

    public Rom unmarshallXMLDefinition(Node rootNode, byte[] input,
            JProgressPane progress) throws RomNotFoundException,
            XMLParseException, StackOverflowError, Exception {

        this.progress = progress;
        Node n;
        NodeList nodes = rootNode.getChildNodes();
        tableScaleHandler.unmarshallBaseScales(nodes);

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

                        //Check if bytes match in file
                        if (romID.checkMatch(input)) {
                            Rom output = unmarshallRom(n, new Rom());

                            // set ram offset
                            output.getRomID().setRamOffset(
                                    output.getRomID().getFileSize()
                                    - input.length);

                            return output;
                        }
                        
                        //ROM only has one ID Node, so we can skip the rest after we found it
                        break;
                    }
                }
            }
        }
        throw new RomNotFoundException();
    }

    public Rom unmarshallRom(Node rootNode, Rom rom) throws XMLParseException,
    RomNotFoundException, StackOverflowError, Exception {
        Node n;
        NodeList nodes = rootNode.getChildNodes();
        tableScaleHandler.filterFoundRomTables(nodes);

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
                        table = tableScaleHandler.unmarshallTable(n, table, rom);
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
                    tableScaleHandler.setMemModelEndian(unmarshallAttribute(n, "endian", null));
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
