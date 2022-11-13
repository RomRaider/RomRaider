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

package com.romraider.xml.ConversionLayer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.romraider.util.HexUtil;
import com.romraider.util.ResourceUtil;

public class XDFConversionLayer extends ConversionLayer {
    protected static final ResourceBundle rb = new ResourceUtil().getBundle(
        XDFConversionLayer.class.getName());
    private static final Logger LOGGER = Logger.getLogger(XDFConversionLayer.class);

    private HashMap < Integer, String > categoryMap = new HashMap < Integer, String > ();
    private HashMap < Integer, Element > tableMap = new HashMap < Integer, Element > ();
    private LinkedList < EmbedInfoData > embedsToSolve = new LinkedList < EmbedInfoData > ();

    int bitCount;
    private boolean signed;
    private int offset;
    private int numDigits;
    // private String dataType;
    private boolean lsbFirst;

    // Defaults
    String defaultDataType;

    private class EmbedInfoData {
        Element tableNodeRR;
        Node axisNode;
        Node flagsNodeTable;
    }
    
    @Override
    public String getDefinitionPickerInfo() {
        return rb.getString("LOADINGWARNING");
    }

    @Override
    public String getRegexFileNameFilter() {
        return "^.*xdf";
    }

    @Override
    public Document convertToDocumentTree(File f) throws Exception {
        Document doc = null;
        FileInputStream fileStream = null;
        BufferedReader br = null;

        try {
            // Check first if its an older definition
            // which is not xml based
            br = new BufferedReader(new FileReader(f));
            String firstLine = br.readLine();

            if (firstLine.equalsIgnoreCase("XDF")) {
                br.close();
                throw new SAXException(rb.getString("ONLYXML"));
            } else {
                br.close();
            }

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setXIncludeAware(true);
            DocumentBuilder docBuilder = factory.newDocumentBuilder();

            fileStream = new FileInputStream(f);
            Document XMLdoc = docBuilder.parse(fileStream, f.getAbsolutePath());
            doc = convertXDFDocument(XMLdoc);
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fileStream != null)
                    fileStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return doc;
    }

    private Element solveLinkObj(int key, Node targetTable) {
        if (tableMap.containsKey(key)) {
            Element sourceTableRR = tableMap.get(key);
            return (Element) sourceTableRR.cloneNode(true);
        } else {
            return null;
        }
    }

