package enginuity.newmaps.ecudata;

public class SourceDefAxis extends Axis {
    
    private String[] values;
    
    public SourceDefAxis(String name) {
        super(name);
    }

    public String[] getValues() {
        return values;
    }

    public void setValues(String[] values) {
        this.values = values;
    }
    
}