package enginuity.newmaps.ecudata;

public class Unit {
    
    public static final int SYSTEM_STANDARD = 0;
    public static final int SYSTEM_METRIC = 1;
    public static final int SYSTEM_UNIVERSAL = 2;
    
    private String name;
    private String to_real;
    private String to_byte;
    private String format;
    private int system;
    private float coarseIncrement;
    private float fineIncrement;
    
    private Unit() { }
    
    public Unit(String name) {
        this.setName(name);
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getTo_real() {
        return to_real;
    }
    
    public void setTo_real(String to_real) {
        this.to_real = to_real;
    }
    
    public String getTo_byte() {
        return to_byte;
    }
    
    public void setTo_byte(String to_byte) {
        this.to_byte = to_byte;
    }
    
    public String getFormat() {
        return format;
    }
    
    public void setFormat(String format) {
        this.format = format;
    }
    
    public float getCoarseIncrement() {
        return coarseIncrement;
    }
    
    public void setCoarseIncrement(float coarseIncrement) {
        this.coarseIncrement = coarseIncrement;
    }
    
    public float getFineIncrement() {
        return fineIncrement;
    }
    
    public void setFineIncrement(float fineIncrement) {
        this.fineIncrement = fineIncrement;
    }

    public int getSystem() {
        return system;
    }

    public void setSystem(int system) {
        this.system = system;
    }
    
}
