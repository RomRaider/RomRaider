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
import java.awt.Window;
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
import com.romraider.editor.ecu.ECUEditorManager;
import com.romraider.logger.ecu.ui.swing.vertical.VerticalLabelUI;
import com.romraider.util.AxisRange;
import com.romraider.xml.RomAttributeParser;

public class Table3D extends Table {

    private static final long serialVersionUID = 3103448753263606599L;
    private Table1D xAxis;
    private Table1D yAxis;
    DataCell[][] data = new DataCell[1][1];
    private boolean swapXY = false;
    private boolean flipX = false;
    private boolean flipY = false;

    CopyTable3DWorker copyTable3DWorker;
    CopySelection3DWorker copySelection3DWorker;

    public Table3D() {
        super();
        xAxis = new Table1D();
        yAxis = new Table1D();
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
    public void populateTable(byte[] input, int ramOffset) throws NullPointerException, ArrayIndexOutOfBoundsException {
        // fill first empty cell
        centerPanel.add(new JLabel());
        if (!beforeRam) {
            this.ramOffset = ramOffset;
        }

        // temporarily remove lock
        boolean tempLock = locked;
        locked = false;

        // populate axiis
        try {
            xAxis.populateTable(input, ramOffset);
            yAxis.populateTable(input, ramOffset);
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

                data[x][y] = new DataCell(scales.get(scaleIndex), getSettings().getCellSize());
                data[x][y].setTable(this);

                // populate data cells
                if (storageType == Settings.STORAGE_TYPE_FLOAT) { //float storage type
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
    public StringBuffer getTableAsString(int valType) {
        StringBuffer output = new StringBuffer(Settings.BLANK);

        if(xAxis.isStatic) {
            for (int i = 0; i < xAxis.data.length; i++) {
                output.append(xAxis.data[i].getText());
                if (i < xAxis.data.length - 1) {
                    output.append(Settings.TAB);
                }
            }
        } else {
            output.append(xAxis.getTableAsString(valType));
        }

        output.append(Settings.NEW_LINE);

        for (int y = 0; y < getSizeY(); y++) {
            if(xAxis.isStatic) {
                output.append(yAxis.data[y].getText());
            } else {
                output.append(yAxis.data[y].getRealValue(valType));
            }

            output.append(Settings.TAB);

            for (int x = 0; x < getSizeX(); x++) {
                output.append(data[x][y].getRealValue(valType));
                if (x < getSizeX() - 1) {
                    output.append(Settings.TAB);
                }
            }

            if (y < getSizeY() - 1) {
                output.append(Settings.NEW_LINE);
            }
        }

        return output;
    }

    @Override
    public void colorize() {
        if (compareDisplay == Settings.COMPARE_DISPLAY_OFF) {
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
                            double value = cell.getValue(Settings.DATA_TYPE_BIN);
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
                        double value = cell.getValue(Settings.DATA_TYPE_BIN);
                        if (value > high || value < low) {

                            // value exceeds limit
                            cell.setColor(getSettings().getWarningColor());

                        } else {
                            // limits not set, scale based on table values
                            double scale;
                            if (high - low == 0) {
                                // if all values are the same, color will be middle value
                                scale = .5;
                            } else {
                                scale = (value - low) / (high - low);
                            }

                            cell.setColor(getScaledColor(scale, getSettings()));
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
                        if (Math.abs(cell.getBinValue() - cell.getCompareValue()) > high) {
                            high = Math.abs(cell.getBinValue() - cell.getCompareValue());
                        }
                    }
                }

                // colorize
                for (DataCell[] column : data) {
                    for (DataCell cell : column) {

                        double cellDifference = Math.abs(cell.getBinValue() - cell.getCompareValue());

                        double scale;
                        if (high == 0) {
                            scale = 0;
                        } else {
                            scale = cellDifference / high;
                        }

                        if (scale == 0) {
                            cell.setColor(Settings.UNCHANGED_VALUE_COLOR);
                        } else {
                            cell.setColor(getScaledColor(scale, getSettings()));
                        }

                        // set border
                        if (cell.getBinValue() > cell.getCompareValue()) {
                            cell.setBorder(createLineBorder(getSettings().getIncreaseBorder()));
                        } else if (cell.getBinValue() < cell.getCompareValue()) {
                            cell.setBorder(createLineBorder(getSettings().getDecreaseBorder()));
                        } else {
                            cell.setBorder(createLineBorder(Color.BLACK, 1));
                        }

                    }
                }
            }
        }

        // colorize borders
        if (!isStatic) {
            for (DataCell[] column : data) {
                for (DataCell cell : column) {
                    double checkValue;
                    if(compareDisplay == Settings.COMPARE_DISPLAY_OFF) {
                        checkValue = cell.getOriginalValue();
                    }
                    else{
                        checkValue = cell.getCompareValue();
                    }
                    if (checkValue > cell.getBinValue()) {
                        cell.setBorder(createLineBorder(getSettings().getIncreaseBorder()));
                    } else if (checkValue < cell.getBinValue()) {
                        cell.setBorder(createLineBorder(getSettings().getDecreaseBorder()));
                    } else {
                        cell.setBorder(createLineBorder(Color.BLACK, 1));
                    }
                }
            }
        }
        repaint();
    }

    @Override
    public boolean fillCompareValues() {
        if(null == compareTable || !(compareTable instanceof Table3D)) {
            return false;
        }

        Table3D compareTable3D = (Table3D) compareTable;
        if(data.length != compareTable3D.data.length ||
                data[0].length != compareTable3D.data[0].length ||
                xAxis.getDataSize() != compareTable3D.xAxis.getDataSize() ||
                yAxis.getDataSize() != compareTable3D.yAxis.getDataSize()) {
            return false;
        }

        clearLiveDataTrace();

        int x=0;
        int y=0;
        for (DataCell[] column : data) {
            y = 0;
            for(DataCell cell : column) {
                if(compareType == Settings.DATA_TYPE_BIN) {
                    cell.setCompareValue(compareTable3D.data[x][y].getBinValue());
                } else {
                    cell.setCompareValue(compareTable3D.data[x][y].getOriginalValue());
                }
                y++;
            }
            x++;
        }

        if(!xAxis.isStatic) {
            xAxis.fillCompareValues();
        }

        if(!yAxis.isStatic) {
            yAxis.fillCompareValues();
        }
        return true;
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
                if(data[x][y].getBinValue() != data[x][y].getOriginalValue()) {
                    data[x][y].setBinValue(data[x][y].getOriginalValue());
                }
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
                    if(data[x][y].getBinValue() != data[x][y].getOriginalValue()) {
                        data[x][y].setBinValue(data[x][y].getOriginalValue());
                    }
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
                userLevel <= getSettings().getUserLevel()
                &&     // and table is not in debug mode, unless saveDebugTables is true
                (userLevel < 5
                        ||
                        getSettings().isSaveDebugTables())) {

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
                    if (storageType != Settings.STORAGE_TYPE_FLOAT) {
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
        if (axisType.getType() == Settings.TABLE_Y_AXIS) {
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
        Window ancestorWindow = SwingUtilities.getWindowAncestor(this);
        if(null != ancestorWindow) {
            ancestorWindow.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        }
        ECUEditorManager.getECUEditor().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        copySelection3DWorker = new CopySelection3DWorker(this);
        copySelection3DWorker.execute();

    }

    @Override
    public void copyTable() {
        Window ancestorWindow = SwingUtilities.getWindowAncestor(this);
        if(null != ancestorWindow) {
            ancestorWindow.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        }
        ECUEditorManager.getECUEditor().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        copyTable3DWorker = new CopyTable3DWorker(this);
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
    public void applyColorSettings() {
        // apply settings to cells
        for (int y = 0; y < getSizeY(); y++) {
            for (int x = 0; x < getSizeX(); x++) {

                this.setMaxColor(getSettings().getMaxColor());
                this.setMinColor(getSettings().getMinColor());
                data[x][y].setHighlightColor(getSettings().getHighlightColor());
                data[x][y].setIncreaseBorder(getSettings().getIncreaseBorder());
                data[x][y].setDecreaseBorder(getSettings().getDecreaseBorder());
                data[x][y].setFont(getSettings().getTableFont());
                data[x][y].repaint();
            }
        }

        this.setAxisColor();
        xAxis.applyColorSettings();
        yAxis.applyColorSettings();
        cellHeight = (int) getSettings().getCellSize().getHeight();
        cellWidth = (int) getSettings().getCellSize().getWidth();

        validateScaling();
        colorize();
    }

    @Override
    public void setAxisColor() {
        xAxis.setAxisColor();
        yAxis.setAxisColor();
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
        if (overlayLog) {
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
                    cell.setDisplayValue(cell.getRealValue(Settings.DATA_TYPE_BIN) + (isNullOrEmpty(liveValue) ? "" : (':' + liveValue)));
                }
            }
            stopHighlight();
            getToolbar().setLiveDataValue(liveValue);
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
                    double value = cell.getValue(Settings.DATA_TYPE_BIN);
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
                    double value = cell.getValue(Settings.DATA_TYPE_BIN);
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

    @Override
    public void setCompareDisplay(int compareDisplay) {
        super.setCompareDisplay(compareDisplay);

        if(!xAxis.isStatic) {
            xAxis.setCompareDisplay(compareDisplay);
        }

        if(!yAxis.isStatic) {
            yAxis.setCompareDisplay(compareDisplay);
        }
    }

    @Override
    public void setCompareType(int comparetype) {
        super.setCompareType(comparetype);

        if(!xAxis.isStatic) {
            xAxis.setCompareType(comparetype);
        }

        if(!yAxis.isStatic) {
            yAxis.setCompareType(comparetype);
        }
    }

    @Override
    public void setCompareTable(Table compareTable){
        super.setCompareTable(compareTable);

        if(null == compareTable || !(compareTable instanceof Table3D)) {
            return;
        }

        Table3D compareTable3D = (Table3D) compareTable;

        if(!xAxis.isStatic) {
            this.xAxis.setCompareTable(compareTable3D.xAxis);
        }

        if(!yAxis.isStatic) {
            this.yAxis.setCompareTable(compareTable3D.yAxis);
        }
    }

    @Override
    public void refreshCellDisplay() {
        for (DataCell[] column : data) {
            for(DataCell cell : column) {
                cell.setCompareDisplay(compareDisplay);
                cell.updateDisplayValue();
            }
        }
        if(!xAxis.isStatic) {
            xAxis.refreshCellDisplay();
        }
        if(!yAxis.isStatic) {
            yAxis.refreshCellDisplay();
        }
        colorize();
    }

    @Override
    public void addComparedToTable(Table table) {
        super.addComparedToTable(table);

        if(!(table instanceof Table3D)) {
            return;
        }

        Table3D table3D = (Table3D) table;
        if(!xAxis.isStatic) {
            xAxis.addComparedToTable(table3D.xAxis);
        }
        if(!yAxis.isStatic) {
            yAxis.addComparedToTable(table3D.yAxis);
        }
    }

    @Override
    public boolean equals(Object other) {
        try {
            if(null == other) {
                return false;
            }

            if(other == this) {
                return true;
            }

            if(!(other instanceof Table3D)) {
                return false;
            }

            Table3D otherTable = (Table3D)other;

            if(!this.getName().equalsIgnoreCase(otherTable.getName())) {
                return false;
            }

            if(! this.xAxis.equals(otherTable.xAxis)) {
                return false;
            }

            if(! this.yAxis.equals(otherTable.yAxis)) {
                return false;
            }

            if(this.data.length != otherTable.data.length || this.data[0].length != otherTable.data[0].length)
            {
                return false;
            }

            if(this.data.equals(otherTable.data))
            {
                return true;
            }

            // Compare Bin Values
            for(int i = 0 ; i < this.data.length ; i++) {
                for(int j = 0; j < this.data[i].length ; j++) {
                    if(this.data[i][j].getBinValue() != otherTable.data[i][j].getBinValue()) {
                        return false;
                    }
                }
            }

            return true;
        } catch(Exception ex) {
            // TODO: Log Exception.
            return false;
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
        Window ancestorWindow = SwingUtilities.getWindowAncestor(table);
        if(null != ancestorWindow) {
            ancestorWindow.setCursor(null);
        }
        table.setCursor(null);
        ECUEditorManager.getECUEditor().setCursor(null);
    }
}

class CopyTable3DWorker extends SwingWorker<Void, Void> {
    Table3D table;

    public CopyTable3DWorker(Table3D table)
    {
        this.table = table;
    }

    @Override
    protected Void doInBackground() throws Exception {
        String tableHeader = table.getSettings().getTable3DHeader();
        StringBuffer output = new StringBuffer(tableHeader);
        output.append(table.getTableAsString(Settings.DATA_TYPE_DISPLAYED));
        //copy to clipboard
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(String.valueOf(output)), null);
        return null;
    }

    @Override
    public void done() {
        Window ancestorWindow = SwingUtilities.getWindowAncestor(table);
        if(null != ancestorWindow){
            ancestorWindow.setCursor(null);
        }
        table.setCursor(null);
        ECUEditorManager.getECUEditor().setCursor(null);
    }
}