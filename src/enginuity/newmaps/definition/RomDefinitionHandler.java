package enginuity.newmaps.definition;

import enginuity.newmaps.ecudata.*;
import enginuity.util.NamedSet;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;
import static java.lang.Boolean.parseBoolean;
import static java.lang.Float.parseFloat;
import static enginuity.util.HexUtil.hexToInt;
import static java.lang.Integer.parseInt;
import static enginuity.newmaps.definition.AttributeParser.*;
import enginuity.newmaps.xml.SaxParserFactory;
import enginuity.util.exception.NameableNotFoundException;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Iterator;
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
    private Category category; // Category currently being created
    private Stack<Category> categoryStack; // Stack used for higher levels in tree
    private Category categories; // Category tree that will be returned
    private ECUData table;
    private Scale scale;
    private Axis axis;
    private Unit unit;
    private String dataValues;
    private int xAddress;
    private int yAddress;
    private NamedSet<ECUData> tables = new NamedSet<ECUData>();
    private NamedSet<Scale> scales = new NamedSet<Scale>();
    private NamedSet<Unit> units = new NamedSet<Unit>();
    
    
    public RomDefinitionHandler(RomTreeBuilder roms) {
        this.roms = roms;
        
        // These lines may cause some problems down the line.. I can't think through it right now
        categoryStack = new Stack<Category>();
        categories = new Category("Root");
        categoryStack.add(categories);    
    }
    
    public void startElement(String uri, String localName, String qName, Attributes attr) {
        
        if (TAG_ROM.equalsIgnoreCase(qName)) {  
            
            try {
                //
                // If "base" attribute is set, find base rom in collection
                //
                String name = attr.getValue(ATTR_NAME);
                if (attr.getIndex(ATTR_BASE) > -1 &&
                    attr.getValue(ATTR_BASE).length() > 0) {
                    
                    rom = (Rom)roms.get(attr.getValue(ATTR_NAME));    
                } else {                
                    rom = new Rom(name);
                }

                // Set all other attributes
                rom.setName(attr.getValue(ATTR_NAME));
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
                
            } catch (NameableNotFoundException ex) {
                // uhh.. do something
            }
            
                        
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
                table = (Table3D)tables.get(attr.getValue(ATTR_NAME));
            } catch (NameableNotFoundException ex) {
                table = new Table3D(attr.getValue(ATTR_NAME));
            }
            
            // Set all other attributes
            if (attr.getIndex(ATTR_USER_LEVEL) > -1)
                table.setUserLevel(parseInt(attr.getValue(ATTR_USER_LEVEL)));            
            if (attr.getIndex(ATTR_ADDRESS) > -1)
                table.setAddress(hexToInt(attr.getValue(ATTR_ADDRESS)));
            
            // Store axis addresses
            if (attr.getIndex(ATTR_X_ADDRESS) > -1)
                xAddress = parseInt(attr.getValue(ATTR_X_ADDRESS));
            if (attr.getIndex(ATTR_Y_ADDRESS) > -1)
                yAddress = parseInt(attr.getValue(ATTR_Y_ADDRESS));
            
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
                table = (Table2D)tables.get(attr.getValue(ATTR_NAME));
            } catch (NameableNotFoundException ex) {
                table = new Table2D(attr.getValue(ATTR_NAME));
            }            
            
            // Set all other attributes
            if (attr.getIndex(ATTR_USER_LEVEL) > -1)
                table.setUserLevel(parseInt(attr.getValue(ATTR_USER_LEVEL)));            
            if (attr.getIndex(ATTR_ADDRESS) > -1)
                table.setAddress(hexToInt(attr.getValue(ATTR_ADDRESS)));
            if (attr.getIndex(ATTR_SIZE) > -1)
                ((Table2D)table).setSize(parseInt(attr.getValue(ATTR_SIZE)));   
            
            // Store axis addresses
            if (attr.getIndex(ATTR_AXIS_ADDRESS) > -1)
                yAddress = parseInt(attr.getValue(ATTR_AXIS_ADDRESS));
            
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
                table = (Parameter)tables.get(attr.getValue(ATTR_NAME));
            } catch (NameableNotFoundException ex) {
                table = new Parameter(attr.getValue(ATTR_NAME));
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
            // Look for table in table set
            //
            try {
                table = (Switch)tables.get(attr.getValue(ATTR_NAME));
            } catch (NameableNotFoundException ex) {
                table = new Switch(attr.getValue(ATTR_NAME));
            }            
            
            // Set all other attributes
            if (attr.getIndex(ATTR_USER_LEVEL) > -1)
                table.setUserLevel(parseInt(attr.getValue(ATTR_USER_LEVEL)));            
            if (attr.getIndex(ATTR_ADDRESS) > -1)
                table.setAddress(hexToInt(attr.getValue(ATTR_ADDRESS)));
            if (attr.getIndex(ATTR_SIZE) > -1)
                ((Switch)table).setSize(parseInt(attr.getValue(ATTR_SIZE)));   
            
            // Add scale
            try {
                if (attr.getIndex(ATTR_SCALE) > -1)
                    table.setScale((Scale)scales.get(attr.getValue(ATTR_SCALE)));
            } catch (NameableNotFoundException ex) {
                // TODO: Handle exception
            }
            
            
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
                        
        } else if (TAG_AXIS.equalsIgnoreCase(qName) ||
                   TAG_X_AXIS.equalsIgnoreCase(qName) ||
                   TAG_Y_AXIS.equalsIgnoreCase(qName)) {
            
            axis = new Axis(attr.getValue(ATTR_NAME));
            
            if (TAG_X_AXIS.equalsIgnoreCase(qName)) {
                if (xAddress > 0) axis.setAddress(xAddress);
            } else {
                 if (yAddress > 0) axis.setAddress(yAddress);
            }
            
            // Set all other attributes
            if (attr.getIndex(ATTR_SIZE) > -1)
                axis.setSize(parseInt(attr.getValue(ATTR_SIZE)));
            if (attr.getIndex(ATTR_ADDRESS) > -1)
                axis.setAddress(hexToInt(attr.getValue(ATTR_ADDRESS)));
            
                        
        } else if (TAG_DATA.equalsIgnoreCase(qName)) {
            
            // TODO: Deal with data
            
            
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
            
            // Remove current category from stack
            categoryStack.pop();    
            category = null;
            
            
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
            
            rom.setTables(tables);
            rom.setScales(scales);
            rom.setCategories(categories);
            
            roms.add(rom);
            
        }
        
    }
    
    public static void main(String[] args) {
        try {
            InputStream inputStream = new BufferedInputStream(new FileInputStream(new File("/ecu_defs/subaru/wrx/16BITBASE.xml")));
            try {
                RomTreeBuilder builder = new RomTreeBuilder();
                RomDefinitionHandler handler = new RomDefinitionHandler(builder);
                SaxParserFactory.getSaxParser().parse(inputStream, handler);
                
                System.out.println(builder.get(0));
                
            } finally {
                inputStream.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
}