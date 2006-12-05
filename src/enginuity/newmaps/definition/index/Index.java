package enginuity.newmaps.definition.index;

import enginuity.util.NamedSet;
import java.io.Serializable;
import java.util.Iterator;

public class Index<IndexItem> extends NamedSet implements Serializable {  
        
    public String toString() {
        StringBuffer sb = new StringBuffer();
        Iterator it = iterator();
        
        while (it.hasNext()) {
            sb.append(it.next()+"\n");
        }
        
        return sb+"";
    }
    
}
