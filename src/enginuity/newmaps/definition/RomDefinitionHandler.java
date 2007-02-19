/*
 *
 * Copyright (C) 2006 Enginuity.org
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
 *
 */

package enginuity.newmaps.definition;

import static enginuity.newmaps.definition.AttributeParser.parseEndian;
import static enginuity.newmaps.definition.AttributeParser.parseStorageType;
import static enginuity.newmaps.definition.AttributeParser.parseUnitSystem;
import enginuity.newmaps.definition.index.Index;
import enginuity.newmaps.definition.index.IndexItem;
import enginuity.newmaps.ecumetadata.AxisMetadata;
import enginuity.newmaps.ecumetadata.Category;
import enginuity.newmaps.ecumetadata.TableMetadata;
import enginuity.newmaps.ecumetadata.ParameterMetadata;
import enginuity.newmaps.ecumetadata.RomMetadata;
import enginuity.newmaps.ecumetadata.Scale;
import enginuity.newmaps.ecumetadata.SourceDefAxisMetadata;
import enginuity.newmaps.ecumetadata.SwitchMetadata;
import enginuity.newmaps.ecumetadata.SwitchGroupMetadata;
import enginuity.newmaps.ecumetadata.Table2DMetadata;
import enginuity.newmaps.ecumetadata.Table3DMetadata;
import enginuity.newmaps.ecumetadata.Unit;
import enginuity.newmaps.xml.SaxParserFactory;
import static enginuity.util.HexUtil.hexToInt;
import enginuity.util.NamedSet;
import enginuity.util.exception.NameableNotFoundException;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import static java.lang.Boolean.parseBoolean;
import static java.lang.Float.parseFloat;
import static java.lang.Integer.parseInt;
import java.util.Stack;

public class RomDefinitionHandler extends DefaultHandler {

    private static final String TAG_ROM = "rom";
    private static final String TAG_PARAMETERS = "parameters";
    private static final String TAG_CATEGORY = "category";
    private static final String TAG_TABLE3D = "table3d";
    private static final String TAG_TABLE2D = "table2d";
    private static final String TAG_PARAMETER = "parameter";
    private static final String TAG_SWITCHGROUP = "switchgroup";
    private static final String TAG_SWITCH = "switch";
    private static final String TAG_DESCRIPTION = "description";
    private static final String TAG_SCALES = "scales";
    private static final String TAG_SCALE = "scale";
    private static final String TAG_AXIS = "axis";
    private static final String TAG_Y_AXIS = "yaxis";
    private static final String TAG_X_AXIS = "xaxis";
    private static final String TAG_DATA = "data";
    private static final String TAG_UNIT = "unit";
    private static final String TAG_STATE = "state";

    private static final String VAL_TRUE = "true";
    private static final String VAL_FALSE = "false";
    private static final String VAL_ON = "on";
    private static final String VAL_OFF = "off";

    private static final String ATTR_NAME = "name";
    private static final String ATTR_ABSTRACT = "abstract";
    private static final String ATTR_ID_ADDRESS = "idaddress";
    private static final String ATTR_ID_STRING = "idstring";
    private static final String ATTR_BASE = "base";
    private static final String ATTR_DESCRIPTION = "description";
    private static final String ATTR_MEMMODEL = "memmodel";
    private static final String ATTR_FLASH_METHOD = "flashmethod";
    private static final String ATTR_CASE_ID = "caseid";
    private static final String ATTR_OBSOLETE = "obsolete";
    private static final String ATTR_USER_LEVEL = "userlevel";
    private static final String ATTR_ADDRESS = "address";
    private static final String ATTR_X_ADDRESS = "xaddress";
    private static final String ATTR_Y_ADDRESS = "yaddress";
    private static final String ATTR_AXIS_ADDRESS = "axisaddress";
    private static final String ATTR_SIZE = "size";
    private static final String ATTR_SCALE = "scale";
    private static final String ATTR_DATA = "data";
    private static final String ATTR_STORAGE_TYPE = "storagetype";
    private static final String ATTR_ENDIAN = "endian";
    private static final String ATTR_LOG_PARAM = "logparam";
    private static final String ATTR_TO_REAL = "to_real";
    private static final String ATTR_TO_BYTE = "to_byte";
    private static final String ATTR_SYSTEM = "system";
    private static final String ATTR_DEFAULT = "default";
    private static final String ATTR_HIDDEN = "hidden";
    private static final String ATTR_FORMAT = "format";
    private static final String ATTR_COARSE_INCREMENT = "coarseincrement";
    private static final String ATTR_FINE_INCREMENT = "fineincrement";

