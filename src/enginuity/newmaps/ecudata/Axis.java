package enginuity.newmaps.ecudata;

import java.util.Iterator;
import java.util.Vector;

public class Axis extends ECUData {
    
    protected int size;

    public Axis(String name) {
        super(name);
    }
    
    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }
 
}