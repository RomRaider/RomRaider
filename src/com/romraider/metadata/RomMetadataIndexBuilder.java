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
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;

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

public final class RomMetadataIndexBuilder {
	
	public static RomMetadataIndex createIndex(File path) throws FileNotFoundException {
		Vector<RomIndexID> romVector = new Vector<RomIndexID>();
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
		
		
		/*long time1 = System.currentTimeMillis();
		try {
			
			File testdir = new File("c:\\users\\owner\\desktop\\rommetadata");
			File input = new File("c:\\users\\owner\\desktop\\test.hex");
			RomMetadataIndex romIndex = createIndex(testdir);		
			long time2 = System.currentTimeMillis();
			System.out.println(romIndex.size() + " index built in " + (time2 - time1) + "ms");
				
			RomMetadataHandler.getMetadata(input, romIndex);*/
			
			
			
			/*InputSource src1 = new InputSource(new FileInputStream(new File("c:\\users\\owner\\desktop\\16BITBASE.xml")));
			DOMParser parser = new DOMParser();
			parser.parse(src1);
			Document root1 = parser.getDocument();
			
			InputSource src2 = new InputSource(new FileInputStream(new File("c:\\users\\owner\\desktop\\A4TF400E.xml")));
			parser.parse(src2); 	
			Document root2 = parser.getDocument();
			
			Document[] docs = new Document[] {root1, root2};
			
			XmlMerge merge = new DefaultXmlMerge();
			Document mergedDoc = merge.merge(docs);

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
		}*/
		
	}

}