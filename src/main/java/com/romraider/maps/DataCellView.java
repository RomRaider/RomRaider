package com.romraider.maps;

import static com.romraider.util.ColorScaler.getScaledColor;
import static com.romraider.util.ParamChecker.isNullOrEmpty;
import static javax.swing.BorderFactory.createLineBorder;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.Serializable;
import java.text.DecimalFormat;

import javax.swing.JLabel;
import javax.swing.border.Border;

import com.romraider.Settings;
import com.romraider.editor.ecu.ECUEditorManager;
import com.romraider.util.JEPUtil;
import com.romraider.util.NumberUtil;
import com.romraider.util.SettingsManager;

public class DataCellView extends JLabel implements MouseListener, Serializable {
	private static final long serialVersionUID = 1L;
	static final Font DEFAULT_FONT = new Font("Arial", Font.BOLD, 12);
    static final String ST_DELIMITER = "\t\n\r\f";
    static final DecimalFormat FORMATTER = new DecimalFormat();
    static final String PERCENT_FORMAT = "#,##0.0%";
    static final String TT_FORMAT = "#,##0.##########";
    static final String TT_PERCENT_FORMAT = "#,##0.0#########%";
    static final String REPLACE_TEXT = "\u0020|\u00a0";
    
    static int UNSELECT_MASK1 = MouseEvent.BUTTON1_DOWN_MASK + MouseEvent.CTRL_DOWN_MASK + MouseEvent.ALT_DOWN_MASK;
    static int UNSELECT_MASK2 = MouseEvent.BUTTON3_DOWN_MASK + MouseEvent.CTRL_DOWN_MASK + MouseEvent.ALT_DOWN_MASK;
        
    private DataCell dataCell; //Data Source
    private Table table = null;
    
    private int x = 0;
    private int y = 0;
    
    private boolean selected = false;
    private boolean highlighted = false;
    private boolean traced = false;
    private boolean tracedStale = false;
 
    private static final Border DEFAULT_BORDER = createLineBorder(new Color(0, 0, 0), 1);
    private static final Border INCREASE_BORDER = createLineBorder(getSettings().getIncreaseBorder(), 2);
    private static final Border DECREASE_BORDER = createLineBorder(getSettings().getDecreaseBorder(), 2);
    private static final Border CURLIVE_BORDER = createLineBorder(getSettings().getCurLiveValueColor(), 2);
    private static final Border STALELIVE_BORDER = createLineBorder(getSettings().getliveValueColor(), 2);
    
    
    public DataCellView(DataCell cell) {
        this.dataCell = cell;
        this.table = cell.getTable();
        this.setHorizontalAlignment(CENTER);
        this.setVerticalAlignment(CENTER);
        this.setFont(DEFAULT_FONT);
        this.setOpaque(true);
        this.setVisible(true);
        this.addMouseListener(this);
        
        cell.addDataView(this);
    }
    
    public DataCellView(DataCell cell, int x, int y) {
    	this(cell);
    	
        this.x = x;
        this.y = y;
        this.setPreferredSize(getSettings().getCellSize());
    }
    
    public DataCell getDataCell() {
    	return dataCell;
    }
    
    private static Settings getSettings() {
        return SettingsManager.getSettings();
    }
    
    public void drawCell() {
        if(table == null) {
            // Table will be null in the static case.
            return;
        }

        this.invalidate();
        setFont(getSettings().getTableFont());
        setText(getCellText());
        setToolTipText(getCellToolTip());
        setBackground(getCellBackgroundColor());
        setForeground(getCellTextColor());
        setBorder(getCellBorder());
        this.validate();
        this.repaint();
        //table.validate();
       // table.repaint();      
    }

    private Color getCellBackgroundColor() {
        Settings settings = getSettings();
        Color backgroundColor;

        if(highlighted) {
            backgroundColor = settings.getHighlightColor();
        } else if(selected) {
            backgroundColor = settings.getSelectColor();
        } else if(null == table.getCompareTable()) {
            backgroundColor = getBinColor();
        }else {
            backgroundColor = getCompareColor();
        }
        
        return backgroundColor;
    }
    
    public Color getCompareColor() {
        if(table instanceof Table1D) {
            Table1D checkTable = (Table1D)table;
            if(checkTable.isAxis() && !getSettings().isColorAxis()) {
                return getSettings().getAxisColor();
            }
        }

        double compareScale;
        if (0.0 == dataCell.getCompareValue()) {
            return Settings.UNCHANGED_VALUE_COLOR;
        }else if(table.getMinCompare() == table.getMaxCompare()) {
            return getSettings().getMaxColor();
        } else {
            compareScale = (dataCell.getCompareValue() - table.getMinCompare()) / (table.getMaxCompare() - table.getMinCompare());
        }
        return getScaledColor(compareScale);
    }

    public Color getBinColor() {
        if(table instanceof Table1D) {
            Table1D checkTable = (Table1D)table;
            if(checkTable.isAxis() && !getSettings().isColorAxis()) {
                return getSettings().getAxisColor();
            }
        }

        if (table.getMaxAllowedBin() < dataCell.getBinValue()) {
            return getSettings().getWarningColor();
        } else if (table.getMinAllowedBin() > dataCell.getBinValue()) {
            return getSettings().getWarningColor();
        } else {
            // limits not set, scale based on table values
            double colorScale;
            if (table.getMaxBin() - table.getMinBin() == 0.0) {
                // if all values are the same, color will be middle value
                colorScale = .5;
            } else {
                colorScale = (dataCell.getRealValue() - table.getMinReal()) / (table.getMaxReal() - table.getMinReal());
            }

            return getScaledColor(colorScale);
        }
    }
      
