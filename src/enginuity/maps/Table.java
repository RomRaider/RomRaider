package enginuity.maps;

import enginuity.Settings;
import enginuity.xml.RomAttributeParser;
import enginuity.swing.TableFrame;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.io.Serializable;
import java.util.StringTokenizer;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.InputMap;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.border.LineBorder;
import org.nfunk.jep.JEP;

public abstract class Table extends JPanel implements Serializable {
    
    public static final int ENDIAN_LITTLE= 1;
    public static final int ENDIAN_BIG   = 2;
    
    public static final int TABLE_1D     = 1;
    public static final int TABLE_2D     = 2;
    public static final int TABLE_3D     = 3;
    public static final int TABLE_X_AXIS = 4;
    public static final int TABLE_Y_AXIS = 5;
    public static final int TABLE_SWITCH = 6;
    
    public static final int COMPARE_OFF      = 0;
    public static final int COMPARE_ORIGINAL = 1;
    public static final int COMPARE_TABLE    = 2;
    public static final int COMPARE_PERCENT  = 0;
    public static final int COMPARE_ABSOLUTE = 1;
    
    public static final int STORAGE_TYPE_FLOAT = 99;
    
    protected String       name;
    protected int          type;
    protected String       category    = "Other";
    protected String       description  = "";
    protected Scale        scale        = new Scale();
    protected int          storageAddress;
    protected int          storageType;
    protected int          endian;
    protected boolean      flip;
    protected DataCell[]   data         = new DataCell[0];
    protected boolean      isStatic     = false;
    protected boolean      beforeRam    = false;
    protected int          ramOffset    = 0;
    protected BorderLayout borderLayout = new BorderLayout();
    protected GridLayout   centerLayout = new GridLayout(1,1,0,0);
    protected JPanel       centerPanel  = new JPanel(centerLayout);
    protected TableFrame   frame;
    protected int          verticalOverhead   = 103;
    protected int          horizontalOverhead = 2;
    protected int          cellHeight         = 18;
    protected int          cellWidth          = 42;
    protected int          minHeight          = 100;
    protected int          minWidth           = 370;
    protected Rom          container;
    protected int          highlightX;
    protected int          highlightY;
    protected boolean      highlight = false;
    protected Table        axisParent;   
    protected Color        maxColor;
    protected Color        minColor;
    protected boolean      isAxis         = false;    
    protected int          compareType    = 0;
    protected int          compareDisplay = 1;
    protected int          userLevel      = 0;
    
