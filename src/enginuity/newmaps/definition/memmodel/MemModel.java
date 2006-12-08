package enginuity.newmaps.definition.memmodel;

import enginuity.util.NamedSet;

public class MemModel extends NamedSet<Segment> {
    
    private String name;
    
    public MemModel(String name) {
        setName(name);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
}
