package enginuity.newmaps.ecudata;

public class Unit {
    
    protected String name;
    protected String to_real;
    protected String to_byte;
    protected String format;
    protected float coarseIncrement;
    protected float fineIncrement;
    
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
    
}
