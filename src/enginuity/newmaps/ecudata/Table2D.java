package enginuity.newmaps.ecudata;

import java.io.Serializable;

public class Table2D extends Axis implements Serializable {
    
    protected Axis axis;
    
    public Table2D(String name) {
        super(name);
    }    

    public Axis getAxis() {
        return axis;
    }

    public void setAxis(Axis axis) {
        this.axis = axis;
    }
    
    public String toString() {
        String scaleName = "";
        
        try {
            scaleName = scale.getName();
        } catch (NullPointerException ex) {
            scaleName = "Not found";
        }
        
        String output = "      --- Table: " + name + " ---" +
                "\n      - Description: " + description +
                "\n      - Scale: " + scaleName +
                "\n      - Address: " + address +
                "\n      - Userlevel: " + userLevel +
                "\n" + axis;
                
                
        return output;
    }

}