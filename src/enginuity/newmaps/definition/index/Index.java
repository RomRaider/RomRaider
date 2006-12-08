package enginuity.newmaps.definition.index;

import enginuity.util.NamedSet;
import java.io.Serializable;
import java.util.Iterator;
import static enginuity.util.MD5Checksum.getMD5Checksum;
import enginuity.util.Nameable;
import enginuity.util.exception.NameableNotFoundException;
import java.io.File;

public class Index extends NamedSet<IndexItem> implements Serializable {  
        
    public String toString() {
        StringBuffer sb = new StringBuffer();
        Iterator it = iterator();
        
        while (it.hasNext()) {
            sb.append(((IndexItem)it.next()).getFile()+"\n");
        }
        
        return sb+"";
    }
    
    
    public boolean fileCurrent(File file, IndexItem item) {
        // Checks whether file exists and matches checksum
                
        if (item.getFile().equals(file)) {
            // Item found, return whether checksum matches

            try {
                return item.getChecksum().equals(getMD5Checksum(file.getPath()));
            } catch (Exception ex) {
                return false;
            }
        } else {        
           return false;
        }
    }
            
    
    public void fixInheritance() {
        
        //
        // Inherit storage addresses where necessary
        //        
        for (int i = size() - 1; i >= 0; i--) {
            IndexItem item = (IndexItem)get(i);
            
            if (!item.isAbstract() && item.getBase() != null) {
                                
                try {
                    IndexItem parentItem = (IndexItem)get(item.getBase());                    
                    if (item.getIdAddress() == 0) item.setIdAddress(parentItem.getIdAddress());
                    
                } catch (Exception ex) { }                           
            }
        }        
    }   

    
    public void cleanup() {
        Iterator it = iterator();
        while (it.hasNext()) {
            IndexItem item = (IndexItem)it.next();
            try {
                if (!item.getFile().exists()) remove(item);
            } catch (NameableNotFoundException ex) {
                // ignore
            }
        }
    }
    
    
    public IndexItem get(File file) throws NameableNotFoundException {
        Iterator it = iterator();
        while (it.hasNext()) {
            IndexItem item = (IndexItem)it.next();
            
            try {  
                if (item.getFile().getPath().equals(file.getPath())) {
                    
                    return item;
                }
            } catch (Exception ex) { }            
        }
        throw new NameableNotFoundException(file.getName());
        
    }
    
    public void add(Nameable n) {
        try {
            IndexItem item = (IndexItem)n;
            item.setChecksum(getMD5Checksum(item.getFile().getPath()));
        } catch (Exception ex) {
        }
        
        super.add((Nameable)n);
    }
}
