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
import static com.romraider.util.ParamChecker.isNullOrEmpty;
import static javax.swing.BorderFactory.createLineBorder;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.Serializable;
import java.text.DecimalFormat;

import javax.swing.JLabel;
import javax.swing.border.Border;

import org.apache.log4j.Logger;

import com.romraider.Settings;
import com.romraider.editor.ecu.ECUEditorManager;
import com.romraider.util.JEPUtil;

public class DataCell extends JLabel implements MouseListener, Serializable {
    private static final long serialVersionUID = -2904293227148940937L;
    private static final Logger LOGGER = Logger.getLogger(DataCell.class);
    private final DecimalFormat PERCENT_FORMAT = new DecimalFormat("#,##0.0%");
    private final Font defaultFont = new Font("Arial", Font.BOLD, 12);

    private Scale scale = new Scale();
    private Table table;

    private final Color scaleTextColor = new Color(0, 0, 0);

    private final Color highlightColor = getSettings().getHighlightColor();
    private final Color highlightTextColor = new Color(255, 255, 255);

    private final Color selectColor = getSettings().getSelectColor();
    private final Color selectTextColor = new Color(0, 0, 0);

    private final Color liveDataTraceTextColor = new Color(229, 20, 0);

    private Boolean selected = false;
    private Boolean highlighted = false;
    private Boolean traced = false;

    private int x = 0;
    private int y = 0;

    private double binValue = 0.0;
    private double originalValue = 0.0;
    private double compareToValue = 0.0;
    private String liveValue = "";

    private final Color defaultBorderColor = new Color(0, 0, 0);
    private final Color increaseBorderColor = getSettings().getIncreaseBorder();
    private final Color decreaseBorderColor = getSettings().getDecreaseBorder();

    private String staticText = "";
    private boolean isStaticValue = false;

    public DataCell(String staticText) {
        this.isStaticValue = true;
        this.staticText = staticText;
        this.setHorizontalAlignment(CENTER);
        this.setVerticalAlignment(CENTER);
        this.setFont(defaultFont);
        this.setOpaque(true);
        this.setVisible(true);
    }

    public DataCell(Table table) {
        this.table = table;
    }

    public DataCell(Table table, double originalValue, int x, int y, Scale scale, Dimension size) {
        this.table = table;
        this.originalValue = originalValue;
        this.binValue = originalValue;
        this.x = x;
        this.y = y;
        this.scale = scale;
        this.setHorizontalAlignment(CENTER);
        this.setVerticalAlignment(CENTER);
        this.setFont(defaultFont);
        this.setOpaque(true);
        this.setVisible(true);
        this.addMouseListener(this);
        this.setPreferredSize(size);
    }

    public double getBinValue() {
        return binValue;
    }

    public double getRealValue() {
        return JEPUtil.evaluate(scale.getExpression(), binValue);
    }

    public void setRealValue(String input) {
        // create parser
        try {
            double result = 0.0;
            if (!"x".equalsIgnoreCase(input)) {
                result = JEPUtil.evaluate(scale.getByteExpression(), Double.parseDouble(input));
                if (table.getStorageType() != Settings.STORAGE_TYPE_FLOAT) {
                    result = (int) Math.round(result);
                }

                if(binValue != result) {
                    this.setBinValue(result);
                }
            }
        } catch (NumberFormatException e) {
            // Do nothing.  input is null or not a valid number.
        }
    }

    public void setStaticValue(String input) {
        // create parser
        try {
            double result = Double.parseDouble(input);
            binValue = result;
            originalValue = result;
        } catch (NumberFormatException e) {
            // Do nothing.  input is null or not a valid number.
        }
    }

    public double getCompareValue() {
        return binValue - compareToValue;
    }

    public double getRealCompareValue() {
        return JEPUtil.evaluate(scale.getExpression(), binValue) - JEPUtil.evaluate(scale.getExpression(), compareToValue);
    }

