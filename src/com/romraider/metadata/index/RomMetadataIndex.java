package com.romraider.metadata.index;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import com.romraider.metadata.exception.RomNotFoundException;
import com.romraider.metadata.rom.Table3DMetadata;
import com.romraider.util.FileListing;
import com.romraider.xml.DOMRomMetadataUnmarshaller;

public class RomMetadataIndex extends ArrayList<RomIndexID> {
	
	public RomMetadataIndex(File path) throws FileNotFoundException {
		List<File> files = FileListing.getFileListing(path);		
		// Read files recursively and parse xml id and address
		for (File f : files) {
			// Ignore directories 
			if (f.isFile()) {
							
				try {
					this.addAll(DOMRomMetadataUnmarshaller.unmarshallRomIDIndex(f));
					
				} catch (Exception x) {
					// TODO: Handle invalid definitions
				}
			}
		}		
	}
	
	public RomIndexID getRomIDByID (String XmlID) throws RomNotFoundException {
		for (RomIndexID r : this) {
			if (r.getXmlid().equalsIgnoreCase(XmlID)) return r;
		}
		throw new RomNotFoundException();
	}	
	
	public static void main(String[] args) {
		// JG: Testing ...
		long time1 = System.currentTimeMillis();
		try {
			
			// Create index
			File testdir = new File("c:\\users\\owner\\desktop\\rommetadata");
			RomMetadataIndex romIndex = new RomMetadataIndex(testdir);		
			long time2 = System.currentTimeMillis();
			System.out.println(romIndex.size() + " defs indexed in " + (time2 - time1) + "ms");
			
			// Open test rom
			RomMetadata r = RomMetadataHandler.getMetadata(new File("c:\\users\\owner\\desktop\\test.hex"), romIndex);
			long time3 = System.currentTimeMillis();
			System.out.println("Rom " + r.getId() + " identified and parsed in " + (time3 - time2) + "ms");
			System.out.println(r.scalingMetadataSize() + " scaling types");
			System.out.println(r.tableMetadataSize() + " tables");
			System.out.println("Memory usage: " + ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024) + "kb");
			System.out.println(r.getTableMetadata(0));

		} catch (Exception e) {
			e.printStackTrace();
		}		
	}
}
