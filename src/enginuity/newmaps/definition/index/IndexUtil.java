package enginuity.newmaps.definition.index;

import enginuity.newmaps.definition.RomDefinitionHandler;
import enginuity.newmaps.definition.RomTreeBuilder;
import enginuity.newmaps.xml.SaxParserFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Iterator;
import static enginuity.util.MD5Checksum.getMD5Checksum;
import java.io.BufferedInputStream;
import java.io.InputStream;
import javax.swing.JFrame;


public abstract class IndexUtil {
    
    public static void validateChecksums(File dir) { 
        Index index = null;
        try {
            index = getIndex(dir);
        } catch (Exception ex) {
            // Index file not found, create new
            new IndexBuilder(dir);
            return;
        }       
        
        // If no exceptions, iterate through index checking checksums
        Iterator it = index.iterator();
        while (it.hasNext()) {
            IndexItem item = (IndexItem)it.next();
            try {
                if (!item.getChecksum().equalsIgnoreCase(getMD5Checksum(item.getFile().getAbsolutePath()))) {

                }
            } catch (Exception ex) {
                // TODO: handle exception
                ex.printStackTrace();
            }
        }
    }
    
    
    
    public static Index getIndex(File dir) throws IOException, ClassNotFoundException {
        ObjectInputStream ois = new ObjectInputStream(new FileInputStream(dir.getAbsoluteFile() + "/" + IndexBuilder.INDEX_FILE_NAME));
        return (Index)ois.readObject();
    }
    
    
    public static void testMemUsage() {
        try {
            File dir = new File("/newdefs");
            Index index = getIndex(dir);
            RomTreeBuilder builder = new RomTreeBuilder();
            RomDefinitionHandler handler = new RomDefinitionHandler(builder);
            
            Iterator it = index.iterator();
            int i = 0;
            while (it.hasNext()) {
                IndexItem item = (IndexItem)it.next();
                System.out.println("Adding " + item.getFile() + " (#" + ++i + ")");
                InputStream inputStream1 = new BufferedInputStream(new FileInputStream(item.getFile()));
                SaxParserFactory.getSaxParser().parse(inputStream1, handler);
                
            }
            
            JFrame frame = new JFrame();
            frame.setVisible(true);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    public static void main(String[] args) {
        testMemUsage();
    }
    
}
