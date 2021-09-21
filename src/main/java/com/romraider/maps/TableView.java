/*
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2021 RomRaider.com
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
import java.text.MessageFormat;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.naming.NameNotFoundException;
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
import org.apache.log4j.Logger;

import com.romraider.Settings;
import com.romraider.editor.ecu.ECUEditorManager;
import com.romraider.swing.TableToolBar;
import com.romraider.util.ByteUtil;
import com.romraider.util.JEPUtil;
import com.romraider.util.NumberUtil;
import com.romraider.util.ResourceUtil;
import com.romraider.util.SettingsManager;

public abstract class TableView extends JPanel implements Serializable {
    private static final long serialVersionUID = 6559256489995552645L;
    protected static final Logger LOGGER = Logger.getLogger(TableView.class);
    private static final ResourceBundle rb = new ResourceUtil().getBundle(TableView.class.getName());

    protected Table table;
    protected PresetPanel presetPanel;      
    protected DataCellView[] data;
   
    protected BorderLayout borderLayout = new BorderLayout();
    protected GridLayout centerLayout = new GridLayout(1, 1, 0, 0);
    protected JPanel centerPanel = new JPanel(centerLayout);
    protected JLabel tableLabel;
    protected int verticalOverhead = 103;
    protected int horizontalOverhead = 2;
    protected int cellHeight = (int) getSettings().getCellSize().getHeight();
    protected int cellWidth = (int) getSettings().getCellSize().getWidth();
    protected int minHeight = 100;
    protected int minWidthNoOverlay = 465;
    protected int minWidthOverlay = 700;
    protected int highlightX;
    protected int highlightY;
    protected boolean highlight = false;
    protected boolean overlayLog = false;

    protected CopyTableWorker copyTableWorker;
    protected CopySelectionWorker copySelectionWorker;

    protected Settings.DataType compareValueType = Settings.DataType.BIN;
    
    protected TableView(Table table) {
    	this.table = table;
    	
    	//Populate Views from table here
    	
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
        Action shiftRightAction = new AbstractAction() {
            private static final long serialVersionUID = 1042888914300385041L;

            @Override
            public void actionPerformed(ActionEvent e) {
                shiftCursorRight();
            }
        };
        Action shiftLeftAction = new AbstractAction() {
            private static final long serialVersionUID = -4970441655277214171L;

            @Override
            public void actionPerformed(ActionEvent e) {
            	shiftCursorLeft();
            }
        };
        Action shiftDownAction = new AbstractAction() {
            private static final long serialVersionUID = -7898502951812125984L;

            @Override
            public void actionPerformed(ActionEvent e) {
            	shiftCursorDown();
            }
        };
        Action shiftUpAction = new AbstractAction() {
            private static final long serialVersionUID = 6937621527147666631L;

            @Override
            public void actionPerformed(ActionEvent e) {
            	shiftCursorUp();
            }
        };
        Action incCoarseAction = new AbstractAction() {
            private static final long serialVersionUID = -8308522736529183148L;

            @Override
            public void actionPerformed(ActionEvent e) {
                getToolbar().incrementCoarse();
            }
        };
        Action decCoarseAction = new AbstractAction() {
            private static final long serialVersionUID = -7407628920997400915L;

            @Override
            public void actionPerformed(ActionEvent e) {
                getToolbar().decrementCoarse();
            }
        };
        Action incFineAction = new AbstractAction() {
            private static final long serialVersionUID = 7261463425941761433L;

            @Override
            public void actionPerformed(ActionEvent e) {
                getToolbar().incrementFine();
            }
        };
        Action decFineAction = new AbstractAction() {
            private static final long serialVersionUID = 8929400237520608035L;

            @Override
            public void actionPerformed(ActionEvent e) {
                getToolbar().decrementFine();
            }
        };
        Action num0Action = new AbstractAction() {
            private static final long serialVersionUID = -6310984176739090034L;

            @Override
            public void actionPerformed(ActionEvent e) {
                getToolbar().focusSetValue('0');
            }
        };
        Action num1Action = new AbstractAction() {
            private static final long serialVersionUID = -6187220355403883499L;

            @Override
            public void actionPerformed(ActionEvent e) {
                getToolbar().focusSetValue('1');
            }
        };
        Action num2Action = new AbstractAction() {
            private static final long serialVersionUID = -8745505977907325720L;

            @Override
            public void actionPerformed(ActionEvent e) {
                getToolbar().focusSetValue('2');
            }
        };
        Action num3Action = new AbstractAction() {
            private static final long serialVersionUID = 4694872385823448942L;

            @Override
            public void actionPerformed(ActionEvent e) {
                getToolbar().focusSetValue('3');
            }
        };
        Action num4Action = new AbstractAction() {
            private static final long serialVersionUID = 4005741329254221678L;

            @Override
            public void actionPerformed(ActionEvent e) {
                getToolbar().focusSetValue('4');
            }
        };
        Action num5Action = new AbstractAction() {
            private static final long serialVersionUID = -5846094949106279884L;

            @Override
            public void actionPerformed(ActionEvent e) {
                getToolbar().focusSetValue('5');
            }
        };
        Action num6Action = new AbstractAction() {
            private static final long serialVersionUID = -5338656374925334150L;

            @Override
            public void actionPerformed(ActionEvent e) {
                getToolbar().focusSetValue('6');
            }
        };
        Action num7Action = new AbstractAction() {
            private static final long serialVersionUID = 1959983381590509303L;

            @Override
            public void actionPerformed(ActionEvent e) {
                getToolbar().focusSetValue('7');
            }
        };
        Action num8Action = new AbstractAction() {
            private static final long serialVersionUID = 7442763278699460648L;

            @Override
            public void actionPerformed(ActionEvent e) {
                getToolbar().focusSetValue('8');
            }
        };
        Action num9Action = new AbstractAction() {
            private static final long serialVersionUID = 7475171864584215094L;

            @Override
            public void actionPerformed(ActionEvent e) {
                getToolbar().focusSetValue('9');
            }
        };
        Action numPointAction = new AbstractAction() {
            private static final long serialVersionUID = -4729135055857591830L;

            @Override
            public void actionPerformed(ActionEvent e) {
                getToolbar().focusSetValue('.');
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
        Action interpolate = new AbstractAction() {
            private static final long serialVersionUID = -2357532575392447149L;

            @Override
            public void actionPerformed(ActionEvent e) {
                table.interpolate();
            }
        };
        Action verticalInterpolate = new AbstractAction() {
            private static final long serialVersionUID = -2375322575392447149L;

            @Override
            public void actionPerformed(ActionEvent e) {
                table.verticalInterpolate();
            }
        };
        Action horizontalInterpolate = new AbstractAction() {
            private static final long serialVersionUID = -6346750245035640773L;

            @Override
            public void actionPerformed(ActionEvent e) {
                table.horizontalInterpolate();
            }
        };
        Action multiplyAction = new AbstractAction() {
            private static final long serialVersionUID = -2753212575392447149L;

            @Override
            public void actionPerformed(ActionEvent e) {
                getToolbar().multiply();
            }
        };
        Action numNegAction = new AbstractAction() {
            private static final long serialVersionUID = -7532750245035640773L;

            @Override
            public void actionPerformed(ActionEvent e) {
                getToolbar().focusSetValue('-');
            }
        };

        // set input mapping
        InputMap im = getInputMap(WHEN_IN_FOCUSED_WINDOW);

        KeyStroke right = KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0);
        KeyStroke left = KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0);
        KeyStroke up = KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0);
        KeyStroke down = KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0);
        KeyStroke shiftRight = KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, KeyEvent.SHIFT_DOWN_MASK);
        KeyStroke shiftLeft = KeyStroke.getKeyStroke(KeyEvent.VK_LEFT,  KeyEvent.SHIFT_DOWN_MASK);
        KeyStroke shiftUp = KeyStroke.getKeyStroke(KeyEvent.VK_UP,  KeyEvent.SHIFT_DOWN_MASK);
        KeyStroke shiftDown = KeyStroke.getKeyStroke(KeyEvent.VK_DOWN,  KeyEvent.SHIFT_DOWN_MASK);
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
        KeyStroke interp = KeyStroke.getKeyStroke("shift I");
        KeyStroke vinterp = KeyStroke.getKeyStroke("shift V");
        KeyStroke hinterp = KeyStroke.getKeyStroke("shift H");
        KeyStroke numNeg = KeyStroke.getKeyStroke('-');

        im.put(right, "right");
        im.put(left, "left");
        im.put(up, "up");
        im.put(down, "down");
        im.put(shiftRight, "shiftRight");
        im.put(shiftLeft, "shiftLeft");
        im.put(shiftUp, "shiftUp");
        im.put(shiftDown, "shiftDown");
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
        im.put(interp, "interpolate");
        im.put(vinterp, "verticalInterpolate");
        im.put(hinterp, "horizontalInterpolate");
        im.put(mulKey, "mulAction");
        im.put(mulKeys, "mulAction");
        im.put(numNeg, "numNeg");

        getActionMap().put(im.get(right), rightAction);
        getActionMap().put(im.get(left), leftAction);
        getActionMap().put(im.get(up), upAction);
        getActionMap().put(im.get(down), downAction);
        getActionMap().put(im.get(shiftRight), shiftRightAction);
        getActionMap().put(im.get(shiftLeft), shiftLeftAction);
        getActionMap().put(im.get(shiftUp), shiftUpAction);
        getActionMap().put(im.get(shiftDown), shiftDownAction);
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
        getActionMap().put(im.get(interp), interpolate);
        getActionMap().put(im.get(vinterp), verticalInterpolate);
        getActionMap().put(im.get(hinterp), horizontalInterpolate);
        getActionMap().put(im.get(numNeg), numNegAction);

        this.setInputMap(WHEN_FOCUSED, im);
    }

    public DataCellView[] getData() {
        return data;
    }

    public void setData(DataCellView[] data) {
        this.data = data;
    }

    public DataCellView getDataCell(int location) {
        return data[location];
    }

    @Override
    public String toString() {
        return table.toString();
    }

    public void drawTable() {
    	
        for(DataCellView cell : data) {
            if(null != cell) {
                cell.drawCell();
            }
        }
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

    public void increment(double increment) {
        if (!locked && !(userLevel > getSettings().getUserLevel())) {
            for (DataCellView cell : data) {
                if (cell.isSelected()) {
                    cell.getDataCell().increment(increment);
                }
            }
        } else if (userLevel > getSettings().getUserLevel()) {
            JOptionPane.showMessageDialog(this, MessageFormat.format(
                    rb.getString("USERLVLTOLOW"), userLevel),
                    rb.getString("TBLNOTMODIFY"),
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    public void multiply(double factor) {
    	
        if (!locked && !(userLevel > getSettings().getUserLevel())) {
            for (DataCellView cell : data) {
                if (cell.isSelected()) {
                	
                	//Use raw or real value, depending on view settings
                	if(getCurrentScale().getName().equals("Raw Value"))
                		cell.getDataCell().multiplyRaw(factor);                	
                	else 
                		cell.getDataCell().multiply(factor);               	
                }
            }
        } else if (userLevel > getSettings().getUserLevel()) {
            JOptionPane.showMessageDialog(this, MessageFormat.format(
                    rb.getString("USERLVLTOLOW"), userLevel),
                    rb.getString("TBLNOTMODIFY"),
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    public void setRealValue(String realValue) {
        if (!locked && userLevel <= getSettings().getUserLevel()) {
            for(DataCellView cell : data) {
                if (cell.isSelected()) {
                    cell.getDataCell().setRealValue(realValue);
                }
            }
        } else if (userLevel > getSettings().getUserLevel()) {
            JOptionPane.showMessageDialog(this, MessageFormat.format(
                    rb.getString("USERLVLTOLOW"), userLevel),
                    rb.getString("TBLNOTMODIFY"),
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    public void clearSelection() {
        for (DataCellView cell : data) {
                cell.getDataCell().setSelected(false);
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
        for (DataCellView cell : data) {
            if (cell.isHighlighted()) {
                cell.setHighlighted(false);
                cell.getDataCell().setSelected(true);
            }
        }
    }

    public abstract void cursorUp();

    public abstract void cursorDown();

    public abstract void cursorLeft();

    public abstract void cursorRight();

    public abstract void shiftCursorUp();

    public abstract void shiftCursorDown();

    public abstract void shiftCursorLeft();

    public abstract void shiftCursorRight();

  
    abstract public byte[] saveFile(byte[] binData);
       
    @Override
    public void addKeyListener(KeyListener listener) {
        super.addKeyListener(listener);
        for (DataCellView cell : data) {
            for (int z = 0; z < table.getStorageType(); z++) {
                cell.addKeyListener(listener);
            }
        }
    }

    public void selectCellAt(int y) {
        if(y >= 0 && y < data.length) {
            clearSelection();
            data[y].getDataCell().setSelected(true);
            highlightY = y;
            ECUEditorManager.getECUEditor().getTableToolBar().updateTableToolBar(table);
        }
    }

    public void selectCellAtWithoutClear(int y) {
        if(y >= 0 && y < data.length) {
            data[y].getDataCell().setSelected(true);
            highlightY = y;
            ECUEditorManager.getECUEditor().getTableToolBar().updateTableToolBar(table);
        }
    }

    public void copySelection() {
        Window ancestorWindow = SwingUtilities.getWindowAncestor(this);

        if(null != ancestorWindow) {
            ancestorWindow.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        }

        ECUEditorManager.getECUEditor().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        copySelectionWorker = new CopySelectionWorker(table);
        copySelectionWorker.execute();
    }

    public StringBuffer getTableAsString() {
        StringBuffer output = new StringBuffer(Settings.BLANK);
        for (int i = 0; i < data.length; i++) {
            if (overlayLog) {
                output.append(data[i].getCellText());
            }
            else {
            	if(data[i]!= null)
            		output.append(NumberUtil.stringValue(data[i].getDataCell().getRealValue()));
            }
            if (i < data.length - 1) {
                output.append(Settings.TAB);
            }
        }
        return output;
    }

    public void copyTable() {
        Window ancestorWindow = SwingUtilities.getWindowAncestor(this);
        if(null != ancestorWindow) {
            ancestorWindow.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        }
        ECUEditorManager.getECUEditor().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        copyTableWorker = new CopyTableWorker(table);
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
                data[i].getDataCell().setRealValue(input[i]);
            } catch (NumberFormatException ex) { /* not a number, do nothing */ }
        }
    }

    public void paste() {
        // TODO: This sounds like desearialize.
        if (!table.isStaticDataTable()) {
            StringTokenizer st = new StringTokenizer(Settings.BLANK);
            try {
                String input = (String) Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null).getTransferData(DataFlavor.stringFlavor);
                st = new StringTokenizer(input, Table.ST_DELIMITER);
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
                            data[i].getDataCell().setRealValue(currentToken);
                        }
                    } catch (ArrayIndexOutOfBoundsException ex) { /* table larger than target, ignore*/ }
                    i++;
                }
            } else if ("[Selection1D]".equalsIgnoreCase(pasteType)) { // copied selection
                if (data[highlightY].getDataCell().isSelected()) {
                    int i = 0;
                    while (st.hasMoreTokens()) {
                        String currentToken = st.nextToken();
                        try {
                            if (!data[highlightY + i].getText().equalsIgnoreCase(currentToken)) {
                                data[highlightY + i].getDataCell().setRealValue(currentToken);
                            }
                        } catch (ArrayIndexOutOfBoundsException ex) { /* paste larger than target, ignore */ }
                        i++;
                    }
                }
            }
        }
    }

    public void setCompareValueType(Settings.DataType compareValueType) {
        this.compareValueType = compareValueType;
        drawTable();
    }

    public Settings.DataType getCompareValueType() {
        return this.compareValueType;
    }

    
    public Settings getSettings()
    {
        return SettingsManager.getSettings();
    }

    public TableToolBar getToolbar()
    {
        return ECUEditorManager.getECUEditor().getTableToolBar();
    }


    public void setOverlayLog(boolean overlayLog) {
        this.overlayLog = overlayLog;
    }

    public boolean getOverlayLog()
    {
        return this.overlayLog;
    }


    public void highlightLiveData(String liveVal) {
        if (getOverlayLog()) {
            double liveValue = 0.0;
            try {
            	liveValue = NumberUtil.doubleValue(liveVal);
            } catch (Exception ex) {
            	LOGGER.error("Table - live data highlight parsing error for value: " + liveVal);
            	return;
            }

            int startIdx = data.length;
            for (int i = 0; i < data.length; i++) {
                double currentValue = data[i].getDataCell().getRealValue();
                if (liveValue == currentValue) {
                    startIdx = i;
                    break;
                } else if (liveValue < currentValue){
                    startIdx = i-1;
                    break;
                }
            }

            setLiveDataIndex(startIdx);
            DataCellView cell = data[getLiveDataIndex()];
            cell.setPreviousLiveDataTrace(false);
            cell.setLiveDataTrace(true);
            cell.getDataCell().setLiveDataTraceValue(liveVal);
            getToolbar().setLiveDataValue(liveVal);
        }
    }

    public void updateLiveDataHighlight() {
        if (getOverlayLog()) {
            data[getPreviousLiveDataIndex()].setPreviousLiveDataTrace(true);
            data[getLiveDataIndex()].setPreviousLiveDataTrace(false);
            data[getLiveDataIndex()].setLiveDataTrace(true);
        }
    }

    public void clearLiveDataTrace() {
        for (DataCellView cell : data) {
            cell.setLiveDataTrace(false);
            cell.setPreviousLiveDataTrace(false);
        }
    }


    public void updateTableLabel() {
        if(null == name || name.isEmpty()) {
            ;// Do not update label.
        } else if(null == getCurrentScale () || "0x" == getCurrentScale().getUnit()) {
            // static or no scale exists.
            tableLabel.setText(getName());
        } else {
            tableLabel.setText(getName() + " (" + getCurrentScale().getUnit() + ")");
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
        String output = "[Selection1D]" + Settings.NEW_LINE;
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
                output = output + NumberUtil.stringValue(table.getData()[i].getDataCell().getRealValue());
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
        ECUEditorManager.getECUEditor().setCursor(null);
    }
}

class CopyTableWorker extends SwingWorker<Void, Void> {
    Table table;

    public CopyTableWorker(Table table) {
        this.table = table;
    }

    @Override
    protected Void doInBackground() throws Exception {
        String tableHeader = table.getSettings().getTableHeader();
        StringBuffer output = new StringBuffer(tableHeader);
        output.append(table.getTableAsString());
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
        ECUEditorManager.getECUEditor().setCursor(null);
    }
}
