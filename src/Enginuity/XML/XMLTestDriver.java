// Just a driver to test the XML parser

package Enginuity.XML;

import Enginuity.Maps.Rom;
import java.io.File;
import java.io.FileInputStream;
import org.xml.sax.InputSource;
import com.sun.org.apache.xerces.internal.parsers.DOMParser;
import org.w3c.dom.Document;

public class XMLTestDriver {
        
   public static void main(String[] args) {
       
        try {
            FileInputStream input = new FileInputStream(new File("./images/A4TF400E.hex"));            
            DOMRomUnmarshaller domUms = new DOMRomUnmarshaller();
            DOMParser parser = new DOMParser();
            parser.parse(new InputSource(new FileInputStream(new File("./ecu_defs.xml"))));
            Document doc = parser.getDocument();
            
            byte[] inputAsByteArray = new byte[input.available()];
            input.read(inputAsByteArray);
            
            Rom rom = domUms.unmarshallXMLDefinition(doc.getDocumentElement(), inputAsByteArray);
            System.out.println(rom);
            
        } catch (Exception e) {
            System.out.println("Exception: " + e);
        }
    }
}