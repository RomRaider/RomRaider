package com.romraider.metadata;

import java.io.BufferedWriter;

import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.management.modelmbean.XMLParseException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import ch.elca.el4j.services.xmlmerge.XmlMerge;
import ch.elca.el4j.services.xmlmerge.merge.DefaultXmlMerge;

import com.romraider.maps.Rom;
import com.romraider.swing.JProgressPane;
import com.romraider.util.FileListing;
import com.romraider.xml.DOMRomMetadataUnmarshaller;
import com.sun.org.apache.xerces.internal.parsers.DOMParser;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;

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
			File testdir = new File("c:\\users\\owner\\desktop\\rommetadata");
			RomMetadataIndex romIndex = createIndex(testdir);		
			long time2 = System.currentTimeMillis();
			System.out.println(romIndex.size() + " defs parsed in " + (time2 - time1) + "ms");
			
			// Open test rom
			RomMetadata r = RomHandler.getMetadata(new File("c:\\users\\owner\\desktop\\test.hex"), romIndex);
			long time3 = System.currentTimeMillis();
			System.out.println("Rom " + r.getRomid().getXmlid() + " identified (and partially parsed) in " + (time3 - time2) + "ms");
			System.out.println(r.getRomid());
			System.out.println(r.scalingMetadataSize() + " scaling elements");
			System.out.println(r.tableMetadataSize() + " tables");

			System.out.println(r.getScalingMetadata("%"));

			System.out.println(((Table3DMetadata)r.getTableMetadata(0)).getYaxis().getScalingMetadata().getName());
			System.out.println("Memory usage: " + ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024) + "kb");
			
			InputSource src1 = new InputSource(new FileInputStream(new File("c:\\users\\owner\\desktop\\16BITBASE.xml")));
			DOMParser parser = new DOMParser();
			parser.parse(src1);
			Document root1 = parser.getDocument();
			
			InputSource src2 = new InputSource(new FileInputStream(new File("c:\\users\\owner\\desktop\\A4TF400E.xml")));
			parser.parse(src2); 	
			Document root2 = parser.getDocument();
			
			Document[] docs = new Document[] {root1, root2};
			
			XmlMerge merge = new DefaultXmlMerge();
			Document mergedDoc = merge.merge(docs);
			System.out.println(mergedDoc.getChildNodes().getLength());
			
		    File output = new File("c:\\users\\owner\\desktop\\new.xml"); 	
	        FileOutputStream fos = new FileOutputStream(output);
	        OutputFormat of = new OutputFormat("XML", "ISO-8859-1", true);
	        of.setIndent(1);
	        of.setIndenting(true);
	        
	        try {
	            XMLSerializer serializer = new XMLSerializer(fos, of);
	            serializer.serialize(mergedDoc);
	            fos.flush();
	        } finally {
	            fos.close();
	        }


		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

}