package Enginuity.Maps;

import java.awt.BorderLayout;
import java.awt.Color;
import java.io.Serializable;
import javax.swing.JLabel;

public class Table1D extends Table implements Serializable {
    
    private boolean isAxis = false;    
    
    public Table1D() {
        super();
    }
    
    public void populateTable(byte[] input) {
        centerLayout.setRows(1);
        centerLayout.setColumns(this.getDataSize());
        super.populateTable(input);
        
        // add to table
        for (int i = 0; i < this.getDataSize(); i++) {
            centerPanel.add(this.getDataCell(i));
        }
        this.add(new JLabel(name + " (" + scale.getUnit() + ")", JLabel.CENTER), BorderLayout.NORTH);
        this.colorize();
    }
    
    public String toString() {
        return super.toString() + " (1D)";
    }

    public boolean isIsAxis() {
        return isAxis;
    }

    public void setIsAxis(boolean isAxis) {
        this.isAxis = isAxis;
    }
    
    public void clearSelection() {
        super.clearSelection();
        //if (isAxis) axisParent.clearSelection();                
    }
    
    public void clearSelection(boolean calledByParent) {
        if (calledByParent) {
            super.clearSelection();           
        } else {
            this.clearSelection();
        }        
    }    
    
    public void colorize() {
        if (!isAxis) {
            super.colorize();
        } else {
            for (int i = 0; i < this.getDataSize(); i++) {
                data[i].setColor(Color.WHITE);
            }
        }
    }  
    
    public void cursorUp() { 
        if (type == Table.TABLE_Y_AXIS) {
            if (highlightY > 0 && data[highlightY].isSelected()) selectCellAt(highlightY - 1);
        } else if (type == Table.TABLE_X_AXIS) { 
            // Y axis is on top.. nothing happens
        } else if (type == Table.TABLE_1D) {
            // no where to move up to
        }
    }
    public void cursorDown() { 
        if (type == Table.TABLE_Y_AXIS) {
            if (highlightY < getDataSize() - 1 && data[highlightY].isSelected()) selectCellAt(highlightY + 1);
        } else if (type == Table.TABLE_X_AXIS && data[highlightY].isSelected()) { 
            ((Table3D)axisParent).selectCellAt(highlightY, this);
        } else if (type == Table.TABLE_1D) {
            // no where to move down to
        }    
    }
    
    public void cursorLeft() { 
        if (type == Table.TABLE_Y_AXIS) {
            // X axis is on left.. nothing happens
        } else if (type == Table.TABLE_X_AXIS && data[highlightY].isSelected()) { 
            if (highlightY > 0) selectCellAt(highlightY - 1);
        } else if (type == Table.TABLE_1D && data[highlightY].isSelected()) {
            if (highlightY > 0) selectCellAt(highlightY - 1);
        }   
    }
    
    public void cursorRight() { 
        if (type == Table.TABLE_Y_AXIS && data[highlightY].isSelected()) {            
            ((Table3D)axisParent).selectCellAt(highlightY, this);
        } else if (type == Table.TABLE_X_AXIS && data[highlightY].isSelected()) { 
            if (highlightY < getDataSize() - 1) selectCellAt(highlightY + 1);
        } else if (type == Table.TABLE_1D && data[highlightY].isSelected()) {
            if (highlightY < getDataSize() - 1) selectCellAt(highlightY + 1);
        }     
    }     
    
    public void startHighlight(int x, int y) {
        if (isAxis) axisParent.clearSelection();
        super.startHighlight(x, y);
    }
    
    public StringBuffer getTableAsString() {
        StringBuffer output = new StringBuffer("");
        for (int i = 0; i < getDataSize(); i++) {
            output.append(data[i].getText());
            if (i < getDataSize() - 1) output.append("\t");
        }
        return output;
    }
    
    public String getCellAsString(int index) {
        return data[index].getText();    
    }
}