    @Override
    public void mouseEntered(MouseEvent e) {
        if (UNSELECT_MASK1 == (e.getModifiersEx() & UNSELECT_MASK1)) {
            clearCell();
        } else if (UNSELECT_MASK2 == (e.getModifiersEx() & UNSELECT_MASK2)) {
            clearCell();
        } else {
        	table.highlight(x, y);
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (!e.isControlDown()) {
        	table.clearSelection();
        }

        if (e.isControlDown() && e.isAltDown()) {
            clearCell();
        } else {
        	table.startHighlight(x, y);
        }
        requestFocus();
        ECUEditorManager.getECUEditor().getTableToolBar().updateTableToolBar(dataCell.getTable());
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    	table.stopHighlight();
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }
    
    private Color getCellTextColor() {
        Color textColor;

        if(traced) {
            if(!dataCell.getLiveValue().isEmpty()) {
                if(table instanceof Table1D) {
                    textColor = Settings.scaleTextColor;
                } else {
                    textColor = Settings.liveDataTraceTextColor;
                }
            } else {
                textColor = Settings.scaleTextColor;
            }
        } else if (highlighted) {
            textColor = Settings.highlightTextColor;
        } else if (selected) {
            textColor = Settings.selectTextColor;
        } else {
            textColor = Settings.scaleTextColor;
        }

        return textColor;
    }

    private Border getCellBorder() {
        Border border;
        if(traced) {
            border = CURLIVE_BORDER;
            if(tracedStale) {
                border = STALELIVE_BORDER;
            }
        } else {
            double checkValue;

            if(null == table.getCompareTable()) {
                checkValue= dataCell.getOriginalValue();
            } else {
                checkValue = dataCell.getCompareToValue();
            }

            if (checkValue < dataCell.getBinValue()) {
                border = INCREASE_BORDER;
            } else if (checkValue > dataCell.getBinValue()) {
                border = DECREASE_BORDER;
            } else {
                border = DEFAULT_BORDER;
            }
        }

        return border;
    }

    public String getCellText() {
        if(table.isStaticDataTable()) {
            return getStaticText();
        }

        FORMATTER.applyPattern(table.getCurrentScale().getFormat());
        String displayString = "";

        if (null == table.getCompareTable()) {
            displayString = FORMATTER.format(dataCell.getRealValue());
        } else if (table.getCompareDisplay() == Settings.CompareDisplay.ABSOLUTE) {
            displayString = FORMATTER.format(dataCell.getRealCompareValue());
        } else if (table.getCompareDisplay() == Settings.CompareDisplay.PERCENT) {
            FORMATTER.applyPattern(PERCENT_FORMAT);
            if (dataCell.getCompareValue() == 0.0) {
                displayString = FORMATTER.format(0.0);
            } else {
                displayString = FORMATTER.format(dataCell.getRealCompareChangeValue());
            }
        }

        if(traced) {
            if(!(table instanceof Table1D)) {
                displayString = getLiveValueString(displayString);
            }
        }
        return displayString;
    }

    private String getCellToolTip() {
        if(table.isStaticDataTable()) {
            return getStaticText();
        }
        String ttString = null;
        FORMATTER.applyPattern(TT_FORMAT);
        if (null == table.getCompareTable()) {
            ttString = FORMATTER.format(dataCell.getRealValue());
        } else if (table.getCompareDisplay() == Settings.CompareDisplay.ABSOLUTE) {
            ttString = FORMATTER.format(dataCell.getRealCompareValue());
        } else if (table.getCompareDisplay() == Settings.CompareDisplay.PERCENT) {
            FORMATTER.applyPattern(TT_PERCENT_FORMAT);
            if (dataCell.getCompareValue() == 0.0) {
                ttString = FORMATTER.format(0.0);
            } else {
                ttString = FORMATTER.format(dataCell.getRealCompareChangeValue());
            }
        }
        if(traced) {
            if(!(table instanceof Table1D)) {
                ttString = getLiveValueString(ttString);
            }
        }
        return ttString;
    }
    
    
    private void clearCell() {
        if(isHighlighted()) {
            setHighlighted(false);
        }
        if(isSelected()) {
            setSelected(false);
        }
    }

    
    @Override
    public String toString() {
        return getCellText();
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        if(!table.isStaticDataTable() && this.selected != selected) {
            this.selected = selected;
            drawCell();
        }
    }

    public void setHighlighted(boolean highlighted) {
        if(!table.isStaticDataTable() && this.highlighted != highlighted) {
            this.highlighted = highlighted;
            drawCell();
        }
    }

    public boolean isHighlighted() {
        return highlighted;
    }
    
    public void setLiveDataTrace(boolean trace) {
        if(traced != trace) {
            traced = trace;
            drawCell();
        }
    }

    public void setPreviousLiveDataTrace(boolean trace) {
        if(tracedStale != trace) {
            tracedStale = trace;
            drawCell();
        }
    }
    
    private String getLiveValueString(String currentValue) {
        return currentValue + (isNullOrEmpty(dataCell.getLiveValue()) ? Settings.BLANK : (':' + dataCell.getLiveValue()));
    }
    
    public String getStaticText() {
        String displayString = null;
        try {
            FORMATTER.applyPattern(table.getCurrentScale().getFormat());
            double staticDouble = NumberUtil.doubleValue(dataCell.getStaticText());
            displayString = FORMATTER.format(JEPUtil.evaluate(table.getCurrentScale().getExpression(), staticDouble));
        } catch (Exception ex) {
            displayString = dataCell.getStaticText();
        }
        return displayString;
    }

    public void setY(int y) {
        this.y = y;
    }
    
}
