/*
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2012 RomRaider.com
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package com.romraider.maps;

import static com.romraider.util.ColorScaler.getScaledColor;
import static javax.swing.BorderFactory.createLineBorder;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.Window;
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
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.InputMap;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import com.romraider.Settings;
import com.romraider.editor.ecu.ECUEditor;
import com.romraider.swing.TableFrame;
import com.romraider.util.JEPUtil;
import com.romraider.xml.RomAttributeParser;


public abstract class Table extends JPanel implements Serializable {
    private static final long serialVersionUID = 6559256489995552645L;
    protected static final String BLANK = "";

    public static final int ENDIAN_LITTLE = 1;
    public static final int ENDIAN_BIG = 2;

    public static final int TABLE_1D = 1;
    public static final int TABLE_2D = 2;
    public static final int TABLE_3D = 3;
    public static final int TABLE_X_AXIS = 4;
    public static final int TABLE_Y_AXIS = 5;
    public static final int TABLE_SWITCH = 6;

    public static final int COMPARE_OFF = 0;
    public static final int COMPARE_ORIGINAL = 1;
    public static final int COMPARE_TABLE = 2;
    public static final int COMPARE_PERCENT = 0;
    public static final int COMPARE_ABSOLUTE = 1;

    public static final int STORAGE_TYPE_FLOAT = 99;
    public static final boolean STORAGE_DATA_SIGNED = false;

    protected static final Color UNCHANGED_VALUE_COLOR = new Color(160, 160, 160);
    protected static final String NEW_LINE = System.getProperty("line.separator");
    protected static final String TAB = "\t";

    protected String name;
    protected int type;
    protected String category = "Other";
    protected String description = BLANK;
    protected Vector<Scale> scales = new Vector<Scale>();
    protected int scaleIndex = 0; // index of selected scale

    protected int storageAddress;
    protected int storageType;
    protected boolean signed;
    protected int endian;
    protected boolean flip;
    protected DataCell[] data = new DataCell[0];
    protected boolean isStatic = false;
    protected boolean beforeRam = false;
    protected int ramOffset = 0;
    protected BorderLayout borderLayout = new BorderLayout();
    protected GridLayout centerLayout = new GridLayout(1, 1, 0, 0);
    protected JPanel centerPanel = new JPanel(centerLayout);
    protected TableFrame frame;
    protected int verticalOverhead = 103;
    protected int horizontalOverhead = 2;
    protected int cellHeight = 18;
    protected int cellWidth = 42;
    protected int minHeight = 100;
    protected int minWidthNoOverlay = 465;
    protected int minWidthOverlay = 700;
    protected Rom container;
    protected int highlightX;
    protected int highlightY;
    protected boolean highlight = false;
    protected Table axisParent;
    protected Color maxColor;
    protected Color minColor;
    protected boolean isAxis = false;
    protected int compareType = 0;
    protected int compareDisplay = 1;
    protected int userLevel = 0;
    protected ECUEditor editor;
    protected boolean locked = false;

    protected String logParam = BLANK;
    protected String liveValue = BLANK;
    protected boolean overlayLog = false;

    protected CopyTableWorker copyTableWorker;
    protected CopySelectionWorker copySelectionWorker;

    public Table(ECUEditor editor) {
        this.editor = editor;
        this.setLayout(borderLayout);
        this.add(centerPanel, BorderLayout.CENTER);
        centerPanel.setVisible(true);

        // key binding actions
        Action rightAction = new AbstractAction() {
            private static final long serialVersionUID = 1042884198300385041L;

            @Override
            public void actionPerformed(ActionEvent e) {
                cursorRight();
            }
        };
        Action leftAction = new AbstractAction() {
            private static final long serialVersionUID = -4970441255677214171L;

            @Override
            public void actionPerformed(ActionEvent e) {
                cursorLeft();
            }
        };
        Action downAction = new AbstractAction() {
            private static final long serialVersionUID = -7898502951121825984L;

            @Override
            public void actionPerformed(ActionEvent e) {
                cursorDown();
            }
        };
        Action upAction = new AbstractAction() {
            private static final long serialVersionUID = 6937621541727666631L;

            @Override
            public void actionPerformed(ActionEvent e) {
                cursorUp();
            }
        };
        Action incCoarseAction = new AbstractAction() {
            private static final long serialVersionUID = -8308522736529183148L;

            @Override
            public void actionPerformed(ActionEvent e) {
                frame.getToolBar().incrementCoarse();
            }
        };
        Action decCoarseAction = new AbstractAction() {
            private static final long serialVersionUID = -7407628920997400915L;

            @Override
            public void actionPerformed(ActionEvent e) {
                frame.getToolBar().decrementCoarse();
            }
        };
        Action incFineAction = new AbstractAction() {
            private static final long serialVersionUID = 7261463425941761433L;

            @Override
            public void actionPerformed(ActionEvent e) {
                frame.getToolBar().incrementFine();
            }
        };
        Action decFineAction = new AbstractAction() {
            private static final long serialVersionUID = 8929400237520608035L;

            @Override
            public void actionPerformed(ActionEvent e) {
                frame.getToolBar().decrementFine();
            }
        };
        Action num0Action = new AbstractAction() {
            private static final long serialVersionUID = -6310984176739090034L;

            @Override
            public void actionPerformed(ActionEvent e) {
                getFrame().getToolBar().focusSetValue('0');
            }
        };
        Action num1Action = new AbstractAction() {
            private static final long serialVersionUID = -6187220355403883499L;

            @Override
            public void actionPerformed(ActionEvent e) {
                getFrame().getToolBar().focusSetValue('1');
            }
        };
        Action num2Action = new AbstractAction() {
            private static final long serialVersionUID = -8745505977907325720L;

            @Override
            public void actionPerformed(ActionEvent e) {
                getFrame().getToolBar().focusSetValue('2');
            }
        };
        Action num3Action = new AbstractAction() {
            private static final long serialVersionUID = 4694872385823448942L;

            @Override
            public void actionPerformed(ActionEvent e) {
                getFrame().getToolBar().focusSetValue('3');
            }
        };
        Action num4Action = new AbstractAction() {
            private static final long serialVersionUID = 4005741329254221678L;

            @Override
            public void actionPerformed(ActionEvent e) {
                getFrame().getToolBar().focusSetValue('4');
            }
        };
        Action num5Action = new AbstractAction() {
            private static final long serialVersionUID = -5846094949106279884L;

            @Override
            public void actionPerformed(ActionEvent e) {
                getFrame().getToolBar().focusSetValue('5');
            }
        };
        Action num6Action = new AbstractAction() {
            private static final long serialVersionUID = -5338656374925334150L;

            @Override
            public void actionPerformed(ActionEvent e) {
                getFrame().getToolBar().focusSetValue('6');
            }
        };
        Action num7Action = new AbstractAction() {
            private static final long serialVersionUID = 1959983381590509303L;

            @Override
            public void actionPerformed(ActionEvent e) {
                getFrame().getToolBar().focusSetValue('7');
            }
        };
        Action num8Action = new AbstractAction() {
            private static final long serialVersionUID = 7442763278699460648L;

            @Override
            public void actionPerformed(ActionEvent e) {
                getFrame().getToolBar().focusSetValue('8');
            }
        };
        Action num9Action = new AbstractAction() {
            private static final long serialVersionUID = 7475171864584215094L;

            @Override
            public void actionPerformed(ActionEvent e) {
                getFrame().getToolBar().focusSetValue('9');
            }
        };
        Action numPointAction = new AbstractAction() {
            private static final long serialVersionUID = -4729135055857591830L;

            @Override
            public void actionPerformed(ActionEvent e) {
                getFrame().getToolBar().focusSetValue('.');
            }
        };
        Action copyAction = new AbstractAction() {
            private static final long serialVersionUID = -6978981449261938672L;

            @Override
            public void actionPerformed(ActionEvent e) {
                copySelection();
            }
        };
        Action pasteAction = new AbstractAction() {
            private static final long serialVersionUID = 2026817603236490899L;

            @Override
            public void actionPerformed(ActionEvent e) {
                paste();
            }
        };
        Action multiplyAction = new AbstractAction() {
            private static final long serialVersionUID = -2350912575392447149L;

            @Override
            public void actionPerformed(ActionEvent e) {
                getFrame().getToolBar().multiply();
            }
        };
        Action numNegAction = new AbstractAction() {
            private static final long serialVersionUID = -6346750245035640773L;

            @Override
            public void actionPerformed(ActionEvent e) {
                getFrame().getToolBar().focusSetValue('-');
            }
        };

        // set input mapping
        InputMap im = getInputMap(WHEN_IN_FOCUSED_WINDOW);

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
        KeyStroke decrement4 = KeyStroke.getKeyStroke("control shift DOWN");
        KeyStroke increment4 = KeyStroke.getKeyStroke("control shift UP");
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
        KeyStroke mulKey = KeyStroke.getKeyStroke('*');
        KeyStroke mulKeys = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, KeyEvent.CTRL_DOWN_MASK);
        KeyStroke numPoint = KeyStroke.getKeyStroke('.');
        KeyStroke copy = KeyStroke.getKeyStroke("control C");
        KeyStroke paste = KeyStroke.getKeyStroke("control V");
        KeyStroke numNeg = KeyStroke.getKeyStroke('-');

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
        im.put(increment4, "incFineAction");
        im.put(decrement4, "decFineAction");
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
        im.put(mulKey, "mulAction");
        im.put(mulKeys, "mulAction");
        im.put(numNeg, "numNeg");

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
        getActionMap().put(im.get(increment4), incFineAction);
        getActionMap().put(im.get(decrement4), decFineAction);
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
        getActionMap().put(im.get(mulKey), multiplyAction);
        getActionMap().put(im.get(mulKeys), multiplyAction);
        getActionMap().put(im.get(copy), copyAction);
        getActionMap().put(im.get(paste), pasteAction);
        getActionMap().put(im.get(numNeg), numNegAction);

        this.setInputMap(WHEN_FOCUSED, im);
    }

    public DataCell[] getData() {
        return data;
    }

    public void setData(DataCell[] data) {
        this.data = data;
    }

    public void populateTable(byte[] input) throws ArrayIndexOutOfBoundsException {
        if (scales.isEmpty()) {
            scales.add(new Scale());
        }

        // temporarily remove lock
        boolean tempLock = locked;
        locked = false;

        if (!isStatic) {
            if (!beforeRam) {
                ramOffset = container.getRomID().getRamOffset();
            }

            for (int i = 0; i < data.length; i++) {
                if (data[i] == null) {
                    data[i] = new DataCell(scales.get(scaleIndex), getEditor().getSettings().getCellSize());
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
                                        storageType,
                                        signed));
                    }

                    data[i].setPreferredSize(new Dimension(cellWidth, cellHeight));
                    centerPanel.add(data[i]);
                    data[i].setYCoord(i);
                    data[i].setOriginalValue(data[i].getBinValue());

                    // show locked cell
                    if (tempLock) {
                        data[i].setForeground(Color.GRAY);
                    }
                }
            }
        }

        // reset locked status
        locked = tempLock;
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

    @Override
    public String getName() {
        return name;
    }

    @Override
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
        return scales.get(scaleIndex);
    }

    public Vector<Scale> getScales() {
        return scales;
    }

    public Scale getScaleByName(String inputName) throws Exception {
        // look for scale, else throw exception
        for (Scale scale : scales) {
            if (scale.getName().equalsIgnoreCase(inputName)) {
                return scale;
            }
        }
        throw new Exception();
    }

    public void setScale(Scale scale) {
        // look for scale, replace or add new
        for (int i = 0; i < scales.size(); i++) {
            if (scales.get(i).getName().equalsIgnoreCase(scale.getName())) {
                scales.remove(i);
                break;
            }
        }
        scales.add(scale);
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

    public boolean isSignedData() {
        return signed;
    }

    public void setSignedData(boolean signed) {
        this.signed = signed;
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

    public void setFlip(boolean flip) {
        this.flip = flip;
    }

    public void setLogParam(String logParam) {
        this.logParam = logParam;
    }

    public String getLogParam() {
        return logParam;
    }

    @Override
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

                double high = Double.MIN_VALUE;
                double low = Double.MAX_VALUE;

                if (getScale().getMax() != 0 || getScale().getMin() != 0) {

                    // set min and max values if they are set in scale
                    high = getScale().getMax();
                    low = getScale().getMin();

                } else {

                    for (int i = 0; i < getDataSize(); i++) {
                        double value = data[i].getValue();
                        if (value > high) {
                            high = value;
                        }
                        if (value < low) {
                            low = value;
                        }
                    }
                }

                for (int i = 0; i < getDataSize(); i++) {
                    double value = data[i].getValue();
                    if (value > high || value < low) {

                        // value exceeds limit
                        data[i].setColor(getEditor().getSettings().getWarningColor());

                    } else {
                        // limits not set, scale based on table values
                        double scale;
                        if (high - low == 0) {
                            // if all values are the same, color will be middle value
                            scale = .5;
                        } else {
                            scale = (value - low) / (high - low);
                        }

                        data[i].setColor(getScaledColor(scale, getEditor().getSettings()));
                    }
                }
            } else { // is static/axis
                for (int i = 0; i < getDataSize(); i++) {
                    data[i].setColor(getEditor().getSettings().getAxisColor());
                    data[i].setOpaque(true);
                    data[i].setBorder(createLineBorder(Color.BLACK, 1));
                    data[i].setHorizontalAlignment(DataCell.CENTER);
                }
            }

        } else { // comparing is on
            if (!isStatic) {
                double high = Double.MIN_VALUE;

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
                    if (high == 0) {
                        scale = 0;
                    } else {
                        scale = cellDifference / high;
                    }

                    if (scale == 0) {
                        data[i].setColor(UNCHANGED_VALUE_COLOR);
                    } else {
                        data[i].setColor(getScaledColor(scale, getEditor().getSettings()));
                    }

                    // set border
                    if (data[i].getBinValue() > data[i].getOriginalValue()) {
                        data[i].setBorder(createLineBorder(getEditor().getSettings().getIncreaseBorder()));
                    } else if (data[i].getBinValue() < data[i].getOriginalValue()) {
                        data[i].setBorder(createLineBorder(getEditor().getSettings().getDecreaseBorder()));
                    } else {
                        data[i].setBorder(createLineBorder(Color.BLACK, 1));
                    }
                }
            }
        }

        // colorize border
        if (!isStatic) {
            for (int i = 0; i < getDataSize(); i++) {
                if (data[i].getBinValue() > data[i].getOriginalValue()) {
                    data[i].setBorder(createLineBorder(getEditor().getSettings().getIncreaseBorder()));
                } else if (data[i].getBinValue() < data[i].getOriginalValue()) {
                    data[i].setBorder(createLineBorder(getEditor().getSettings().getDecreaseBorder()));
                } else {
                    data[i].setBorder(createLineBorder(Color.BLACK, 1));
                }
            }
        }
    }

    public void setFrame(TableFrame frame) {
        this.frame = frame;
        //frame.setSize(getFrameSize());
        frame.pack();
    }

    public Dimension getFrameSize() {
        int height = verticalOverhead + cellHeight;
        int width = horizontalOverhead + data.length * cellWidth;
        if (height < minHeight) {
            height = minHeight;
        }
        int minWidth = isLiveDataSupported() ? minWidthOverlay : minWidthNoOverlay;
        if (width < minWidth) {
            width = minWidth;
        }
        return new Dimension(width, height);
    }

    public TableFrame getFrame() {
        return frame;
    }

    public void increment(double increment) {
        if (!isStatic && !locked && !(userLevel > getEditor().getSettings().getUserLevel())) {
            for (DataCell cell : data) {
                if (cell.isSelected()) {
                    cell.increment(increment);
                }
            }
            colorize();
        } else if (userLevel > getEditor().getSettings().getUserLevel()) {
            JOptionPane.showMessageDialog(this, "This table can only be modified by users with a userlevel of \n" +
                    userLevel + " or greater. Click View->User Level to change your userlevel.",
                    "Table cannot be modified",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    public void multiply(double factor) {
        if (!isStatic && !locked && !(userLevel > getEditor().getSettings().getUserLevel())) {
            for (DataCell cell : data) {
                if (cell.isSelected()) {
                    cell.multiply(factor);
                }
            }
        } else if (userLevel > getEditor().getSettings().getUserLevel()) {
            JOptionPane.showMessageDialog(this, "This table can only be modified by users with a userlevel of \n" +
                    userLevel + " or greater. Click View->User Level to change your userlevel.",
                    "Table cannot be modified",
                    JOptionPane.INFORMATION_MESSAGE);
        }
        colorize();
    }

    public void setRealValue(String realValue) {
        if (!isStatic && !locked && !(userLevel > getEditor().getSettings().getUserLevel())) {
            for (DataCell cell : data) {
                if (cell.isSelected()) {
                    cell.setRealValue(realValue);
                }
            }
        } else if (userLevel > getEditor().getSettings().getUserLevel()) {
            JOptionPane.showMessageDialog(this, "This table can only be modified by users with a userlevel of \n" +
                    userLevel + " or greater. Click View->User Level to change your userlevel.",
                    "Table cannot be modified",
                    JOptionPane.INFORMATION_MESSAGE);
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
        for (DataCell cell : data) {
            cell.setSelected(false);
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
        for (DataCell cell : data) {
            if (cell.isHighlighted()) {
                cell.setSelected(true);
                cell.setHighlighted(false);
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
            for (DataCell cell : data) {
                cell.setOriginalValue(cell.getBinValue());
            }
        }
        colorize();
    }

    public void undoAll() {
        clearLiveDataTrace();
        if (!isStatic) {
            for (DataCell cell : data) {
                cell.setBinValue(cell.getOriginalValue());
            }
        }
        colorize();
    }

    public void undoSelected() {
        clearLiveDataTrace();
        if (!isStatic) {
            for (DataCell cell : data) {
                // reset current value to original value
                if (cell.isSelected()) {
                    cell.setBinValue(cell.getOriginalValue());
                }
            }
        }
        colorize();
    }

    public byte[] saveFile(byte[] binData) {
        if (!isStatic  // save if table is not static
                &&     // and user level is great enough
                userLevel <= getEditor().getSettings().getUserLevel()
                &&     // and table is not in debug mode, unless saveDebugTables is true
                (userLevel < 5
                        ||
                        getEditor().getSettings().isSaveDebugTables())) {

            for (int i = 0; i < data.length; i++) {

                // determine output byte values
                byte[] output;
                if (storageType != STORAGE_TYPE_FLOAT) {
                    // convert byte values
                    output = RomAttributeParser.parseIntegerValue((int) data[i].getBinValue(), endian, storageType);
                    for (int z = 0; z < storageType; z++) { // insert into file
                        binData[i * storageType + z + storageAddress - ramOffset] = output[z];
                    }

                } else { // float
                    // convert byte values
                    output = RomAttributeParser.floatToByte((float) data[i].getBinValue(), endian);
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

    @Override
    public void addKeyListener(KeyListener listener) {
        super.addKeyListener(listener);
        for (DataCell cell : data) {
            for (int z = 0; z < storageType; z++) {
                cell.addKeyListener(listener);
            }
        }
    }

    public void selectCellAt(int y) {
        if(y >= 0 && y < data.length) {
            if (type == TABLE_X_AXIS || type == TABLE_Y_AXIS) {
                axisParent.clearSelection();
            } else {
                clearSelection();
            }

            data[y].setSelected(true);
            highlightY = y;
        }
    }

    public void copySelection() {
        Window ancestorWindow = SwingUtilities.getWindowAncestor(this);
        if(null != ancestorWindow) {
            ancestorWindow.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        }
        getEditor().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        copySelectionWorker = new CopySelectionWorker(this);
        copySelectionWorker.execute();
    }

    public StringBuffer getTableAsString() {
        //make a string of the selection
        StringBuffer output = new StringBuffer(BLANK);
        for (int i = 0; i < data.length; i++) {
            output.append(data[i].getText());
            if (i < data.length - 1) {
                output.append(TAB);
            }
        }
        return output;
    }

    public void copyTable() {
        Window ancestorWindow = SwingUtilities.getWindowAncestor(this);
        if(null != ancestorWindow) {
            ancestorWindow.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        }
        getEditor().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        copyTableWorker = new CopyTableWorker(getEditor().getSettings(), this);
        copyTableWorker.execute();
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
        StringTokenizer st = new StringTokenizer(BLANK);
        try {
            String input = (String) Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null).getTransferData(DataFlavor.stringFlavor);
            st = new StringTokenizer(input);
        } catch (UnsupportedFlavorException ex) { /* wrong paste type -- do nothing */
        } catch (IOException ex) {
        }

        String pasteType = st.nextToken();

        if ("[Table1D]".equalsIgnoreCase(pasteType)) { // copied entire table
            int i = 0;
            while (st.hasMoreTokens()) {
                String currentToken = st.nextToken();
                try {
                    if (!data[i].getText().equalsIgnoreCase(currentToken)) {
                        data[i].setRealValue(currentToken);
                    }
                } catch (ArrayIndexOutOfBoundsException ex) { /* table larger than target, ignore*/ }
                i++;
            }
        } else if ("[Selection1D]".equalsIgnoreCase(pasteType)) { // copied selection
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
        colorize();
    }

    public void applyColorSettings() {
        if (this.getType() != TABLE_SWITCH) {
            // apply settings to cells
            for (int i = 0; i < getDataSize(); i++) {
                this.setMaxColor(editor.getSettings().getMaxColor());
                this.setMinColor(editor.getSettings().getMinColor());
                data[i].setHighlightColor(editor.getSettings().getHighlightColor());
                data[i].setIncreaseBorder(editor.getSettings().getIncreaseBorder());
                data[i].setDecreaseBorder(editor.getSettings().getDecreaseBorder());
                data[i].setFont(editor.getSettings().getTableFont());
                data[i].repaint();
            }
            cellHeight = (int) editor.getSettings().getCellSize().getHeight();
            cellWidth = (int) editor.getSettings().getCellSize().getWidth();
            colorize();
            validateScaling();
        }
    }

    public void resize() {
        //frame.setSize(getFrameSize());
        frame.pack();
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
        if (type != Table.TABLE_SWITCH && !isStatic) {

            // make sure a scale is present
            if (scales.isEmpty()) {
                scales.add(new Scale());
            }

            double startValue = 5;
            double toReal = JEPUtil.evaluate(scales.get(scaleIndex).getExpression(), startValue); // convert real world value of "5"
            double endValue = JEPUtil.evaluate(scales.get(scaleIndex).getByteExpression(), toReal);

            // if real to byte doesn't equal 5, report conflict
            if (Math.abs(endValue - startValue) > .001) {

                JPanel panel = new JPanel();
                panel.setLayout(new GridLayout(4, 1));
                panel.add(new JLabel("The real value and byte value conversion expressions for table " + name + " are invalid."));
                panel.add(new JLabel("To real value: " + scales.get(scaleIndex).getExpression()));
                panel.add(new JLabel("To byte: " + scales.get(scaleIndex).getByteExpression()));

                JCheckBox check = new JCheckBox("Always display this message", true);
                check.setHorizontalAlignment(JCheckBox.RIGHT);
                panel.add(check);

                check.addActionListener(
                        new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                getEditor().getSettings().setCalcConflictWarning(((JCheckBox) e.getSource()).isSelected());
                            }
                        }
                        );

                JOptionPane.showMessageDialog(editor, panel,
                        "Warning", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void compare(Table compareTable, int compareType) {
        if(!(compareTable instanceof Table)) {
            return;
        }

        DataCell[] compareData = compareTable.getData();
        int i = 0;

        if(data.length != compareData.length) {
            return;
        }

        for(DataCell cell : compareData) {
            data[i].setCompareValue(cell.getOriginalValue());
            i++;
        }
        compare(compareType);
    }

    public void compare(int compareType) {
        clearLiveDataTrace();
        this.compareType = compareType;

        for (int i = 0; i < getDataSize(); i++) {
            if (compareType == COMPARE_ORIGINAL) {
                data[i].setCompareValue(data[i].getOriginalValue());
            }
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

    public int getUserLevel() {
        return userLevel;
    }

    public void setUserLevel(int userLevel) {
        this.userLevel = userLevel;
        if (userLevel > 5) {
            userLevel = 5;
        } else if (userLevel < 1) {
            userLevel = 1;
        }
    }

    public int getScaleIndex() {
        return scaleIndex;
    }

    public void setScaleIndex(int scaleIndex) {
        this.scaleIndex = scaleIndex;
        refreshValues();
    }

    public void setScaleByName(String scaleName) {
        for (int i = 0; i < scales.size(); i++) {
            if (scales.get(i).getName().equalsIgnoreCase(scaleName)) {
                setScaleIndex(i);
            }
        }
    }

    public void refreshValues() {
        if (!isStatic) {
            for (int i = 0; i < getDataSize(); i++) {
                data[i].refreshValue();
            }
        }
    }

    public void setEditor(ECUEditor editor) {
        this.editor = editor;
    }

    public ECUEditor getEditor() {
        return this.editor;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public void setOverlayLog(boolean overlayLog) {
        this.overlayLog = overlayLog;
        if (overlayLog) {
            clearLiveDataTrace();
        }
    }

    public boolean getOverlayLog()
    {
        return this.overlayLog;
    }

    public void setLiveValue(String liveValue) {
        this.liveValue = liveValue;
    }

    public double getLiveValue() {
        try {
            return Double.parseDouble(liveValue);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    public abstract boolean isLiveDataSupported();

    public abstract boolean isButtonSelected();

    protected void highlightLiveData() {
    }

    public void clearLiveDataTrace() {
        liveValue = BLANK;
    }

    public double getMin() {
        if (getScale().getMin() == 0 && getScale().getMax() == 0) {
            double low = Double.MAX_VALUE;
            for (int i = 0; i < getDataSize(); i++) {
                double value = data[i].getValue();
                if (value < low) {
                    low = value;
                }
            }
            return low;
        } else {
            return getScale().getMin();
        }
    }

    public double getMax() {
        if (getScale().getMin() == 0 && getScale().getMax() == 0) {
            double high = Double.MIN_VALUE;
            for (int i = 0; i < getDataSize(); i++) {
                double value = data[i].getValue();
                if (value > high) {
                    high = value;
                }
            }
            return high;
        } else {
            return getScale().getMax();
        }
    }

    public boolean getIsStatic() {
        return this.isStatic;
    }

    public boolean getIsAxis() {
        return this.isAxis;
    }

    public int getCompareType() {
        return this.compareType;
    }
}

class CopySelectionWorker extends SwingWorker<Void, Void> {
    Table table;

    public CopySelectionWorker(Table table) {
        this.table = table;
    }

    @Override
    protected Void doInBackground() throws Exception {
        // find bounds of selection
        // coords[0] = x min, y min, x max, y max
        String newline = System.getProperty("line.separator");
        String output = "[Selection1D]" + newline;
        boolean copy = false;
        int[] coords = new int[2];
        coords[0] = table.getDataSize();

        for (int i = 0; i < table.getDataSize(); i++) {
            if (table.getData()[i].isSelected()) {
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
            if (table.getData()[i].isSelected()) {
                output = output + table.getData()[i].getText();
            } else {
                output = output + "x"; // x represents non-selected cell
            }
            if (i < coords[1]) {
                output = output + "\t";
            }
        }
        //copy to clipboard
        if (copy) {
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(output), null);
        }
        return null;
    }

    @Override
    public void done() {
        Window ancestorWindow = SwingUtilities.getWindowAncestor(table);
        if(null != ancestorWindow) {
            ancestorWindow.setCursor(null);
        }
        table.setCursor(null);
        table.getEditor().setCursor(null);
    }
}

class CopyTableWorker extends SwingWorker<Void, Void> {
    Settings settings;
    Table table;

    public CopyTableWorker(Settings settings, Table table) {
        this.settings = settings;
        this.table = table;
    }

    @Override
    protected Void doInBackground() throws Exception {
        String tableHeader = settings.getTableHeader();

        StringBuffer output = new StringBuffer(tableHeader);
        for (int i = 0; i < table.getDataSize(); i++) {
            output.append(table.getData()[i].getText());
            if (i < table.getDataSize() - 1) {
                output.append(Table.TAB);
            }
        }
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(String.valueOf(output)), null);
        return null;
    }

    @Override
    public void done() {
        Window ancestorWindow = SwingUtilities.getWindowAncestor(table);
        if(null != ancestorWindow) {
            ancestorWindow.setCursor(null);
        }
        table.setCursor(null);
        table.getEditor().setCursor(null);
    }
}