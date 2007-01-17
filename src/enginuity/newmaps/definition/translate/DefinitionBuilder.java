package enginuity.newmaps.definition.translate;

import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;
import enginuity.maps.Rom;
import enginuity.maps.RomID;
import enginuity.maps.Scale;
import enginuity.maps.Table;
import enginuity.maps.Table2D;
import enginuity.maps.Table3D;
import enginuity.maps.TableSwitch;
import enginuity.newmaps.xml.XmlHelper;
import enginuity.util.HexUtil;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Iterator;
import java.util.Vector;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

public class DefinitionBuilder {
        
    Vector<Rom> roms;        
    Vector<Scale> scales = new Vector<Scale>();
    Rom parentRom = null;
    Document doc;
    int num = 0;
    
    public void saveRom(Rom rom) {
        try {
            FileOutputStream fileOut = new FileOutputStream("/sizetest/" + rom.getRomIDString() + ".dat");
            ObjectOutputStream out = new ObjectOutputStream(fileOut);

            System.out.println("Writing Hashtable Object...");
            out.writeObject(rom);

            System.out.println("Closing all output streams...\n");
            out.close();
            fileOut.close();  
        } catch(FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } 
    }
    
    public DefinitionBuilder(Vector<Rom> roms, File folder) {
        this.roms = roms;
        OutputFormat of = new OutputFormat("XML", "ISO-8859-1", true);
        of.setIndent(1);
        of.setLineWidth(0);
        of.setIndenting(true);
        of.setDoctype(null, "ecu_defs.dtd");
        doc = XmlHelper.newDocument();
        
        //
        // Iterate through ROMs for processing
        //
        Iterator it = roms.iterator();
        while (it.hasNext()) {            
            
            Rom rom = (Rom)it.next();
            Vector<Table> tables = rom.getTables();
            
            //
            /* MEMORY USAGE TEST -- REMOVE LATER! */
            //
            saveRom(rom);
            
            // Build rom info
            parentRom = null;
            Element node = buildRom(rom);
            
            //
            // Iterate and build tables
            //
            Iterator tableit = tables.iterator();
            Element tablesNode = doc.createElement("tables");
            Vector<Element> cats = new Vector<Element>();
            
            while (tableit.hasNext()) {
                Table table = (Table)tableit.next();
                try {
                    //tablesNode.appendChild(buildTable(table));
                    
                    Element tableNode = buildTable(table);
                    
                    if (rom.isAbstract() && (table.getCategory() != null && table.getCategory().length() > 0)) {
                        //
                        // Look for existing category
                        //
                        boolean catExists = false;
                        Iterator catit = cats.iterator();
                        while (catit.hasNext()) {
                            Element cat = (Element)catit.next();
                            
                            if (cat.getAttribute("name").equalsIgnoreCase(table.getCategory())) {
                                cat.appendChild(tableNode);
                                catExists = true;
                                break;                                
                            }

                        }
                        
                        if (!catExists) {
                            // Category not found, add it and append table
                            Element catNode = doc.createElement("category");
                            cats.add(catNode);
                            catNode.setAttribute("name", table.getCategory());                            
                            tablesNode.appendChild(catNode);
                            catNode.appendChild(tableNode);
                        }
                        
                    } else {
                        //
                        // Table has no category, add to root node
                        //
                        tablesNode.appendChild(tableNode);
                        
                    }
                    
                    
                } catch (Exception ex) {
                    // Table address not defined or table is identical to parent
                    //ex.printStackTrace();
                }
            }
            
            
            //
            // Append tables scales to rom node
            //
            if (rom.isAbstract()){ 
                Element scalesNode = doc.createElement("scales");
                Iterator scalesit = scales.iterator();
                while (scalesit.hasNext()) {
                    scalesNode.appendChild(buildScale((Scale)scalesit.next()));
                }

                if (scalesNode.getChildNodes().getLength() > 0) node.appendChild(scalesNode);
            }
                        
            // add tables
            if (tablesNode.getChildNodes().getLength() > 0) {
                node.appendChild(tablesNode);
            }
            
            //
            // Output to file
            //
            try {
                StringBuffer path = new StringBuffer(folder.getAbsolutePath()+"/");
                path.append(rom.getRomID().getMake()+"/");             
                new File(path+"").mkdir();
                path.append(rom.getRomID().getMemModel()+"/");             
                new File(path+"").mkdir();
                
                path.append(rom.getRomIDString() + ".xml");
                
                FileOutputStream fos = new FileOutputStream(new File(path+""));
                XMLSerializer serializer = new XMLSerializer(fos, of);
                serializer.serialize(node);
                fos.flush();
                
            } catch (Exception ex) {
                ex.printStackTrace();
                
            } finally {
                scales = new Vector<Scale>();
            }
        }
    }
    
