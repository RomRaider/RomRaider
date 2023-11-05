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

import java.util.HashMap;
import java.util.Map;

import javax.management.modelmbean.XMLParseException;
import javax.swing.JOptionPane;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.romraider.Settings;
import com.romraider.editor.ecu.ECUEditorManager;
import com.romraider.logger.ecu.ui.handler.dataflow.DataflowSimulationHandler;
import com.romraider.dataflowSimulation.DataflowSimulation;
import com.romraider.dataflowSimulation.TableAction;
import com.romraider.dataflowSimulation.CalculationAction;
import com.romraider.maps.Rom;
import com.romraider.maps.Scale;
import com.romraider.maps.Table;
import com.romraider.maps.Table1D;
import com.romraider.maps.Table1DView.Table1DType;
import com.romraider.maps.Table2D;
import com.romraider.maps.Table3D;
import com.romraider.maps.TableBitwiseSwitch;
import com.romraider.maps.TableSwitch;
import com.romraider.swing.DebugPanel;
import com.romraider.util.ObjectCloner;
import com.romraider.util.SettingsManager;

public class TableScaleUnmarshaller {
    private static final Logger LOGGER = Logger.getLogger(TableScaleUnmarshaller.class);
    private final Map<String, Integer> tableNames = new HashMap<String, Integer>();
    private final Map<String, Scale> scales = new HashMap<String, Scale>();
    private String memModelEndian = null;

    public void setMemModelEndian(String endian) {
        memModelEndian = endian;
    }

    public void unmarshallBaseScales(Node rootNode) {
        NodeList nodes = rootNode.getChildNodes();
        Node n;

        for (int i = 0; i < nodes.getLength(); i++) {
            n = nodes.item(i);

            if (n.getNodeType() == ELEMENT_NODE
                    && n.getNodeName().equalsIgnoreCase("scalingbase")) {
                unmarshallScale(n, new Scale());
            }
        }
    }
 
