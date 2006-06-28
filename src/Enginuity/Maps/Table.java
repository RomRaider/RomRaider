package Enginuity.Maps;

import Enginuity.XML.RomAttributeParser;
import Enginuity.SwingComponents.TableFrame;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public abstract class Table extends JPanel {
    
    public static final int ENDIAN_LITTLE= 0;
    public static final int ENDIAN_BIG   = 1;
    public static final int TABLE_1D     = 1;
    public static final int TABLE_2D     = 2;
    public static final int TABLE_3D     = 3;
    public static final int TABLE_X_AXIS = 4;
    public static final int TABLE_Y_AXIS = 5;
    
    protected String     name;
    protected int        type;
    protected String     category;
    protected String     description;
    protected Scale      scale = new Scale();
    protected int        storageAddress;
    protected int        storageType;//number of bytes per cell
    protected int        endian;
    protected boolean    flip;
    protected DataCell[] data = new DataCell[0];
    protected boolean    isStatic = false;
    protected BorderLayout borderLayout = new BorderLayout();
    protected GridLayout centerLayout = new GridLayout(1,1,1,1);
    protected JPanel     centerPanel = new JPanel(centerLayout);
    protected TableFrame frame;
    protected int verticalOverhead = 96;
    protected int horizontalOverhead = 2;
    protected int cellHeight = 23;
    protected int cellWidth = 45;
    protected int minHeight = 100;
    protected int minWidth = 200;
    protected Rom container;
    protected int highlightX;
    protected int highlightY;
    protected boolean highlight = false;
    protected Table axisParent;
    
    public Table() {
        this.setLayout(borderLayout);
        this.add(centerPanel, BorderLayout.CENTER);
        centerPanel.setVisible(true);
    }
    
    public DataCell[] getData() {
        return data;
    }
    
    public void setData(DataCell[] data) {
        this.data = data;
    }
    
    public void populateTable(byte[] input) throws ArrayIndexOutOfBoundsException {
        if (!isStatic) {
            for (int i = 0; i < data.length; i++) {
                if (data[i] == null) {
                    data[i] = new DataCell(scale);
                    data[i].setTable(this);
                    try {
                        data[i].setBinValue(RomAttributeParser.parseByteValue(input, endian, storageAddress + i * storageType, storageType));
                    } catch (ArrayIndexOutOfBoundsException ex) {
                        throw new ArrayIndexOutOfBoundsException();
                    }
                    centerPanel.add(data[i]);
                    data[i].setYCoord(i);
                    data[i].setOriginalValue(data[i].getBinValue());
                }
            }
        }
        try {
            this.add(new JLabel(name + " (" + scale.getUnit() + ")", JLabel.CENTER), BorderLayout.NORTH);
        } catch (NullPointerException e) {
            //need to deal with this later
        }
        this.colorize();
    }
    
    public int getType() {
        return type;
    }
    
    public DataCell getDataCell(int location) {
        return data[location];
    }
    
    public void setType(int type) {
        this.type = type;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Scale getScale() {
        return scale;
    }
    
    public void setScale(Scale scale) {
        this.scale = scale;
    }
    
    public int getStorageAddress() {
        return storageAddress;
    }
    
    public void setStorageAddress(int storageAddress) {
        this.storageAddress = storageAddress;
    }
    
    public int getStorageType() {
        return storageType;
    }
    
    public void setStorageType(int storageType) {
        this.storageType = storageType;
    }
    
    public int getEndian() {
        return endian;
    }
    
    public void setEndian(int endian) {
        this.endian = endian;
    }
    
    public void setDataSize(int size) {
        data = new DataCell[size];
    }
    
    public int getDataSize() {
        return data.length;
    }
    
    public boolean getFlip() {
        return flip;
    }
    
    public void setFlip(boolean flipY) {
        this.flip = flip;
    }
    public String toString() {
        String output = "\n   ---- Table " + name + " ----" +
                scale +
                "\n   Category: " + category +
                "\n   Type: " + type +
                "\n   Description: " + description +
                "\n   Storage Address: " + Integer.toHexString(storageAddress) +
                "\n   Storage Type: " + storageType +
                "\n   Endian: " + endian +
                "\n   Flip: " + flip +
                "\n   ---- End Table " + name + " ----";
        for (int i = 0; i < data.length; i++) {
            if (data[i] != null) {
                output = output + "\nData: " + data[i];
            }
        }
        
        return output;
    }
    
    public boolean isStatic() {
        return isStatic;
    }
    
    public void setIsStatic(boolean isStatic) {
        this.isStatic = isStatic;
    }
    
    public void addStaticDataCell(DataCell input) {
        if (isStatic) {
            for (int i = 0; i < data.length; i++) {
                if (data[i] == null) {
                    data[i] = input;
                    break;
                }
            }
        }
    }
    
    public void colorize() {
        if (!isStatic) {
            int high = 0;
            int low  = 999999999;
            for (int i = 0; i < data.length; i++) {
                if (data[i].getBinValue() > high) {
                    high = data[i].getBinValue();
                } 
                if (data[i].getBinValue() < low) {
                    low = data[i].getBinValue();
                }
            }
            for (int i = 0; i < data.length; i++) {
                int range = high - low;
                if (range == 0) range = 1;
                double scale = ((data[i].getBinValue() - low) / range);
                int g = (int)(255 - (255 - 140) * scale);
                data[i].setColor(new Color(255, g, 125));
            }
        }
    }
    
    public void setFrame(TableFrame frame) {
        this.frame = frame;
        int height = verticalOverhead + cellHeight;
        int width = horizontalOverhead + data.length * cellWidth;
        if (height < minHeight) height = minHeight;
        if (width < minWidth) width = minWidth;
        frame.setSize(width, height);
    }
    
    public TableFrame getFrame() {
        return frame;
    }
    
    public void increment() {
        if (!isStatic) {
            for (int i = 0; i < data.length; i++) {
                if (data[i].isSelected()) data[i].increment();
            }
        }
    }
    
    public void decrement() {
        if (!isStatic) {
            for (int i = 0; i < data.length; i++) {
                if (data[i].isSelected()) data[i].decrement();
            }
        }
    }
    
    public Rom getContainer() {
        return container;
    }
    
    public void setContainer(Rom container) {
        this.container = container;
    }
    
    public void clearSelection() {
        for (int i = 0; i < data.length; i++) {
            data[i].setSelected(false);
        }
    }
    
    public void startHighlight(int x, int y) {
        this.highlightY = y;
        this.highlightX = x;
        highlight = true;
        highlight(x, y);
    }
    
    public void highlight(int x, int y) {
        if (highlight) {
            for (int i = 0; i < data.length; i++) {
                if ((i >= highlightY && i <= y) || (i <= highlightY && i >= y)) {
                    data[i].setHighlighted(true);
                } else {
                    data[i].setHighlighted(false);
                }
            }
        }
    }
    
    public void stopHighlight() {
        highlight = false;
        // loop through, selected and un-highlight
        for (int i = 0; i < data.length; i++) {
            if (data[i].isHighlighted()) {
                data[i].setSelected(true);
                data[i].setHighlighted(false);
            }
        }
    }

    public Table getAxisParent() {
        return axisParent;
    }

    public void setAxisParent(Table axisParent) {
        this.axisParent = axisParent;
    }
    
    public void setRevertPoint() {
        if (!isStatic) {
            for (int i = 0; i < data.length; i++) {
                data[i].setOriginalValue(data[i].getBinValue());
            }
        }
    }
    
    public void undoAll() {
        if (!isStatic) {
            for (int i = 0; i < data.length; i++) {
                data[i].setBinValue(data[i].getOriginalValue());
            }
        }
    }
    
    public void undoSelected() {
        if (!isStatic) {
            for (int i = 0; i < data.length; i++) {
                if (data[i].isSelected()) data[i].setBinValue(data[i].getOriginalValue());
            }
        }
    }
    
    public byte[] saveFile(byte[] binData) {
        if (!isStatic) {
            for (int i = 0; i < data.length; i++) {
                // need to deal with storage type (num bytes)
                byte[] output = RomAttributeParser.parseIntegerValue(data[i].getBinValue(), endian, storageType);
                for (int z = 0; z < storageType; z++) {                    
                    binData[i * storageType + z + storageAddress] = output[z];
                }
            }
        }              
        return binData;
    }
}