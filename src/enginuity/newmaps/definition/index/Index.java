package enginuity.newmaps.definition.index;

import enginuity.util.NamedSet;
import java.io.Serializable;
import java.util.Iterator;
import static enginuity.util.MD5Checksum.getMD5Checksum;
import java.io.File;

public class Index extends NamedSet<IndexItem> implements Serializable {  
        
    public String toString() {
        StringBuffer sb = new StringBuffer();
        Iterator it = iterator();
        
        while (it.hasNext()) {
            sb.append(it.next()+"\n");
        }
        
        return sb+"";
    }
    
    
    public boolean fileCurrent(File file) {
        // Checks whether file exists and matches checksum
        Iterator it = iterator();
        while (it.hasNext()) {
            IndexItem item = (IndexItem)it.next();
            
            if (item.getFile().getAbsolutePath().equals(file.getAbsolutePath())) {
                // Item found, return whether checksum matches
                try {
                    return item.getChecksum().equals(getMD5Checksum(file.getAbsolutePath()));
                } catch (Exception ex) {
                    return false;
                }
            }
        }
        
        // Iterator has finished without match
        return false;
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
                           
            } else if (item.isAbstract()) {
                
            }
        }        
    }    
}
