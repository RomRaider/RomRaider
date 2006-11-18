package enginuity.newmaps.ecudata;

import enginuity.util.Nameable;

public class Scale implements Nameable {
    
    public static final int ENDIAN_BIG = 0;
    public static final int ENDIAN_LITTLE = 1;
    
    public static final int STORAGE_TYPE_INT8 = 0;
    public static final int STORAGE_TYPE_UINT8 = 1;
    public static final int STORAGE_TYPE_INT16 = 2;
    public static final int STORAGE_TYPE_UINT16 = 3;
    public static final int STORAGE_TYPE_FLOAT = 4;
    public static final int STORAGE_TYPE_HEX = 5;
    public static final int STORAGE_TYPE_CHAR = 6;
    
    protected String description;
    protected Unit[] units;    
    protected int storageType;
    protected int endian;
    protected String logParam;
    protected int selectedUnit;
    protected String name;
    
    // Disallow default constructor
    private Scale() { }
    
    public Scale(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getStorageType() {
        return storageType;
    }

    public void setStorageType(int storageType) {
        this.storageType = storageType;
    }

    public int getEndian() {
        return endian;
    }

    public void setEndian(int endian) {
        this.endian = endian;
    }

    public String getLogParam() {
        return logParam;
    }

    public void setLogParam(String logParam) {
        this.logParam = logParam;
    }
  
    public Unit getUnit() {
        return getUnits()[selectedUnit];
    }    

    public Unit[] getUnits() {
        return units;
    }

    public void setUnits(Unit[] units) {
        this.units = units;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
    
    public String toString() {
        String output = "     --- SCALE: " + name + " ---" +
                "\n      - Description: " + description +
                "\n      - Storage Type: " + storageType +
                "\n      - Endian: " + endian + 
                "\n      - Log Param: " + logParam;
        return output;
    }
    
}