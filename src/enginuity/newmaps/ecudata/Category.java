package enginuity.newmaps.ecudata;

import enginuity.util.Nameable;
import enginuity.util.NamedSet;

public class Category extends NamedSet implements Nameable {
    
    private String name;
    private String description;
    private NamedSet<Category> subcats;
    private ECUData[] tables;
    
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

    public ECUData[] getEcuData() {
        return tables;
    }

    public void setEcuData(ECUData[] ecuData) {
        this.tables = tables;
    }
    
    
}