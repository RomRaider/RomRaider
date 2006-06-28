package Enginuity.Maps;

import Enginuity.XML.RomAttributeParser;
import Enginuity.SwingComponents.TableFrame;
import Enginuity.SwingComponents.VTextIcon;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class Table3D extends Table {
    
    private Table1D      xAxis = new Table1D();
    private Table1D      yAxis = new Table1D();
    private DataCell[][] data = new DataCell[1][1];
    private boolean      swapXY = false;
    private boolean      flipX = false;
    private boolean      flipY = false;
    
    public Table3D() {
        super();
        verticalOverhead += 31;
        horizontalOverhead += 5;
    }

    public Table1D getXAxis() {
        return xAxis;
    }

    public void setXAxis(Table1D xAxis) {
        this.xAxis = xAxis;
    }

    public Table1D getYAxis() {
        return yAxis;
    }

    public void setYAxis(Table1D yAxis) {
        this.yAxis = yAxis;
    }

    public boolean isSwapXY() {
        return swapXY;
    }

    public void setSwapXY(boolean swapXY) {
        this.swapXY = swapXY;
    }

    public boolean getFlipX() {
        return flipX;
    }

    public void setFlipX(boolean flipX) {
        this.flipX = flipX;
    }

    public boolean getFlipY() {
        return flipY;
    }

    public void setFlipY(boolean flipY) {
        this.flipY = flipY;
    }
    
    public void setSizeX(int size) {
        data = new DataCell[size][data[0].length];
        centerLayout.setColumns(size + 1);
    }
            
    public int getSizeX() {
        return data.length;
    }    
    
    public void setSizeY(int size) {
        data = new DataCell[data.length][size];
        centerLayout.setRows(size + 1);
    }
            
    public int getSizeY() {
        return data[0].length;
    }   
    
    public void populateTable(byte[] input) throws NullPointerException, ArrayIndexOutOfBoundsException {
        // fill first empty cell        
        centerPanel.add(new JLabel());
        
        // populate axiis
        try {
            xAxis.populateTable(input);
            yAxis.populateTable(input);
        } catch (ArrayIndexOutOfBoundsException ex) {
            throw new ArrayIndexOutOfBoundsException();
        }
            
        for (int i = 0; i < xAxis.getDataSize(); i++) {
            centerPanel.add(xAxis.getDataCell(i));
        }  
        
        int offset = 0; 
        
        for (int x = 0; x < yAxis.getDataSize(); x++) {
            centerPanel.add(yAxis.getDataCell(x));
            for (int y = 0; y < xAxis.getDataSize(); y++) {
                data[y][x] = new DataCell(scale);
                data[y][x].setTable(this);
                try {
                    data[y][x].setBinValue(
                            RomAttributeParser.parseByteValue(input,
                                                              endian, 
                                                              storageAddress + offset * storageType,
                                                              storageType)); 
                } catch (ArrayIndexOutOfBoundsException ex) {
                    throw new ArrayIndexOutOfBoundsException();
                }
                
                centerPanel.add(data[y][x]);
                data[y][x].setXCoord(y);
                data[y][x].setYCoord(x);                
                data[y][x].setOriginalValue(data[y][x].getBinValue());
                offset++;
            }
        }
        this.colorize(); 
        
        GridLayout topLayout = new GridLayout(2,1);
        JPanel topPanel = new JPanel(topLayout);
        this.add(topPanel, BorderLayout.NORTH);
        topPanel.add(new JLabel(name + " (" + scale.getUnit() + ")", JLabel.CENTER), BorderLayout.NORTH);            
        topPanel.add(new JLabel(xAxis.getName() + " (" + xAxis.getScale().getUnit() + ")", JLabel.CENTER), BorderLayout.NORTH);
        JLabel yLabel = new JLabel();
        yLabel.setFont(new Font("Arial", Font.BOLD, 12));
        VTextIcon icon = new VTextIcon(yLabel, yAxis.getName() + " (" + yAxis.getScale().getUnit()+ ")", VTextIcon.ROTATE_LEFT);
        yLabel.setIcon(icon);
        this.add(yLabel, BorderLayout.WEST);
    }
    
    public void colorize() {
        if (!isStatic) {
            int high = 0;
            int low  = 999999999;
            for (int x = 0; x < data.length; x++) {
                for (int y = 0; y < data[0].length; y++) {
                    if (data[x][y].getBinValue() > high) {
                        high = data[x][y].getBinValue();
                    } else if (data[x][y].getBinValue() < low) {
                        low = data[x][y].getBinValue();
                    }
                }
            }
            for (int x = 0; x < data.length; x++) {
                for (int y = 0; y < data[0].length; y++) {
                    double scale = (double)(data[x][y].getBinValue() - low) / (high - low);
                    int g = (int)(255 - (255 - 140) * scale);
                    if (g > 255) g = 255;
                    data[x][y].setColor(new Color(255, g, 125));
                }
            }
        }
    }
    
    public void setFrame(TableFrame frame) {
        this.frame = frame;
        int height = verticalOverhead + cellHeight * data[0].length;
        int width = horizontalOverhead + data.length * cellWidth;
        if (height < minHeight) height = minHeight;
        if (width < minWidth) width = minWidth;        
        frame.setSize(width, height);
    }
    
    public String toString() {
        return super.toString() + 
                "\n   Flip X: " + flipX +
                "\n   Size X: " + data.length +
                "\n   Flip Y: " + flipY +
                "\n   Size Y: " + data[0].length +
                "\n   Swap X/Y: " + swapXY +
                xAxis + 
                yAxis;
    }    
    
    public void increment() {
        if (!isStatic) {
            for (int x = 0; x < this.getSizeX(); x++) {
                for (int y = 0; y < this.getSizeY(); y++) {
                    if (data[x][y].isSelected()) data[x][y].increment();
                }
            }
        }
        xAxis.increment();
        yAxis.increment();
    }

    public void decrement() {
        if (!isStatic) {
            for (int x = 0; x < this.getSizeX(); x++) {
                for (int y = 0; y < this.getSizeY(); y++) {
                    if (data[x][y].isSelected()) data[x][y].decrement();
                }
            }
        }
        xAxis.decrement();
        yAxis.decrement();
    }
    
    public void clearSelection() {
        xAxis.clearSelection(true);
        yAxis.clearSelection(true);
        for (int x = 0; x < this.getSizeX(); x++) {
            for (int y = 0; y < this.getSizeY(); y++) {
                data[x][y].setSelected(false);
            }
        }
    }    
    
    public void highlight(int xCoord, int yCoord) {
        if (highlight) {
            for (int x = 0; x < this.getSizeX(); x++) {
                for (int y = 0; y < this.getSizeY(); y++) {
                    if (((y >= highlightY && y <= yCoord) ||
                        (y <= highlightY && y >= yCoord)) &&
                        ((x >= highlightX && x <= xCoord) ||
                        (x <= highlightX && x >= xCoord))  ) {
                            data[x][y].setHighlighted(true);
                    } else {
                        data[x][y].setHighlighted(false);
                    }
                }
            }
        }
    }
    
    public void stopHighlight() {
        highlight = false;
        // loop through, selected and un-highlight
        for (int x = 0; x < this.getSizeX(); x++) {
            for (int y = 0; y < this.getSizeY(); y++) {
                if (data[x][y].isHighlighted()) {
                    data[x][y].setSelected(true);
                    data[x][y].setHighlighted(false);
                }
            }
        }
    } 
    
    public void setRevertPoint() {
        for (int x = 0; x < this.getSizeX(); x++) {
            for (int y = 0; y < this.getSizeY(); y++) {
                data[x][y].setOriginalValue(data[x][y].getBinValue());
            }
        }
        yAxis.setRevertPoint();
        xAxis.setRevertPoint();
    }
    
    public void undoAll() {
        for (int x = 0; x < this.getSizeX(); x++) {
            for (int y = 0; y < this.getSizeY(); y++) {
                data[x][y].setBinValue(data[x][y].getOriginalValue());
            }
        }
        yAxis.undoAll();
        xAxis.undoAll();
    }
    
    public void undoSelected() {
        for (int x = 0; x < this.getSizeX(); x++) {
            for (int y = 0; y < this.getSizeY(); y++) {
                if (data[x][y].isSelected()) data[x][y].setBinValue(data[x][y].getOriginalValue());
            }
        }
        yAxis.undoSelected();
        xAxis.undoSelected();
    }      
    
    
    public byte[] saveFile(byte[] binData) {
        binData = xAxis.saveFile(binData);
        binData = yAxis.saveFile(binData);
        int offset = 0; 
        
        for (int x = 0; x < yAxis.getDataSize(); x++) {
            for (int y = 0; y < xAxis.getDataSize(); y++) {
                byte[] output = RomAttributeParser.parseIntegerValue(data[y][x].getBinValue(), endian, storageType);
                for (int z = 0; z < storageType; z++) {
                    binData[offset * storageType + storageAddress] = output[z];
                    offset++;
                }
            }
        }
        return binData;
    }
}