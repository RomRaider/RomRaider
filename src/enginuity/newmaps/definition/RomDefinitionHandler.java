package enginuity.newmaps.definition;

import enginuity.newmaps.Rom;
import enginuity.newmaps.ecudata.ECUData;
import enginuity.newmaps.ecudata.Parameter;
import enginuity.newmaps.ecudata.Scale;
import enginuity.newmaps.ecudata.Switch;
import enginuity.newmaps.ecudata.Table2D;
import enginuity.newmaps.ecudata.Table3D;
import enginuity.newmaps.ecudata.Unit;
import enginuity.util.NamedSet;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;
import static java.lang.Boolean.parseBoolean;
import static java.lang.Float.parseFloat;
import static enginuity.util.HexUtil.hexToInt;
import static java.lang.Integer.parseInt;
import static enginuity.newmaps.definition.AttributeParser.*;
import enginuity.newmaps.ecudata.Axis;
import enginuity.newmaps.ecudata.Category;
import enginuity.util.exception.NameableNotFoundException;
import java.util.Stack;

public class RomDefinitionHandler extends DefaultHandler {

    private static final String TAG_ROM = "rom";
    private static final String TAG_PARAMETERS = "parameters";
    private static final String TAG_CATEGORY = "category";
    private static final String TAG_TABLE3D = "table3d";
    private static final String TAG_TABLE2D = "table2d";
    private static final String TAG_PARAMETER = "parameter";
    private static final String TAG_SWITCH = "switch";
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
    private static final String ATTR_FORMAT = "format";
    private static final String ATTR_COARSE_INCREMENT = "coarseincrement";
    private static final String ATTR_FINE_INCREMENT = "fineincrement";  
     
    private RomTreeBuilder roms;
    
    private Rom rom;
    private Category category;
    private ECUData table;
    private Scale scale;
    private Axis axis;
    private Unit unit;
    private String dataValues;
    private Stack<Category> categoryStack;
    private Category categories;
    private NamedSet<ECUData> tables;
    private NamedSet<Scale> scales;
    private NamedSet<Unit> units;
    
    
    public RomDefinitionHandler(RomTreeBuilder roms) {
        this.roms = roms;
        
        // These lines may cause some problems down the line.. I can't think through it right now
        categoryStack = new Stack<Category>();
        category = new Category("Root");
        categoryStack.add(category);         
    }
    