    private RomMetadata rom;
    private Category category = new Category("Root"); // Category currently being created
    private Stack<Category> categoryStack; // Stack used for higher levels in tree
    private Category categories; // Category tree that will be returned
    private TableMetadata table;
    private Scale scale;
    private AxisMetadata axis;
    private Unit unit;
    private String dataValues;
    private SwitchGroupMetadata switchGroup;
    private NamedSet<TableMetadata> tables = new NamedSet<TableMetadata>();
    private NamedSet<Scale> scales = new NamedSet<Scale>();
    private NamedSet<Unit> units = new NamedSet<Unit>();
    private NamedSet<RomMetadata> roms = new NamedSet<RomMetadata>();
    private StringBuilder charBuffer;
    private Index index;
    private boolean inheriting = false;


    public RomDefinitionHandler(Index index) {

        this.index = index;
        categoryStack = new Stack<Category>();
        categories = new Category("Root");
        category = categories;
        categoryStack.add(categories);

    }


    public void startElement(String uri, String localName, String qName, Attributes attr) {

        if (TAG_ROM.equalsIgnoreCase(qName)) {

            //
            // If "base" attribute is set, find base rom in collection
            //
            rom = null;

            if (attr.getIndex(ATTR_BASE) > -1 &&
                attr.getValue(ATTR_BASE).length() > 0) {

                // Try to find ROM in existing definitions
                try {
                    // Look through parsed roms first
                    rom = (RomMetadata)roms.get(attr.getValue(ATTR_BASE));

                } catch (NameableNotFoundException ex) {
                    // Or try opening the file
                    try {
                        rom = (RomMetadata)getParentRom((IndexItem)index.get(attr.getValue(ATTR_BASE)));

                    } catch (NameableNotFoundException ex2) {
                        // No base definition found, cannot continue
                        return;
                    } catch (Exception ex2) {
                        ex2.printStackTrace();
                    }
                }

                // If found, set handler variables
                inheriting = true;
                rom.setName(attr.getValue(ATTR_NAME));
                tables = rom.getTables();
                categories = rom.getCategories();
                scales = rom.getScales();

            } else {
                rom = new RomMetadata(attr.getValue(ATTR_NAME));
            }

            // Set all other attributes
            if (attr.getIndex(ATTR_ID_ADDRESS) > -1)
                rom.setIdAddress(hexToInt(attr.getValue(ATTR_ID_ADDRESS)));
            if (attr.getIndex(ATTR_ID_STRING) > -1)
                rom.setIdString(attr.getValue(ATTR_ID_STRING));
            if (attr.getIndex(ATTR_DESCRIPTION) > -1)
                rom.setDescription(attr.getValue(ATTR_DESCRIPTION));
            if (attr.getIndex(ATTR_MEMMODEL) > -1)
                rom.setMemmodel(attr.getValue(ATTR_MEMMODEL));
            if (attr.getIndex(ATTR_FLASH_METHOD) > -1)
                rom.setFlashmethod(attr.getValue(ATTR_FLASH_METHOD));
            if (attr.getIndex(ATTR_CASE_ID) > -1)
                rom.setCaseid(attr.getValue(ATTR_CASE_ID));
            if (attr.getIndex(ATTR_OBSOLETE) > -1)
                rom.setObsolete(parseBoolean(attr.getValue(ATTR_OBSOLETE)));
            if (attr.getIndex(ATTR_ABSTRACT) > -1)
                rom.setAbstract(parseBoolean(attr.getValue(ATTR_ABSTRACT)));



        } else if (TAG_CATEGORY.equalsIgnoreCase(qName)) {

            category = new Category(attr.getValue(ATTR_NAME));

            // Set all other attributes
            category.setDescription(attr.getValue(ATTR_DESCRIPTION));

            // Add to stack and tree
            Category parentCategory = categoryStack.peek();
            parentCategory.add(category);
            categoryStack.add(category);



        } else if (TAG_TABLE3D.equalsIgnoreCase(qName)) {

            //
            // Look for table in table set
            //
            try {
                table = (Table3DMetadata)tables.get(attr.getValue(ATTR_NAME));
            } catch (NameableNotFoundException ex) {
                table = new Table3DMetadata(attr.getValue(ATTR_NAME));
            }

            // Set all other attributes
            if (attr.getIndex(ATTR_USER_LEVEL) > -1)
                table.setUserLevel(parseInt(attr.getValue(ATTR_USER_LEVEL)));
            if (attr.getIndex(ATTR_ADDRESS) > -1)
                table.setAddress(hexToInt(attr.getValue(ATTR_ADDRESS)));

            // Store axis addresses
            if (attr.getIndex(ATTR_X_ADDRESS) > -1)
                ((Table3DMetadata) table).getXaxis().setAddress(hexToInt(attr.getValue(ATTR_X_ADDRESS)));
            if (attr.getIndex(ATTR_Y_ADDRESS) > -1)
                ((Table3DMetadata) table).getYaxis().setAddress(hexToInt(attr.getValue(ATTR_Y_ADDRESS)));

            // Add scale
            try {
                if (attr.getIndex(ATTR_SCALE) > -1)
                    table.setScale((Scale)scales.get(attr.getValue(ATTR_SCALE)));
            } catch (NameableNotFoundException ex) {
                // TODO: Handle exception
            }



        } else if (TAG_TABLE2D.equalsIgnoreCase(qName)) {

            //
            // Look for table in table set
            //
            try {
                table = (Table2DMetadata)tables.get(attr.getValue(ATTR_NAME));
            } catch (NameableNotFoundException ex) {
                table = new Table2DMetadata(attr.getValue(ATTR_NAME));
            }

            // Set all other attributes
            if (attr.getIndex(ATTR_USER_LEVEL) > -1)
                table.setUserLevel(parseInt(attr.getValue(ATTR_USER_LEVEL)));
            if (attr.getIndex(ATTR_ADDRESS) > -1)
                table.setAddress(hexToInt(attr.getValue(ATTR_ADDRESS)));
            if (attr.getIndex(ATTR_SIZE) > -1)
                ((Table2DMetadata)table).setSize(parseInt(attr.getValue(ATTR_SIZE)));

            // Store axis addresses
            if (attr.getIndex(ATTR_AXIS_ADDRESS) > -1)
                ((Table2DMetadata) table).getAxis().setAddress(hexToInt(attr.getValue(ATTR_AXIS_ADDRESS)));

            // Add scale
            try {
                if (attr.getIndex(ATTR_SCALE) > -1)
                    table.setScale((Scale)scales.get(attr.getValue(ATTR_SCALE)));
            } catch (NameableNotFoundException ex) {
                // TODO: Handle exception
            }



        } else if (TAG_PARAMETER.equalsIgnoreCase(qName)) {

            //
            // Look for table in table set
            //
            try {
                table = (ParameterMetadata)tables.get(attr.getValue(ATTR_NAME));
            } catch (NameableNotFoundException ex) {
                table = new ParameterMetadata(attr.getValue(ATTR_NAME));
            }

            // Set all other attributes
            if (attr.getIndex(ATTR_USER_LEVEL) > -1)
                table.setUserLevel(parseInt(attr.getValue(ATTR_USER_LEVEL)));
            if (attr.getIndex(ATTR_ADDRESS) > -1)
                table.setAddress(hexToInt(attr.getValue(ATTR_ADDRESS)));

            // Add scale
            try {
                if (attr.getIndex(ATTR_SCALE) > -1)
                    table.setScale((Scale)scales.get(attr.getValue(ATTR_SCALE)));
            } catch (NameableNotFoundException ex) {
                // TODO: Handle exception
            }



        } else if (TAG_SWITCH.equalsIgnoreCase(qName)) {

            //
            // Look for switch in switch group, then in parsed tables
            //
            boolean found = false;
            if (switchGroup != null) {
                try {
                    table = (SwitchMetadata)switchGroup.get(attr.getValue(ATTR_NAME));
                    found = true;
                } catch (NameableNotFoundException ex) {
                    table = new SwitchMetadata(attr.getValue(ATTR_NAME));
                }

            } else {

                try {
                    table = (SwitchMetadata)tables.get(attr.getValue(ATTR_NAME));
                } catch (NameableNotFoundException ex) {
                    table = new SwitchMetadata(attr.getValue(ATTR_NAME));
                }
            }

            // Set all other attributes
            if (attr.getIndex(ATTR_USER_LEVEL) > -1)
                table.setUserLevel(parseInt(attr.getValue(ATTR_USER_LEVEL)));
            if (attr.getIndex(ATTR_ADDRESS) > -1)
                table.setAddress(hexToInt(attr.getValue(ATTR_ADDRESS)));
            if (attr.getIndex(ATTR_SIZE) > -1)
                ((SwitchMetadata)table).setSize(parseInt(attr.getValue(ATTR_SIZE)));

            // Add scale
            try {
                if (attr.getIndex(ATTR_SCALE) > -1)
                    table.setScale((Scale)scales.get(attr.getValue(ATTR_SCALE)));
            } catch (NameableNotFoundException ex) {
                // TODO: Handle exception
            }



        } else if (TAG_SWITCHGROUP.equalsIgnoreCase(qName)) {

            //
            // Look for table in table set
            //
            try {
                switchGroup = (SwitchGroupMetadata)tables.get(attr.getValue(ATTR_NAME));
            } catch (NameableNotFoundException ex) {
                switchGroup = new SwitchGroupMetadata(attr.getValue(ATTR_NAME));
            }

            // Set all other attributes
            System.out.println(attr.getValue(ATTR_USER_LEVEL));
            if (attr.getIndex(ATTR_USER_LEVEL) > -1)
                switchGroup.setUserLevel(parseInt(attr.getValue(ATTR_USER_LEVEL)));



        } else if (TAG_SCALE.equalsIgnoreCase(qName)) {

            //
            // Look for scale in scale set
            //
            try {
                scale = (Scale)tables.get(attr.getValue(ATTR_NAME));
            } catch (NameableNotFoundException ex) {
                scale = new Scale(attr.getValue(ATTR_NAME));
            }

            // Set all other attributes
            if (attr.getIndex(ATTR_ENDIAN) > -1)
                scale.setEndian(parseEndian(attr.getValue(ATTR_ENDIAN)));
            if (attr.getIndex(ATTR_STORAGE_TYPE) > -1)
                scale.setStorageType(parseStorageType(attr.getValue(ATTR_STORAGE_TYPE)));
            if (attr.getIndex(ATTR_LOG_PARAM) > -1)
                scale.setLogParam(attr.getValue(ATTR_LOG_PARAM));



        } else if (TAG_X_AXIS.equalsIgnoreCase(qName)) {

            try {
                axis = ((Table3DMetadata)table).getXaxis();
                axis.getName();
            } catch (NullPointerException ex) {
                axis = new AxisMetadata(attr.getValue(ATTR_NAME));
            }

            // Set all other attributes
            if (attr.getIndex(ATTR_SIZE) > -1)
                axis.setSize(parseInt(attr.getValue(ATTR_SIZE)));
            if (attr.getIndex(ATTR_ADDRESS) > -1)
                axis.setAddress(hexToInt(attr.getValue(ATTR_ADDRESS)));

            // Add scale
            try {
                if (attr.getIndex(ATTR_SCALE) > -1)
                    axis.setScale((Scale)scales.get(attr.getValue(ATTR_SCALE)));
            } catch (NameableNotFoundException ex) {
                // TODO: Handle exception
            }



        } else if (TAG_Y_AXIS.equalsIgnoreCase(qName)) {

            try {
                axis = ((Table3DMetadata)table).getYaxis();
                axis.getName();
            } catch (NullPointerException ex) {
                axis = new AxisMetadata(attr.getValue(ATTR_NAME));
            }

            // Set all other attributes
            if (attr.getIndex(ATTR_SIZE) > -1)
                axis.setSize(parseInt(attr.getValue(ATTR_SIZE)));
            if (attr.getIndex(ATTR_ADDRESS) > -1)
                axis.setAddress(hexToInt(attr.getValue(ATTR_ADDRESS)));

            // Add scale
            try {
                if (attr.getIndex(ATTR_SCALE) > -1)
                    axis.setScale((Scale)scales.get(attr.getValue(ATTR_SCALE)));
            } catch (NameableNotFoundException ex) {
                // TODO: Handle exception
            }



        } else if (TAG_AXIS.equalsIgnoreCase(qName)) {

            try {
                axis = ((Table2DMetadata)table).getAxis();
                axis.getName();
            } catch (NullPointerException ex) {
                axis = new AxisMetadata(attr.getValue(ATTR_NAME));
            }

            // Set all other attributes
            if (attr.getIndex(ATTR_SIZE) > -1)
                axis.setSize(parseInt(attr.getValue(ATTR_SIZE)));
            if (attr.getIndex(ATTR_ADDRESS) > -1)
                axis.setAddress(hexToInt(attr.getValue(ATTR_ADDRESS)));

            // Add scale
            try {
                if (attr.getIndex(ATTR_SCALE) > -1)
                    axis.setScale((Scale)scales.get(attr.getValue(ATTR_SCALE)));
            } catch (NameableNotFoundException ex) {
                // TODO: Handle exception
            }



        } else if (TAG_UNIT.equalsIgnoreCase(qName)) {
            unit = new Unit(attr.getValue(ATTR_NAME));

            // Set all other attributes
            if (attr.getIndex(ATTR_SYSTEM) > -1)
                unit.setSystem(parseUnitSystem(attr.getValue(ATTR_SYSTEM)));
            if (attr.getIndex(ATTR_TO_BYTE) > -1)
                unit.setTo_byte(attr.getValue(ATTR_TO_BYTE));
            if (attr.getIndex(ATTR_TO_REAL) > -1)
                unit.setTo_real(attr.getValue(ATTR_TO_REAL));
            if (attr.getIndex(ATTR_FORMAT) > -1)
                unit.setFormat(attr.getValue(ATTR_FORMAT));
            if (attr.getIndex(ATTR_COARSE_INCREMENT) > -1)
                unit.setCoarseIncrement(parseFloat(attr.getValue(ATTR_COARSE_INCREMENT)));
            if (attr.getIndex(ATTR_FINE_INCREMENT) > -1)
                unit.setFineIncrement(parseFloat(attr.getValue(ATTR_FINE_INCREMENT)));



        } else if (TAG_STATE.equalsIgnoreCase(qName)) {

            // Determine state and values and apply to table
            if (attr.getValue(ATTR_NAME).equalsIgnoreCase(VAL_ON)) {
                ((SwitchMetadata)table).setStateOn(attr.getValue("values"));

            } else if (attr.getValue(ATTR_NAME).equalsIgnoreCase(VAL_OFF)) {
                ((SwitchMetadata)table).setStateOff(attr.getValue("values"));

            }

        }

        // Build PCDATA
        charBuffer = new StringBuilder();

    }


