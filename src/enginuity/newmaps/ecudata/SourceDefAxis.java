package enginuity.newmaps.ecudata;

import static enginuity.newmaps.definition.AttributeParser.stringToStringArray;
import java.io.Serializable;

public class SourceDefAxis extends Axis implements Serializable {
    
    private String[] values;
    
    public SourceDefAxis(String name) {
        super(name);
    }

    public String[] getValues() {
        return values;
    }

    public void setValues(String s, String delim) {
        values = stringToStringArray(s, delim);
    }
    
}