    public void startElement(String uri, String localName, String qName, Attributes attributes) {
        
        if (TAG_ROM.equalsIgnoreCase(qName)) {  
            
            try {
                //
                // If "base" attribute is set, find base rom in collection
                //
                String name = attributes.getValue(ATTR_NAME);
                if (attributes.getValue(ATTR_BASE).length() > 0) {
                    rom = (Rom)roms.get(attributes.getValue(ATTR_NAME));    
                } else {                
                    rom = new Rom(name);
                }

                // Set all other attributes
                rom.setName(attributes.getValue(ATTR_NAME));
                rom.setIdAddress(hexToInt(attributes.getValue(ATTR_ID_ADDRESS)));
                rom.setIdString(attributes.getValue(ATTR_ID_STRING));
                rom.setDescription(attributes.getValue(ATTR_DESCRIPTION));
                rom.setMemmodel(attributes.getValue(ATTR_MEMMODEL));
                rom.setFlashmethod(attributes.getValue(ATTR_FLASH_METHOD));
                rom.setCaseid(attributes.getValue(ATTR_CASE_ID));
                rom.setObsolete(parseBoolean(attributes.getValue(ATTR_OBSOLETE)));            
                rom.setAbstract(parseBoolean(attributes.getValue(ATTR_ABSTRACT)));
                
            } catch (NameableNotFoundException ex) {
                // uhh.. do something
            }
            
                        
        } else if (TAG_CATEGORY.equalsIgnoreCase(qName)) {
            
            category = new Category(attributes.getValue(ATTR_NAME));
            
            // Set all other attributes
            category.setDescription(attributes.getValue(ATTR_DESCRIPTION));
            
        } else if (TAG_TABLE3D.equalsIgnoreCase(qName)) {
            
            //
            // Look for table in table set
            //
            Table3D table;
            try {
                table = (Table3D)tables.get(attributes.getValue(ATTR_NAME));
            } catch (NameableNotFoundException ex) {
                table = new Table3D(attributes.getValue(ATTR_NAME));
            }
            
            // Set all other attributes
            table.setUserLevel(parseInt(attributes.getValue(ATTR_USER_LEVEL)));            
            table.setAddress(hexToInt(attributes.getValue(ATTR_NAME)));
            
            // TODO: Deal with scale
            //table(attributes.getValue(ATTR_NAME));
            
            // TODO: Deal with axis addresses
            
            
        } else if (TAG_TABLE2D.equalsIgnoreCase(qName)) {
            
            //
            // Look for table in table set
            //
            Table2D table;
            try {
                table = (Table2D)tables.get(attributes.getValue(ATTR_NAME));
            } catch (NameableNotFoundException ex) {
                table = new Table2D(attributes.getValue(ATTR_NAME));
            }            
            
            // Set all other attributes
            table.setUserLevel(parseInt(attributes.getValue(ATTR_USER_LEVEL)));            
            table.setAddress(hexToInt(attributes.getValue(ATTR_NAME)));
            table.setSize(parseInt(attributes.getValue(ATTR_SIZE)));   
            
            // TODO: Deal with scale
            //table(attributes.getValue(ATTR_NAME));
            
            // TODO: Deal with axis address
            
            
        } else if (TAG_PARAMETER.equalsIgnoreCase(qName)) {
            
            //
            // Look for table in table set
            //
            Parameter table;
            try {
                table = (Parameter)tables.get(attributes.getValue(ATTR_NAME));
            } catch (NameableNotFoundException ex) {
                table = new Parameter(attributes.getValue(ATTR_NAME));
            }            
            
            // Set all other attributes
            table.setUserLevel(parseInt(attributes.getValue(ATTR_USER_LEVEL)));            
            table.setAddress(hexToInt(attributes.getValue(ATTR_NAME)));
            
            // TODO: Deal with scale      
            
            
        } else if (TAG_SWITCH.equalsIgnoreCase(qName)) {
            
            //
            // Look for table in table set
            //
            Switch table;
            try {
                table = (Switch)tables.get(attributes.getValue(ATTR_NAME));
            } catch (NameableNotFoundException ex) {
                table = new Switch(attributes.getValue(ATTR_NAME));
            }            
            
            // Set all other attributes
            table.setUserLevel(parseInt(attributes.getValue(ATTR_USER_LEVEL)));            
            table.setAddress(hexToInt(attributes.getValue(ATTR_NAME)));
            table.setSize(parseInt(attributes.getValue(ATTR_SIZE)));   
            
            // TODO: Deal with scale
            
            
        } else if (TAG_SCALE.equalsIgnoreCase(qName)) {
                    
            //
            // Look for scale in scale set
            //
            Scale scale;
            try {
                scale = (Scale)tables.get(attributes.getValue(ATTR_NAME));
            } catch (NameableNotFoundException ex) {
                scale = new Scale(attributes.getValue(ATTR_NAME));
            }  
            
            // Set all other attributes
            scale.setEndian(parseEndian(attributes.getValue(ATTR_ENDIAN)));   
            scale.setStorageType(parseStorageType(attributes.getValue(ATTR_STORAGE_TYPE)));
            scale.setLogParam(attributes.getValue(ATTR_LOG_PARAM));
                        
        } else if (TAG_AXIS.equalsIgnoreCase(qName) ||
                   TAG_X_AXIS.equalsIgnoreCase(qName) ||
                   TAG_Y_AXIS.equalsIgnoreCase(qName)) {
            Axis axis = new Axis(attributes.getValue(ATTR_NAME));
            
            // Set all other attributes
            axis.setSize(parseInt(attributes.getValue(ATTR_SIZE)));
            axis.setAddress(hexToInt(attributes.getValue(ATTR_ADDRESS)));
            
            // TODO: Deal with scales
            
                        
        } else if (TAG_DATA.equalsIgnoreCase(qName)) {
            
            // TODO: Deal with data
            
            
        } else if (TAG_UNIT.equalsIgnoreCase(qName)) {
            Unit unit = new Unit(attributes.getValue(ATTR_NAME));
            
            // Set all other attributes
            unit.setSystem(parseUnitSystem(attributes.getValue(ATTR_SYSTEM)));      
            unit.setTo_byte(attributes.getValue(ATTR_TO_BYTE));
            unit.setTo_real(attributes.getValue(ATTR_TO_REAL));
            unit.setFormat(attributes.getValue(ATTR_FORMAT));
            unit.setCoarseIncrement(parseFloat(attributes.getValue(ATTR_COARSE_INCREMENT)));
            unit.setFineIncrement(parseFloat(attributes.getValue(ATTR_FINE_INCREMENT)));
            
            
        } else if (TAG_STATE.equalsIgnoreCase(qName)) {
            
        }
        
    }

    public void endElement(String uri, String localName, String qName) {
        
        if (TAG_SCALE.equalsIgnoreCase(qName)) {
            scales.add(scale);
                    
            // Clear object for next element
            scale = null;
            
            
        } else if (TAG_UNIT.equalsIgnoreCase(qName)) {
            units.add(unit);
                    
            // Clear object for next element
            unit = null;
            

        } else if (TAG_CATEGORY.equalsIgnoreCase(qName)) {
            
            // Ugh.. I think I'm at least along the right lines
            categoryStack.pop().add(category);
            // But I know it's not quite right. Very confusing.
            
            
        } else if (TAG_TABLE3D.equalsIgnoreCase(qName) ||
                   TAG_TABLE2D.equalsIgnoreCase(qName) ||
                   TAG_PARAMETER.equalsIgnoreCase(qName) ||
                   TAG_SWITCH.equalsIgnoreCase(qName)) {
            
            tables.add(table);
            
            // Clear object for next element
            table = null;
            
            
        } else if (TAG_AXIS.equalsIgnoreCase(qName)) {
            ((Table2D)table).setAxis(axis);
                    
            // Clear object for next element
            axis = null;
            
            
        } else if (TAG_X_AXIS.equalsIgnoreCase(qName)) {
            ((Table3D)table).setXaxis(axis);
                    
            // Clear object for next element
            axis = null;
            
            
        } else if (TAG_Y_AXIS.equalsIgnoreCase(qName)) {
            ((Table3D)table).setYaxis(axis);
                    
            // Clear object for next element
            axis = null;
            
            
        } else if (TAG_ROM.equalsIgnoreCase(qName)) {
            roms.add(rom);
            
            
        }
        
    }
    
}