package com.romraider.metadata;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.management.modelmbean.XMLParseException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import com.romraider.maps.Rom;
import com.romraider.swing.JProgressPane;
import com.romraider.util.FileListing;
import com.romraider.xml.DOMRomMetadataUnmarshaller;

public final class RomMetadataIndexBuilder {

	
	public static RomMetadataIndex createIndex(File path) throws FileNotFoundException {
		Vector<RomID> romVector = new Vector<RomID>();
		List<File> files = FileListing.getFileListing(path);
		
		// Read files recursively and parse xml id and address
		for (File f : files) {
			// Ignore directories 
			if (f.isFile()) {
							
				try {
					romVector.addAll(DOMRomMetadataUnmarshaller.unmarshallRomIDIndex(f));
					
				} catch (Exception x) {
					// TODO: Handle invalid definitions
				}
			}
		}
		
		RomID[] romArray = romVector.toArray(new RomID[romVector.size()]);
		return new RomMetadataIndex(romArray);
	}


	public static void main(String[] args) {
		// JG: Testing ...
		long time1 = System.currentTimeMillis();
		try {
			
			// Create index
			File testdir = new File("c:\\documents and settings\\owner\\desktop\\rommetadata");
			RomMetadataIndex romIndex = createIndex(testdir);		
			long time2 = System.currentTimeMillis();
			System.out.println(romIndex.size() + " defs parsed in " + (time2 - time1) + "ms");
			
			// Open test rom
			RomHandler.getMetadata(new File("c:\\documents and settings\\owner\\desktop\\test.hex"), romIndex);
			long time3 = System.currentTimeMillis();
			System.out.println("ROM identified in " + (time3 - time2) + "ms");
            
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

}