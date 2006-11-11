package enginuity.maps;

import enginuity.util.JEPUtil;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.Serializable;
import java.text.DecimalFormat;

public class DataCell extends JLabel implements MouseListener, Serializable {

    private double binValue = 0;
    private double originalValue = 0;
    private Scale scale = new Scale();
    private String displayValue = "";
    private Color scaledColor = new Color(0, 0, 0);
    private Color highlightColor = new Color(155, 155, 255);
    private Color increaseBorder = Color.RED;
    private Color decreaseBorder = Color.BLUE;
    private Boolean selected = false;
    private Boolean highlighted = false;
    private Table table;
    private int x = 0;
    private int y = 0;
    private double compareValue = 0;
    private int compareType = Table.COMPARE_OFF;
    private int compareDisplay = Table.COMPARE_ABSOLUTE;

    public DataCell() {
    }

    public DataCell(Scale scale) {
        this.scale = scale;
        this.setHorizontalAlignment(CENTER);
        this.setVerticalAlignment(CENTER);
        this.setFont(new Font("Arial", Font.BOLD, 12));
        this.setBorder(new LineBorder(Color.BLACK, 1));
        this.setOpaque(true);
        this.setVisible(true);
        this.addMouseListener(this);
    }

    public void updateDisplayValue() {
        DecimalFormat formatter = new DecimalFormat(scale.getFormat());

        if (getCompareType() == Table.COMPARE_OFF) {
            displayValue = formatter.format(calcDisplayValue(binValue, table.getScale().getExpression()));

        } else {
            if (getCompareDisplay() == Table.COMPARE_ABSOLUTE) {
                displayValue = formatter.format(
                        calcDisplayValue(binValue, table.getScale().getExpression()) -
                                calcDisplayValue(compareValue, table.getScale().getExpression()));

            } else if (getCompareDisplay() == Table.COMPARE_PERCENT) {
                double difference = calcDisplayValue(binValue, table.getScale().getExpression()) -
                        calcDisplayValue(compareValue, table.getScale().getExpression());
                if (difference == 0) {
                    displayValue = "0%";
                } else {
                    displayValue = (int) (difference / calcDisplayValue(binValue, table.getScale().getExpression()) * 100) + "%";
                }
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
        if (table.getStorageType() != Table.STORAGE_TYPE_FLOAT) {
            if (binValue < 0) {
                this.setBinValue(0);

            } else if (binValue > Math.pow(256, table.getStorageType()) - 1) {
                this.setBinValue((int) (Math.pow(256, table.getStorageType()) - 1));

            }
        }

        this.updateDisplayValue();
    }

    public double getBinValue() {
        return binValue;
    }

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
            table.getFrame().getToolBar().setFineValue(Math.abs(table.getScale().getFineIncrement()));
            table.getFrame().getToolBar().setCoarseValue(Math.abs(table.getScale().getCoarseIncrement()));
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

    public void mouseEntered(MouseEvent e) {
        table.highlight(x, y);
    }

    public void mousePressed(MouseEvent e) {
        if (!table.isStatic()) {
            if (!e.isControlDown()) {
                table.clearSelection();
            }
            table.startHighlight(x, y);
        }
        requestFocus();
    }

    public void mouseReleased(MouseEvent e) {
        if (!table.isStatic()) {
            table.stopHighlight();
        }
    }

    public void mouseClicked(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void increment(double increment) {
        double oldValue = Double.parseDouble(getText());

        if (table.getScale().getCoarseIncrement() < 0) {
            increment = 0 - increment;
        }

        setRealValue(String.valueOf((calcDisplayValue(binValue, scale.getExpression()) + increment)));

        // make sure table is incremented if change isnt great enough
        int maxValue = (int) Math.pow(8, (double) table.getStorageType());

        if (table.getStorageType() != Table.STORAGE_TYPE_FLOAT &&
                oldValue == Double.parseDouble(getText()) &&
                binValue > 0 &&
                binValue < maxValue) {
            System.out.println(maxValue + " " + binValue);
            increment(increment * 2);

        }

        table.colorize();

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
            this.setBorder(new LineBorder(Color.RED, 3));
        } else {
            this.setBorder(new LineBorder(Color.BLACK, 1));
        }
    }

    public void undo() {
        this.setBinValue(originalValue);
    }

    public void setRevertPoint() {
        this.setOriginalValue(binValue);
    }

    public void setRealValue(String input) {
        // create parser
        if (!"x".equalsIgnoreCase(input)) {
            double result = JEPUtil.evaluate(table.getScale().getByteExpression(), Double.parseDouble(input));

            if (table.getStorageType() == Table.STORAGE_TYPE_FLOAT) {
                this.setBinValue(result);

            } else {
                this.setBinValue((int) Math.round(result));

            }
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

    public int getCompareType() {
        return compareType;
    }

    public void setCompareType(int compareType) {
        this.compareType = compareType;
    }

    public int getCompareDisplay() {
        return compareDisplay;
    }

    public void setCompareRealValue(String input) {
        double result = JEPUtil.evaluate(table.getScale().getByteExpression(), Double.parseDouble(input));
        this.setCompareValue((int) Math.round(result));
    }

    public void setCompareDisplay(int compareDisplay) {
        this.compareDisplay = compareDisplay;
    }

    public void refreshValue() {
        setBinValue(binValue);
    }

    public void multiply(double factor) {
        setRealValue(String.valueOf(Double.parseDouble(getText()) * factor));
    }

    /**
     * Used by 3d graph renderer.
     */
    public double getValue() {
        return calcDisplayValue(binValue, table.getScale().getExpression());
    }
}