package enginuity.newmaps.definition.translate;

import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;
import enginuity.maps.Rom;
import enginuity.maps.RomID;
import enginuity.maps.Scale;
import enginuity.maps.Table;
import enginuity.maps.Table2D;
import enginuity.maps.Table3D;
import enginuity.util.HexUtil;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Iterator;
import java.util.Vector;
import javax.imageio.metadata.IIOMetadataNode;

public class DefinitionBuilder {
    
    Vector<Rom> roms;        
    Vector<Scale> scales = new Vector<Scale>();
    Rom parentRom = null;
    
    public DefinitionBuilder(Vector<Rom> roms, File folder) {
        this.roms = roms;
        OutputFormat of = new OutputFormat("XML", "ISO-8859-1", true);
        of.setIndent(1);
        of.setLineWidth(100);
        of.setIndenting(true);
        
        //
        // Iterate through ROMs for processing
        //
        Iterator it = roms.iterator();
        while (it.hasNext()) {
            Rom rom = (Rom)it.next();
            Vector<Table> tables = rom.getTables();
            
            // Build rom info
            IIOMetadataNode node = buildRom(rom);
            
            //
            // Iterate and build tables
            //
            Iterator tableit = tables.iterator();
            IIOMetadataNode tablesNode = new IIOMetadataNode("tables");
            
            while (tableit.hasNext()) {
                Table table = (Table)tableit.next();
                try {
                    tablesNode.appendChild(buildTable(table));
                } catch (Exception ex) {
                    // Table address not defined)
                }
            }
            
            
            //
            // Append tables scales to rom node
            //
            if (rom.isAbstract()){ 
                IIOMetadataNode scalesNode = new IIOMetadataNode("scales");
                Iterator scalesit = scales.iterator();
                while (scalesit.hasNext()) {
                    scalesNode.appendChild(buildScale((Scale)scalesit.next()));
                }

                if (scalesNode.getChildNodes().getLength() > 0) node.appendChild(scalesNode);
            }
                        
            // add tables
            node.appendChild(tablesNode);
            
            //
            // Output to file
            //
            try {
                FileOutputStream fos = new FileOutputStream(new File(folder.getAbsolutePath() + "/" + rom.getRomIDString() + ".xml"));
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
    
    private IIOMetadataNode buildRom(Rom rom) {
        IIOMetadataNode node = new IIOMetadataNode("rom");

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
        
            System.out.println("Building " +  rom.getRomID().getXmlid() + " ...");
                               
            
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

        parentRom = null;
        
        return node;
        
    }
    
    
    private IIOMetadataNode buildTable(Table table) throws Exception {
        
        if (table.getStorageAddress() < 1 && !parentRom.isAbstract()) throw new Exception();
        
        IIOMetadataNode node = new IIOMetadataNode("table");
        Table parentTable = null;
        
        // Get parent table if it exists
        if (parentRom != null) {
            Iterator it = parentRom.getTables().iterator();
            while (it.hasNext()) {
                Table tempTable = (Table)it.next();
                if (tempTable.getName().equalsIgnoreCase(table.getName())) parentTable = tempTable;
            }
        }
        
        //
        // Do table type specific attributes
        //
        if (table.getType() == Table.TABLE_3D) {
            node = new IIOMetadataNode("table3d");
            
            //
            // Do axes
            //
            if (table.getRom().isAbstract()) {
                node.appendChild(buildAxis(((Table3D)table).getXAxis()));
                node.appendChild(buildAxis(((Table3D)table).getYAxis()));
            } else {
                // add addresses to table
            }
            
        } else if (table.getType() == Table.TABLE_2D) {
            node = new IIOMetadataNode("table2d");
            
            //
            // Do axes
            //
            if (table.getRom().isAbstract()) {
                node.appendChild(buildAxis(((Table2D)table).getAxis()));
            } else {
                // add addresses to table
            }            
            
        } else if (table.getType() == Table.TABLE_1D) {
            node = new IIOMetadataNode("parameter");
            
        } else if (table.getType() == Table.TABLE_SWITCH) {
            node = new IIOMetadataNode("switch");
            
        }
        
        //
        // Do attributes common to all table types
        //        
        node.setAttribute("name", table.getName());    
       
        if (table.getRom().isAbstract())
            node.setAttribute("userlevel", table.getUserLevel()+"");
        if (table.getStorageAddress() > 0 && (parentTable == null || parentTable.getStorageAddress() != table.getStorageAddress() && table.getStorageAddress() > 0)) 
            node.setAttribute("address", HexUtil.intToHexString(table.getStorageAddress()));
                
        try {
            table.getScale().setName(table.getScale().getUnit());
        
            // add scale to list if it doesnt already exist
            Iterator scalesit = scales.iterator();
            boolean addScale = true;
            while (scalesit.hasNext()) {
                Scale tempScale = (Scale)scalesit.next();
                try {
                    
                    if (table.getRom().isAbstract()) node.setAttribute("scale", table.getScale().getUnit());
                    
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
        
        return node;
    }
    
    
    private IIOMetadataNode buildAxis(Table axis) {
        IIOMetadataNode node = new IIOMetadataNode("axis");
        Table parentAxis = null;
        try {
            if (axis.getRom() != null && axis.getRom().getTable(axis.getAxisParent().getName()) != null)
                parentAxis = axis.getRom().getTable(axis.getAxisParent().getName());
        } catch (Exception ex) { }
        
        boolean noOverride = true;
        
        if (axis.getType() == Table.TABLE_X_AXIS) 
            node = new IIOMetadataNode("xaxis");           
        else if (axis.getType() == Table.TABLE_Y_AXIS && axis.getAxisParent().getType() == Table.TABLE_3D) 
            node = new IIOMetadataNode("yaxis");
           
        node.setAttribute("name", axis.getName()); 
        
        if (axis.isStatic() && (parentAxis == null || axis.isStatic() != parentAxis.isStatic()))
            node.setAttribute("static", "true");
        if (axis.getDescription().length() > 0 && (parentAxis == null || !axis.getDescription().equalsIgnoreCase(parentAxis.getDescription())))
            node.setAttribute("description", axis.getDescription());    
        if (parentAxis == null || axis.getDataSize() != parentAxis.getDataSize())
            node.setAttribute("size", axis.getDataSize()+"");
        if (parentAxis == null || axis.getStorageAddress() != parentAxis.getStorageAddress()) {
            node.setAttribute("storageaddress", HexUtil.intToHexString(axis.getStorageAddress()));
        }
        
        try {
            axis.getScale().setName(axis.getScale().getUnit());
        
            // add scale to list if it doesnt already exist
            Iterator scalesit = scales.iterator();
            boolean addScale = true;
            while (scalesit.hasNext()) {
                Scale tempScale = (Scale)scalesit.next();
                try {
                    
                    if (axis.getRom().isAbstract()) node.setAttribute("scale", axis.getScale().getUnit());
                    
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
        
        return node;
    }
    
    private IIOMetadataNode buildStaticData(Table axis) {
        IIOMetadataNode node = new IIOMetadataNode("data");        
        StringBuffer sb = new StringBuffer();
        
        for (int i = 0; i < axis.getData().length; i++) {
            sb.append(axis.getData()[i].getValue()+"");
            if (i < axis.getData().length - 1) sb.append("|");
        }
        
        node.setNodeValue(sb+"");
        
        return node;
    }
        
    
    private IIOMetadataNode buildScale(Scale scale) {       
        
        IIOMetadataNode node = new IIOMetadataNode("scale");
        node.setAttribute("name", scale.getName());  
        node.setAttribute("storagetype", parseStorage(scale.getTable().getStorageType()));
        node.setAttribute("endian", parseEndian(scale.getTable().getEndian()));
        
        IIOMetadataNode unitNode = new IIOMetadataNode("unit");
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