    private Element buildRom(Rom rom) {
        Element node = doc.createElement("rom");

        //
        // Set all attributes
        //

        // Start by getting base rom if it exists
        parentRom = new Rom();
        boolean child = false;
        try {
            parentRom = get(rom.getParent());
            child = true;
        } catch (Exception ex) {
            // Rom wasn't found, not a child            
        }
            System.out.println("Building " +  rom.getRomID().getXmlid() + " (#" + ++num + ") ...");
                               
            
        // Set all RomID data (now in Rom tag)
        node.setAttribute("name", rom.getRomID().getXmlid());

        if (rom.getParent().length() > 0 && (!child || !rom.getParent().equalsIgnoreCase(parentRom.getParent()))) 
            node.setAttribute("base", rom.getParent());                 
        if (buildDescription(rom.getRomID()).length() > 0 && (!child || !buildDescription(rom.getRomID()).equalsIgnoreCase(buildDescription(parentRom.getRomID())))) 
            node.setAttribute("description", buildDescription(rom.getRomID()));        
        if (rom.getRomID().getInternalIdAddress() > 0 && (!child || rom.getRomID().getInternalIdAddress() != parentRom.getRomID().getInternalIdAddress())) 
            node.setAttribute("idaddress", HexUtil.intToHexString(rom.getRomID().getInternalIdAddress()));
        if (rom.getRomID().getInternalIdString().length() > 0 && (!child || !rom.getRomID().getInternalIdString().equalsIgnoreCase(parentRom.getRomID().getInternalIdString()))) 
            node.setAttribute("idstring", rom.getRomID().getInternalIdString());        
        if (rom.getRomID().getMemModel().length() > 0 && (!child || !rom.getRomID().getMemModel().equalsIgnoreCase(parentRom.getRomID().getMemModel()))) 
            node.setAttribute("memmodel", rom.getRomID().getMemModel());        
        if (rom.getRomID().getFlashMethod().length() > 0 && (!child || !rom.getRomID().getFlashMethod().equalsIgnoreCase(parentRom.getRomID().getFlashMethod()))) 
            node.setAttribute("flashmethod", rom.getRomID().getFlashMethod());        
        if (rom.getRomID().getCaseId().length() > 0 && (!child || !rom.getRomID().getCaseId().equalsIgnoreCase(parentRom.getRomID().getCaseId()))) 
            node.setAttribute("caseid", rom.getRomID().getCaseId());        
        if (rom.getRomID().isObsolete() && (!child || rom.getRomID().isObsolete() != parentRom.getRomID().isObsolete())) 
            node.setAttribute("obsolete", rom.getRomID().isObsolete()+"");
        if (rom.isAbstract() && (!child || rom.isAbstract() != parentRom.isAbstract())) 
            node.setAttribute("abstract", rom.isAbstract()+"");
        
        return node;
        
    }
    
    
    private Element buildTable(Table table) throws Exception {
        
        boolean differentThanParent = false;
        
        if (table.getStorageAddress() == 0 && !table.getRom().isAbstract()) throw new Exception();
        
        Element node = doc.createElement("table");
        Table parentTable = null;
        
        // Get parent table if it exists
        if (parentRom != null) {
            Iterator it = parentRom.getTables().iterator();
            while (it.hasNext()) {
                Table tempTable = (Table)it.next();
                if (tempTable.getName().equalsIgnoreCase(table.getName())) parentTable = tempTable;
            }
        }
        
        
        if (table.getType() == Table.TABLE_3D) node = doc.createElement("table3d");        
        else if (table.getType() == Table.TABLE_2D) node = doc.createElement("table2d");            
        else if (table.getType() == Table.TABLE_1D) node = doc.createElement("parameter");            
        else if (table.getType() == Table.TABLE_SWITCH) node = doc.createElement("switch");            
        
        
        //
        // Do attributes common to all table types
        //    
        node.setAttribute("name", table.getName());    
       
        if (table.getRom().isAbstract() || table.getUserLevel() != parentTable.getUserLevel()) {
            node.setAttribute("userlevel", table.getUserLevel()+"");
            differentThanParent = true;
        }
        if (!table.getRom().isAbstract() && parentTable.getStorageAddress() != table.getStorageAddress()) {
            node.setAttribute("address", HexUtil.intToHexString(table.getStorageAddress()));
            differentThanParent = true;
        }
        if (table.getDescription().length() > 0 && (parentTable == null || !table.getDescription().equalsIgnoreCase(parentTable.getDescription()))) {            
            //node.appendChild(doc.createElement("description").appendChild(doc.createTextNode(table.getDescription())));
            Text textNode = doc.createTextNode(table.getDescription());
            Element desc = doc.createElement("description");
            desc.appendChild(textNode);
            node.appendChild(desc);
            
        }
                
        try {
            table.getScale().setName(table.getScale().getUnit());
        
            // add scale to list if it doesnt already exist
            Iterator scalesit = scales.iterator();
            boolean addScale = true;
            while (scalesit.hasNext()) {
                Scale tempScale = (Scale)scalesit.next();
                try {
                    
                    if (table.getRom().isAbstract() || !table.getScale().getName().equalsIgnoreCase(parentTable.getScale().getName())) {
                        node.setAttribute("scale", table.getScale().getUnit());
                        differentThanParent = true;
                    }
                    
                    if (table.getScale().getName().equalsIgnoreCase(tempScale.getName())) {
                        addScale = false;
                        break;
                    }
                } catch (Exception ex) {
                    // scale was found
                    addScale = false;
                }
            }
            if (addScale) {
                scales.add(table.getScale());                    
            }
            
        } catch (Exception ex) {
            // Table has no scale, ignore
        }        
        
        //
        // Do table type specific attributes
        //
        if (table.getType() == Table.TABLE_3D) {
            
            try {
                if (parentTable == null || ((Table3D)table).getSizeX() != ((Table3D)parentTable).getSizeX()) 
                    node.setAttribute("sizex", ((Table3D)table).getSizeX()+"");
                node.appendChild(buildAxis(((Table3D)table).getXAxis()));
                differentThanParent = true;
            } catch (Exception ex) {
                // X Axis has no non-address changes, so add address to table tag
                if (((Table3D)table).getXAxis().getStorageAddress() != ((Table3D)parentTable).getXAxis().getStorageAddress()) {
                    node.setAttribute("xaddress", HexUtil.intToHexString(((Table3D)table).getXAxis().getStorageAddress()));
                    differentThanParent = true;
                }
            }
            try {
                if (parentTable == null || ((Table3D)table).getSizeY() != ((Table3D)parentTable).getSizeY()) 
                    node.setAttribute("sizey", ((Table3D)table).getSizeY()+"");
                node.appendChild(buildAxis(((Table3D)table).getYAxis()));
                differentThanParent = true;
            } catch (Exception ex) {
                // y Axis has no non-address changes, so add address to table tag
                if (((Table3D)table).getYAxis().getStorageAddress() != ((Table3D)parentTable).getYAxis().getStorageAddress()) {
                    node.setAttribute("yaddress", HexUtil.intToHexString(((Table3D)table).getYAxis().getStorageAddress()));
                    differentThanParent = true;
                }
            }                    

            
        } else if (table.getType() == Table.TABLE_2D) {
            
            try {
                if (parentTable == null || table.getDataSize() != parentTable.getDataSize()) 
                    node.setAttribute("size", table.getDataSize()+"");
                node.appendChild(buildAxis(((Table2D)table).getAxis()));
                differentThanParent = true;
            } catch (Exception ex) {
                // y Axis has no non-address changes, so add address to table tag
                if (((Table2D)table).getAxis().getStorageAddress() != ((Table2D)parentTable).getAxis().getStorageAddress()) {
                    node.setAttribute("axisaddress", HexUtil.intToHexString(((Table2D)table).getAxis().getStorageAddress()));
                    differentThanParent = true;
                }
            }  
        } else if (table.getType() == Table.TABLE_SWITCH) {
            
            boolean update = false;
            
            if (parentTable == null) {
                update = true;
            } else {
                for (int i = 0; i < ((TableSwitch)table).getOnValues().length; i++) {
                    if (parentTable != null && (((TableSwitch)parentTable).getOnValues()[i] != ((TableSwitch)table).getOnValues()[i] ||
                            ((TableSwitch)parentTable).getOffValues()[i] != ((TableSwitch)table).getOffValues()[i])) {
                        update = true;
                        break;
                    }
                }
            }
         
            if (update) {
                node.appendChild(buildSwitchData((TableSwitch)table, "on"));
                node.appendChild(buildSwitchData((TableSwitch)table, "off"));
            }
            
        }
        
        if (!differentThanParent) throw new Exception();
        
        return node;
    }
    
    
    private Element buildAxis(Table axis) throws Exception {
        Element node = doc.createElement("axis");
        Table parentTable = null;
        Table parentAxis = null;
        boolean noOverride = true;
        
        try {
            if (axis.getAxisParent().getRom() != null && axis.getAxisParent().getRom().getTable(axis.getAxisParent().getName()) != null)
                parentTable = parentRom.getTable(axis.getAxisParent().getName());
        } catch (Exception ex) { /*ex.printStackTrace();*/ }
        
        
        if (axis.getType() == Table.TABLE_X_AXIS) {
            node = doc.createElement("xaxis");     
            if (parentTable != null) parentAxis = ((Table3D)parentTable).getXAxis();
            
        } else if (axis.getType() == Table.TABLE_Y_AXIS && axis.getAxisParent().getType() == Table.TABLE_3D) {
            node = doc.createElement("yaxis");
            if (parentTable != null) parentAxis = ((Table3D)parentTable).getYAxis();
            
        } else if (axis.getType() == Table.TABLE_Y_AXIS && axis.getAxisParent().getType() == Table.TABLE_2D) {
            node = doc.createElement("axis");
            if (parentTable != null) parentAxis = ((Table2D)parentTable).getAxis();
            
        }
           
        node.setAttribute("name", axis.getName()); 
        
        if (axis.getAxisParent().getRom().isAbstract() && axis.isStatic()) {
            node.setAttribute("static", "true");
            noOverride = false;
        }
        if (axis.getDescription().length() > 0 && (parentAxis == null || !axis.getDescription().equalsIgnoreCase(parentAxis.getDescription()))) {
            //desc.appendChild(doc.createTextNode(axis.getDescription()));
            //noOverride = false;
        }       
        if (axis.getStorageAddress() > 0 && (parentAxis == null || axis.getStorageAddress() != parentAxis.getStorageAddress())) 
            node.setAttribute("address", HexUtil.intToHexString(axis.getStorageAddress()));
        
        try {
            axis.getScale().setName(axis.getScale().getUnit());
        
            // add scale to list if it doesnt already exist
            Iterator scalesit = scales.iterator();
            boolean addScale = true;
            while (scalesit.hasNext()) {
                Scale tempScale = (Scale)scalesit.next();
                try {
                    
                    if (parentAxis == null || !axis.getScale().getUnit().equalsIgnoreCase(parentAxis.getScale().getUnit())) {
                        node.setAttribute("scale", axis.getScale().getUnit());
                        noOverride = false;
                    }
                    
                    if (axis.getScale().getName().equalsIgnoreCase(tempScale.getName())) {
                        addScale = false;
                        break;
                    }
                } catch (Exception ex) {
                    // scale was found
                    addScale = false;
                }
            }
            if (addScale) {
                scales.add(axis.getScale());                
            }
            
        } catch (Exception ex) {
            // Table has no scale, ignore
        }        
        
        if (axis.isStatic()) {
            // add static data
            node.appendChild(buildStaticData(axis));

        }
        
        if (noOverride) throw new Exception();
        
        return node;
    }
    
