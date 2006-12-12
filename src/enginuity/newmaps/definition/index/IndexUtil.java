package enginuity.newmaps.definition.index;

import enginuity.newmaps.definition.RomDefinitionHandler;
import enginuity.newmaps.xml.SaxParserFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Iterator;
import static enginuity.util.MD5Checksum.getMD5Checksum;
import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public abstract class IndexUtil {
    
    public static void validateChecksums(File dir) { 
        Index index = null;
        try {
            index = getIndex(dir);
        } catch (Exception ex) {
            // Index file not found, create new
            new IndexBuilder(dir, index);
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
    
    
    
    public static Index getIndex(File dir) {
        try {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(dir.getAbsoluteFile() + "/" + IndexBuilder.INDEX_FILE_NAME));
            return (Index)ois.readObject();
        } catch (Exception ex) {
            return new Index();
        }
    }
    
    
    public static void testMemUsage() {
        try {
            File dir = new File("/newdefs");
            Index index = getIndex(dir);
            RomDefinitionHandler handler = new RomDefinitionHandler(index);
            
            Iterator it = index.iterator();
            int i = 0;
            long time = 0;
            while (it.hasNext()) {
                IndexItem item = (IndexItem)it.next();
                System.out.println("Adding " + item.getFile() + " (#" + ++i + ")");
                InputStream inputStream1 = new BufferedInputStream(new FileInputStream(item.getFile()));
                long start = System.currentTimeMillis();
                SaxParserFactory.getSaxParser().parse(inputStream1, handler);
                time += (System.currentTimeMillis() - start);
                
            }
            
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    public static void main(String[] args) {
        testMemUsage();
    }
    
}
