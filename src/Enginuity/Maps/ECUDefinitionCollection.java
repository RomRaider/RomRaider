package Enginuity.Maps;

import Enginuity.Exceptions.RomNotFoundException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Vector;
import javax.swing.JOptionPane;

public class ECUDefinitionCollection {
    
    Vector<Rom> roms = new Vector<Rom>();
    
    public ECUDefinitionCollection() { }
    
    public void addRom(Rom input) {
        roms.add(input);
    }
    
    public Rom getRom(String romid) throws RomNotFoundException { // find rom by romid from xml
        for (int i = 0; i < roms.size(); i++) {
            if (roms.get(i).getRomIDString().equalsIgnoreCase(romid)) {
                return roms.get(i);
            }
        }        
        //rom not found
        throw new RomNotFoundException();   
    }
    
    public Rom getRom(byte[] input) throws RomNotFoundException { // find rom by romid in data file
        // search for ECU version in roms vector
        for (int i = 0; i < roms.size(); i++) {
            String xmlID     = roms.get(i).getRomID().getXmlid();
            int    idAddress = roms.get(i).getRomID().getInternalIdAddress();
            String ecuID = new String(input, idAddress, xmlID.length());

            if (ecuID.equalsIgnoreCase(xmlID)) {
                return roms.get(i);
            }        
        }
        throw new RomNotFoundException(); 
    }
    
    public Rom parseRom(File input) throws IOException, RomNotFoundException {
        
        // load file
        Rom rom = new Rom();
        
        // find ECU version definition
        FileInputStream inputStream = new FileInputStream(input);
        byte[] binData = new byte[inputStream.available()];
        inputStream.read(binData);
        inputStream.close();

        rom = this.getRom(binData);

        // populate tables
        rom.populateTables(binData);

        return rom;
    }
    
    public String toString() {
        String output = "";
        for (int i = 0; i < roms.size(); i++) {
            output = output + "\n" + roms.get(i).toString();
        }
        return output;
    }
}