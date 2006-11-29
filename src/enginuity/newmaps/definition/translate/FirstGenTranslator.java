package enginuity.newmaps.definition.translate;

import com.sun.org.apache.xerces.internal.parsers.DOMParser;
import enginuity.maps.Rom;
import java.io.File;
import java.io.FileInputStream;
import java.util.Vector;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

public class FirstGenTranslator {
        
    public FirstGenTranslator() {
    }
    
    public static void main(String args[]) {
        FirstGenDefinitionHandler handler = new FirstGenDefinitionHandler();
        File inputFile = new File("ecu_defs.xml");        
        File outputFolder = new File("/newdefs/");
        
        try {
            //
            // Parse existing definitions
            //
            System.out.println("*******************************\n" +
                               "* Parsing old definitions ... *\n" +
                               "*******************************");
            DOMParser parser = new DOMParser();
            InputSource src = new InputSource(new FileInputStream(inputFile));
            parser.parse(src);
            Document doc = parser.getDocument();
            
            System.out.println("\n***************************************\n" +
                               "* Building old definition objects ... *\n" +
                               "***************************************");
            // Create old ROMs
            Vector<Rom> roms = handler.unmarshallXMLDefinition(doc.getDocumentElement());        
            
            // 
            // Create new document
            //
            System.out.println("\n*****************************\n" +
                               "* Generating XML output ... *\n" +
                               "*****************************");
            DefinitionBuilder builder = new DefinitionBuilder(roms, outputFolder);
            
            
            System.out.println("\n*************\n" +
                               "* Finished! *\n" +
                               "*************");
        
        } catch (Exception ex) {
            ex.printStackTrace();
        }        
    }  
    
}
