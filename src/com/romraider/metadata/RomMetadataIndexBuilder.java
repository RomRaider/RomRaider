package com.romraider.metadata;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Vector;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import com.romraider.util.FileListing;
import com.romraider.xml.DOMRomMetadataUnmarshaller;
import com.sun.org.apache.xerces.internal.parsers.DOMParser;

public final class RomMetadataIndexBuilder {
	
	public static RomMetadataIndex createIndex(File path) throws FileNotFoundException {
		Vector<RomIndexID> romVector = new Vector<RomIndexID>();
		List<File> files = FileListing.getFileListing(path);
		
		// Read files recursively and parse xml id and address
		for (File f : files) {
			// Ignore directories 
			if (f.isFile()) {
							
				try {
					//romVector.addAll(DOMRomMetadataUnmarshaller.unmarshallRomIDIndex(f));
					
				} catch (Exception x) {
					// TODO: Handle invalid definitions
				}
			}
		}
		
		RomIndexID[] romArray = romVector.toArray(new RomIndexID[romVector.size()]);
		return new RomMetadataIndex(romArray);
	}


	public static void main(String[] args) {
		// JG: Testing ...
		try {
			TransformerFactory tFactory = TransformerFactory.newInstance();
			Transformer transformer = 
				tFactory.newTransformer(new javax.xml.transform.stream.StreamSource("c:\\users\\owner\\desktop\\rommetadata\\rr.xsl"));
				transformer.transform(
						new javax.xml.transform.stream.StreamSource("c:\\users\\owner\\desktop\\16BITBASE.xml"),
						new javax.xml.transform.stream.StreamResult(new FileOutputStream("c:\\users\\owner\\desktop\\new.xml")));
		} catch (Exception e) {
			e.printStackTrace( );
		}
		
		
		long time1 = System.currentTimeMillis();
		try {
			
			File testdir = new File("c:\\users\\owner\\desktop\\rommetadata");
			File input = new File("c:\\users\\owner\\desktop\\test.hex");
			RomMetadataIndex romIndex = createIndex(testdir);		
			long time2 = System.currentTimeMillis();
			System.out.println(romIndex.size() + " index built in " + (time2 - time1) + "ms");
				
			RomMetadataHandler.getMetadata(input, romIndex);


		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

}