    public void endElement(String uri, String localName, String qName) {

        if (TAG_SCALE.equalsIgnoreCase(qName)) {
            scales.add(scale);
            scale = null;



        } else if (TAG_UNIT.equalsIgnoreCase(qName)) {
            units.add(unit);
            unit = null;



        } else if (TAG_CATEGORY.equalsIgnoreCase(qName)) {

            // Remove current category from stack
            categoryStack.pop();



        } else if (TAG_TABLE3D.equalsIgnoreCase(qName) ||
                   TAG_TABLE2D.equalsIgnoreCase(qName) ||
                   TAG_PARAMETER.equalsIgnoreCase(qName)) {

            tables.add(table);
            if (!inheriting) category.addTable(table);
            table = null;



        } else if (TAG_AXIS.equalsIgnoreCase(qName)) {
            ((Table2DMetadata)table).setAxis(axis);
            axis = null;



        } else if (TAG_X_AXIS.equalsIgnoreCase(qName)) {
            ((Table3DMetadata)table).setXaxis(axis);
            axis = null;



        } else if (TAG_Y_AXIS.equalsIgnoreCase(qName)) {
            ((Table3DMetadata)table).setYaxis(axis);
            axis = null;



        } else if (TAG_DESCRIPTION.equalsIgnoreCase(qName)) {

            if (table == null && switchGroup != null) switchGroup.setDescription(charBuffer.toString());
            else table.setDescription(charBuffer.toString());



        } else if (TAG_ROM.equalsIgnoreCase(qName)) {

            rom.setTables(tables);
            rom.setScales(scales);
            rom.setCategories(categories);
            roms.add(rom);

            // Clear all temp variables
            tables = new NamedSet<TableMetadata>();
            scales = new NamedSet<Scale>();
            units = new NamedSet<Unit>();



        } else if (TAG_DATA.equalsIgnoreCase(qName)) {

            // Set static axis values
            if (axis instanceof SourceDefAxisMetadata) {
                ((SourceDefAxisMetadata)axis).setValues(charBuffer+"", " ");
            }



        } else if (TAG_SWITCHGROUP.equalsIgnoreCase(qName)) {

            tables.add(switchGroup);
            if (!inheriting) category.addTable(switchGroup);
            switchGroup = null;



        }  else if (TAG_SWITCH.equalsIgnoreCase(qName)) {

            // Add switch to list of tables OR to switchgroup
            if (switchGroup == null) {
                tables.add(table);
                if (!inheriting) category.addTable(table);

            } else {
                switchGroup.add((SwitchMetadata)table);
            }

            table = null;
        }
    }

    public void characters(char[] ch, int start, int length) {
        charBuffer.append(ch, start, length);
    }


    public RomMetadata getRom() {
        return rom;
    }


    private RomMetadata getParentRom(IndexItem parent) throws Exception {

            InputStream is = new BufferedInputStream(new FileInputStream(parent.getFile()));
            RomDefinitionHandler handler = new RomDefinitionHandler(index);
            SaxParserFactory.getSaxParser().parse(is, handler);
            return handler.getRom();

    }

}