    private Element parseAxis(Document doc, Element tableNodeRR, Node axisNode, Node flagsNodeTable) {
        Node idNode = axisNode.getAttributes().getNamedItem("id");
        String id = "";

        if (idNode != null) {
            id = idNode.getNodeValue();
        }

        Element targetTable = null;
        Element scaling = null;

        boolean hasEmbedInfo = false;
        String staticTable = "";

        int nodeCountAxis = axisNode.getChildNodes().getLength();
        Node n;

        // Check first if we need to copy attributes from the base table
        for (int i = 0; i < nodeCountAxis; i++) {
            n = axisNode.getChildNodes().item(i);

            if (n.getNodeName().equalsIgnoreCase("embedinfo") && n.getAttributes().getNamedItem("linkobjid") != null) {

                Integer key = HexUtil.hexToInt(n.getAttributes().getNamedItem("linkobjid").getNodeValue());
                Element refTable = solveLinkObj(key, targetTable);

                // Table was already parsed
                if (refTable != null) {
                    targetTable = refTable;

                    int nodeCountRefTable = targetTable.getChildNodes().getLength();
                    targetTable.removeAttribute("type");
                    targetTable.removeAttribute("category");

                    // Find scaling child and delete child tables
                    LinkedList < Node > nodesToRemove = new LinkedList <Node> ();
                    for (int j = 0; j < nodeCountRefTable; j++) {
                        Node tN = targetTable.getChildNodes().item(j);

                        if (tN.getNodeName().equalsIgnoreCase("table") ||
                            tN.getNodeName().equalsIgnoreCase("description")) {
                            nodesToRemove.add(tN);
                        } else if (tN.getNodeName().equalsIgnoreCase("scaling")) {
                            scaling = (Element) tN;
                        }
                    }
                    for (Node nodeToRemove: nodesToRemove)
                        targetTable.removeChild(nodeToRemove);
                }
                // Referenced Table is not yet parsed
                else {
                    EmbedInfoData e = new EmbedInfoData();
                    e.axisNode = axisNode;
                    e.flagsNodeTable = flagsNodeTable;
                    e.tableNodeRR = tableNodeRR;
                    embedsToSolve.add(e);

                    return null;
                }

                hasEmbedInfo = true;
                break;
            }
        }

        if (targetTable == null) {
            targetTable = id.equalsIgnoreCase("z") || id.isEmpty() ? tableNodeRR : doc.createElement("table");
        }

        if (scaling == null) {
            scaling = doc.createElement("scaling");
            targetTable.appendChild(scaling);
        }

        Node addressNode = null;
        LinkedList<String> staticCells = new LinkedList<String>();
        int indexCount = -1;
        int numDigitsStatic = -1;
        int localNumDigits = -1;

        for (int i = 0; i < nodeCountAxis; i++) {
            n = axisNode.getChildNodes().item(i);

            if (n.getNodeName().equalsIgnoreCase("units")) {
                scaling.setAttribute("units", n.getTextContent());
            } else if (n.getNodeName().equalsIgnoreCase("indexcount")) {
                indexCount = Integer.parseInt(n.getTextContent());
                targetTable.setAttribute("size" + id.toLowerCase(), "" + indexCount);
            }

            if (!hasEmbedInfo) {
                if (n.getNodeName().equalsIgnoreCase("embeddeddata")) {

                    addressNode = n.getAttributes().getNamedItem("mmedaddress");
                    if (addressNode != null) {
                        String address = addressNode.getNodeValue();
                        targetTable.setAttribute("storageaddress", address);
                    }

                    Node flagsNode = n.getAttributes().getNamedItem("mmedtypeflags");
                    Node sizeBitsNode = n.getAttributes().getNamedItem("mmedelementsizebits");

                    boolean signedLocal = signed;
                    boolean lsbFirstLocal = lsbFirst;
                    int flags = 0;
                    int sizeBits = 0;

                    if (flagsNode == null)
                        flagsNode = flagsNodeTable;

                    if (flagsNode != null) {
                        try {
                            flags = HexUtil.hexToInt(flagsNode.getNodeValue());

                            if ((flags & (0x01)) > 0) {
                                signedLocal = true;
                            } else if ((flags & 0x01) == 0) {
                                signedLocal = false;
                            }

                            if ((flags & 0x02) > 0) {
                                lsbFirstLocal = false;
                            } else {
                                lsbFirstLocal = true;
                            }

                            if ((flags & 0x04) > 0) {
                                targetTable.setAttribute("swapxy", "true");
                            }
                        } catch (NumberFormatException e) {
                            // TODO: Not sure how to handle this yet...
                            LOGGER.error("Failed to parse flag " + flagsNode.getNodeValue());
                        }
                    }

                    if (sizeBitsNode != null) {
                        sizeBits = Integer.parseInt(sizeBitsNode.getNodeValue());
                    } else {
                        sizeBits = bitCount;
                    }

                    targetTable.setAttribute("storagetype", (signedLocal ? "" : "u") + "int" + sizeBits);
                    targetTable.setAttribute("endian", lsbFirstLocal ? "big" : "little");
                } else if (!hasEmbedInfo && n.getNodeName().equalsIgnoreCase("math")) {
                    String formula = n.getAttributes().getNamedItem("equation").getNodeValue();
                    formula = formula.replace("X", "x").replace(",", ".");
                    scaling.setAttribute("expression", formula);
                } else if (n.getNodeName().equalsIgnoreCase("decimalpl")) {
                    try {
                        localNumDigits = Math.abs(Integer.parseInt(n.getTextContent()));
                    } catch (NumberFormatException e) {
                        //Do nothing
                    }
                } else if (n.getNodeName().equalsIgnoreCase("label")) {
                    String label = n.getAttributes().getNamedItem("value").getNodeValue();
                    staticCells.add(label);
                }
            }
        }

        boolean isStatic = staticCells.size() == indexCount && indexCount > 1 && !staticCells.peekLast().equalsIgnoreCase("0.00");
        if (isStatic) {
            staticTable = "Static ";
            targetTable.setAttribute("size" + id, "" + staticCells.size());
            targetTable.removeAttribute("endian");
            targetTable.removeAttribute("storagetype");
            
            for(String label : staticCells)
            {
                Element data = doc.createElement("data");
                data.setTextContent(label);
            	targetTable.appendChild(data);
            	
                if (numDigitsStatic == -1) {
                    // Assume the format from the static data
                    String split[] = label.split("\\.");
                    if (split.length > 1) {
                        numDigitsStatic = split[1].length();
                    } else {
                        numDigitsStatic = 0;
                    }
                }
            }

        }

        // Case 1: Static table and no num digits set == Deduce from text
        // Case 2: Non static table and digits are set
        // Case 3: Non static table and digits arent sent --> use defaults
        int digits = isStatic && localNumDigits == -1 ? numDigitsStatic : (localNumDigits == -1 ? numDigits : localNumDigits);
        if (digits == 0)
            scaling.setAttribute("format", "0");
        else
            scaling.setAttribute("format", "0." + new String(new char[digits]).replace("\0", "0"));

        if (id.equalsIgnoreCase("z"))
            return null;
        else {
            if (!id.isEmpty())
                targetTable.setAttribute("type", staticTable + id.toUpperCase() + " Axis");
            return targetTable;
        }
    }

