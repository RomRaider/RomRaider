/*
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2022 RomRaider.com
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

package com.romraider.xml;

import static com.romraider.xml.DOMHelper.unmarshallAttribute;
import static com.romraider.xml.DOMHelper.unmarshallText;
import static org.w3c.dom.Node.ELEMENT_NODE;

import java.io.File;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

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
import com.romraider.util.HexUtil;
import com.romraider.util.ResourceUtil;

public final class DOMRomUnmarshaller {
    private static final Logger LOGGER = Logger
            .getLogger(DOMRomUnmarshaller.class);
    private static final ResourceBundle rb = new ResourceUtil().getBundle(
            DOMRomUnmarshaller.class.getName());
    private JProgressPane progress = null;
    private ChecksumManager checksumManager = null;
    private TableScaleUnmarshaller tableScaleHandler = new TableScaleUnmarshaller();

    public Node checkDefinitionMatch(Node rootNode, byte[] input){
        Node n = findRomNodeMatch(rootNode, null, input);
        return n;
    }

    public Rom unmarshallXMLDefinition(File definition, Node rootNode, Node romNode, byte[] input,
            JProgressPane progress) throws
            XMLParseException, StackOverflowError, Exception {

        this.progress = progress;

        // Unmarshall scales first
        tableScaleHandler.unmarshallBaseScales(rootNode);

        Rom rom = new Rom(new RomID());
        Rom output = unmarshallRom(romNode, rom);

        //Set ram offset
        output.getRomID().setRamOffset(
                output.getRomID().getFileSize()
                - input.length);
        
        return output;
    }

    public static Node findFirstRomNode(Node rootNode) {
        Node n;
        NodeList nodes = rootNode.getChildNodes();

        for (int i = 0; i < nodes.getLength(); i++) {
            n = nodes.item(i);

            if (n.getNodeType() == ELEMENT_NODE
                    && n.getNodeName().equalsIgnoreCase("rom")) return n;
        }

        return null;
    }

    //Find the correct Rom Node either by xmlID or by input bytes
    //Supplying both will return null
    private Node findRomNodeMatch(Node rootNode, String xmlID, byte[] input) {
        if(xmlID == null && input == null) return null;
        if(xmlID != null && input != null) return null;

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

                        RomID romId = new RomID();
                        romId = unmarshallRomID(n2, romId);

                        //Check if bytes match in file
                        if(input != null && romId.checkMatch(input)) {
                            return n;
                        }

                        //Check if the ID matches
                        else if(xmlID != null && romId.getXmlid().equalsIgnoreCase(xmlID)) {
                            return n;
                        }

                        break;
                    }
                }
            }
        }

        return null;
    }

    public Rom unmarshallRom(Node rootNode, Rom rom) throws XMLParseException,
    RomNotFoundException, StackOverflowError, Exception {
        Node n;
        NodeList nodes = rootNode.getChildNodes();
        tableScaleHandler.filterFoundRomTables(nodes);

        progress.update(MessageFormat.format(rb.getString("CREATETABLES"),
                rom.getRomID().getXmlid()), 0);

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
            progress.update(MessageFormat.format(rb.getString("CREATETABLES"),
                    rom.getRomID().getXmlid()), currProgress);

            if (n.getNodeType() == ELEMENT_NODE) {
                if (n.getNodeName().equalsIgnoreCase("romid")) {
                    rom.setRomID(unmarshallRomID(n, rom.getRomID()));

                } else if (n.getNodeName().equalsIgnoreCase("table")) {
                    Table table = null;
                    table = rom.getTableByName(unmarshallAttribute(n, "name", null));

                    try {
                        table = tableScaleHandler.unmarshallTable(n, table, rom);
                        if (table != null) {
                            rom.addTableByName(table);
                        }
                    } catch (TableIsOmittedException ex) {
                        // table is not supported in inherited def (skip)
                        if (table != null) {
                            rom.removeTableByName(table);
                        }
                    } catch (XMLParseException ex) {
                        LOGGER.error(ex.getMessage());
                    }
                } else if (n.getNodeName().equalsIgnoreCase("checksum")) {
                    rom.getRomID().setChecksum(unmarshallAttribute(n, "type", ""));
                    checksumManager = unmarshallChecksum(rom, n);
                    rom.addChecksumManager(checksumManager);
                }
                else if (n.getNodeType() == ELEMENT_NODE
                            && n.getNodeName().equalsIgnoreCase("Simulation")) {
                    	tableScaleHandler.unmarshallSimulation(rom, n);
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

            Node n = findRomNodeMatch(rootNode, xmlID, null);

            if(n != null) {
                Rom returnrom = unmarshallRom(n, rom);
                returnrom.getRomID().setObsolete(false);
                return returnrom;
            }

        throw new RomNotFoundException();
    }

    public RomID unmarshallRomID(Node romIDNode, RomID romID) {
        Node n;
        NodeList nodes = romIDNode.getChildNodes();

        for (int i = 0; i < nodes.getLength(); i++) {
            n = nodes.item(i);

            if (n.getNodeType() == ELEMENT_NODE) {
                String nodeName = n.getNodeName();

                if (nodeName.equalsIgnoreCase("xmlid")) {
                    romID.setXmlid(unmarshallText(n));

                } else if (nodeName.equalsIgnoreCase("internalidaddress")) {
                    romID.setInternalIdAddress(RomAttributeParser
                            .parseHexString(unmarshallText(n)));

                } else if (nodeName.equalsIgnoreCase("internalidstring")) {
                    romID.setInternalIdString(unmarshallText(n));

                } else if (nodeName.equalsIgnoreCase("author")) {
                    romID.setAuthor(unmarshallText(n));

                } else if (nodeName.equalsIgnoreCase("version")) {
                    romID.setVersion(unmarshallText(n));

                } else if (nodeName.equalsIgnoreCase("caseid")) {
                    romID.setCaseId(unmarshallText(n));

                } else if (nodeName.equalsIgnoreCase("ecuid")) {
                    romID.setEcuId(unmarshallText(n));

                } else if (nodeName.equalsIgnoreCase("make")) {
                    romID.setMake(unmarshallText(n));

                } else if (nodeName.equalsIgnoreCase("market")) {
                    romID.setMarket(unmarshallText(n));

                } else if (nodeName.equalsIgnoreCase("model")) {
                    romID.setModel(unmarshallText(n));

                } else if (nodeName.equalsIgnoreCase("submodel")) {
                    romID.setSubModel(unmarshallText(n));

                } else if (nodeName.equalsIgnoreCase("transmission")) {
                    romID.setTransmission(unmarshallText(n));

                } else if (nodeName.equalsIgnoreCase("year")) {
                    romID.setYear(unmarshallText(n));

                } else if (nodeName.equalsIgnoreCase("noramoffset")) {
                    romID.disableRamOffset();

                } else if (nodeName.equalsIgnoreCase("offset")) {
                    romID.setOffset(HexUtil.hexToInt(unmarshallText(n)));

                } else if (nodeName.equalsIgnoreCase("flashmethod")) {
                    romID.setFlashMethod(unmarshallText(n));

                } else if (nodeName.equalsIgnoreCase("memmodel")) {
                    romID.setMemModel(unmarshallText(n));

                    tableScaleHandler.setMemModelEndian(unmarshallAttribute(n, "endian", null));
                } else if (nodeName.equalsIgnoreCase("filesize")) {
                    romID.setFileSize(RomAttributeParser
                            .parseFileSize(unmarshallText(n)));

                } else if (nodeName.equalsIgnoreCase("obsolete")) {
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
    private ChecksumManager unmarshallChecksum(Rom rom, Node node) {
        final Map<String, String> attrs = new HashMap<String, String>();

        for (int i = 0; i < node.getAttributes().getLength(); i++) {
            attrs.put(node.getAttributes().item(i).getNodeName().toLowerCase(),
                    node.getAttributes().item(i).getNodeValue());
        }
           return ChecksumFactory.getManager(rom, attrs);
    }

}
