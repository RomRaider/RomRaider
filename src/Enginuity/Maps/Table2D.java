package Enginuity.Maps;

import Enginuity.SwingComponents.TableFrame;
import java.awt.BorderLayout;
import java.io.Serializable;
import javax.swing.JLabel;

public class Table2D extends Table implements Serializable {
    
    private Table1D axis = new Table1D();
    
    public Table2D() {
        super();
        verticalOverhead += 15;
    }

    public Table1D getAxis() {
        return axis;
    }

    public void setAxis(Table1D axis) {
        this.axis = axis;
    }
    
    public String toString() {
        return super.toString() + " (2D)";// + axis;
    }
    
    public void colorize() {
        super.colorize();
        axis.colorize();
    }

    public void setFrame(TableFrame frame) {
        this.frame = frame;
        axis.setFrame(frame);
        int height = verticalOverhead + cellHeight * 2;
        int width = horizontalOverhead + data.length * cellWidth;
        if (height < minHeight) height = minHeight;
        if (width < minWidth) width = minWidth;        
        frame.setSize(width, height);
    }
    
    public void populateTable(byte[] input) throws ArrayIndexOutOfBoundsException {
        centerLayout.setRows(2);
        centerLayout.setColumns(this.getDataSize());
        
        try {
            axis.setContainer(container);
            axis.populateTable(input);
            super.populateTable(input);
        } catch (ArrayIndexOutOfBoundsException ex) {
            throw new ArrayIndexOutOfBoundsException();
        }
        
        // add to table
        for (int i = 0; i < this.getDataSize(); i++) {
            centerPanel.add(axis.getDataCell(i));
        }
        for (int i = 0; i < this.getDataSize(); i++) {
            centerPanel.add(this.getDataCell(i));
        }   
        this.add(new JLabel(axis.getName() + " (" + axis.getScale().getUnit() + ")", JLabel.CENTER), BorderLayout.NORTH);
        this.add(new JLabel(name + " (" + scale.getUnit() + ")", JLabel.CENTER), BorderLayout.SOUTH);
        this.colorize();     
    }
    
    public void increment(int increment) {
        super.increment(increment);
        axis.increment(increment);
    }
    
    public void clearSelection() {
        axis.clearSelection(true);
        for (int i = 0; i < data.length; i++) {
            data[i].setSelected(false);
        }
    }
    
    public void setRevertPoint() {
        super.setRevertPoint();
        axis.setRevertPoint();
    }
    
    public void undoAll() {
        super.undoAll();
        axis.undoAll();
    }
    
    public void undoSelected() {
        super.undoSelected();
        axis.undoSelected();
    }      
    
    public byte[] saveFile(byte[] binData) {
        binData = super.saveFile(binData);
        binData = axis.saveFile(binData);
        return binData;
    }
       
    public void setRealValue(String realValue) {
        axis.setRealValue(realValue);
        super.setRealValue(realValue);
    }
}