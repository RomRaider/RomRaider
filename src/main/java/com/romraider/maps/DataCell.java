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

import static java.awt.Color.BLACK;
import static java.awt.Color.BLUE;
import static java.awt.Color.RED;
import static java.lang.Math.abs;
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
import com.romraider.util.JEPUtil;

public class DataCell extends JLabel implements MouseListener, Serializable {
    private static final long serialVersionUID = -2904293227148940937L;
    private static final Logger LOGGER = Logger.getLogger(DataCell.class);
    private final DecimalFormat PERCENT_FORMAT = new DecimalFormat("#,##0.0%");
    private final Border defaultBorder = createLineBorder(BLACK, 1);
    private final Border modifiedBorder = createLineBorder(RED, 3);
    private final Font defaultFont = new Font("Arial", Font.BOLD, 12);
    private double binValue = 0;
    private double originalValue = 0;
    private Scale scale = new Scale();
    private String displayValue = "";
    private Color scaledColor = new Color(0, 0, 0);
    private Color highlightColor = new Color(155, 155, 255);
    private Color increaseBorder = RED;
    private Color decreaseBorder = BLUE;
    private Boolean selected = false;
    private Boolean highlighted = false;
    private Table table;
    private int x = 0;
    private int y = 0;
    private double compareValue = 0;
    private int compareDisplay = Settings.COMPARE_DISPLAY_OFF;

    public DataCell() {
    }

    public DataCell(Scale scale, Dimension size) {
        this.scale = scale;
        this.setHorizontalAlignment(CENTER);
        this.setVerticalAlignment(CENTER);
        this.setFont(defaultFont);
        this.setBorder(defaultBorder);
        this.setOpaque(true);
        this.setVisible(true);
        this.addMouseListener(this);
        this.setPreferredSize(size);
    }

    public void updateDisplayValue() {
        DecimalFormat formatter = new DecimalFormat(scale.getFormat());

        if (getCompareDisplay() == Settings.COMPARE_DISPLAY_OFF) {
            displayValue = getRealValue();

        } else if (getCompareDisplay() == Settings.COMPARE_DISPLAY_ABSOLUTE) {
            displayValue = formatter.format(
                    calcDisplayValue(binValue, table.getScale().getExpression()) -
                    calcDisplayValue(compareValue, table.getScale().getExpression()));

        } else if (getCompareDisplay() == Settings.COMPARE_DISPLAY_PERCENT) {
            String expression = table.getScale().getExpression();
            double thisValue = calcDisplayValue(binValue, expression);
            double thatValue = calcDisplayValue(compareValue, expression);
            double difference = thisValue - thatValue;
            if (difference == 0) {
                displayValue = PERCENT_FORMAT.format(0.0);
            } else if (thatValue == 0.0) {
                displayValue = '\u221e' + "%";
            } else {
                double d = difference / abs(thatValue);
                displayValue = PERCENT_FORMAT.format(d);
            }
        }
        setText(displayValue);
    }

    public double calcDisplayValue(double input, String expression) {
        return JEPUtil.evaluate(expression, input);
    }

    public void setColor(Color color) {
        scaledColor = color;
        if (!selected) {
            super.setBackground(color);
        }
    }

    public void setDisplayValue(String displayValue) {
        this.displayValue = displayValue;
        this.setText(displayValue);
    }

