package enginuity.newmaps.ecudata;

import enginuity.util.Nameable;
import enginuity.util.NamedSet;
import java.io.Serializable;
import java.util.Iterator;

public class Category extends NamedSet implements Nameable, Serializable {
    
    private String name;
    private String description;
    private NamedSet<ECUData> tables = new NamedSet<ECUData>();
    
    private Category() { }
    
    public Category(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    
    public String toString() {
        StringBuffer output = new StringBuffer();
        output.append(" --- CATEGORY: " + name + " ---\n");
            
        Iterator it = iterator();        
        while (it.hasNext()) {
            output.append("  " + it.next().toString() + "\n");
        }   
        
        it = tables.iterator();
        while (it.hasNext()) {
            output.append(it.next().toString() + "\n");
        }                            

        return output + " --- END CATEGORY: " + name + " ---\n";
    }

    public void addTable(ECUData table) {
        tables.add(table);
    }
    
    public void removeTable(ECUData table) {
        try {
            tables.remove(table);
        } catch (Exception ex) {
            
        } finally {
            Iterator it = iterator();
            while (it.hasNext()) {
                ((Category)it.next()).removeTable(table);
            }
        }
    }
    
}