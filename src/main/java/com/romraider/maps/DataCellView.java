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
    private TableView tableView = null;
    
    private int x = 0;
    private int y = 0;
    

    private boolean highlighted = false;
    private boolean traced = false;
    private boolean tracedStale = false;
 
    private static final Border DEFAULT_BORDER = createLineBorder(new Color(0, 0, 0), 1);
    private static final Border INCREASE_BORDER = createLineBorder(getSettings().getIncreaseBorder(), 2);
    private static final Border DECREASE_BORDER = createLineBorder(getSettings().getDecreaseBorder(), 2);
    private static final Border CURLIVE_BORDER = createLineBorder(getSettings().getCurLiveValueColor(), 2);
    private static final Border STALELIVE_BORDER = createLineBorder(getSettings().getliveValueColor(), 2);
    
    
    public DataCellView(DataCell cell, TableView view) {
        this.dataCell = cell;
        this.tableView = view;
        this.setHorizontalAlignment(CENTER);
        this.setVerticalAlignment(CENTER);
        this.setFont(DEFAULT_FONT);
        this.setOpaque(true);
        this.setVisible(true);
        this.addMouseListener(this);
        
        cell.setDataView(this);
        this.y = cell.getIndexInTable();
        this.setPreferredSize(getSettings().getCellSize());      
    }
    
    public DataCellView(DataCell cell, TableView view, int x, int y) {
    	this(cell, view);
    	
        this.x = x;
        this.y = y;
    }
        
    public boolean equals (DataCellView v) {
    	return v.dataCell.equals(dataCell);
    }
    
    public DataCell getDataCell() {
    	return dataCell;
    }
    
    private static Settings getSettings() {
        return SettingsManager.getSettings();
    }
    
    public void drawCell() {
        if(tableView == null || tableView.isHidden()) {
            // Table will be null in the static case.
            return;
        }

        tableView.updatePresetPanel();
        //this.invalidate();
        setFont(getSettings().getTableFont());
        setText(getCellText());
        setToolTipText(getCellToolTip());
        setBackground(getCellBackgroundColor());
        setForeground(getCellTextColor());
        setBorder(getCellBorder());
        //this.validate();   
        //super.repaint();
    }

    private Color getCellBackgroundColor() {
        Settings settings = getSettings();
        Color backgroundColor;

        if(highlighted) {
            backgroundColor = settings.getHighlightColor();
        } else if(dataCell.isSelected()) {
            backgroundColor = settings.getSelectColor();
        } else if(null == tableView.getTable().getCompareTable()) {
            backgroundColor = getBinColor();
        }else {
            backgroundColor = getCompareColor();
        }
        
        return backgroundColor;
    }
    
    public Color getCompareColor() {
    	Table t = tableView.getTable();
    	
        if(tableView instanceof Table1DView) {;
	        if(((Table1DView)tableView).isAxis() && !getSettings().isColorAxis()) {
	            return getSettings().getAxisColor();
	        }
        }

        double compareScale;
        if (0.0 == dataCell.getCompareValue()) {
            return Settings.UNCHANGED_VALUE_COLOR;
        }else if(t.getMinCompare() == t.getMaxCompare()) {
            return getSettings().getMaxColor();
        } else {
            compareScale = (dataCell.getCompareValue() - t.getMinCompare()) / (t.getMaxCompare() - t.getMinCompare());
        }
        return getScaledColor(compareScale);
    }

    public Color getBinColor() {
    	Table t = tableView.getTable();
    	
        if(tableView instanceof Table1DView) {;
            if(((Table1DView)tableView).isAxis() && !getSettings().isColorAxis()) {
                return getSettings().getAxisColor();
            }
        }

        if (dataCell.getMaxAllowedBin() < dataCell.getBinValue()) {
            return getSettings().getWarningColor();
        } else if (dataCell.getMinAllowedBin() > dataCell.getBinValue()) {
            return getSettings().getWarningColor();
        } else {
            // limits not set, scale based on table values
            double colorScale;
            if (t.getMaxBin() - t.getMinBin() == 0.0) {
                // if all values are the same, color will be middle value
                colorScale = .5;
            } else {
                colorScale = (dataCell.getRealValue() - t.getMinReal()) / (t.getMaxReal() - t.getMinReal());
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
        	tableView.highlight(x, y);
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (!e.isControlDown()) {
        	dataCell.getTable().clearSelection();
        }

        if (e.isControlDown() && e.isAltDown()) {
            clearCell();
        } else {
        	tableView.startHighlight(x, y);
        }
        requestFocus();
        ECUEditorManager.getECUEditor().getTableToolBar().updateTableToolBar(dataCell.getTable());
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    	tableView.stopHighlight();
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
                if(tableView.getTable() instanceof Table1D) {
                    textColor = Settings.scaleTextColor;
                } else {
                    textColor = Settings.liveDataTraceTextColor;
                }
            } else {
                textColor = Settings.scaleTextColor;
            }
        } else if (highlighted) {
            textColor = Settings.highlightTextColor;
        } else if (dataCell.isSelected()) {
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

            if(null == tableView.getTable().getCompareTable()) {
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
    	if(tableView.getTable().isStaticDataTable()) {
            return getStaticText();
        }
               
        FORMATTER.applyPattern(tableView.getTable().getCurrentScale().getFormat());
        String displayString = "";

        if (null == tableView.getTable().getCompareTable()) {
            displayString = FORMATTER.format(dataCell.getRealValue());
        } else if (tableView.getCompareDisplay() == Settings.CompareDisplay.ABSOLUTE) {
            displayString = FORMATTER.format(dataCell.getRealCompareValue());
        } else if (tableView.getCompareDisplay() == Settings.CompareDisplay.PERCENT) {
            FORMATTER.applyPattern(PERCENT_FORMAT);
            if (dataCell.getCompareValue() == 0.0) {
                displayString = FORMATTER.format(0.0);
            } else {
                displayString = FORMATTER.format(dataCell.getRealCompareChangeValue());
            }
        }

        if(traced) {
            if(!(tableView.getTable() instanceof Table1D)) {
                displayString = getLiveValueString(displayString);
            }
        }
        return displayString;
    }

    private String getCellToolTip() {
        if(tableView.getTable().isStaticDataTable()) {
            return getStaticText();
        }
        String ttString = null;
        FORMATTER.applyPattern(TT_FORMAT);
        if (null == tableView.getTable().getCompareTable()) {
            ttString = FORMATTER.format(dataCell.getRealValue());
        } else if (tableView.getCompareDisplay() == Settings.CompareDisplay.ABSOLUTE) {
            ttString = FORMATTER.format(dataCell.getRealCompareValue());
        } else if (tableView.getCompareDisplay() == Settings.CompareDisplay.PERCENT) {
            FORMATTER.applyPattern(TT_PERCENT_FORMAT);
            if (dataCell.getCompareValue() == 0.0) {
                ttString = FORMATTER.format(0.0);
            } else {
                ttString = FORMATTER.format(dataCell.getRealCompareChangeValue());
            }
        }
        if(traced) {
            if(!(tableView.getTable() instanceof Table1D)) {
                ttString = getLiveValueString(ttString);
            }
        }
        return ttString;
    }
    
    
    private void clearCell() {
        if(isHighlighted()) {
            setHighlighted(false);
        }
        if(dataCell.isSelected()) {
        	dataCell.setSelected(false);
        }
    }
    
    public boolean isSelected() {
    	return dataCell.isSelected();
    }
    
    @Override
    public String toString() {
        return getCellText();
    }


    public void setHighlighted(boolean highlighted) {
        if(!tableView.getTable().isStaticDataTable() && this.highlighted != highlighted) {
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
            FORMATTER.applyPattern(tableView.getTable().getCurrentScale().getFormat());
            double staticDouble = Double.parseDouble(dataCell.getStaticText());
            displayString = FORMATTER.format(JEPUtil.evaluate(tableView.getTable().getCurrentScale().getExpression(), staticDouble));
        } catch (Exception ex) {
            displayString = dataCell.getStaticText();
        }
        return displayString;
    }

    public void setY(int y) {
        this.y = y;
    }
    
}
