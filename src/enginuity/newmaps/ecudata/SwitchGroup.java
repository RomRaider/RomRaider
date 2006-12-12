package enginuity.newmaps.ecudata;

import enginuity.util.NamedSet;

public class SwitchGroup extends ECUData {
    
    public static final int DEFAULT_ON = 0;
    public static final int DEFAULT_OFF = 1;
    public static final int DEFAULT_NONE = 2;
    
    private int defaultValue = DEFAULT_NONE;
    private boolean hidden = false;
    
    NamedSet<Switch> switches = new NamedSet<Switch>();    
    
    public SwitchGroup(String name) {
        super(name);
    }
    
    public void setDefaultValue(int defaultVal) {
        this.defaultValue = defaultValue;
    }
    
    public int getDefaultValue() {
        return defaultValue;
    }
    
    public void setDefaultValue(String value) {
        if (value.equalsIgnoreCase("on")) defaultValue = DEFAULT_ON;
        else if (value.equalsIgnoreCase("off")) defaultValue = DEFAULT_OFF;
        else defaultValue = DEFAULT_NONE;       
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }
    
}