    public Color getCompareColor() {
        if(table instanceof Table1D) {
            Table1D checkTable = (Table1D)table;
            if(checkTable.isAxis()) {
                return getSettings().getAxisColor();
            }
        }

        double compareScale;
        if (0.0 == getCompareValue()) {
            return Settings.UNCHANGED_VALUE_COLOR;
        }else if(table.getMinCompare() == table.getMaxCompare()) {
            return getSettings().getMaxColor();
        } else {
            compareScale = (getCompareValue() - table.getMinCompare()) / (table.getMaxCompare() - table.getMinCompare());
        }
        return getScaledColor(compareScale);
    }

    public Color getBinColor() {
        if(table instanceof Table1D) {
            Table1D checkTable = (Table1D)table;
            if(checkTable.isAxis()) {
                return getSettings().getAxisColor();
            }
        }

        double scaleValue = getBinValue();
        table.getMaxValue();
        if (table.getMaxValue() < table.getMaxBin()) {
            return getSettings().getWarningColor();
        } else if (table.getMinValue() > table.getMinBin()) {
            return getSettings().getWarningColor();
        } else {
            // limits not set, scale based on table values
            double colorScale;
            if (table.getMaxBin() - table.getMinBin() == 0.0) {
                // if all values are the same, color will be middle value
                colorScale = .5;
            } else {
                colorScale = (scaleValue - table.getMinBin()) / (table.getMaxBin() - table.getMinBin());
            }

            return getScaledColor(colorScale);
        }
    }

