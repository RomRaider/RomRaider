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
import static com.romraider.util.TableAxisUtil.getLiveDataRangeForAxis;
import static javax.swing.BorderFactory.createLineBorder;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.util.StringTokenizer;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import com.romraider.Settings;
import com.romraider.logger.ecu.ui.swing.vertical.VerticalLabelUI;
import com.romraider.swing.TableFrame;
import com.romraider.util.AxisRange;
import com.romraider.xml.RomAttributeParser;

public class Table3D extends Table {

    private static final long serialVersionUID = 3103448753263606599L;
    private Table1D xAxis = new Table1D(new Settings());
    private Table1D yAxis = new Table1D(new Settings());
    DataCell[][] data = new DataCell[1][1];
    private boolean swapXY = false;
    private boolean flipX = false;
    private boolean flipY = false;

    CopyTable3DWorker copyTable3DWorker;
    CopySelection3DWorker copySelection3DWorker;

    public Table3D(Settings settings) {
        super(settings);
        verticalOverhead += 39;
        horizontalOverhead += 10;
    }

    public Table1D getXAxis() {
        return xAxis;
    }

    public void setXAxis(Table1D xAxis) {
        this.xAxis = xAxis;
    }

    public Table1D getYAxis() {
        return yAxis;
    }

    public void setYAxis(Table1D yAxis) {
        this.yAxis = yAxis;
    }

    public boolean getSwapXY() {
        return swapXY;
    }

    public void setSwapXY(boolean swapXY) {
        this.swapXY = swapXY;
    }

    public boolean getFlipX() {
        return flipX;
    }

    public void setFlipX(boolean flipX) {
        this.flipX = flipX;
    }

    public boolean getFlipY() {
        return flipY;
    }

    public void setFlipY(boolean flipY) {
        this.flipY = flipY;
    }

    public void setSizeX(int size) {
        data = new DataCell[size][data[0].length];
        centerLayout.setColumns(size + 1);
    }

    public int getSizeX() {
        return data.length;
    }

    public void setSizeY(int size) {
        data = new DataCell[data.length][size];
        centerLayout.setRows(size + 1);
    }

    public int getSizeY() {
        return data[0].length;
    }

    @Override
    public void populateTable(byte[] input) throws NullPointerException, ArrayIndexOutOfBoundsException {
        // fill first empty cell
        centerPanel.add(new JLabel());
        if (!beforeRam) {
            ramOffset = container.getRomID().getRamOffset();
        }

        // temporarily remove lock
        boolean tempLock = locked;
        locked = false;

        // populate axiis
        try {
            xAxis.setRom(container);
            xAxis.populateTable(input);
            yAxis.setRom(container);
            yAxis.populateTable(input);
        } catch (ArrayIndexOutOfBoundsException ex) {
            throw new ArrayIndexOutOfBoundsException();
        }

        for (int x = 0; x < xAxis.getDataSize(); x++) {
            centerPanel.add(xAxis.getDataCell(x));
        }

        int offset = 0;

        int iMax = swapXY ? xAxis.getDataSize() : yAxis.getDataSize();
        int jMax = swapXY ? yAxis.getDataSize() : xAxis.getDataSize();
        for (int i = 0; i < iMax; i++) {
            for (int j = 0; j < jMax; j++) {

                int x = flipY ? jMax - j - 1 : j;
                int y = flipX ? iMax - i - 1 : i;
                if (swapXY) {
                    int z = x;
                    x = y;
                    y = z;
                }

                data[x][y] = new DataCell(scales.get(scaleIndex), settings.getCellSize());
                data[x][y].setTable(this);

                // populate data cells
                if (storageType == STORAGE_TYPE_FLOAT) { //float storage type
                    byte[] byteValue = new byte[4];
                    byteValue[0] = input[storageAddress + offset * 4 - ramOffset];
                    byteValue[1] = input[storageAddress + offset * 4 - ramOffset + 1];
                    byteValue[2] = input[storageAddress + offset * 4 - ramOffset + 2];
                    byteValue[3] = input[storageAddress + offset * 4 - ramOffset + 3];
                    data[x][y].setBinValue(RomAttributeParser.byteToFloat(byteValue, endian));

                } else { // integer storage type
                    data[x][y].setBinValue(
                            RomAttributeParser.parseByteValue(input,
                                    endian,
                                    storageAddress + offset * storageType - ramOffset,
                                    storageType,
                                    signed));
                }

                // show locked cell
                if (tempLock) {
                    data[x][y].setForeground(Color.GRAY);
                }

                data[x][y].setXCoord(x);
                data[x][y].setYCoord(y);
                data[x][y].setOriginalValue(data[x][y].getBinValue());
                offset++;
            }
        }

        for (int y = 0; y < yAxis.getDataSize(); y++) {
            centerPanel.add(yAxis.getDataCell(y));
            for (int x = 0; x < xAxis.getDataSize(); x++) {
                centerPanel.add(data[x][y]);
            }
        }

        // reset locked status
        locked = tempLock;

        GridLayout topLayout = new GridLayout(2, 1);
        JPanel topPanel = new JPanel(topLayout);
        this.add(topPanel, BorderLayout.NORTH);
        topPanel.add(new JLabel(name, JLabel.CENTER), BorderLayout.NORTH);
        topPanel.add(new JLabel(xAxis.getName() + " (" + xAxis.getScale().getUnit() + ")", JLabel.CENTER), BorderLayout.NORTH);

        JLabel yLabel = new JLabel(yAxis.getName() + " (" + yAxis.getScale().getUnit() + ")");
        yLabel.setUI(new VerticalLabelUI(false));
        add(yLabel, BorderLayout.WEST);

        add(new JLabel(getScale().getUnit(), JLabel.CENTER), BorderLayout.SOUTH);
    }

