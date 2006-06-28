//This object defines the scaling factor and offset for calculating real values

package Enginuity.Maps;

public class Scale {
    
    public static final int LINEAR  = 0;
    public static final int INVERSE = 1;
    
    private String unit;
    private String expression;
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
}