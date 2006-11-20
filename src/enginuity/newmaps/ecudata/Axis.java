package enginuity.newmaps.ecudata;

import java.io.Serializable;

public class Axis extends ECUData implements Serializable {
    
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
    
    public String toString() { 
        String scaleName = "";
        
        try {
            scaleName = scale.getName();
        } catch (NullPointerException ex) {
            scaleName = "Not found";
        }
        
        String output = "         --- Axis: " + name + " ---" +
                "\n         - Scale: " + scaleName +
                "\n         - Address: " + address;
                
        
        return output;
                       
    }
 
}