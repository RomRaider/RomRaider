/*
 *
 * Enginuity Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006 Enginuity.org
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
 *
 */

package enginuity.maps;

import enginuity.Settings;
import enginuity.swing.TableFrame;
import enginuity.util.AxisRange;
import static enginuity.util.ParamChecker.isNullOrEmpty;
import static enginuity.util.TableAxisUtil.getLiveDataRangeForAxis;

import javax.swing.JLabel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.util.StringTokenizer;

public class Table2D extends Table {
    private static final String NEW_LINE = System.getProperty("line.separator");
    private Table1D axis = new Table1D(new Settings());

    public Table2D(Settings settings) {
        super(settings);
        verticalOverhead += 18;
    }

    public Table1D getAxis() {
        return axis;
    }

    public void setAxis(Table1D axis) {
        this.axis = axis;
    }

    public String toString() {
        return super.toString() + " (2D)";// + axis;
    }

    public void colorize() {
        super.colorize();
        axis.colorize();
    }

    public void setFrame(TableFrame frame) {
        this.frame = frame;
        axis.setFrame(frame);
        frame.setSize(getFrameSize());
    }

    public Dimension getFrameSize() {
        int height = verticalOverhead + cellHeight * 2;
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

    public void applyColorSettings(Settings settings) {
        this.setAxisColor(settings.getAxisColor());
        axis.applyColorSettings(settings);
        super.applyColorSettings(settings);
    }

    public void populateTable(byte[] input) throws ArrayIndexOutOfBoundsException {
        centerLayout.setRows(2);
        centerLayout.setColumns(this.getDataSize());

        try {
            axis.setRom(container);
            axis.populateTable(input);
            super.populateTable(input);
        } catch (ArrayIndexOutOfBoundsException ex) {
            throw new ArrayIndexOutOfBoundsException();
        }

        // add to table
        for (int i = 0; i < this.getDataSize(); i++) {
            centerPanel.add(axis.getDataCell(i));
        }
        for (int i = 0; i < this.getDataSize(); i++) {
            centerPanel.add(this.getDataCell(i));
        }
        add(new JLabel(axis.getName() + " (" + axis.getScale().getUnit() + ")", JLabel.CENTER), BorderLayout.NORTH);

        if (axis.isStatic()) {
            add(new JLabel(axis.getName(), JLabel.CENTER), BorderLayout.NORTH);
        } else {
            add(new JLabel(axis.getName() + " (" + axis.getScale().getUnit() + ")", JLabel.CENTER), BorderLayout.NORTH);
        }

        add(new JLabel(scales.get(scaleIndex).getUnit(), JLabel.CENTER), BorderLayout.SOUTH);

        //this.colorize();
    }

    public void increment(double increment) {
        super.increment(increment);
        axis.increment(increment);
    }

    public void multiply(double factor) {
        super.multiply(factor);
        axis.multiply(factor);
    }

    public void clearSelection() {
        axis.clearSelection(true);
        for (DataCell aData : data) {
            aData.setSelected(false);
        }
    }

    public void setRevertPoint() {
        super.setRevertPoint();
        axis.setRevertPoint();
    }

    public void undoAll() {
        super.undoAll();
        axis.undoAll();
    }

    public void undoSelected() {
        super.undoSelected();
        axis.undoSelected();
    }

    public byte[] saveFile(byte[] binData) {
        binData = super.saveFile(binData);
        binData = axis.saveFile(binData);
        return binData;
    }

    public void setRealValue(String realValue) {
        axis.setRealValue(realValue);
        super.setRealValue(realValue);
    }

    public void addKeyListener(KeyListener listener) {
        super.addKeyListener(listener);
        axis.addKeyListener(listener);
    }

    public void selectCellAt(int y, Table1D axisType) {
        selectCellAt(y);
    }

    public void cursorUp() {
        if (!axis.isStatic() && data[highlightY].isSelected()) {
            axis.selectCellAt(highlightY);
        }
    }

    public void cursorDown() {
        axis.cursorDown();
    }

    public void cursorLeft() {
        if (highlightY > 0 && data[highlightY].isSelected()) {
            selectCellAt(highlightY - 1);
        } else {
            axis.cursorLeft();
        }
    }

    public void cursorRight() {
        if (highlightY < data.length - 1 && data[highlightY].isSelected()) {
            selectCellAt(highlightY + 1);
        } else {
            axis.cursorRight();
        }
    }

    public void startHighlight(int x, int y) {
        axis.clearSelection();
        super.startHighlight(x, y);
    }

    public void copySelection() {
        super.copySelection();
        axis.copySelection();
    }

    public void copyTable() {
        // create string
        StringBuffer output = new StringBuffer("[Table2D]" + NEW_LINE);
        output.append(axis.getTableAsString()).append(NEW_LINE);
        output.append(super.getTableAsString());
        //copy to clipboard
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(output.toString()), null);
    }

