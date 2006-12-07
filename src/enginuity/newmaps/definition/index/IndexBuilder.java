package enginuity.newmaps.definition.index;

import enginuity.newmaps.xml.SaxParserFactory;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import static enginuity.util.MD5Checksum.getMD5Checksum;

public class IndexBuilder {
        
    public static final String INDEX_FILE_NAME = "index.xml";
    public static final String MEMMODEL_FILE_NAME = "memmodels.xml";
    
    private File file;
    private Index index;
    
    public IndexBuilder(File file, Index index) {
        this.file = file;          
        this.index = index;
        
        // Process all definition files
        traverse(file);      
        
        // Output index
        save(index, file);
        
    }
    
    private static void save(Index index, File file) {

        index.fixInheritance();
        
        try {           
            
            FileOutputStream fos = new FileOutputStream(file.getAbsoluteFile() + "/" + INDEX_FILE_NAME);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(index);
            oos.flush();
            oos.close();
            
            // Open stuff
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file.getAbsoluteFile() + "/" + INDEX_FILE_NAME));
            System.out.println((Index)ois.readObject());
            ois.close();
            
             
        } catch (Exception ex) {
            // TODO: Exception handling
            ex.printStackTrace();
        }
    }
    
    private void traverse(File file) {
        if (file.isDirectory()) {
            String[] children = file.list();
            for (int i=0; i<children.length; i++) {
                traverse(new File(file, children[i]));
            }
        } else {            
            processFile(file);
        }
    }
    
    
    private void processFile(File file) {
        if (!file.getName().equalsIgnoreCase(INDEX_FILE_NAME) && 
            !file.getName().equalsIgnoreCase(MEMMODEL_FILE_NAME) && 
            !index.fileCurrent(file)) {
            try {
                IndexHandler handler = new IndexHandler();
                SaxParserFactory.getSaxParser().parse(new BufferedInputStream(new FileInputStream(file)), handler); 
                IndexItem item = handler.getItem();
                item.setFile(file);
                item.setChecksum(getMD5Checksum(file.getAbsolutePath()));
                index.add(item);
                
            } catch (Exception ex) {
                // TODO: Handle exceptions
                ex.printStackTrace();
            }
        }        
    }
    

    
    public static void main(String[] args) {
        IndexBuilder b = new IndexBuilder(new File("/newdefs"), new Index());
        
    } 
}