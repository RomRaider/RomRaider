//This object defines the scaling factor and offset for calculating real values

package Enginuity.Maps;

import java.io.Serializable;

public class Scale implements Serializable {
    
    public static final int LINEAR  = 1;
    public static final int INVERSE = 2;
    
    private String unit;
    private String expression = "x";
    private String byteExpression = "x";
    private String format;
    private int    increment;
    
    public Scale() {
    }
    
    public String toString() {
       return "\n      ---- Scale ----" +
              "\n      Expression: " + getExpression() +
              "\n      Unit: " + getUnit() +
              "\n      ---- End Scale ----";
    
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    public int getIncrement() {
        return increment;
    }

    public void setIncrement(int increment) {
        this.increment = increment;
    }
    
    public boolean isReady() {
        if (unit == null) return false;
        else if (expression == null) return false;
        else if (format == null) return false;
        else if (increment < 1) return false;
        
        return true;
    }

    public String getByteExpression() {
        return byteExpression;
    }

    public void setByteExpression(String byteExpression) {
        this.byteExpression = byteExpression;
    }
}