    @Override
    public StringBuffer getTableAsString() {
        // Make a string of the table
        StringBuffer output = new StringBuffer(BLANK);
        output.append(xAxis.getTableAsString()).append(NEW_LINE);

        for (int y = 0; y < getSizeY(); y++) {
            output.append(yAxis.getCellAsString(y)).append(TAB);
            for (int x = 0; x < getSizeX(); x++) {
                output.append(data[x][y].getText());
                if (x < getSizeX() - 1) {
                    output.append(TAB);
                }
            }
            if (y < getSizeY() - 1) {
                output.append(NEW_LINE);
            }
        }
        return output;
    }

    @Override
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
                    // min/max not set in scale
                    for (DataCell[] column : data) {
                        for (DataCell cell : column) {
                            double value = cell.getValue();
                            if (value > high) {
                                high = value;
                            }
                            if (value < low) {
                                low = value;
                            }
                        }
                    }
                }


                for (DataCell[] column : data) {
                    for (DataCell cell : column) {
                        double value = cell.getValue();
                        if (value > high || value < low) {

                            // value exceeds limit
                            cell.setColor(settings.getWarningColor());

                        } else {
                            // limits not set, scale based on table values
                            double scale;
                            if (high - low == 0) {
                                // if all values are the same, color will be middle value
                                scale = .5;
                            } else {
                                scale = (value - low) / (high - low);
                            }

                            cell.setColor(getScaledColor(scale, settings));
                        }
                    }
                }
            }
        } else { // comparing is on
            if (!isStatic) {
                double high = Double.MIN_VALUE;

                // determine ratios
                for (DataCell[] column : data) {
                    for (DataCell cell : column) {
                        if (Math.abs(cell.getBinValue() - cell.getOriginalValue()) > high) {
                            high = Math.abs(cell.getBinValue() - cell.getOriginalValue());
                        }
                    }
                }

                // colorize
                for (DataCell[] column : data) {
                    for (DataCell cell : column) {

                        double cellDifference = Math.abs(cell.getBinValue() - cell.getOriginalValue());

                        double scale;
                        if (high == 0) {
                            scale = 0;
                        } else {
                            scale = cellDifference / high;
                        }

                        if (scale == 0) {
                            cell.setColor(UNCHANGED_VALUE_COLOR);
                        } else {
                            cell.setColor(getScaledColor(scale, settings));
                        }

                        // set border
                        if (cell.getBinValue() > cell.getOriginalValue()) {
                            cell.setBorder(createLineBorder(settings.getIncreaseBorder()));
                        } else if (cell.getBinValue() < cell.getOriginalValue()) {
                            cell.setBorder(createLineBorder(settings.getDecreaseBorder()));
                        } else {
                            cell.setBorder(createLineBorder(Color.BLACK, 1));
                        }

                    }
                }
            }
            xAxis.colorize();
            yAxis.colorize();
        }

        // colorize borders
        if (!isStatic) {
            for (DataCell[] column : data) {
                for (DataCell cell : column) {

                    if (cell.getBinValue() > cell.getOriginalValue()) {
                        cell.setBorder(createLineBorder(settings.getIncreaseBorder()));
                    } else if (cell.getBinValue() < cell.getOriginalValue()) {
                        cell.setBorder(createLineBorder(settings.getDecreaseBorder()));
                    } else {
                        cell.setBorder(createLineBorder(Color.BLACK, 1));
                    }
                }
            }
        }
    }

    @Override
    public void compare(int compareType) {
        this.compareType = compareType;

        for (DataCell[] column : data) {
            for (DataCell cell : column) {
                if (compareType == Table.COMPARE_ORIGINAL) {
                    cell.setCompareValue(cell.getOriginalValue());
                }
                cell.setCompareType(compareType);
                cell.setCompareDisplay(compareDisplay);
                cell.updateDisplayValue();
            }
        }
        colorize();
    }

    @Override
    public void setFrame(TableFrame frame) {
        this.frame = frame;
        xAxis.setFrame(frame);
        yAxis.setFrame(frame);
        //frame.setSize(getFrameSize());
        frame.pack();
    }

    @Override
    public Dimension getFrameSize() {
        int height = verticalOverhead + cellHeight * data[0].length;
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

    @Override
    public String toString() {
        return super.toString() + " (3D)";/* +
                "\n   Flip X: " + flipX +
                "\n   Size X: " + data.length +
                "\n   Flip Y: " + flipY +
                "\n   Size Y: " + data[0].length +
                "\n   Swap X/Y: " + swapXY +
                xAxis +
                yAxis;*/
    }

    @Override
    public void increment(double increment) {
        if (!isStatic && !locked) {
            for (int x = 0; x < this.getSizeX(); x++) {
                for (int y = 0; y < this.getSizeY(); y++) {
                    if (data[x][y].isSelected()) {
                        data[x][y].increment(increment);
                    }
                }
            }
        }
        xAxis.increment(increment);
        yAxis.increment(increment);
        colorize();
    }

    @Override
    public void multiply(double factor) {
        if (!isStatic && !locked) {
            for (int x = 0; x < this.getSizeX(); x++) {
                for (int y = 0; y < this.getSizeY(); y++) {
                    if (data[x][y].isSelected()) {
                        data[x][y].multiply(factor);
                    }
                }
            }
        }
        xAxis.multiply(factor);
        yAxis.multiply(factor);
        colorize();
    }

    @Override
    public void clearSelection() {
        xAxis.clearSelection(true);
        yAxis.clearSelection(true);
        for (int x = 0; x < this.getSizeX(); x++) {
            for (int y = 0; y < this.getSizeY(); y++) {
                data[x][y].setSelected(false);
            }
        }
    }

    @Override
    public void highlight(int xCoord, int yCoord) {
        if (highlight) {
            for (int x = 0; x < this.getSizeX(); x++) {
                for (int y = 0; y < this.getSizeY(); y++) {
                    if (((y >= highlightY && y <= yCoord) ||
                            (y <= highlightY && y >= yCoord)) &&
                            ((x >= highlightX && x <= xCoord) ||
                                    (x <= highlightX && x >= xCoord))) {
                        data[x][y].setHighlighted(true);
                    } else {
                        data[x][y].setHighlighted(false);
                    }
                }
            }
        }
    }

    @Override
    public void stopHighlight() {
        highlight = false;
        // loop through, selected and un-highlight
        for (int x = 0; x < this.getSizeX(); x++) {
            for (int y = 0; y < this.getSizeY(); y++) {
                if (data[x][y].isHighlighted()) {
                    data[x][y].setSelected(true);
                    data[x][y].setHighlighted(false);
                }
            }
        }
    }

    @Override
    public void setRevertPoint() {
        for (int x = 0; x < this.getSizeX(); x++) {
            for (int y = 0; y < this.getSizeY(); y++) {
                data[x][y].setOriginalValue(data[x][y].getBinValue());
            }
        }
        yAxis.setRevertPoint();
        xAxis.setRevertPoint();
        colorize();
    }

    @Override
    public void undoAll() {
        clearLiveDataTrace();
        for (int x = 0; x < this.getSizeX(); x++) {
            for (int y = 0; y < this.getSizeY(); y++) {
                data[x][y].setBinValue(data[x][y].getOriginalValue());
            }
        }
        yAxis.undoAll();
        xAxis.undoAll();
        colorize();
    }

    @Override
    public void undoSelected() {
        clearLiveDataTrace();
        for (int x = 0; x < this.getSizeX(); x++) {
            for (int y = 0; y < this.getSizeY(); y++) {
                if (data[x][y].isSelected()) {
                    data[x][y].setBinValue(data[x][y].getOriginalValue());
                }
            }
        }
        yAxis.undoSelected();
        xAxis.undoSelected();
        colorize();
    }


    @Override
    public byte[] saveFile(byte[] binData) {
        if (!isStatic  // save if table is not static
                &&     // and user level is great enough
                userLevel <= settings.getUserLevel()
                &&     // and table is not in debug mode, unless saveDebugTables is true
                (userLevel < 5
                        ||
                        settings.isSaveDebugTables())) {

            binData = xAxis.saveFile(binData);
            binData = yAxis.saveFile(binData);
            int offset = 0;

            int iMax = swapXY ? xAxis.getDataSize() : yAxis.getDataSize();
            int jMax = swapXY ? yAxis.getDataSize() : xAxis.getDataSize();
            for (int i = 0; i < iMax; i++) {
                for (int j = 0; j < jMax; j++) {

                    int x = flipY ? jMax - j - 1 : j;
                    int y = flipX ? iMax - i - 1 : i;
                    if (swapXY) {
                        int z = x;
                        x = y;
                        y = z;
                    }

                    // determine output byte values
                    byte[] output;
                    if (storageType != STORAGE_TYPE_FLOAT) {
                        output = RomAttributeParser.parseIntegerValue((int) data[x][y].getBinValue(), endian, storageType);
                        for (int z = 0; z < storageType; z++) {
                            binData[offset * storageType + z + storageAddress - ramOffset] = output[z];
                        }
                    } else { // float
                        output = RomAttributeParser.floatToByte((float) data[x][y].getBinValue(), endian);
                        for (int z = 0; z < 4; z++) {
                            binData[offset * 4 + z + storageAddress - ramOffset] = output[z];
                        }
                    }


                    offset++;
                }
            }
        }
        return binData;
    }

    @Override
    public void setRealValue(String realValue) {
        if (!isStatic && !locked) {
            for (int x = 0; x < this.getSizeX(); x++) {
                for (int y = 0; y < this.getSizeY(); y++) {
                    if (data[x][y].isSelected()) {
                        data[x][y].setRealValue(realValue);
                    }
                }
            }
        }
        xAxis.setRealValue(realValue);
        yAxis.setRealValue(realValue);
        colorize();
    }

    @Override
    public void addKeyListener(KeyListener listener) {
        xAxis.addKeyListener(listener);
        yAxis.addKeyListener(listener);
        for (int x = 0; x < this.getSizeX(); x++) {
            for (int y = 0; y < this.getSizeY(); y++) {
                data[x][y].addKeyListener(listener);
            }
        }
    }

    public void selectCellAt(int y, Table1D axisType) {
        if (axisType.getType() == TABLE_Y_AXIS) {
            selectCellAt(0, y);
        } else { // y axis
            selectCellAt(y, 0);
        }
    }

    public void deSelectCellAt(int x, int y) {
        clearSelection();
        data[x][y].setSelected(false);
        highlightX = x;
        highlightY = y;
    }

    public void selectCellAt(int x, int y) {
        clearSelection();
        data[x][y].setSelected(true);
        highlightX = x;
        highlightY = y;
    }

    public void selectCellAtWithoutClear(int x, int y) {
        data[x][y].setSelected(true);
        highlightX = x;
        highlightY = y;
    }

    @Override
    public void cursorUp() {
        if (highlightY > 0 && data[highlightX][highlightY].isSelected()) {
            selectCellAt(highlightX, highlightY - 1);
        } else if (!xAxis.isStatic() && data[highlightX][highlightY].isSelected()) {
            xAxis.selectCellAt(highlightX);
        } else {
            xAxis.cursorUp();
            yAxis.cursorUp();
        }
    }

    @Override
    public void cursorDown() {
        if (highlightY < getSizeY() - 1 && data[highlightX][highlightY].isSelected()) {
            selectCellAt(highlightX, highlightY + 1);
        } else {
            xAxis.cursorDown();
            yAxis.cursorDown();
        }
    }

    @Override
    public void cursorLeft() {
        if (highlightX > 0 && data[highlightX][highlightY].isSelected()) {
            selectCellAt(highlightX - 1, highlightY);
        } else if (!yAxis.isStatic() && data[highlightX][highlightY].isSelected()) {
            yAxis.selectCellAt(highlightY);
        } else {
            xAxis.cursorLeft();
            yAxis.cursorLeft();
        }
    }

    @Override
    public void cursorRight() {
        if (highlightX < getSizeX() - 1 && data[highlightX][highlightY].isSelected()) {
            selectCellAt(highlightX + 1, highlightY);
        } else {
            xAxis.cursorRight();
            yAxis.cursorRight();
        }
    }

    @Override
    public void startHighlight(int x, int y) {
        xAxis.clearSelection();
        yAxis.clearSelection();
        super.startHighlight(x, y);
    }

    @Override
    public void copySelection() {
        SwingUtilities.getWindowAncestor(this).setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        copySelection3DWorker = new CopySelection3DWorker(this);
        copySelection3DWorker.execute();

    }

    @Override
    public void copyTable() {
        SwingUtilities.getWindowAncestor(this).setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        copyTable3DWorker = new CopyTable3DWorker(settings, this);
        copyTable3DWorker.execute();
    }

    @Override
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

        if ("[Table3D]".equalsIgnoreCase(pasteType)) { // Paste table
            String newline = System.getProperty("line.separator");
            String xAxisValues = "[Table1D]" + newline + st.nextToken(newline);

            // build y axis and data values
            StringBuffer yAxisValues = new StringBuffer("[Table1D]" + newline + st.nextToken("\t"));
            StringBuffer dataValues = new StringBuffer("[Table3D]" + newline + st.nextToken("\t") + st.nextToken(newline));
            while (st.hasMoreTokens()) {
                yAxisValues.append("\t").append(st.nextToken("\t"));
                dataValues.append(newline).append(st.nextToken("\t")).append(st.nextToken(newline));
            }

            // put x axis in clipboard and paste
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(xAxisValues), null);
            xAxis.paste();
            // put y axis in clipboard and paste
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(String.valueOf(yAxisValues)), null);
            yAxis.paste();
            // put datavalues in clipboard and paste
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(String.valueOf(dataValues)), null);
            pasteValues();
            colorize();
            // reset clipboard
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(input), null);

        } else if ("[Selection3D]".equalsIgnoreCase(pasteType)) { // paste selection
            pasteValues();
            colorize();
        } else if ("[Selection1D]".equalsIgnoreCase(pasteType)) { // paste selection
            xAxis.paste();
            yAxis.paste();
        }
    }

    @Override
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

        if ("[Table3D]".equalsIgnoreCase(pasteType)) { // Paste table
            String newline = System.getProperty("line.separator");
            String xAxisValues = "[Table1D]" + newline + st.nextToken(newline);
            StringBuffer yAxisValues = new StringBuffer("");
            StringBuffer dataValues = new StringBuffer("");

            while (st.hasMoreTokens()) {
                StringTokenizer currentLine = new StringTokenizer(st.nextToken(newline));
                yAxisValues.append(currentLine.nextToken("\t")).append("\t");
                //dataValues.append(currentLine.nextToken(newline));

                while (currentLine.hasMoreTokens()) {
                    dataValues.append(currentLine.nextToken()).append("\t");
                }
                dataValues.append(newline);
            }

            // put x axis in clipboard and paste
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(xAxisValues), null);
            xAxis.pasteCompare();
            // put y axis in clipboard and paste
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(yAxisValues.toString()), null);
            yAxis.pasteCompare();
            // put datavalues in clipboard and paste
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(dataValues.toString()), null);
            pasteCompareValues();
            // reset clipboard
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(input), null);
        }
        colorize();
    }

    public void pasteCompareValues() {
        StringTokenizer st = new StringTokenizer("");
        try {
            String input = (String) Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null).getTransferData(DataFlavor.stringFlavor);
            st = new StringTokenizer(input);
        } catch (UnsupportedFlavorException ex) { /* wrong paste type -- do nothing */
        } catch (IOException ex) {
        }

        // set values
        for (int y = 0; y < getSizeY(); y++) {
            if (st.hasMoreTokens()) {

                for (int x = 0; x < getSizeX(); x++) {
                    String currentToken = st.nextToken();
                    data[x][y].setCompareRealValue(currentToken);
                }
            }
        }

    }

    public void pasteValues() {
        StringTokenizer st = new StringTokenizer("");
        String newline = System.getProperty("line.separator");
        try {
            String input = (String) Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null).getTransferData(DataFlavor.stringFlavor);
            st = new StringTokenizer(input);
        } catch (UnsupportedFlavorException ex) { /* wrong paste type -- do nothing */
        } catch (IOException ex) {
        }

        String pasteType = st.nextToken();

        // figure paste start cell
        int startX = 0;
        int startY = 0;
        // if pasting a table, startX and Y at 0, else highlight is start
        if ("[Selection3D]".equalsIgnoreCase(pasteType)) {
            startX = highlightX;
            startY = highlightY;
        }

        // set values
        for (int y = startY; y < getSizeY(); y++) {
            if (st.hasMoreTokens()) {
                StringTokenizer currentLine = new StringTokenizer(st.nextToken(newline));
                for (int x = startX; x < getSizeX(); x++) {
                    if (currentLine.hasMoreTokens()) {
                        String currentToken = currentLine.nextToken();

                        try {
                            if (!data[x][y].getText().equalsIgnoreCase(currentToken)) {
                                data[x][y].setRealValue(currentToken);
                            }
                        } catch (ArrayIndexOutOfBoundsException ex) { /* copied table is larger than current table*/ }
                    }
                }
            }
        }
    }

    @Override
    public void applyColorSettings(Settings settings) {
        // apply settings to cells
        this.settings = settings;

        for (int y = 0; y < getSizeY(); y++) {
            for (int x = 0; x < getSizeX(); x++) {

                this.setMaxColor(settings.getMaxColor());
                this.setMinColor(settings.getMinColor());
                data[x][y].setHighlightColor(settings.getHighlightColor());
                data[x][y].setIncreaseBorder(settings.getIncreaseBorder());
                data[x][y].setDecreaseBorder(settings.getDecreaseBorder());
                data[x][y].setFont(settings.getTableFont());
                data[x][y].repaint();
            }
        }

        this.setAxisColor(settings.getAxisColor());
        xAxis.applyColorSettings(settings);
        yAxis.applyColorSettings(settings);
        cellHeight = (int) settings.getCellSize().getHeight();
        cellWidth = (int) settings.getCellSize().getWidth();

        validateScaling();
        resize();
        colorize();
    }

    @Override
    public void setAxisColor(Color axisColor) {
        xAxis.setAxisColor(axisColor);
        yAxis.setAxisColor(axisColor);
    }

    @Override
    public void validateScaling() {
        super.validateScaling();
        xAxis.validateScaling();
        yAxis.validateScaling();
    }

    @Override
    public void refreshValues() {
        if (!isStatic && !isAxis) {
            for (DataCell[] column : data) {
                for (DataCell cell : column) {
                    cell.refreshValue();
                }
            }
        }
    }

    @Override
    public boolean isLiveDataSupported() {
        return !isNullOrEmpty(xAxis.getLogParam()) && !isNullOrEmpty(yAxis.getLogParam());
    }

    @Override
    public boolean isButtonSelected() {
        return true;
    }

    @Override
    protected void highlightLiveData() {
        if (overlayLog && frame.isVisible()) {
            AxisRange rangeX = getLiveDataRangeForAxis(xAxis);
            AxisRange rangeY = getLiveDataRangeForAxis(yAxis);
            clearSelection();
            boolean first = true;
            for (int x = rangeX.getStartIndex(); x <= rangeX.getEndIndex(); x++) {
                for (int y = rangeY.getStartIndex(); y <= rangeY.getEndIndex(); y++) {
                    if (first) {
                        startHighlight(x, y);
                        first = false;
                    } else {
                        highlight(x, y);
                    }
                    DataCell cell = data[x][y];
                    cell.setLiveDataTrace(true);
                    cell.setDisplayValue(cell.getRealValue() + (isNullOrEmpty(liveValue) ? "" : (':' + liveValue)));
                }
            }
            stopHighlight();
            frame.getToolBar().setLiveDataValue(liveValue);
        }
    }

    @Override
    public void clearLiveDataTrace() {
        for (int x = 0; x < getSizeX(); x++) {
            for (int y = 0; y < getSizeY(); y++) {
                data[x][y].setLiveDataTrace(false);
                data[x][y].updateDisplayValue();
            }
        }
    }

    @Override
    public void setScaleIndex(int scaleIndex) {
        super.setScaleIndex(scaleIndex);
        xAxis.setScaleByName(getScale().getName());
        yAxis.setScaleByName(getScale().getName());
    }

    public DataCell[][] get3dData() {
        return data;
    }

    @Override
    public double getMin() {
        if (getScale().getMin() == 0 && getScale().getMax() == 0) {
            double low = Double.MAX_VALUE;

            for (DataCell[] column : data) {
                for (DataCell cell : column) {
                    double value = cell.getValue();
                    if (value < low) {
                        low = value;
                    }
                }
            }

            return low;
        } else {
            return getScale().getMin();
        }
    }

    @Override
    public double getMax() {
        if (getScale().getMin() == 0 && getScale().getMax() == 0) {
            double high = Double.MIN_VALUE;

            for (DataCell[] column : data) {
                for (DataCell cell : column) {
                    double value = cell.getValue();
                    if (value > high) {
                        high = value;
                    }
                }
            }

            return high;
        } else {
            return getScale().getMax();
        }
    }
}