    private Element buildStaticData(Table axis) {
        Element node = doc.createElement("data");        
        StringBuffer sb = new StringBuffer();
        
        for (int i = 0; i < axis.getData().length; i++) {
            if (axis.getData()[i].getText() != null)
                sb.append(axis.getData()[i].getText()+"");
            if (i < axis.getData().length - 1) sb.append("|");
        }
         
        node.appendChild(doc.createElement("data").appendChild(doc.createTextNode(sb+"")));
        
        return node;
    }
    
    
    private Element buildSwitchData(TableSwitch table, String state) {
        Element node = doc.createElement(state);
        StringBuffer sb = new StringBuffer();
        
        byte[] values = null;
        
        if (state.equalsIgnoreCase("on")) values = table.getOnValues();
        else values = table.getOffValues();
        
        for (int i = 0; i < values.length; i++) {
            sb.append(values[i]+"");
            if (i < values.length - 1) sb.append("|");
        }
        
        node.setAttribute("data", sb+"");
        
        return node;
    }
        
    
    private Element buildScale(Scale scale) {       
        
        Element node = doc.createElement("scale");
        node.setAttribute("name", scale.getName());  
        node.setAttribute("storagetype", parseStorage(scale.getTable().getStorageType()));
        node.setAttribute("endian", parseEndian(scale.getTable().getEndian()));
        
        Element unitNode = doc.createElement("unit");
        unitNode.setAttribute("name", scale.getName());  
        unitNode.setAttribute("to_real", scale.getExpression());
        unitNode.setAttribute("to_byte", scale.getByteExpression());
        unitNode.setAttribute("format", scale.getFormat());
        unitNode.setAttribute("fineincrement", scale.getFineIncrement()+"");
        unitNode.setAttribute("coarseincrement", scale.getCoarseIncrement()+"");
        if (scale.getTable().getLogParam().length() > 0) unitNode.setAttribute("logparam", scale.getTable().getLogParam());
        
        node.appendChild(unitNode);
        
        return node;        
    }
    
    private String parseStorage(int type) {
        if (type == Table.STORAGE_TYPE_FLOAT) return "float";
        else if (type == 1) return "uint8";
        else return "uint16";
    }
    
    private String parseEndian(int type) {
        if (type == Table.ENDIAN_BIG) return "big";
        else return "little";
    }
    
            
    public String buildDescription(RomID id) {
        StringBuffer output = new StringBuffer();
        
        if (id.getYear().length() > 0) output.append(id.getYear() + " ");
        if (id.getMarket().length() > 0) output.append(id.getMarket() + " ");
        if (id.getMake().length() > 0) output.append(id.getMake() + " ");
        if (id.getModel().length() > 0) output.append(id.getModel() + " ");
        if (id.getSubModel().length() > 0) output.append(id.getSubModel() + " ");
        
        return new String(output).trim();
    }

    public Vector<Rom> getRoms() {
        return roms;
    }
    
    private Rom get(String name) throws Exception {
        Iterator it = roms.iterator();
        while (it.hasNext()) {
            Rom rom = (Rom)it.next();
            if (rom.getRomIDString().equalsIgnoreCase(name)) {
                return rom;
            }
        }
        throw new Exception();
    }
    
}