    public Table() {
        this.setLayout(borderLayout);
        this.add(centerPanel, BorderLayout.CENTER);
        centerPanel.setVisible(true);
                
        // key binding actions
        Action rightAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                cursorRight();
            }
        };            
        Action leftAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                cursorLeft();
            }
        }; 
        Action downAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                cursorDown();
            }
        };  
        Action upAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                cursorUp();
            }
        };  
        Action incCoarseAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {                
                frame.getToolBar().incrementCoarse();                
            }
        };  
        Action decCoarseAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                frame.getToolBar().decrementCoarse();
            }
        }; 
        Action incFineAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                frame.getToolBar().incrementFine();
            }
        }; 
        Action decFineAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                frame.getToolBar().decrementFine();
            }
        };  
        Action num0Action = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                getFrame().getToolBar().focusSetValue('0');
            }
        };  
        Action num1Action = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                getFrame().getToolBar().focusSetValue('1');
            }
        };  
        Action num2Action = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                getFrame().getToolBar().focusSetValue('2');
            }
        };  
        Action num3Action = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                getFrame().getToolBar().focusSetValue('3');
            }
        };  
        Action num4Action = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                getFrame().getToolBar().focusSetValue('4');
            }
        };  
        Action num5Action = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                getFrame().getToolBar().focusSetValue('5');
            }
        };  
        Action num6Action = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                getFrame().getToolBar().focusSetValue('6');
            }
        };  
        Action num7Action = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                getFrame().getToolBar().focusSetValue('7');
            }
        };  
        Action num8Action = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                getFrame().getToolBar().focusSetValue('8');
            }
        };  
        Action num9Action = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                getFrame().getToolBar().focusSetValue('9');
            }
        }; 
        Action numPointAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                getFrame().getToolBar().focusSetValue('.');
            }
        };      
        Action copyAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                copySelection();
            }
        }; 
        Action pasteAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                paste();
            }
        };   
        
        // set input mapping
        InputMap im = getInputMap(this.WHEN_IN_FOCUSED_WINDOW);
        
        KeyStroke right = KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0);
        KeyStroke left = KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0);
        KeyStroke up = KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0);
        KeyStroke down = KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0);
        KeyStroke decrement = KeyStroke.getKeyStroke('-');
        KeyStroke increment = KeyStroke.getKeyStroke('+');
        KeyStroke decrement2 = KeyStroke.getKeyStroke("control DOWN");
        KeyStroke increment2 = KeyStroke.getKeyStroke("control UP");
        KeyStroke decrement3 = KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, KeyEvent.CTRL_DOWN_MASK);
        KeyStroke increment3 = KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, KeyEvent.CTRL_DOWN_MASK);
        KeyStroke num0 = KeyStroke.getKeyStroke('0');
        KeyStroke num1 = KeyStroke.getKeyStroke('1');
        KeyStroke num2 = KeyStroke.getKeyStroke('2');
        KeyStroke num3 = KeyStroke.getKeyStroke('3');
        KeyStroke num4 = KeyStroke.getKeyStroke('4');
        KeyStroke num5 = KeyStroke.getKeyStroke('5');
        KeyStroke num6 = KeyStroke.getKeyStroke('6');
        KeyStroke num7 = KeyStroke.getKeyStroke('7');
        KeyStroke num8 = KeyStroke.getKeyStroke('8');
        KeyStroke num9 = KeyStroke.getKeyStroke('9');
        KeyStroke numPoint = KeyStroke.getKeyStroke('.');
        KeyStroke copy = KeyStroke.getKeyStroke("control C");
        KeyStroke paste = KeyStroke.getKeyStroke("control V");
        
        im.put(right, "right");     
        im.put(left, "left"); 
        im.put(up, "up");
        im.put(down, "down");
        im.put(increment, "incCoarseAction");
        im.put(decrement, "decCoarseAction");
        im.put(increment2, "incCoarseAction");
        im.put(decrement2, "decCoarseAction");
        im.put(increment3, "incFineAction");
        im.put(decrement3, "decFineAction");
        im.put(num0, "num0Action");
        im.put(num1, "num1Action");
        im.put(num2, "num2Action");
        im.put(num3, "num3Action");
        im.put(num4, "num4Action");
        im.put(num5, "num5Action");
        im.put(num6, "num6Action");
        im.put(num7, "num7Action");
        im.put(num8, "num8Action");
        im.put(num9, "num9Action");
        im.put(numPoint, "numPointAction");
        im.put(copy, "copyAction");
        im.put(paste, "pasteAction");
        
        getActionMap().put(im.get(right), rightAction);     
        getActionMap().put(im.get(left), leftAction);       
        getActionMap().put(im.get(up), upAction);           
        getActionMap().put(im.get(down), downAction); 
        getActionMap().put(im.get(increment), incCoarseAction);
        getActionMap().put(im.get(decrement), decCoarseAction); 
        getActionMap().put(im.get(increment2), incCoarseAction);
        getActionMap().put(im.get(decrement2), decCoarseAction);  
        getActionMap().put(im.get(increment3), incFineAction);
        getActionMap().put(im.get(decrement3), decFineAction);       
        getActionMap().put(im.get(num0), num0Action);
        getActionMap().put(im.get(num1), num1Action);
        getActionMap().put(im.get(num2), num2Action);
        getActionMap().put(im.get(num3), num3Action);
        getActionMap().put(im.get(num4), num4Action);
        getActionMap().put(im.get(num5), num5Action);
        getActionMap().put(im.get(num6), num6Action);
        getActionMap().put(im.get(num7), num7Action);
        getActionMap().put(im.get(num8), num8Action);
        getActionMap().put(im.get(num9), num9Action);
        getActionMap().put(im.get(numPoint), numPointAction);
        getActionMap().put(im.get(copy), copyAction);
        getActionMap().put(im.get(paste), pasteAction);
        
        this.setInputMap(this.WHEN_FOCUSED, im);
    }
    
    public DataCell[] getData() {
        return data;
    }
    
    public void setData(DataCell[] data) {
        this.data = data;
    }
    
    public void populateTable(byte[] input) throws ArrayIndexOutOfBoundsException {
        if (!isStatic) {
            if (!beforeRam) ramOffset = container.getRomID().getRamOffset();
            
            for (int i = 0; i < data.length; i++) {
                if (data[i] == null) {
                    data[i] = new DataCell(scale);
                    data[i].setTable(this);
                    
                    // populate data cells
                    if (storageType == STORAGE_TYPE_FLOAT) { //float storage type
                        byte[] byteValue = new byte[4];
                        byteValue[0] = input[storageAddress + i * 4 - ramOffset];
                        byteValue[1] = input[storageAddress + i * 4 - ramOffset + 1];
                        byteValue[2] = input[storageAddress + i * 4 - ramOffset + 2];
                        byteValue[3] = input[storageAddress + i * 4 - ramOffset + 3];
                        data[i].setBinValue(RomAttributeParser.byteToFloat(byteValue, endian));
                        
                    } else { // integer storage type            
                        data[i].setBinValue(
                                RomAttributeParser.parseByteValue(input,
                                                                  endian, 
                                                                  storageAddress + i * storageType - ramOffset,
                                                                  storageType)); 
                    }
                    
                    data[i].setPreferredSize(new Dimension(cellWidth, cellHeight));
                    centerPanel.add(data[i]);
                    data[i].setYCoord(i);
                    data[i].setOriginalValue(data[i].getBinValue());
                }
            }
        }
       //this.colorize();
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
        this.flip = flipY;
    }
    public String toString() {
        /*String output = "\n   ---- Table " + name + " ----" +
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
        
        return output;*/
        return name;
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
        if (compareType == COMPARE_OFF) {
            if (!isStatic && !isAxis) {
                double high = -999999999;
                double low  = 999999999;

                for (int i = 0; i < getDataSize(); i++) {
                    if (data[i].getBinValue() > high) {
                        high = data[i].getBinValue();
                    } 
                    if (data[i].getBinValue() < low) {
                        low = data[i].getBinValue();
                    }
                }
                for (int i = 0; i < getDataSize(); i++) {
                    double scale = (double)(data[i].getBinValue() - low) / (high - low);
                    int r = (int)((maxColor.getRed() - minColor.getRed()) * scale) + minColor.getRed();
                    int g = (int)((maxColor.getGreen() - minColor.getGreen()) * scale) + minColor.getGreen();
                    int b = (int)((maxColor.getBlue() - minColor.getBlue()) * scale) + minColor.getBlue();

                    data[i].setColor(new Color(r, g, b));
                }
            } else { // is static/axis
                for (int i = 0; i < getDataSize(); i++) {
                    data[i].setColor(axisParent.getRom().getContainer().getSettings().getAxisColor());
                    data[i].setOpaque(true);
                    data[i].setBorder(new LineBorder(Color.BLACK, 1));
                    data[i].setHorizontalAlignment(data[i].CENTER);
                }
            }  
            
        } else { // comparing is on
            if (!isStatic) {
                double high = -999999999;
                
                // determine ratios
                for (int i = 0; i < getDataSize(); i++) {
                    if (Math.abs(data[i].getBinValue() - data[i].getOriginalValue()) > high) {
                        high = Math.abs(data[i].getBinValue() - data[i].getOriginalValue());
                    } 
                }
                
                // colorize
                for (int i = 0; i < getDataSize(); i++) {
                    double cellDifference = Math.abs(data[i].getBinValue() - data[i].getOriginalValue());
                    double scale;
                    if (high == 0) scale = 0;
                    else scale = (double)cellDifference / (double)high;
                    
                    int r = (int)((maxColor.getRed() - minColor.getRed()) * scale) + minColor.getRed();
                    int g = (int)((maxColor.getGreen() - minColor.getGreen()) * scale) + minColor.getGreen();
                    int b = (int)((maxColor.getBlue() - minColor.getBlue()) * scale) + minColor.getBlue();
                    if (r > 255) r = 255;
                    if (g > 255) g = 255;
                    if (b > 255) b = 255;
                    if (r < 0) r = 0;
                    if (g < 0) g = 0;
                    if (b < 0) b = 0;

                    if (scale != 0) data[i].setColor(new Color(r, g, b));
                    else data[i].setColor(new Color(160,160,160));

                    // set border
                    if (data[i].getBinValue() > data[i].getOriginalValue()) {
                        data[i].setBorder(new LineBorder(getRom().getContainer().getSettings().getIncreaseBorder()));
                    } else if (data[i].getBinValue() < data[i].getOriginalValue()) {
                        data[i].setBorder(new LineBorder(getRom().getContainer().getSettings().getDecreaseBorder()));
                    } else {
                        data[i].setBorder(new LineBorder(Color.BLACK, 1));
                    }
                }    
            }
        }  
        
        // colorize border
        if (!isStatic) {
            for (int i = 0; i < getDataSize(); i++) {
                if (data[i].getBinValue() > data[i].getOriginalValue()) {
                    data[i].setBorder(new LineBorder(getRom().getContainer().getSettings().getIncreaseBorder()));
                } else if (data[i].getBinValue() < data[i].getOriginalValue()) {
                    data[i].setBorder(new LineBorder(getRom().getContainer().getSettings().getDecreaseBorder()));
                } else {
                    data[i].setBorder(new LineBorder(Color.BLACK, 1));
                }        
            }
        }
    }
    
    public void setFrame(TableFrame frame) {
        this.frame = frame;
        frame.setSize(getFrameSize());
    }
    
    public Dimension getFrameSize() {
        int height = verticalOverhead + cellHeight;
        int width = horizontalOverhead + data.length * cellWidth;
        if (height < minHeight) height = minHeight;
        if (width < minWidth) width = minWidth;
        return new Dimension(width, height);
    }
    
    public TableFrame getFrame() {
        return frame;
    }
    
    public void increment(double increment) {
        if (!isStatic) {
            for (int i = 0; i < data.length; i++) {
                if (data[i].isSelected()) data[i].increment(increment);
            }
        }
    }
       
    public void setRealValue(String realValue) {
        if (!isStatic) {
            for (int i = 0; i < data.length; i++) {
                if (data[i].isSelected()) data[i].setRealValue(realValue);
            }
        }
        colorize();
    }
    
    public Rom getRom() {
        return container;
    }
    
    public void setRom(Rom container) {
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
    
    public abstract void cursorUp();
    public abstract void cursorDown();    
    public abstract void cursorLeft();    
    public abstract void cursorRight();
    
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
        colorize();
    }
    
    public void undoAll() {
        if (!isStatic) {
            for (int i = 0; i < data.length; i++) {
                data[i].setBinValue(data[i].getOriginalValue());
            }
        }
        colorize();
    }
    
    public void undoSelected() {
        if (!isStatic) {
            for (int i = 0; i < data.length; i++) {
                // reset current value to original value
                if (data[i].isSelected()) data[i].setBinValue(data[i].getOriginalValue());
            }
        }
        colorize();
    }
    
    public byte[] saveFile(byte[] binData) {
        if (!isStatic) {
            for (int i = 0; i < data.length; i++) {
                
                // determine output byte values
                byte[] output;
                if (storageType != STORAGE_TYPE_FLOAT) {
                    // calculate byte values
                    output = RomAttributeParser.parseIntegerValue((int)data[i].getBinValue(), endian, storageType);
                    for (int z = 0; z < storageType; z++) { // insert into file      
                        binData[i * storageType + z + storageAddress - ramOffset] = output[z];
                    }
                    
                } else { // float
                    // calculate byte values
                    output = RomAttributeParser.floatToByte((float)data[i].getBinValue(), endian);
                    for (int z = 0; z < 4; z++) { // insert in to file
                        binData[i * 4 + z + storageAddress - ramOffset] = output[z];
                    }      
                }
            }
        }              
        return binData;
    }

    public boolean isBeforeRam() {
        return beforeRam;
    }

    public void setBeforeRam(boolean beforeRam) {
        this.beforeRam = beforeRam;
    }
   
    public void addKeyListener(KeyListener listener) {
        super.addKeyListener(listener);
        for (int i = 0; i < data.length; i++) {
            
            // determine output byte values
            byte[] output;
            if (storageType != STORAGE_TYPE_FLOAT) {
                output = RomAttributeParser.parseIntegerValue((int)data[i].getBinValue(), endian, storageType);
            } else { // float
                output = RomAttributeParser.floatToByte((float)data[i].getBinValue(), endian);
            }
            
            for (int z = 0; z < storageType; z++) {                    
                data[i].addKeyListener(listener);
            }
        }        
    }    
    
    public void selectCellAt(int y) {
        if (type == TABLE_X_AXIS || type == TABLE_Y_AXIS) axisParent.clearSelection();
        else clearSelection();
        data[y].setSelected(true);
        highlightY = y;
    }   
    
    public void copySelection() { 
        // find bounds of selection
        // coords[0] = x min, y min, x max, y max
        String newline = System.getProperty("line.separator");
        String output ="[Selection1D]" + newline;
        boolean copy = false;
        int[] coords = new int[2];
        coords[0] = this.getDataSize();
        
        for (int i = 0; i < this.getDataSize(); i++) {
            if (data[i].isSelected()) {
                if (i < coords[0]) {
                    coords[0] = i;
                    copy = true;
                }
                if (i > coords[1]) {
                    coords[1] = i;                        
                    copy = true;
                }
            }
        }        
        //make a string of the selection
        for (int i = coords[0]; i <= coords[1]; i++) {
            if (data[i].isSelected()) output = output + data[i].getText();
            else output = output + "x"; // x represents non-selected cell
            if (i < coords[1]) output = output + "\t";
        }
        //copy to clipboard
        if (copy) Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(output), null);        
    }
    
    public StringBuffer getTableAsString() {
        //make a string of the selection
        StringBuffer output = new StringBuffer("");
        for (int i = 0; i < getDataSize(); i++) {
            output.append(data[i].getText());
            if (i < getDataSize() - 1) output.append("\t");
        }      
        return output;
    }
    
    public void copyTable() {
        String newline = System.getProperty("line.separator");
        StringBuffer output = new StringBuffer("[Table1D]" + newline);
        for (int i = 0; i < getDataSize(); i++) {
            output.append(data[i].getText());
            if (i < getDataSize() - 1) output.append("\t");
        }
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(output+""), null);    
    }
    
    public String getCellAsString(int index) {
        return data[index].getText();
    }
    
    public void pasteValues(String[] input) {
        //set real values
        for (int i = 0; i < input.length; i++) {            
            try {
                Double.parseDouble(input[i]);
                data[i].setRealValue(input[i]);
            } catch (NumberFormatException ex) { /* not a number, do nothing */ } 
        }
    }
    
    public void paste() {
        StringTokenizer st = new StringTokenizer("");
        try {
            String input = (String)Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null).getTransferData(DataFlavor.stringFlavor);
            st = new StringTokenizer(input);
        } catch (UnsupportedFlavorException ex) { /* wrong paste type -- do nothing */ 
        } catch (IOException ex) { }
        
        String pasteType = st.nextToken();

        if (pasteType.equalsIgnoreCase("[Table1D]")) { // copied entire table
            int i = 0;
            while (st.hasMoreTokens()) {
                String currentToken = st.nextToken();
                try {
                    if (!data[i].getText().equalsIgnoreCase(currentToken)) data[i].setRealValue(currentToken);
                } catch (ArrayIndexOutOfBoundsException ex) { /* table larger than target, ignore*/ }
                i++; 
            }            
        } else if (pasteType.equalsIgnoreCase("[Selection1D]")) { // copied selection
            if (data[highlightY].isSelected()) {
                int i = 0;
                while (st.hasMoreTokens()) {  
                    try {
                        data[highlightY + i].setRealValue(st.nextToken());
                    } catch (ArrayIndexOutOfBoundsException ex) { /* paste larger than target, ignore */ }
                    i++;
                }            
            }
        }         
    }   
    
    public void pasteCompare() {
        StringTokenizer st = new StringTokenizer("");
        try {
            String input = (String)Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null).getTransferData(DataFlavor.stringFlavor);
            st = new StringTokenizer(input);
        } catch (UnsupportedFlavorException ex) { /* wrong paste type -- do nothing */ 
        } catch (IOException ex) { }
        
        String pasteType = st.nextToken();

        if (pasteType.equalsIgnoreCase("[Table1D]")) { // copied entire table
            int i = 0;
            while (st.hasMoreTokens()) {
                String currentToken = st.nextToken();
                try {
                    if (!data[i].getText().equalsIgnoreCase(currentToken)) data[i].setCompareRealValue(currentToken);
                } catch (ArrayIndexOutOfBoundsException ex) { /* table larger than target, ignore*/ }
                i++; 
            }        
        }
    }
    
    public void finalize(Settings settings) {
        // apply settings to cells
        for (int i = 0; i < getDataSize(); i++) {
            this.setMaxColor(settings.getMaxColor());
            this.setMinColor(settings.getMinColor());
            data[i].setHighlightColor(settings.getHighlightColor());
            data[i].setIncreaseBorder(settings.getIncreaseBorder());
            data[i].setDecreaseBorder(settings.getDecreaseBorder());
            data[i].setFont(settings.getTableFont());
            data[i].repaint();
        }
        cellHeight = (int)settings.getCellSize().getHeight();
        cellWidth = (int)settings.getCellSize().getWidth();
        colorize();
        validateScaling();
    }
    
    public void resize() {
        frame.setSize(getFrameSize());        
    }

    public Color getMaxColor() {
        return maxColor;
    }

    public void setMaxColor(Color maxColor) {
        this.maxColor = maxColor;
    }

    public Color getMinColor() {
        return minColor;
    }

    public void setMinColor(Color minColor) {
        this.minColor = minColor;
    }
    
    public abstract void setAxisColor(Color color);
    
    public void validateScaling() {
        JEP parser = new JEP();
        parser.initSymTab(); // clear the contents of the symbol table
        parser.addVariable("x", 5);
        parser.parseExpression(scale.getExpression());
        double toReal = parser.getValue(); // calculate real world value of "5"
        
        parser.addVariable("x", toReal);
        parser.parseExpression(scale.getByteExpression());
        
        // if real to byte doesn't equal 5, report conflict
        //if (parser.getValue() != 5 && container.getContainer().getSettings().isCalcConflictWarning()) {
        if (Math.abs(parser.getValue() - 5) > .001) {
                        
            JPanel panel = new JPanel();
            panel.setLayout(new GridLayout(4, 1));
            panel.add(new JLabel("The real value and byte value conversion expressions for table " + name + " are invalid."));
            panel.add(new JLabel("To real value: " + scale.getExpression()));
            panel.add(new JLabel("To byte: " + scale.getByteExpression()));
            
            JCheckBox check = new JCheckBox("Always display this message", true);
            check.setHorizontalAlignment(JCheckBox.RIGHT);
            panel.add(check);
                        
            check.addActionListener( 
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        getRom().getContainer().getSettings().setCalcConflictWarning(((JCheckBox)e.getSource()).isSelected());            
                    }
                } 
            );            
            
            JOptionPane.showMessageDialog(container.getContainer(), panel,
                    "Warning", JOptionPane.ERROR_MESSAGE);
        }        
    }
    
    public void compare(int compareType) {
        this.compareType = compareType;
        
        for (int i = 0; i < getDataSize(); i++) {
            if (compareType == COMPARE_ORIGINAL) data[i].setCompareValue(data[i].getOriginalValue());  
            data[i].setCompareType(compareType);
            data[i].setCompareDisplay(compareDisplay);
            data[i].updateDisplayValue();
        }       
        colorize();
    }

    public void setCompareDisplay(int compareDisplay) {
        this.compareDisplay = compareDisplay;
        compare(compareType);
        colorize();
    }
    
    /**
     * Help the GC clean things up.
     */
    public void finalize() {
		try {
			super.finalize();
			data = null;
			container = null;
		}
		catch (Throwable t) {}
	}

    public int getUserLevel() {
        return userLevel;
    }

    public void setUserLevel(int userLevel) {
        this.userLevel = userLevel;
        if (userLevel > 5) userLevel = 5;
        else if (userLevel < 1) userLevel = 1;
    }
}