    public Table unmarshallTable(Node tableNode, Table table, Rom rom)
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
            } catch (NullPointerException ex) {
                JOptionPane.showMessageDialog(ECUEditorManager.getECUEditor(),
                        new DebugPanel(ex, SettingsManager.getSettings().getSupportURL()), "Exception",
                        JOptionPane.ERROR_MESSAGE);
            }
        }

        if (table == null) {
            // create new instance (otherwise it is inherited)
            final String tn = unmarshallAttribute(tableNode, "name", "unknown");
            final String type = unmarshallAttribute(tableNode, "type", "none");

            if (tableNames.containsKey(tn) || type.contains("xis")) {
                if (type.equalsIgnoreCase("3D")) {
                    table = new Table3D();

                } else if (type.equalsIgnoreCase("2D")) {
                    table = new Table2D();

                } else if (type.equalsIgnoreCase("1D")) {
                    table = new Table1D();

                } else if (type.equalsIgnoreCase("X Axis")
                        || type.equalsIgnoreCase("Static X Axis"))  {
                    table = new Table1D();

                } else if (type.equalsIgnoreCase("Y Axis")
                        || type.equalsIgnoreCase("Static Y Axis")) {
                    table = new Table1D();
                } else if (type.equalsIgnoreCase("Switch")) {
                    table = new TableSwitch();

                } else if (type.equalsIgnoreCase("BitwiseSwitch")) {
                    table = new TableBitwiseSwitch();
                }
                else if (type.equalsIgnoreCase("none")){
                    throw new XMLParseException("Table type unspecified for "
                            + tableNode.getAttributes().getNamedItem("name"));
                }
                else {
                    throw new XMLParseException("Table type " + type + " unknown for "
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

        table.setDataLayout(unmarshallAttribute(tableNode, "dataLayout", ""));
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
        // Set axis size, if sizex is specified use it, if sizey is specified use it,
        // if neither are specified use the base definition size
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
        table.setStringMask(
                unmarshallAttribute(tableNode, "mask", ""));

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
            ((Table3D) table).setSkipCells(unmarshallAttribute(tableNode, "skipCells",
                    ((Table3D) table).getSkipCells()));
        }

        Node n;
        NodeList nodes = tableNode.getChildNodes();

        for (int i = 0; i < nodes.getLength(); i++) {
            n = nodes.item(i);

            if (n.getNodeType() == ELEMENT_NODE) {
                if (n.getNodeName().equalsIgnoreCase("table")) {
                    Table tempTable = null;

                    if (table.getType() == Table.TableType.TABLE_2D) { // if table is 2D,
                        // parse axis
                        if (RomAttributeParser
                                .parseTableAxis(unmarshallAttribute(n, "type",
                                        "unknown")) == Table1DType.Y_AXIS
                                        || RomAttributeParser
                                        .parseTableAxis(unmarshallAttribute(n,
                                                "type", "unknown")) == Table1DType.X_AXIS) {


                            tempTable = unmarshallTable(n, ((Table2D) table).getAxis(), rom);

                            if (tempTable.getDataSize() != table.getDataSize()) {
                                tempTable.setDataSize(table.getDataSize());
                            }

                            tempTable.setData(((Table2D) table).getAxis().getData());
                            ((Table2D) table).setAxis((Table1D)tempTable);
                        }
                    } else if (table.getType() == Table.TableType.TABLE_3D) { // if table
                        // is 3D, populate xAxis
                        if (RomAttributeParser
                                .parseTableAxis(unmarshallAttribute(n, "type",
                                        "unknown")) == Table1DType.X_AXIS) {

                            tempTable = unmarshallTable(n, ((Table3D) table).getXAxis(), rom);

                            if (tempTable.getDataSize() != ((Table3D) table).getSizeX()) {
                                tempTable.setDataSize(((Table3D) table).getSizeX());
                            }

                            tempTable.setData(((Table3D) table).getXAxis().getData());

                            ((Table3D) table).setXAxis((Table1D)tempTable);
                        }
                        else if (RomAttributeParser
                                .parseTableAxis(unmarshallAttribute(n, "type",
                                        "unknown")) == Table1DType.Y_AXIS) {

                            tempTable = unmarshallTable(n,((Table3D) table).getYAxis(), rom);

                            if (tempTable.getDataSize() != ((Table3D) table).getSizeY()) {
                                tempTable.setDataSize(((Table3D) table).getSizeY());
                            }

                            tempTable.setData(((Table3D) table).getYAxis().getData());
                            ((Table3D) table).setYAxis((Table1D)tempTable);
                        }
                    }

                } else if (n.getNodeName().equalsIgnoreCase("scaling")) {
                    // check whether scale already exists. if so, modify, else
                    // use new instance
                    Scale baseScale = table.getScale(unmarshallAttribute(n, "category", "Default"));
                    table.addScale(unmarshallScale(n, baseScale));

                } else if (n.getNodeName().equalsIgnoreCase("data")) {
                    // parse and add data to table
                    if (table instanceof Table1D) {
                        ((Table1D)table).addStaticDataCell(unmarshallText(n));
                    } else {
                        // Why would this happen.  Static should only be for axis.
                        LOGGER.error("Error adding static data cell.");
                    }

                } else if (n.getNodeName().equalsIgnoreCase("description")) {
                    table.setDescription(unmarshallText(n));

                } else if (n.getNodeName().equalsIgnoreCase("state")) {
                    //Check for duplicate names, then replace if exist or add otherwise
                    table.addPresetValue(
                                unmarshallAttribute(n, "name", ""),
                                unmarshallAttribute(n, "data", "0"));
                } else if (n.getNodeName().equalsIgnoreCase("bit")) {
                    table.setPresetValues(
                            unmarshallAttribute(n, "name", ""),
                            unmarshallAttribute(n, "position", ""));

                } else { /* unexpected element in Table (skip) */
                }
            } else { /* unexpected node-type in Table (skip) */
            }
        }
        return table;
    }

    /**
     * Create a list of table names to be used as a filter on the inherited
     * tables to reduce unnecessary table object creation.
     * @param nodes -  the NodeList to filter
     * @throws XMLParseException
     * @throws TableIsOmittedException
     * @throws Exception
     */
    public void filterFoundRomTables (NodeList nodes) {
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

    void unmarshallSimulation(Rom rom, Node simulationNode) {

        // look for base scaling attribute first
        String name = unmarshallAttribute(simulationNode, "name", "Unnamed");
        DataflowSimulation sim = new DataflowSimulation(rom, name);
    
        Node n;
        NodeList nodes = simulationNode.getChildNodes();
        
        for (int i = 0; i < nodes.getLength(); i++) {
            n = nodes.item(i);

            if (n.getNodeType() == ELEMENT_NODE && n.getNodeName().equalsIgnoreCase("inputs"))
                {
                    Node nodeInput;
                    NodeList inputNodes = n.getChildNodes();
                    
                    for (int j = 0; j < inputNodes.getLength(); j++) {
                    	nodeInput = inputNodes.item(j);
                    	
                    	if (nodeInput.getNodeType() == ELEMENT_NODE && nodeInput.getNodeName().equalsIgnoreCase("input"))
                    	{
                    		String inputName =  unmarshallAttribute(nodeInput, "name", "");
                    		String logParam = unmarshallAttribute(nodeInput, "logparam", "");
                    		sim.addInput(inputName, !logParam.isEmpty());
                    		
                    		DataflowSimulationHandler.getInstance().registerInput(logParam, inputName, sim);                   				
                    	} 
                }                    
            }    
            else if (n.getNodeType() == ELEMENT_NODE && n.getNodeName().equalsIgnoreCase("dataflow"))
                {
                	 Node nodeAction;
                     NodeList dataflowNodes = n.getChildNodes();
                     
                     for (int j = 0; j < dataflowNodes.getLength(); j++) {
                     	nodeAction = dataflowNodes.item(j);
                     	
                     	if (nodeAction.getNodeType() == ELEMENT_NODE && nodeAction.getNodeName().equalsIgnoreCase("table"))
                     		{
                     			String referenceName = unmarshallAttribute(nodeAction, "reference", "");                			
                     			String input_x = unmarshallAttribute(nodeAction, "input_x", "");
                     			String input_y = unmarshallAttribute(nodeAction, "input_y", "");
                     			String output = unmarshallAttribute(nodeAction, "output", "");
                     			TableAction action = new TableAction(output, referenceName, input_x, input_y);
	                     		sim.addAction(action);	
                     	}     
                     	else if (nodeAction.getNodeType() == ELEMENT_NODE && nodeAction.getNodeName().equalsIgnoreCase("action"))
                 		{
                 			String expression = unmarshallAttribute(nodeAction, "expression", "");                			
                 			String output = unmarshallAttribute(nodeAction, "output", "");
                 			CalculationAction action = new CalculationAction(output, expression);
                     		sim.addAction(action);	
                 	}
                }
            }
            else if (n.getNodeType() == ELEMENT_NODE && n.getNodeName().equalsIgnoreCase("description"))
            {
            	sim.setDescription(n.getTextContent());
            }
        }

        rom.addSimulation(sim);
    }
    
    public Scale unmarshallScale(Node scaleNode, Scale scale) {

        // look for base scaling attribute first
        String base = unmarshallAttribute(scaleNode, "base", "none");
        if (!base.equalsIgnoreCase("none")) {
            // check whether base value matches the name of a an existing
            // scalingbase, if so, inherit from scalingbase
            if (scales.containsKey(base.toLowerCase())) {
                try {
                    scale = (Scale) ObjectCloner.deepCopy(scales.get(base.toLowerCase()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        // Set Category to Default if missing or not inherited from scalingbase
        if (base.equalsIgnoreCase("none")) {
            scale.setCategory(unmarshallAttribute(scaleNode, "category", "Default"));
        }

        // Set scaling name, if a scaling has no name attribute (scaling is
        // defined in a table element), try and use the units value as its
        // name, otherwise use none
        if (!scale.getCategory().equalsIgnoreCase("Raw Value") &&
                scale.getName().equalsIgnoreCase("Raw Value")) {
                scale.setName(unmarshallAttribute(scaleNode, "name",
                    unmarshallAttribute(scaleNode, "units", "none")));
        }

        scale.setByteExpression(unmarshallAttribute(scaleNode, "to_byte", ""));

        // Iterate over other available attributes
        for(int i=0; i < scaleNode.getAttributes().getLength(); i++) {
            Node attr = scaleNode.getAttributes().item(i);
            String name = attr.getNodeName();
            String value = attr.getNodeValue();

            if (name.equalsIgnoreCase("units")) scale.setUnit(value);
            else if (name.equalsIgnoreCase("expression")) scale.setExpression(value);
            else if (name.equalsIgnoreCase("format")) scale.setFormat(value);
            else if (name.equalsIgnoreCase("max")) scale.setMax(Double.parseDouble(value));
            else if (name.equalsIgnoreCase("min")) scale.setMin(Double.parseDouble(value));
            else if (name.equalsIgnoreCase("coarseincrement") || name.equalsIgnoreCase("increment"))
                scale.setCoarseIncrement(Double.parseDouble(value));
            else if (name.equalsIgnoreCase("fineincrement")) scale.setFineIncrement(Double.parseDouble(value));
        }

        //Keep track of the scales if the base attribute is used later
        scales.put(scale.getName().toLowerCase(), scale);
        return scale;
    }

    // for unit testing
    public Map<String, Scale> getScales() {
        return scales;
    }
}
