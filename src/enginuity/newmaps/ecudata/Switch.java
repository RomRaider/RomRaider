package enginuity.newmaps.ecudata;

import java.io.Serializable;

public class Switch extends ECUData implements Serializable {
    
    protected byte[] stateOn = new byte[1];
    protected byte[] stateOff = new byte[1];
    protected int size;
    
    public Switch(String name) {
        super(name);
    } 
    
    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }  

    public byte[] getStateOn() {
        return stateOn;
    }

    public void setStateOn(byte[] stateOn) {
        this.stateOn = stateOn;
    }

    public byte[] getStateOff() {
        return stateOff;
    }

    public void setStateOff(byte[] stateOff) {
        this.stateOff = stateOff;
    }
    
    public String toString() { 
        String scaleName = "";
        
        try {
            scaleName = scale.getName();
        } catch (NullPointerException ex) {
            scaleName = "Not found";
        }
        
        String output = "      --- Switch: " + name + " ---" +
                "\n      - Description: " + description +
                "\n      - Address: " + address +
                "\n      - Userlevel: " + userLevel;
                
        
        return output;
                       
    }    
}
