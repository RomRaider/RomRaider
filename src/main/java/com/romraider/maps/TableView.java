/*
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2022 RomRaider.com
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
import java.text.MessageFormat;
import java.util.ResourceBundle;
import java.util.StringTokenizer;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.InputMap;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import org.apache.log4j.Logger;

import com.romraider.Settings;
import com.romraider.editor.ecu.ECUEditorManager;
import com.romraider.swing.TableFrame;
import com.romraider.swing.TableToolBar;
import com.romraider.util.NumberUtil;
import com.romraider.util.ResourceUtil;
import com.romraider.util.SettingsManager;

public abstract class TableView extends JPanel implements Serializable {
    private static final long serialVersionUID = 6559256489995552645L;
    protected static final Logger LOGGER = Logger.getLogger(TableView.class);
    private static final ResourceBundle rb = new ResourceUtil().getBundle(TableView.class.getName());

    protected Table table;
    protected TableView parent;
    protected PresetPanel presetPanel;   
    protected DataCellView[] data;
    
    protected boolean hide; //Hide the actual data
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
    
    protected int highlightBeginX;
    protected int highlightBeginY;
    
    protected boolean highlight = false;
    protected boolean overlayLog = false;
    protected String liveAxisValue = Settings.BLANK;
    protected int liveDataIndex = 0;
    protected int previousLiveDataIndex = 0;
  
    protected Settings.CompareDisplay compareDisplay = Settings.CompareDisplay.ABSOLUTE;

    protected TableView(Table table) {    	
    	this.table = table;
    	   	
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
                try {
                getToolbar().incrementCoarse();
			} catch (UserLevelException e1) {
				showInvalidUserLevelPopup(e1);
			}
            }
        };
        Action decCoarseAction = new AbstractAction() {
            private static final long serialVersionUID = -7407628920997400915L;

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
					getToolbar().decrementCoarse();
				} catch (UserLevelException e1) {
					showInvalidUserLevelPopup(e1);
				}
            }
        };
        Action incFineAction = new AbstractAction() {
            private static final long serialVersionUID = 7261463425941761433L;

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                getToolbar().incrementFine();
			} catch (UserLevelException e1) {
				showInvalidUserLevelPopup(e1);
			}
            }
        };
        Action decFineAction = new AbstractAction() {
            private static final long serialVersionUID = 8929400237520608035L;

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                getToolbar().decrementFine();
			} catch (UserLevelException e1) {
				showInvalidUserLevelPopup(e1);
			}
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
                try {
					paste();
				} catch (UserLevelException e1) {
					showInvalidUserLevelPopup(e1);
				}
            }
        };
     
        class InterpolateAction extends AbstractAction{
            private static final long serialVersionUID = -2357532575392447149L;
            Table t;
            
            public InterpolateAction(Table t) {
            	this.t = t;
            }
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                	t.interpolate();
			} catch (UserLevelException e1) {
				showInvalidUserLevelPopup(e1);
			}
            }
        };
        
        Action interpolate = new InterpolateAction(table);
        
        class VerticalInterpolateAction extends  AbstractAction {
            private static final long serialVersionUID = -2375322575392447149L;
            Table t;
            
            public VerticalInterpolateAction(Table t) {
            	this.t = t;
            }
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                	t.verticalInterpolate();
				} catch (UserLevelException e1) {
					showInvalidUserLevelPopup(e1);
				}
            }
        };
        
        Action verticalInterpolate = new VerticalInterpolateAction(table);

        class HorizontalInterpolateAction extends AbstractAction {
            private static final long serialVersionUID = -6346750245035640773L;
            Table t;
                    
            public HorizontalInterpolateAction(Table t) {
            	this.t = t;
            }
            
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                	t.horizontalInterpolate();
				} catch (UserLevelException e1) {
					showInvalidUserLevelPopup(e1);
				}
            }
        };
        
        Action horizontalInterpolate = new HorizontalInterpolateAction(table);
        		
        class MultiplyAction extends AbstractAction {
            private static final long serialVersionUID = -2753212575392447149L;                       
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                getToolbar().multiply();
    			} catch (UserLevelException e1) {
    				showInvalidUserLevelPopup(e1);
    			}
            }
        };
        
        Action multiplyAction = new MultiplyAction();
        
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
      
    public TableFrame getFrame() {
    	return table.getTableFrame();
    }
    
    public TableView getAxisParent() {
    	return parent;
    }
    
    public void setAxisParent(TableView v) {
    	this.parent = v;
    }
    
    public void setTable(Table t) {
    	 this.table = t;
    }
    
    public Table getTable() {
    	return this.table;
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
    
    public boolean isHidden() {
    	return hide;
    }
    
    public void setHidden(boolean b) {
    	this.hide = b;
    	
    	if(this.hide!=b) {
    		if(!b) {
    			data = null;
    		}
    		else {
    			populateTableVisual();
    		}
    	}
    }
    
    @Override
    public String toString() {
        return table.toString();
    }
    
    public void updatePresetPanel() {
    	if(presetPanel != null)
    		presetPanel.repaint();
    }
    
    public void drawTable() {
    	updateTableLabel();

    	if(data!=null && !isHidden()) {
	        for(DataCellView cell : data) {
	            if(null != cell) {
	                cell.drawCell();
	            }
	        }
    	}
    }
    
    protected void addPresetPanel(PresetManager m) {
    	 presetPanel = new PresetPanel(this, m);
    }
    
    public void populateTableVisual() {
    	//Populate Views from table here
    	if(getTable().presetManager != null) addPresetPanel(getTable().presetManager);
    	
    	if(!isHidden() && table.getData() != null) {
	    	data = new DataCellView[table.getDataSize()];
	
	    	for(int i= 0; i < table.getDataSize(); i++) {
	    		DataCell c = table.getData()[i];
	    		if (c!=null) {
	    			data[i] = new DataCellView(c, this);
	    		}
	    	}
    	}
    }
    
    public Dimension getFrameSize() {
        int height = verticalOverhead + cellHeight;
        int width = horizontalOverhead + data.length * cellWidth;
        if (height < minHeight) {
            height = minHeight;
        }
        int minWidth = table.isLiveDataSupported() ? minWidthOverlay : minWidthNoOverlay;
        if (width < minWidth) {
            width = minWidth;
        }
        return new Dimension(width, height);
    }

    public void startHighlight(int x, int y) {
        this.highlightBeginY = y;
        this.highlightBeginX = x;        
        highlight = true;
        highlight(x, y);
    }

    public void highlight(int x, int y) {
        if (highlight) {         	
            for (int i = 0; i < data.length; i++) {
                if ((i >= highlightBeginY && i <= y) || (i <= highlightBeginY && i >= y)) {
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
 
    public void undoSelected() throws UserLevelException {
        for (DataCellView cell : data) {
            // reset current value to original value
            if (cell.isSelected()) {
                cell.getDataCell().undo();
            }
        }
    }
    
    public static void showInvalidUserLevelPopup(UserLevelException e) {
        JOptionPane.showMessageDialog(null, MessageFormat.format(
                rb.getString("USERLVLTOLOW"), e.getLevel()),
                rb.getString("TBLNOTMODIFY"),
                JOptionPane.INFORMATION_MESSAGE);
    }
      
    @Override
    public void addKeyListener(KeyListener listener) {
        super.addKeyListener(listener);
        for (DataCellView cell : data) {
        	cell.addKeyListener(listener);
        }
    }
    
    public void copySelection() {
        StringBuilder output =  new StringBuilder("[Selection1D]" + Settings.NEW_LINE);
        
        boolean copy = false;
        int[] coords = new int[2];
        coords[0] = table.getDataSize();

        for (int i = 0; i < table.getDataSize(); i++) {
            if (getData()[i].isSelected()) {
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
        
        //Make a string of the selection
        for (int i = coords[0]; i <= coords[1]; i++) {
            if (getData()[i].isSelected()) {
                output.append(NumberUtil.stringValue(table.getData()[i].getRealValue()));
            } else {
            	output.append("x"); // x represents non-selected cell
            }
            if (i < coords[1]) {
            	output.append("\t");
            }
        }
        
        //Copy to clipboard
        if (copy) {
            setClipboard(output.toString());
        } 
    }
    
    //TODO: Clean this up
    protected void setClipboard(String s) {
        try {
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(s), null);
         } catch(IllegalStateException e) {
        	 
             try {
				Thread.sleep(20);
			} catch (InterruptedException e1) {}
             
             Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(s), null);
         }
    }
    
    public void copyTable() {
        String tableHeader = table.getSettings().getTableHeader();
        StringBuffer output = new StringBuffer(tableHeader);
        output.append(table.getTableAsString());
        
        setClipboard(String.valueOf(output));
    }

    public String getCellAsString(int index) {
        return data[index].getText();
    }

    public void pasteValues(String[] input) throws UserLevelException {
        //set real values
        for (int i = 0; i < input.length; i++) {
            try {
                Double.parseDouble(input[i]);
                data[i].getDataCell().setRealValue(input[i]);
            } catch (NumberFormatException ex) { /* not a number, do nothing */ }
        }
    }

    public void paste() throws UserLevelException {
    	String input;
    	
        try {
            input = (String) Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null).getTransferData(DataFlavor.stringFlavor);
        } catch (UnsupportedFlavorException ex) {
        	return;
        } catch (IOException ex) {
        	return;
        }
        
        paste(input);
    }
    
    public void paste(String s) throws UserLevelException {
    	StringTokenizer st = new StringTokenizer(s, Table.ST_DELIMITER);
        
        if (!table.isStaticDataTable()) {  
            String pasteType = st.nextToken();
            boolean selectedOnly = false;
            if ("[Selection1D]".equalsIgnoreCase(pasteType)) selectedOnly = true;
            
            if ("[Table1D]".equalsIgnoreCase(pasteType) || "[Selection1D]".equalsIgnoreCase(pasteType)) {
            	
            	//Find the leftmost selected cell as start
            	int startSelection = 0;           	
            	for(int i=0; i < data.length;i++) {
            		if(data[i].isSelected()) {
            			startSelection=i;
            			break;
            		}
            	}
            	
                if ((selectedOnly && data[startSelection].isSelected()) || !selectedOnly) {
                    int i = 0;
                    while (st.hasMoreTokens()) {
                        String currentToken = st.nextToken();
                        try {
                            if (!data[startSelection + i].getText().equalsIgnoreCase(currentToken)) {
                                data[startSelection + i].getDataCell().setRealValue(currentToken);
                            }
                        } catch (ArrayIndexOutOfBoundsException ex) {
                        	break;
                        }
                        i++;
                    }
                }
            }
        }
    }
    
    public void setCompareDisplay(Settings.CompareDisplay compareDisplay) {
        this.compareDisplay = compareDisplay;
        drawTable();
    }

    public Settings.CompareDisplay getCompareDisplay() {
        return this.compareDisplay;
    }
    
    public static Settings getSettings()
    {
        return SettingsManager.getSettings();
    }

    public TableToolBar getToolbar()
    {
        return ECUEditorManager.getECUEditor().getTableToolBar();
    }

    public void setOverlayLog(boolean overlayLog) {
        this.overlayLog = overlayLog;
        
        if(!overlayLog)
        {
        	clearLiveDataTrace();
        }
    }

    public boolean getOverlayLog()
    {
        return this.overlayLog;
    }

    public int getLiveDataIndex() {
        return liveDataIndex;
    }

    public int getPreviousLiveDataIndex() {
        return previousLiveDataIndex;
    }

    public void setLiveDataIndex(int index) {
        if (index < 0) {
            index = 0;
        }
        if (index >= data.length) {
            index = data.length - 1;
        }
        this.previousLiveDataIndex = this.liveDataIndex;
        this.liveDataIndex = index;
    }
    
    public double getLiveAxisValue() {
        try {
            return Double.parseDouble(liveAxisValue);
        } catch (NumberFormatException e) {
            return 0.0;
        }
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
    	if(tableLabel != null) {
	        if(null == table.name || table.name.isEmpty()) {
	            ;// Do not update label.
	        } else if(null == table.getCurrentScale () || "0x" == table.getCurrentScale().getUnit()) {
	            // static or no scale exists.
	            tableLabel.setText(getName());
	        } else {
	            tableLabel.setText(getName() + " (" + table.getCurrentScale().getUnit() + ")");
	        }
    	}
    }
    
    public String getName() {
    	return table.getName();
    }
    
    public static void showBadScalePopup(Table table, Scale scale) {
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(4, 1));
        panel.add(new JLabel(MessageFormat.format(
                rb.getString("REALBYTEINVALID"), table.toString())));
        panel.add(new JLabel(MessageFormat.format(
                rb.getString("REALVALUE"), scale.getExpression())));
        panel.add(new JLabel(MessageFormat.format(
                rb.getString("BYTEVALUE"), scale.getByteExpression())));

        JCheckBox check = new JCheckBox(rb.getString("DISPLAYMSG"), true);
        check.setHorizontalAlignment(JCheckBox.RIGHT);
        panel.add(check);
        
        
        check.addActionListener(
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        getSettings().setCalcConflictWarning(((JCheckBox) e.getSource()).isSelected());
                    }
                }
                );

        JOptionPane.showMessageDialog(null, panel,
                rb.getString("WARNING"), JOptionPane.ERROR_MESSAGE);
    }
}
