package enginuity.newmaps.definition.index;

import enginuity.newmaps.xml.SaxParserFactory;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Iterator;

public class IndexBuilder {
        
    private static final String INDEX_FILE_NAME = "index.xml";
    
    private File file;
    private Index index = new Index();
    
    public IndexBuilder(File file) {
        this.file = file;           
        // Process all definition files
        traverse(file);        
        // Output index
        save();
        
    }
    
    private void save() {
        //
        // Inherit storage addresses where necessary
        //
        Iterator it1 = index.iterator();
        while (it1.hasNext()) {
            IndexItem item = (IndexItem)it1.next();
            
            if (!item.isAbstract() && item.getIdAddress() == 0) {
                Iterator it2 = index.iterator();
                while (it2.hasNext()) {
                    IndexItem parentItem = (IndexItem)it2.next();
                    if (parentItem.getName().equalsIgnoreCase(item.getBase())) {
                        item.setIdAddress(parentItem.getIdAddress());
                        break;
                    }
                }
            }
        }
        
        //System.out.println(index);
        
        try {           
            
            FileOutputStream fos = new FileOutputStream(file.getAbsoluteFile() + "/" + INDEX_FILE_NAME);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(index);
            oos.flush();
            oos.close();
            
            /* Open stuff
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file.getAbsoluteFile() + "/" + INDEX_FILE_NAME));
            System.out.println((Index)ois.readObject());
            ois.close();
            */
             
        } catch (Exception ex) {
            // TODO: Exception handling
            ex.printStackTrace();
        }
    }
    
    private void traverse(File file) {
        processFile(file);
        if (file.isDirectory()) {
            String[] children = file.list();
            for (int i=0; i<children.length; i++) {
                traverse(new File(file, children[i]));
            }
        }
    }
    
    
    private void processFile(File file) {
        if (file.isFile() && !file.getName().equalsIgnoreCase(INDEX_FILE_NAME)) {
            try {
                IndexHandler handler = new IndexHandler();
                SaxParserFactory.getSaxParser().parse(new BufferedInputStream(new FileInputStream(file)), handler); 
                IndexItem item = handler.getItem();
                item.setFile(file);
                index.add(item);
                
            } catch (Exception ex) {
                // TODO: Handle exceptions
                ex.printStackTrace();
            }
        }        
    }
    

    
    public static void main(String[] args) {
        IndexBuilder b = new IndexBuilder(new File("/newdefs"));
        
    } 
}