package enginuity.newmaps.ecudata;

import static enginuity.newmaps.definition.AttributeParser.parseValueString;

public class SourceDefAxis extends Axis {
    
    private String[] values;
    
    public SourceDefAxis(String name) {
        super(name);
    }

    public String[] getValues() {
        return values;
    }

    public void setValues(String s, String delim) {
        values = parseValueString(s, delim);
    }
    
}