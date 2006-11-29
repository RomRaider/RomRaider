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
        File file = new File("ecu_defs.xml");
        
        try {
            //
            // Parse existing definitions
            //
            DOMParser parser = new DOMParser();
            InputSource src = new InputSource(new FileInputStream(file));
            parser.parse(src);
            Document doc = parser.getDocument();
            
            // Create old ROMs
            Vector<Rom> roms = handler.unmarshallXMLDefinition(doc.getDocumentElement());        
            
            // 
            // Create new document
            //
            DefinitionBuilder builder = new DefinitionBuilder(roms);
        
        } catch (Exception ex) {
            ex.printStackTrace();
        }        
    }  
    
}
