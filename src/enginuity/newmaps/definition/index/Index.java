package enginuity.newmaps.definition.index;

import enginuity.util.NamedSet;
import java.io.Serializable;
import java.util.Iterator;

public class Index extends NamedSet<IndexItem> implements Serializable {  
        
    public String toString() {
        StringBuffer sb = new StringBuffer();
        Iterator it = iterator();
        
        while (it.hasNext()) {
            sb.append(it.next()+"\n");
        }
        
        return sb+"";
    }
    
    public void fixInheritance() {
        //
        // Inherit storage addresses where necessary
        //
        
        Iterator it1 = iterator();
        while (it1.hasNext()) {
            IndexItem item = (IndexItem)it1.next();
            
            if (!item.isAbstract() && item.getIdAddress() == 0) {
                Iterator it2 = iterator();
                while (it2.hasNext()) {
                    IndexItem parentItem = (IndexItem)it2.next();
                    if (parentItem.getName().equalsIgnoreCase(item.getBase())) {
                        item.setIdAddress(parentItem.getIdAddress());
                        break;
                    }
                }
            }
        }     
    }
    
    // TODO: Move parent index items before children
    
}
