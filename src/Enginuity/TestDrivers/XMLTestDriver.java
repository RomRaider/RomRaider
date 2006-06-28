// Just a driver to test the XML parser

package Enginuity.TestDrivers;

import Enginuity.Maps.Rom;
import Enginuity.Maps.ECUDefinitionCollection;
import java.io.File;
import java.io.FileInputStream;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import com.sun.org.apache.xerces.internal.parsers.DOMParser;

public class XMLTestDriver {
    
   public static void main(String[] args) {
	/*ECUDefinitionCollection roms = null;

	try {
            
            // Parse XML
            System.out.println("Parsing XML...");
	    InputSource src = new InputSource(new FileInputStream(new File("./ecu_defs.xml")));

            DOMRomUnmarshaller domUms = new DOMRomUnmarshaller();
            DOMParser parser = new DOMParser();
            parser.parse(src);
            Document doc = parser.getDocument();
		
            roms = domUms.unmarshallXMLDefinition(doc.getDocumentElement());            
            System.out.println(roms.toString());
            
            // Open image and find ECU version
            System.out.println("Opening Image...");            
            Rom ecuImage = roms.parseRom(new File("./A4TF400E.hex"));
            
            String output = ecuImage.getRomIDString();
            System.out.println("Found ECU Version: " + output);
            System.out.println("Done.");
            
	} catch(Exception e) {
	    System.err.println("Exception: " + e);
	}*/
            System.out.println(Double.parseDouble("-12"));
    }
}