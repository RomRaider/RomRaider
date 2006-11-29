package enginuity.newmaps.definition.translate;

import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;
import enginuity.maps.Rom;
import enginuity.maps.RomID;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Iterator;
import java.util.Vector;
import javax.imageio.metadata.IIOMetadataNode;

public class DefinitionBuilder {
    
    Vector<Rom> roms;    
    
    public DefinitionBuilder(Vector<Rom> roms, File folder) {
        this.roms = roms;
        OutputFormat of = new OutputFormat("XML", "ISO-8859-1", true);
        of.setIndent(1);
        of.setIndenting(true);
        
        //
        // Iterate through ROMs for processing
        //
        Iterator it = roms.iterator();
        while (it.hasNext()) {
            Rom rom = (Rom)it.next();
            IIOMetadataNode node = buildRom(rom);
            
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
                
            }
        }
    }
    
    private IIOMetadataNode buildRom(Rom rom) {
        IIOMetadataNode node = new IIOMetadataNode("rom");

        //
        // Set all attributes
        //

        // Start by getting base rom if it exists
        Rom parentRom = new Rom();
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
            node.setAttribute("idaddress", rom.getRomID().getInternalIdAddress()+"");
        
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

        return node;
        
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
