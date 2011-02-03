package com.romraider.metadata.index;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Vector;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;
import org.xml.sax.SAXException;
import com.romraider.metadata.exception.RomNotFoundException;
import com.romraider.xml.DOMRomMetadataUnmarshaller;

public class RomMetadataHandler {
	
	public static RomMetadata getMetadata(File rom, RomMetadataIndex romIndex) throws IOException, RomNotFoundException, SAXException, TransformerFactoryConfigurationError, TransformerException {
		// Read rom file
        FileInputStream fis = new FileInputStream(rom);
    	DOMRomMetadataUnmarshaller ums = new DOMRomMetadataUnmarshaller();
        try {
        	byte[] buffer = new byte[(int)rom.length()];
            fis.read(buffer);	
            
            // Identify rom and find metadata
            RomIndexID romid = identifyRom(buffer, romIndex);
            
            // Build array of rom metadata hierarchy starting with lowest level
            Vector<RomIndexID> hierarchy = new Vector<RomIndexID>();
            getIncludeHierarchy(romid, romIndex, hierarchy);
        	
            // Pass hierarchy to unmarshaller one at a time
            for (RomIndexID r : hierarchy) {
            	ums.unmarshallRomMetadata(r);
            }
            
        } finally {
            fis.close();
        }		
		return ums.getRomMetadata();
	}
	
	
	private static RomIndexID[] getIncludeHierarchy(RomIndexID base, RomMetadataIndex romIndex, Vector<RomIndexID> hierarchy) throws RomNotFoundException {
		hierarchy.add(0, base);
		if (base.getInclude() != null) {
			getIncludeHierarchy(romIndex.getRomIDByID(base.getInclude()), romIndex, hierarchy);
		}
		return hierarchy.toArray(new RomIndexID[hierarchy.size()]);
	}
	
	
	private static RomIndexID identifyRom(byte[] buffer, RomMetadataIndex romIndex) throws RomNotFoundException {
        for (int i = 0; i < romIndex.size(); i++) {
        	RomIndexID romid = romIndex.get(i);
        	
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