    private Element parseTable(Document doc, Node romNode, Node tableNode) {
        int nodeCountTable = tableNode.getChildNodes().getLength();
        Node n;
        Element tableNodeRR = doc.createElement("table");

        Node uniqueIDNode = tableNode.getAttributes().getNamedItem("uniqueid");
        Node flagsNode = tableNode.getAttributes().getNamedItem("flags");

        if (uniqueIDNode != null) {
            tableMap.put(HexUtil.hexToInt(uniqueIDNode.getNodeValue()), tableNodeRR);
        }

        LinkedList < String > categories = new LinkedList < String > ();

        for (int i = 0; i < nodeCountTable; i++) {
            n = tableNode.getChildNodes().item(i);

            if (n.getNodeName().equalsIgnoreCase("title")) {
                // TunerPro can currently not edit axis directly, but we can
                // These tables contain the axis, which we can skip
                if (n.getTextContent().endsWith("(autogen)")) {
                    return null;
                }

                tableNodeRR.setAttribute("name", n.getTextContent());
            }
            if (n.getNodeName().equalsIgnoreCase("description")) {
                Element desc = doc.createElement("description");
                desc.setTextContent(n.getTextContent());
                tableNodeRR.appendChild(desc);
            } else if (n.getNodeName().equalsIgnoreCase("categorymem")) {
                int category = Integer.parseInt(n.getAttributes().getNamedItem("category").getNodeValue());

                if (categoryMap.containsKey(category - 1)) {
                    categories.add(categoryMap.get(category - 1));
                }
            } else if (n.getNodeName().equalsIgnoreCase("xdfaxis")) {
                Element axis = parseAxis(doc, tableNodeRR, n, flagsNode);

                if (axis != null) {
                    tableNodeRR.appendChild(axis);
                }
            }
        }

        tableNodeRR.setAttribute("category", convertToRRCategoryString(categories));
        return tableNodeRR;
    }

    private Element getScalingNodeForTable(Element tableNodeRR) {
        for (int i = 0; i < tableNodeRR.getChildNodes().getLength(); i++) {
            Element n = (Element) tableNodeRR.getChildNodes().item(i);

            if (n.getNodeName().equalsIgnoreCase("scaling")) {
                return n;
            }
        }

        return null;
    }

