package com.romraider.metadata;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Vector;
import org.xml.sax.SAXException;
import com.romraider.xml.DOMRomMetadataUnmarshaller;

public class RomHandler {
	
	public static RomMetadata getMetadata(File rom, RomMetadataIndex romIndex) throws IOException, RomNotFoundException, SAXException {

        RomMetadata rm = new RomMetadata();
        
		// Read rom file
        FileInputStream fis = new FileInputStream(rom);
        try {
        	byte[] buffer = new byte[(int)rom.length()];
            fis.read(buffer);	
            
            // Identify rom and find metadata
            RomID romid = identifyRom(buffer, romIndex);
            
            // Build array of rom metadata hierarchy starting with lowest level
            Vector<RomID> hierarchy = new Vector<RomID>();
            getIncludeHierarchy(romid, romIndex, hierarchy);
            
            // Pass hierarchy to unmarshaller one at a time
            for (RomID r : hierarchy) {
            	DOMRomMetadataUnmarshaller.unmarshallRomMetadata(r, rm);
            }
            
        } finally {
            fis.close();
        }
		
		return rm;
	}
	
	
	private static RomID[] getIncludeHierarchy(RomID base, RomMetadataIndex romIndex, Vector<RomID> hierarchy) throws RomNotFoundException {
		hierarchy.add(0, base);
		if (base.getInclude() != null) {
			getIncludeHierarchy(romIndex.getRomIDByXmlID(base.getInclude()), romIndex, hierarchy);
		}
		return hierarchy.toArray(new RomID[hierarchy.size()]);
	}
	
	
	private static RomID identifyRom(byte[] buffer, RomMetadataIndex romIndex) throws RomNotFoundException {
        for (int i = 0; i < romIndex.size(); i++) {
        	RomID romid = romIndex.getRomIDByIndex(i);
        	
        	if (!romid.isAbstract() && 
    			buffer.length >= romid.getInternalIDAddress() + romid.getInternalIDString().length()) {
    		
        		String testString = new String(buffer, romid.getInternalIDAddress(), romid.getInternalIDString().length());

				if (testString.equalsIgnoreCase(romid.getInternalIDString())) {
					return romid;
				}	        	
        	}
        }	
        // If not found in index, throw exception
        throw new RomNotFoundException();
	}

}
