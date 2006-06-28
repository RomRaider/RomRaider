package Enginuity.Maps;

import Enginuity.SwingComponents.TableFrame;

public class Table2D extends Table {
    
    private Table1D axis = new Table1D();
    
    public Table2D() {
        super();
    }

    public Table1D getAxis() {
        return axis;
    }

    public void setAxis(Table1D axis) {
        this.axis = axis;
    }
    
    public String toString() {
        return super.toString() + axis;
    }
    
    public void colorize() {
        super.colorize();
        axis.colorize();
    }

    public void setFrame(TableFrame frame) {
        this.frame = frame;
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
        this.colorize();     
    }
    
    public void increment() {
        super.increment();
        axis.increment();
    }

    public void decrement() {
        super.decrement();
        axis.decrement();
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
}