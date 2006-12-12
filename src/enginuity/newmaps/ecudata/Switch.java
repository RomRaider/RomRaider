package enginuity.newmaps.ecudata;

import java.io.Serializable;
import static enginuity.newmaps.definition.AttributeParser.stringToByteArray;

public class Switch extends ECUData implements Serializable {
    
    protected byte[] stateOn = new byte[1];
    protected byte[] stateOff = new byte[1];
    protected int size;
    
    private int defaultValue = SwitchGroup.DEFAULT_NONE;
    boolean hidden = false;
    
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

    public void setStateOn(String values) {
        this.stateOn = stringToByteArray(values, " ");
    }

    public byte[] getStateOff() {
        return stateOff;
    }

    public void setStateOff(String values) {
        this.stateOff = stringToByteArray(values, " ");
    }
    
    public void setDefaultValue(int defaultVal) {
        this.defaultValue = defaultValue;
    }
    
    public int getDefaultValue() {
        return defaultValue;
    }
    
    public void setDefaultValue(String value) {
        if (value.equalsIgnoreCase("on")) defaultValue = SwitchGroup.DEFAULT_ON;
        else if (value.equalsIgnoreCase("off")) defaultValue = SwitchGroup.DEFAULT_OFF;
        else defaultValue = SwitchGroup.DEFAULT_NONE;       
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
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
