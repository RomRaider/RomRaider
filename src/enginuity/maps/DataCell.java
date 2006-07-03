package enginuity.maps;

import com.sun.corba.se.spi.activation._ActivatorImplBase;
import enginuity.maps.Scale;
import enginuity.maps.Table;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.Serializable;
import java.text.DecimalFormat;
import javax.swing.JLabel;
import javax.swing.border.LineBorder;
import org.nfunk.jep.*;

public class DataCell extends JLabel implements MouseListener, Serializable {
    
    private double  binValue       = 0;
    private double  originalValue  = 0;
    private Scale   scale          = new Scale();
    private String  displayValue   = "";
    private Color   scaledColor    = new Color(0,0,0);
    private Color   highlightColor = new Color(155,155,255);
    private Color   increaseBorder = Color.RED;
    private Color   decreaseBorder = Color.BLUE;
    private Boolean selected       = false;
    private Boolean highlighted    = false;
    private Table   table;
    private int     x = 0;
    private int     y = 0;
    private double  compareValue   = 0;
    private int     compareType    = Table.COMPARE_OFF;
    private int     compareDisplay = Table.COMPARE_ABSOLUTE;
    
    public DataCell() { }
    
    public DataCell(Scale scale) {
        this.scale = scale;
        this.setHorizontalAlignment(CENTER);
        this.setVerticalAlignment(CENTER);
        this.setFont(new Font("Arial", Font.BOLD, 12));
        this.setBorder(new LineBorder(Color.BLACK, 1));
        this.setOpaque(true);
        this.setVisible(true);
        this.addMouseListener(this);
    }
    
    public void updateDisplayValue() {
        DecimalFormat formatter = new DecimalFormat(scale.getFormat());
        
        if (getCompareType() == Table.COMPARE_OFF) {
            displayValue = formatter.format(calcDisplayValue(binValue, table.getScale().getExpression()));
            
        } else {
            if (getCompareDisplay() == Table.COMPARE_ABSOLUTE) {
                displayValue = formatter.format(
                            calcDisplayValue(binValue, table.getScale().getExpression()) -
                            calcDisplayValue(compareValue, table.getScale().getExpression()));    
                
            } else if (getCompareDisplay() == Table.COMPARE_PERCENT) {
                double difference = calcDisplayValue(binValue, table.getScale().getExpression()) - 
                                    calcDisplayValue(compareValue, table.getScale().getExpression()); 
                if (difference == 0) displayValue = "0%";
                else displayValue = (int)(difference / calcDisplayValue(binValue, table.getScale().getExpression()) * 100)+"%";
            }
        }
        setText(displayValue);
    }
    
    public double calcDisplayValue(double input, String expression) {
        JEP parser = new JEP();
        parser.initSymTab(); // clear the contents of the symbol table
        parser.addVariable("x", input);
        parser.parseExpression(expression);
        return parser.getValue();
    }
    
    public void setColor(Color color) {
        scaledColor = color;
        if (!selected) super.setBackground(color);
    }
    
    public void setDisplayValue(String displayValue) {
        this.displayValue = displayValue;
        this.setText(displayValue);
    }
    
    public void setBinValue(double binValue) {
        this.binValue = binValue;
        
        // make sure it's in range
        if (table.getStorageType() != table.STORAGE_TYPE_FLOAT) {
            if (binValue < 0) {
                this.setBinValue(0);
            } else if (binValue > Math.pow(256, table.getStorageType()) - 1) {
                this.setBinValue((int)(Math.pow(256, table.getStorageType()) - 1));
            }
            binValue = Math.round(binValue);
        }
        
        this.updateDisplayValue();
    }
    
    public double getBinValue() {
        return binValue;
    }
    
    public String toString() {
        return displayValue;
    }
    
    public Boolean isSelected() {
        return selected;
    }
    