    public void setBinValue(double binValue) {
        this.binValue = binValue;

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
                if (binValue < minAllowedValue ) {
                    this.setBinValue(minAllowedValue);
                }
                else if (binValue > maxAllowedValue) {
                    this.setBinValue(maxAllowedValue);
                }
            }
            else {
                if (binValue < 0) {
                    this.setBinValue(0);
                }
                else if (binValue > Math.pow(256, table.getStorageType()) - 1) {
                    this.setBinValue((int) (Math.pow(256, table.getStorageType()) - 1));
                }
            }
        }
        this.updateDisplayValue();
        table.refreshCompares();
    }

    public double getBinValue() {
        return binValue;
    }

    @Override
    public String toString() {
        return displayValue;
    }

    public Boolean isSelected() {
        return selected;
    }

    public void setSelected(Boolean selected) {
        this.selected = selected;
        if (selected) {
            this.setBackground(getHighlightColor());
            table.getToolbar().setFineValue(abs(table.getScale().getFineIncrement()));
            table.getToolbar().setCoarseValue(abs(table.getScale().getCoarseIncrement()));
        } else {
            this.setBackground(scaledColor);
        }

        //TODO Uncomment if needed after further testing
        //Removed to test with 3d graph
        //requestFocus();
    }

    public void setHighlighted(Boolean highlighted) {
        if (!table.isStatic()) {
            this.highlighted = highlighted;
            if (highlighted) {
                this.setBackground(getHighlightColor());
            } else {
                if (!selected) {
                    this.setBackground(scaledColor);
                }
            }
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
        if (!table.isStatic()) {
            if (!e.isControlDown()) {
                table.clearSelection();
            }
            table.startHighlight(x, y);
        }
        requestFocus();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (!table.isStatic()) {
            table.stopHighlight();
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    public void increment(double increment) {
        updateDisplayValue();

        double oldValue = Double.parseDouble(getText());

        if (table.getScale().getCoarseIncrement() < 0) {
            increment = 0 - increment;
        }

        setRealValue(String.valueOf((calcDisplayValue(binValue, scale.getExpression()) + increment)));

        // make sure table is incremented if change isn't great enough
        int maxValue = (int) Math.pow(8, table.getStorageType());

        if (table.getStorageType() != Settings.STORAGE_TYPE_FLOAT &&
                oldValue == Double.parseDouble(getText()) &&
                binValue > 0 &&
                binValue < maxValue) {
            LOGGER.debug(maxValue + " " + binValue);
            increment(increment * 2);
        }

    }

    public void setTable(Table table) {
        this.table = table;
    }

    public void setXCoord(int x) {
        this.x = x;
    }

    public void setYCoord(int y) {
        this.y = y;
    }

    public double getOriginalValue() {
        return originalValue;
    }

    public void setOriginalValue(double originalValue) {
        this.originalValue = originalValue;
        if (binValue != getOriginalValue()) {
            this.setBorder(modifiedBorder);
        } else {
            this.setBorder(defaultBorder);
        }
    }

    public void undo() {
        if(this.getBinValue() != originalValue) {
            this.setBinValue(originalValue);
        }
    }

    public void setRevertPoint() {
        this.setOriginalValue(binValue);
    }

    public double getValue() {
        return calcDisplayValue(binValue, table.getScale().getExpression());
    }

    public String getRealValue() {
        return new DecimalFormat(scale.getFormat()).format(getValue());
    }

    public void setRealValue(String input) {
        // create parser
        try {
            if (!"x".equalsIgnoreCase(input)) {
                double result = JEPUtil.evaluate(table.getScale().getByteExpression(), Double.parseDouble(input));
                if (table.getStorageType() == Settings.STORAGE_TYPE_FLOAT) {
                    if(this.getBinValue() != result) {
                        this.setBinValue(result);
                    }
                } else {
                    int roundResult = (int) Math.round(result);
                    if(this.getBinValue() != roundResult) {
                        this.setBinValue(roundResult);
                    }
                }
            }
        } catch (NumberFormatException e) {
            // Do nothing.  input is null or not a valid number.
        }
    }

    public Color getHighlightColor() {
        return highlightColor;
    }

    public void setHighlightColor(Color highlightColor) {
        this.highlightColor = highlightColor;
    }

    public Color getIncreaseBorder() {
        return increaseBorder;
    }

    public void setIncreaseBorder(Color increaseBorder) {
        this.increaseBorder = increaseBorder;
    }

    public Color getDecreaseBorder() {
        return decreaseBorder;
    }

    public void setDecreaseBorder(Color decreaseBorder) {
        this.decreaseBorder = decreaseBorder;
    }

    public double getCompareValue() {
        return compareValue;
    }

    public void setCompareValue(double compareValue) {
        this.compareValue = compareValue;
    }

    public int getCompareDisplay() {
        return compareDisplay;
    }

    public void setCompareDisplay(int compareDisplay) {
        this.compareDisplay = compareDisplay;
    }

    public void refreshValue() {
        setBinValue(binValue);
    }

    public void multiply(double factor) {
        updateDisplayValue();
        setRealValue(String.valueOf(Double.parseDouble(getText()) * factor));
    }

    public void setLiveDataTrace(boolean trace) {
        if (trace) {
            //setBorder(liveDataTraceBorder);
            setForeground(RED);
        } else {
            //setBorder(defaultBorder);
            setForeground(BLACK);
        }
    }
}