    private void postProcessTable(Element tableNodeRR) {
        int validAxis = 0;
        int nodeCountTable = tableNodeRR.getChildNodes().getLength();
        
        LinkedList <Element> nodesToRemove = new LinkedList <Element> ();
        for (int i = 0; i < nodeCountTable; i++) {
            Element n = (Element) tableNodeRR.getChildNodes().item(i);
            if (n.getNodeName().equalsIgnoreCase("table")) {
                if (n.hasAttribute("storageaddress") || n.getAttributeNode("type").getValue().contains("Static")) {
                    validAxis++;
                    
                    // Use the sizes of the X and Y axis
                    // for the main table
                    Attr sizex = n.getAttributeNode("sizex");
                    Attr sizey = n.getAttributeNode("sizey");
                    if (sizex != null)
                        tableNodeRR.setAttributeNode((Attr) sizex.cloneNode(false));
                    else if (sizey != null)
                        tableNodeRR.setAttributeNode((Attr) sizey.cloneNode(false));
                } else {
                    Element scalingNode = getScalingNodeForTable(tableNodeRR);
                    Element axisScalingNode = getScalingNodeForTable(n);
                    
                    // 2D Tables work different in XDFs
                    // We have to use the unit of the "missing" axis for the main table
                    if (scalingNode != null && axisScalingNode != null && !scalingNode.hasAttribute("units")) {
                        scalingNode.setAttribute("units", axisScalingNode.getAttribute("units"));
                    }
                    nodesToRemove.add(n);
                }
            }
        }

        for (Element n: nodesToRemove) {
            tableNodeRR.removeChild(n);
        }

        tableNodeRR.setAttribute("type", (validAxis + 1) + "D");
    }


    private String convertToRRCategoryString(List < String > categories) {
        String category = "";
        for (int i = 0; i < categories.size(); i++) {
            String cat = categories.get(i);
            category += cat;

            if (i < categories.size() - 1)
                category += "//";
        }

        return category;
    }

    private Node parseXDFHeader(Document doc, Node romNode, Node header) {
        int nodeCountHeader = header.getChildNodes().getLength();
        Node n;
        Node romIDNode = doc.createElement("romid");

        for (int i = 0; i < nodeCountHeader; i++) {
            n = header.getChildNodes().item(i);

            if (n.getNodeName().equalsIgnoreCase("CATEGORY")) {
                categoryMap.put(HexUtil.hexToInt(n.getAttributes().getNamedItem("index").getNodeValue()),
                    n.getAttributes().getNamedItem("name").getNodeValue());
            } else if (n.getNodeName().equalsIgnoreCase("flags")) {
                // TODO
            } else if (n.getNodeName().equalsIgnoreCase("author")) {
                String author = n.getTextContent();
                Node ecuID = doc.createElement("author");
                ecuID.setTextContent(author);
                romIDNode.appendChild(ecuID);
            } else if (n.getNodeName().equalsIgnoreCase("fileversion")) {
                String version = n.getTextContent();
                Node ecuID = doc.createElement("version");
                ecuID.setTextContent(version);
                romIDNode.appendChild(ecuID);
            } else if (n.getNodeName().equalsIgnoreCase("deftitle")) {
                String title = n.getTextContent();
                Node ecuID = doc.createElement("xmlid");
                ecuID.setTextContent(title);
                romIDNode.appendChild(ecuID);
            } else if (n.getNodeName().equalsIgnoreCase("description")) {
                // TODO
            } else if (n.getNodeName().equalsIgnoreCase("BASEOFFSET")) {
                Node offsetNode = n.getAttributes().getNamedItem("offset");

                if (offsetNode != null) {
                    offset = Integer.parseInt(offsetNode.getNodeValue());

                    if (!n.getAttributes().getNamedItem("subtract").getNodeValue().equals("0")) {
                        offset *= -1;
                    }
                }
            } else if (n.getNodeName().equalsIgnoreCase("DEFAULTS")) {
                if (!n.getAttributes().getNamedItem("float").getNodeValue().equalsIgnoreCase("0")) {
                    // dataType = "float";
                } else {
                    bitCount = Integer.parseInt(n.getAttributes().getNamedItem("datasizeinbits").getNodeValue());
                    signed = !n.getAttributes().getNamedItem("signed").getNodeValue().equalsIgnoreCase("0");

                    // dataType = (signed ? "" : "u") + "int" + bitCount;
                    lsbFirst = !n.getAttributes().getNamedItem("lsbfirst").getNodeValue().equalsIgnoreCase("0");
                    numDigits = HexUtil.hexToInt(n.getAttributes().getNamedItem("sigdigits").getNodeValue());
                }
            } else if (n.getNodeName().equalsIgnoreCase("REGION")) {
                // Ignored currently: type, startAddress, regionFlags, name, desc
                // TODO: Start address probably matters....
                int fileSize = HexUtil.hexToInt(n.getAttributes().getNamedItem("size").getNodeValue());
                Node fileSizeN = doc.createElement("filesize");
                fileSizeN.setTextContent(fileSize + "b");
            }
        }

        // XDFs dont have an identification component
        // So we just load it, no questions asked
        Node idAddress = doc.createElement("internalidaddress");
        Node idString = doc.createElement("internalidstring");
        idString.setTextContent("force");
        idAddress.setTextContent("-1");

        Element offsetNode = doc.createElement("offset");
        offsetNode.setTextContent("0x" + Integer.toHexString(offset));

        romIDNode.appendChild(offsetNode);
        romIDNode.appendChild(idAddress);
        romIDNode.appendChild(idString);

        return romIDNode;
    }