    public void setSelected(Boolean selected) {
        this.selected = selected;
        if (selected) {
            this.setBackground(getHighlightColor());
            table.getFrame().getToolBar().setCoarseValue(Math.abs(table.getScale().getIncrement()));
        } else {
            this.setBackground(scaledColor);
        }
        requestFocus();
    }
    
    public void setHighlighted(Boolean highlighted) {
        if (!table.isStatic()) {
            this.highlighted = highlighted;
            if (highlighted) {
                this.setBackground(getHighlightColor());
            } else {
                if (!selected) this.setBackground(scaledColor);
            }
        }
    }
    
    public boolean isHighlighted() {
        return highlighted;
    }
    
    public void mouseEntered(MouseEvent e) {
        table.highlight(x, y);
    }
    
    public void mousePressed(MouseEvent e) {
        if (!table.isStatic()) {
            if (!e.isControlDown()) table.clearSelection();
            table.startHighlight(x, y);
        }
        requestFocus();
    }
    
    public void mouseReleased(MouseEvent e) {
        if (!table.isStatic()) {
            table.stopHighlight();
        }
    }
    
    public void mouseClicked(MouseEvent e) { }
    public void mouseExited(MouseEvent e) { }
    
    public void increment(double increment) {
        if (table.getScale().getIncrement() < 0) increment = 0 - increment;
        //this.setBinValue(binValue + increment);
        setRealValue((Double.parseDouble(displayValue) + increment) + "");
        table.colorize();
    }
    
    public void setTable(Table table) {
        this.table = table;
    }
    
    public void setXCoord(int x) {
        this.x = x;
    }
    
    public void setYCoord(int y) {
        this.y = y;
    }
    
    public double getOriginalValue() {
        return originalValue;
    }
    
    public void setOriginalValue(double originalValue) {
        this.originalValue = originalValue;
        if (binValue != getOriginalValue()) {
            this.setBorder(new LineBorder(Color.RED, 3));
        } else {
            this.setBorder(new LineBorder(Color.BLACK, 1));
        }
    }
    
    public void undo() {
        this.setBinValue(originalValue);
    }
    
    public void setRevertPoint() {
        this.setOriginalValue(binValue);
    }
    
    public void setRealValue(String input) {
        // create parser
        if (!input.equalsIgnoreCase("x")) {
            JEP parser = new JEP();
            parser.initSymTab(); // clear the contents of the symbol table
            parser.addVariable("x", Double.parseDouble(input));
            parser.parseExpression(table.getScale().getByteExpression());
            if (table.getStorageType() == Table.STORAGE_TYPE_FLOAT) {
                this.setBinValue(parser.getValue());
            } else {
                this.setBinValue((int)Math.round(parser.getValue()));
            }
        }
    }
    
    public Color getHighlightColor() {
        return highlightColor;
    }
    
    public void setHighlightColor(Color highlightColor) {
        this.highlightColor = highlightColor;
    }

    public Color getIncreaseBorder() {
        return increaseBorder;
    }

    public void setIncreaseBorder(Color increaseBorder) {
        this.increaseBorder = increaseBorder;
    }

    public Color getDecreaseBorder() {
        return decreaseBorder;
    }

    public void setDecreaseBorder(Color decreaseBorder) {
        this.decreaseBorder = decreaseBorder;
    }

    public double getCompareValue() {
        return compareValue;
    }

    public void setCompareValue(double compareValue) {
        this.compareValue = compareValue;
    }

    public int getCompareType() {
        return compareType;
    }

    public void setCompareType(int compareType) {
        this.compareType = compareType;
    }

    public int getCompareDisplay() {
        return compareDisplay;
    }
    
    public void setCompareRealValue(String input) {
            JEP parser = new JEP();
            parser.initSymTab(); // clear the contents of the symbol table
            parser.addVariable("x", Double.parseDouble(input));
            parser.parseExpression(table.getScale().getByteExpression());
            this.setCompareValue((int)Math.round(parser.getValue()));   
    }

    public void setCompareDisplay(int compareDisplay) {
        this.compareDisplay = compareDisplay;
    }
}