class CopySelection3DWorker extends SwingWorker<Void, Void> {
    Table3D table;

    public CopySelection3DWorker(Table3D table)
    {
        this.table = table;
    }

    @Override
    protected Void doInBackground() throws Exception {
        // find bounds of selection
        // coords[0] = x min, y min, x max, y max
        boolean copy = false;
        int[] coords = new int[4];
        coords[0] = table.getSizeX();
        coords[1] = table.getSizeY();

        for (int x = 0; x < table.getSizeX(); x++) {
            for (int y = 0; y < table.getSizeY(); y++) {
                if (table.get3dData()[x][y].isSelected()) {
                    if (x < coords[0]) {
                        coords[0] = x;
                        copy = true;
                    }
                    if (x > coords[2]) {
                        coords[2] = x;
                        copy = true;
                    }
                    if (y < coords[1]) {
                        coords[1] = y;
                        copy = true;
                    }
                    if (y > coords[3]) {
                        coords[3] = y;
                        copy = true;
                    }
                }
            }
        }
        // make string of selection
        if (copy) {
            String newline = System.getProperty("line.separator");
            StringBuffer output = new StringBuffer("[Selection3D]" + newline);
            for (int y = coords[1]; y <= coords[3]; y++) {
                for (int x = coords[0]; x <= coords[2]; x++) {
                    if (table.get3dData()[x][y].isSelected()) {
                        output.append(table.get3dData()[x][y].getText());
                    } else {
                        output.append("x"); // x represents non-selected cell
                    }
                    if (x < coords[2]) {
                        output.append("\t");
                    }
                }
                if (y < coords[3]) {
                    output.append(newline);
                }
                //copy to clipboard
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(String.valueOf(output)), null);
            }
        } else {
            table.getXAxis().copySelection();
            table.getYAxis().copySelection();
        }
        return null;
    }

    @Override
    public void done() {
        SwingUtilities.getWindowAncestor(table).setCursor(null);
        table.setCursor(null);
    }
}

class CopyTable3DWorker extends SwingWorker<Void, Void> {
    Settings settings;
    Table3D table;

    public CopyTable3DWorker(Settings settings, Table3D table)
    {
        this.settings = settings;
        this.table = table;
    }

    @Override
    protected Void doInBackground() throws Exception {
        String tableHeader = settings.getTable3DHeader();

        StringBuffer output = new StringBuffer(tableHeader);
        output.append(table.getXAxis().getTableAsString()).append(Table3D.NEW_LINE);

        for (int y = 0; y < table.getSizeY(); y++) {
            output.append(table.getYAxis().getCellAsString(y)).append(Table3D.TAB);
            for (int x = 0; x < table.getSizeX(); x++) {
                output.append(table.get3dData()[x][y].getText());
                if (x < table.getSizeX() - 1) {
                    output.append(Table3D.TAB);
                }
            }
            if (y < table.getSizeY() - 1) {
                output.append(Table3D.NEW_LINE);
            }
        }
        //copy to clipboard
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(String.valueOf(output)), null);
        return null;
    }

    @Override
    public void done() {
        SwingUtilities.getWindowAncestor(table).setCursor(null);
        table.setCursor(null);
    }
}