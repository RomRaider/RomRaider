package com.romraider.metadata;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Vector;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.romraider.metadata.exception.RomNotFoundException;
import com.romraider.xml.DOMRomMetadataRefactorUtil;
import com.romraider.xml.DOMRomMetadataUnmarshaller;
import com.sun.org.apache.xerces.internal.parsers.DOMParser;

public class RomMetadataHandler {
	
	public static RomMetadata getMetadata(File rom, RomMetadataIndex romIndex) throws IOException, RomNotFoundException, SAXException {

        RomMetadata rm = new RomMetadata();
        
		// Read rom file
        FileInputStream fis = new FileInputStream(rom);
        try {
        	byte[] buffer = new byte[(int)rom.length()];
            fis.read(buffer);	
            
            // Identify rom and find in index
            RomIndexID romid = identifyRom(buffer, romIndex);
            
            // Build array of rom metadata hierarchy starting with lowest level
            Vector<RomIndexID> hierarchy = new Vector<RomIndexID>();
            getIncludeHierarchy(romid, romIndex, hierarchy);
            Document[] refactoredDocuments = new Document[hierarchy.size()];
            
            // Pass hierarchy to unmarshaller one at a time
            for (int i = 0; i < hierarchy.size(); i++) {
            	// Refactor document to RomRaider spec
            	RomIndexID r = hierarchy.get(i);
        		InputSource src = new InputSource(new FileInputStream(r.getDefinitionFile()));
        		DOMParser parser = new DOMParser();
        		parser.parse(src);
        		
        		refactoredDocuments[i] = DOMRomMetadataRefactorUtil.refactorDocument(parser.getDocument());
            }
            
        } finally {
            fis.close();
        }
		
		return rm;
	}
	
	
	private static RomIndexID[] getIncludeHierarchy(RomIndexID base, RomMetadataIndex romIndex, Vector<RomIndexID> hierarchy) throws RomNotFoundException {
		hierarchy.add(0, base);
		if (base.getInclude() != null) {
			getIncludeHierarchy(romIndex.getRomIDByXmlID(base.getInclude()), romIndex, hierarchy);
		}
		return hierarchy.toArray(new RomIndexID[hierarchy.size()]);
	}
	
	
	private static RomIndexID identifyRom(byte[] buffer, RomMetadataIndex romIndex) throws RomNotFoundException {
        for (int i = 0; i < romIndex.size(); i++) {
        	RomIndexID romid = romIndex.getRomIDByIndex(i);
        	
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
