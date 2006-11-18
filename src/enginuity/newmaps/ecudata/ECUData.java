package enginuity.newmaps.ecudata;

import enginuity.util.Nameable;

public abstract class ECUData implements Nameable {
       
    protected String name;
    protected Scale scale;
    protected int address;
    protected int userLevel;
    protected String description;
    private boolean isStatic;
    private boolean isAbstract;
    
    public ECUData() { }

    public ECUData(String name) {
        this.name = name;
    }
    
    public final void setName(String name) {
        this.name = name;
    }
    
    public final String getName() {
        return name;
    }
    
    public final void setScale(Scale scale) {
        this.scale = scale;
    }
        
    public final Scale getScale() {
        return scale;
    }
    
    public final void setAddress(int address) {
        this.address = address;
    }
    
    public final int getAddress() {
        return address;
    }
        
    public final void setUserLevel(int level) {
        this.userLevel = level;        
        if (userLevel < 0) userLevel = 0;
        else if (userLevel > 5) userLevel = 5;
    }
    
    public final int getUserLevel() {
        return userLevel;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isIsStatic() {
        return isStatic;
    }

    public void setIsStatic(boolean isStatic) {
        this.isStatic = isStatic;
    }

    public boolean isIsAbstract() {
        return isAbstract;
    }

    public void setIsAbstract(boolean isAbstract) {
        this.isAbstract = isAbstract;
    }
    
    public String toString() {
        String scaleName = "";
        
        try {
            scaleName = scale.getName();
        } catch (NullPointerException ex) {
            scaleName = "Not found";
        }
        
        String output = "     --- Table: " + name + " ---" +
                "\n      - Description: " + description +
                "\n      - Scale: " + scaleName +
                "\n      - Address: " + address +
                "\n      - Userlevel: " + userLevel;
                
        return output;
    }
    
}