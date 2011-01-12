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

	
	public static RomID[] createIndex(File path) throws FileNotFoundException {
		Vector<RomID> romVector = new Vector<RomID>();
		List<File> files = FileListing.getFileListing(path);
		
		// Read files recursively and parse xml id and address
		for (File f : files) {
			// Ignore directories 
			if (f.isFile()) {
				
				//System.out.println("Parsing " + f.getName() + " ...");				
				try {
					romVector.addAll(DOMRomMetadataUnmarshaller.unmarshallRomIDIndex(f));
					
				} catch (Exception x) {
					// TODO: Handle invalid definitions
				}
			}
		}
		
		return romVector.toArray(new RomID[romVector.size()]);
	}


	public static void main(String[] args) {
		// JG: Testing ...
		long startTime = System.currentTimeMillis();
		try {
			File testdir = new File("c:\\documents and settings\\owner\\desktop\\rommetadata");
			RomID[] romIndex = createIndex(testdir);		
			
			/*for (int i = 0; i < romIndex.length; i++) {
				System.out.println(romIndex[i]);
			}*/
			System.out.println(romIndex.length + " parsed in " + (System.currentTimeMillis() - startTime) + "ms");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

}