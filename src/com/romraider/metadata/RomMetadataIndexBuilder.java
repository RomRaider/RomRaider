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
		long startTime = System.currentTimeMillis();
		try {
			// Create index
			File testdir = new File("c:\\documents and settings\\owner\\desktop\\rommetadata");
			RomMetadataIndex romIndex = createIndex(testdir);		
			System.out.println(romIndex.size() + " defs parsed in " + (System.currentTimeMillis() - startTime) + "ms");
			
			// Open test rom
			RomHandler.getMetadata(new File("c:\\documents and settings\\owner\\desktop\\test.hex"), romIndex);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

}