    public void paste() {
        StringTokenizer st = new StringTokenizer("");
        String input = "";
        try {
            input = (String) Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null).getTransferData(DataFlavor.stringFlavor);
            st = new StringTokenizer(input);
        } catch (UnsupportedFlavorException ex) { /* wrong paste type -- do nothing */
        } catch (IOException ex) {
        }

        String pasteType = st.nextToken();

        if (pasteType.equalsIgnoreCase("[Table2D]")) { // Paste table
            String axisValues = "[Table1D]" + NEW_LINE + st.nextToken(NEW_LINE);
            String dataValues = "[Table1D]" + NEW_LINE + st.nextToken(NEW_LINE);

            // put axis in clipboard and paste
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(axisValues), null);
            axis.paste();
            // put datavalues in clipboard and paste
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(dataValues), null);
            super.paste();
            // reset clipboard
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(input), null);

        } else if (pasteType.equalsIgnoreCase("[Selection1D]")) { // paste selection
            if (data[highlightY].isSelected()) {
                super.paste();
            } else {
                axis.paste();
            }
        }
        colorize();
    }

    public void pasteCompare() {
        StringTokenizer st = new StringTokenizer("");
        String input = "";
        try {
            input = (String) Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null).getTransferData(DataFlavor.stringFlavor);
            st = new StringTokenizer(input);
        } catch (UnsupportedFlavorException ex) { /* wrong paste type -- do nothing */
        } catch (IOException ex) {
        }

        String pasteType = st.nextToken();

        if (pasteType.equalsIgnoreCase("[Table2D]")) { // Paste table
            String axisValues = "[Table1D]" + NEW_LINE + st.nextToken(NEW_LINE);
            String dataValues = "[Table1D]" + NEW_LINE + st.nextToken(NEW_LINE);

            // put axis in clipboard and paste
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(axisValues), null);
            axis.pasteCompare();
            // put datavalues in clipboard and paste
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(dataValues), null);
            super.pasteCompare();
            // reset clipboard
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(input), null);

        }
    }

    public void setAxisColor(Color axisColor) {
        axis.setAxisColor(axisColor);
    }

    public void validateScaling() {
        super.validateScaling();
        axis.validateScaling();
    }

    public void setScaleIndex(int scaleIndex) {
        super.setScaleIndex(scaleIndex);
        axis.setScaleByName(getScale().getName());
    }

    public boolean isLiveDataSupported() {
        return !isNullOrEmpty(axis.getLogParam());
    }

    protected void highlightLiveData() {
        if (overlayLog && frame.isVisible()) {
            AxisRange range = getLiveDataRangeForAxis(axis);
            clearSelection();
            boolean first = true;
            for (int i = range.getStartIndex(); i <= range.getEndIndex(); i++) {
                int x = 0;
                int y = i;
                if (axis.getType() == TABLE_X_AXIS) {
                    x = i;
                    y = 0;
                }
                if (first) {
                    startHighlight(x, y);
                    first = false;
                } else {
                    highlight(x, y);
                }
                DataCell cell = data[i];
                cell.setLiveDataTrace(true);
                cell.setDisplayValue(cell.getRealValue() + (isNullOrEmpty(liveValue) ? "" : (':' + liveValue)));
            }
            stopHighlight();
            frame.getToolBar().setLiveDataValue(liveValue);
        }
    }

    public void clearLiveDataTrace() {
        for (DataCell cell : data) {
            cell.setLiveDataTrace(false);
            cell.updateDisplayValue();
        }
    }
}