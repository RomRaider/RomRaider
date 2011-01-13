package com.romraider.metadata;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Vector;

import org.xml.sax.SAXException;

import com.romraider.xml.DOMRomMetadataUnmarshaller;
import com.romraider.xml.RomNotFoundException;

public class RomHandler {
	
	public static RomMetadata getMetadata(File rom, RomMetadataIndex romIndex) throws IOException, RomNotFoundException, SAXException {
		
		// Read rom file
        FileInputStream fis = new FileInputStream(rom);
        try {
        	byte[] buffer = new byte[(int)rom.length()];
            fis.read(buffer);	
            
            // Identify rom and find metadata
            RomID romid = identifyRom(buffer, romIndex);
            
            // Build array of rom metadata hierarchy starting with lowest level
            RomID[] hierarchy = getIncludeHierarchy(romid, romIndex);
            
            // Pass hierarchy to unmarshaller one at a time
            RomMetadata romMetadata = new RomMetadata();
            for (RomID r : hierarchy) {
            	DOMRomMetadataUnmarshaller.unmarshallRomMetadata(r, romMetadata);
            }
            
        } finally {
            fis.close();
        }
		
		return null;
	}
	
	
	private static RomID[] getIncludeHierarchy(RomID base, RomMetadataIndex romIndex) {
		Vector<RomID> hierarchy = new Vector<RomID>();
		hierarchy.add(base);
		if (base.getInclude() != null) {
			
		}
		return hierarchy.toArray(new RomID[hierarchy.size()]);
	}
	
	
	private static RomID identifyRom(byte[] buffer, RomMetadataIndex romIndex) throws RomNotFoundException {
        for (int i = 0; i < romIndex.size(); i++) {
        	RomID romid = romIndex.getRomIDByIndex(i);
        	if (!romid.isAbstract()) {
        		
	        	/*String testString = new String(buffer, (romid.getInternalIDAddress() & 0xff), 
	        									romid.getInternalIDString().length());
	        	
	        	System.out.println("RomMetadata:" + romid.getXmlid() + 
	        					   "; ID Address:" + (romid.getInternalIDAddress() & 0xff) + 
	        					   "; ID String:" + romid.getInternalIDString() + 
	        					   "; ROM id:" + testString);
	        	if (testString.equalsIgnoreCase(romid.getInternalIDString())) {
	        		return romid;
	        	}*/
        		
        		byte[] testString = new String(buffer, (romid.getInternalIDAddress() & 0xff), 
						romid.getInternalIDString().length()).getBytes();

				System.out.println("RomMetadata:" + romid.getXmlid() + 
						   "; ID String:" + romid.getInternalIDString() + 
						   "; ROM id Arrays.toString():" + Arrays.toString(testString) + 
						   "; ROM id:" + testString);
				if (testString == romid.getInternalIDString().getBytes()) {
					return romid;
				}	        	
        	}
        }	
        // If not found in index, throw exception
        throw new RomNotFoundException();
	}

}