    private Document convertXDFDocument(Document xdfDoc) throws SAXException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = null;

        try {
            builder = dbf.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }

        // New RomRaider document
        Document doc = builder.newDocument();

        Node baseNode = xdfDoc;
        Node romNode = null;
        int nodeCount = 0;
        int nodeCountBase = baseNode.getChildNodes().getLength();

        for (int i = 0; i < nodeCountBase; i++) {
            Node n = baseNode.getChildNodes().item(i);
            if (n.getNodeName().equalsIgnoreCase("XDFFORMAT")) {
                baseNode = n;
                break;
            }
        }

        if (baseNode == xdfDoc) {
            throw new SAXException(rb.getString("NOXDFFORMAT"));
        }

        nodeCount = baseNode.getChildNodes().getLength();
        Node header = null;

        // Find XDF Header first
        for (int i = 0; i < nodeCount; i++) {
            Node n = baseNode.getChildNodes().item(i);
            if (n.getNodeName().equalsIgnoreCase("XDFHEADER")) {

                // Create the initial document
                Node roms = doc.createElement("roms");
                romNode = doc.createElement("rom");

                doc.appendChild(roms);
                roms.appendChild(romNode);

                header = n;
                Node headerNode = parseXDFHeader(doc, romNode, header);
                romNode.appendChild(headerNode);
                break;
            }
        }

        if (header == null) {
            throw new SAXException(rb.getString("NOXDFHEADER"));
        }

        LinkedList <Element> tables = new LinkedList <Element> ();

        // Go through all tables and create RR tables
        for (int i = 0; i < nodeCount; i++) {
            Node n = baseNode.getChildNodes().item(i);
            if (n.getNodeName().equalsIgnoreCase("XDFTABLE")) {
                Element table = parseTable(doc, romNode, n);

                if (table != null) {
                    tables.add(table);
                }
            }
            // A constant is a mix between a table and an axis
            // So parse it as both
            else if (n.getNodeName().equalsIgnoreCase("XDFCONSTANT")) {
                Element table = parseTable(doc, romNode, n);
                parseAxis(doc, table, n, null);
                if (table != null) {
                    tables.add(table);
                }
            }
        }

        // Some references could not be solved because we didnt parse the table yet
        // So we have to do these now
        for (EmbedInfoData e: embedsToSolve) {
            Element axis = parseAxis(doc, e.tableNodeRR, e.axisNode, e.flagsNodeTable);

            if (axis != null)
                e.tableNodeRR.appendChild(axis);
        }

        // Final cleanup and add tables to ROM
        for (Element t: tables) {
            postProcessTable(t);
            romNode.appendChild(t);
        }

        categoryMap.clear();
        tableMap.clear();
        embedsToSolve.clear();
        return doc;
    }
}