    public void drawCell() {
        if(!isStaticValue && (table == null || !table.isLoaded()) ) {
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
        if(null != table) {
            table.repaint();
        }
    }

    private Color getCellBackgroundColor() {
        if(isStaticValue) {
            return getSettings().getAxisColor();
        }

        Color backgroundColor;
        if(highlighted) {
            backgroundColor = highlightColor;
        } else if(selected) {
            backgroundColor = selectColor;
        } else if(!table.comparing) {
            backgroundColor = getBinColor();
        }else {
            backgroundColor = getCompareColor();
        }
        return backgroundColor;
    }

    private Color getCellTextColor() {
        if(isStaticValue) {
            return scaleTextColor;
        }

        Color textColor;
        if(traced) {
            textColor = liveDataTraceTextColor;
        } else if (highlighted) {
            textColor = highlightTextColor;
        } else if (selected) {
            textColor = selectTextColor;
        } else {
            textColor = scaleTextColor;
        }
        return textColor;
    }

    private Border getCellBorder() {
        if(isStaticValue) {
            return createLineBorder(defaultBorderColor, 1);
        }

        Border border;
        if(table.comparing) {
            if (compareToValue < binValue) {
                border = createLineBorder(increaseBorderColor, 2);
            } else if (compareToValue > binValue) {
                border = createLineBorder(decreaseBorderColor, 2);
            } else {
                border = createLineBorder(defaultBorderColor, 1);
            }
        } else {
            if (originalValue < binValue) {
                border = createLineBorder(increaseBorderColor, 2);
            } else if (originalValue > binValue) {
                border = createLineBorder(decreaseBorderColor, 2);
            } else {
                border = createLineBorder(defaultBorderColor, 1);
            }
        }

        return border;
    }

    private String getCellText() {
        if(isStaticValue) {
            return staticText;
        }

        DecimalFormat formatter = new DecimalFormat(scale.getFormat());
        String displayString = "";

        if (!table.comparing) {
            if(table.getDisplayValueType() == Settings.DATA_TYPE_REAL) {
                displayString = formatter.format(getRealValue());
            } else {
                displayString = formatter.format(getBinValue());
            }

        } else if (table.getCompareDisplay() == Settings.COMPARE_DISPLAY_ABSOLUTE) {
            if(table.getDisplayValueType() == Settings.DATA_TYPE_REAL) {
                displayString = formatter.format(getRealCompareValue());
            } else {
                displayString = formatter.format(getCompareValue());
            }

        } else if (table.getCompareDisplay() == Settings.COMPARE_DISPLAY_PERCENT) {
            if (getCompareValue() == 0.0) {
                displayString = PERCENT_FORMAT.format(0.0);
            } else {
                if(table.getDisplayValueType() == Settings.DATA_TYPE_REAL) {
                    displayString = PERCENT_FORMAT.format(getRealCompareValue());
                } else {
                    displayString = PERCENT_FORMAT.format(getCompareValue());
                }
            }
        }

        if(traced) {
            displayString = displayString + (isNullOrEmpty(liveValue) ? "" : (':' + liveValue));
        }
        return displayString;
    }

    private String getCellToolTip() {
        if(isStaticValue) {
            return staticText;
        }

        String displayToolTipString = "";

        if (!table.comparing) {
            if(table.getDisplayValueType() == Settings.DATA_TYPE_REAL) {
                displayToolTipString = Double.toString(getRealValue());
            } else {
                displayToolTipString = Double.toString(getBinValue());
            }

        } else if (table.getCompareDisplay() == Settings.COMPARE_DISPLAY_ABSOLUTE) {
            if(table.getDisplayValueType() == Settings.DATA_TYPE_REAL) {
                displayToolTipString = Double.toString(getRealCompareValue());
            } else {
                displayToolTipString = Double.toString(getCompareValue());
            }

        } else if (table.getCompareDisplay() == Settings.COMPARE_DISPLAY_PERCENT) {
            if (getCompareValue() == 0.0) {
                displayToolTipString = PERCENT_FORMAT.format(0.0);
            } else {
                if(table.getDisplayValueType() == Settings.DATA_TYPE_REAL) {
                    displayToolTipString = Double.toString(getRealCompareValue());
                } else {
                    displayToolTipString = Double.toString(getCompareValue());
                }
            }
        }

        if(traced) {
            displayToolTipString = displayToolTipString + (isNullOrEmpty(liveValue) ? "" : (':' + liveValue));
        }
        return displayToolTipString;
    }

    public void setBinValue(double newBinValue) {
        if(binValue == newBinValue) {
            return;
        }

        double checkedValue = newBinValue;

        // make sure it's in range
        if (table.getStorageType() != Settings.STORAGE_TYPE_FLOAT) {
            if (table.isSignedData()) {
                int minAllowedValue = 0;
                int maxAllowedValue = 0;
                switch (table.getStorageType()) {
                case 1:
                    minAllowedValue = Byte.MIN_VALUE;
                    maxAllowedValue = Byte.MAX_VALUE;
                    break;
                case 2:
                    minAllowedValue = Short.MIN_VALUE;
                    maxAllowedValue = Short.MAX_VALUE;
                    break;
                case 4:
                    minAllowedValue = Integer.MIN_VALUE;
                    maxAllowedValue = Integer.MAX_VALUE;
                    break;
                }
                if (checkedValue < minAllowedValue ) {
                    checkedValue = minAllowedValue;
                }
                else if (checkedValue > maxAllowedValue) {
                    checkedValue = maxAllowedValue;
                }
            }
            else {
                if (checkedValue < 0) {
                    checkedValue = 0;
                }
                else if (checkedValue > Math.pow(256, table.getStorageType()) - 1) {
                    checkedValue = (int) (Math.pow(256, table.getStorageType()) - 1);
                }
            }
        }


        if(binValue == checkedValue) {
            return;
        }


        // get current real and compare.
        double currentBin = getBinValue();
        double currentCompare = getCompareValue();

        // set bin.
        binValue = checkedValue;

        table.refreshCompares();

        // get new real and compare.
        double compareValue = getCompareValue();

        // check for compare refresh.
        if( (currentCompare == table.getMaxCompare() && compareValue < currentCompare) ||
                (currentCompare == table.getMinCompare() && compareValue > currentCompare)) {
            // look for a new max or min.
            table.refreshDataBounds();
            return;
        }

        boolean drawTable = false;

        // if new max set max.
        if(compareValue > table.getMaxCompare()) {
            table.setMaxCompare(compareValue);
            drawTable = true;
        }
        // if new min set min.
        if(compareValue < table.getMinCompare()) {
            table.setMinCompare(compareValue);
            drawTable = true;
        }

        // check for bin refresh.
        if( (currentBin == table.getMaxBin() && checkedValue < currentBin) ||
                (currentBin == table.getMinBin() && checkedValue > currentBin)){
            // look for a new max or min.
            table.refreshDataBounds();
            return;
        }

        // if new max set max.
        if(checkedValue > table.getMaxBin()) {
            table.setMaxBin(checkedValue);
            drawTable = true;
        }
        // if new min set min.
        if(checkedValue < table.getMinBin()) {
            table.setMinBin(checkedValue);
            drawTable = true;
        }

        if(drawTable) {
            table.drawTable();
        } else {
            drawCell();
        }
    }

    @Override
    public String toString() {
        return getCellText();
    }

    public Boolean isSelected() {
        return selected;
    }

    public void setSelected(Boolean selected) {
        if(this.selected != selected) {
            this.selected = selected;
            drawCell();
        }
    }

    public void setHighlighted(Boolean highlighted) {
        if(this.highlighted != highlighted) {
            this.highlighted = highlighted;
            drawCell();
        }
    }

    public boolean isHighlighted() {
        return highlighted;
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        table.highlight(x, y);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (!e.isControlDown()) {
            table.clearSelection();
        }
        table.startHighlight(x, y);
        requestFocus();
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

    public void increment(double increment) {
        double oldValue = getRealValue();

        if (table.getScale().getCoarseIncrement() < 0.0) {
            increment = 0.0 - increment;
        }

        double incResult = JEPUtil.evaluate(scale.getByteExpression(), (oldValue + increment));
        if (table.getStorageType() == Settings.STORAGE_TYPE_FLOAT) {
            if(binValue != incResult) {
                this.setBinValue(incResult);
            }
        } else {
            int roundResult = (int) Math.round(incResult);
            if(binValue != roundResult) {
                this.setBinValue(roundResult);
            }
        }

        // make sure table is incremented if change isn't great enough
        int maxValue = (int) Math.pow(8, table.getStorageType());

        if (table.getStorageType() != Settings.STORAGE_TYPE_FLOAT &&
                oldValue == getRealValue() &&
                binValue > 0.0 &&
                binValue < maxValue) {
            LOGGER.debug(maxValue + " " + binValue);
            increment(increment * 2);
        }
    }

    public void undo() {
        this.setBinValue(originalValue);
    }

    public void setRevertPoint() {
        this.setOriginalValue(binValue);
    }

    public void setOriginalValue(double originalValue) {
        this.originalValue = originalValue;
    }

    public void setCompareValue(DataCell compareCell) {
        if(Settings.DATA_TYPE_BIN == table.getCompareValueType())
        {
            if(this.compareToValue == compareCell.binValue) {
                return;
            }

            this.compareToValue = compareCell.binValue;
        } else {
            if(this.compareToValue == compareCell.originalValue) {
                return;
            }

            this.compareToValue = compareCell.originalValue;
        }

        drawCell();
    }

    public void multiply(double factor) {
        setBinValue(binValue * factor);
    }

    public void setLiveDataTrace(boolean trace) {
        if(traced != trace) {
            traced = trace;
            drawCell();
        }
    }

    public void setLiveDataTraceValue(String liveValue) {
        if(this.liveValue != liveValue) {
            this.liveValue = liveValue;
            drawCell();
        }
    }

    public double getLiveDataTraceValue() {
        try {
            return Double.parseDouble(liveValue);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private Settings getSettings() {
        return ECUEditorManager.getECUEditor().getSettings();
    }

    @Override
    public boolean equals(Object other) {
        if(other == null) {
            return false;
        }

        if(!(other instanceof DataCell)) {
            return false;
        }

        DataCell otherCell = (DataCell) other;
        return binValue == otherCell.binValue;
    }

}