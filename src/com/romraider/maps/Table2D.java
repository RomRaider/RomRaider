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

import static com.romraider.util.ParamChecker.isNullOrEmpty;
import static com.romraider.util.TableAxisUtil.getLiveDataRangeForAxis;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.util.StringTokenizer;

import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import com.romraider.Settings;
import com.romraider.editor.ecu.ECUEditor;
import com.romraider.swing.TableFrame;
import com.romraider.util.AxisRange;

public class Table2D extends Table {
    private static final long serialVersionUID = -7684570967109324784L;
    static final String NEW_LINE = System.getProperty("line.separator");
    private Table1D axis;

    private CopyTable2DWorker copyTable2DWorker;
    private CopySelection2DWorker copySelection2DWorker;

    public Table2D(ECUEditor editor) {
        super(editor);
        axis = new Table1D(editor);
        verticalOverhead += 18;
    }

    public Table1D getAxis() {
        return axis;
    }

    public void setAxis(Table1D axis) {
        this.axis = axis;
    }

    @Override
    public String toString() {
        return super.toString() + " (2D)";// + axis;
    }

    @Override
    public void colorize() {
        super.colorize();
        axis.colorize();
    }

    @Override
    public void setFrame(TableFrame frame) {
        this.frame = frame;
        axis.setFrame(frame);
        frame.setSize(getFrameSize());
    }

    @Override
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

    @Override
    public void applyColorSettings() {
        this.setAxisColor(editor.getSettings().getAxisColor());
        axis.applyColorSettings();
        super.applyColorSettings();
    }

    @Override
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
        if (flip) {
            for (int i = this.getDataSize() - 1; i >= 0; i--) {
                centerPanel.add(this.getDataCell(i));
            }
        } else {
            for (int i = 0; i < this.getDataSize(); i++) {
                centerPanel.add(this.getDataCell(i));
            }
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

    @Override
    public void increment(double increment) {
        super.increment(increment);
        axis.increment(increment);
    }

    @Override
    public void multiply(double factor) {
        super.multiply(factor);
        axis.multiply(factor);
    }

    @Override
    public void clearSelection() {
        axis.clearSelection(true);
        for (DataCell aData : data) {
            aData.setSelected(false);
        }
    }

    @Override
    public void setRevertPoint() {
        super.setRevertPoint();
        axis.setRevertPoint();
    }

    @Override
    public void undoAll() {
        super.undoAll();
        axis.undoAll();
    }

    @Override
    public void undoSelected() {
        super.undoSelected();
        axis.undoSelected();
    }

    @Override
    public byte[] saveFile(byte[] binData) {
        binData = super.saveFile(binData);
        binData = axis.saveFile(binData);
        return binData;
    }

    @Override
    public void setRealValue(String realValue) {
        axis.setRealValue(realValue);
        super.setRealValue(realValue);
    }

    @Override
    public void addKeyListener(KeyListener listener) {
        super.addKeyListener(listener);
        axis.addKeyListener(listener);
    }

    public void selectCellAt(int y, Table1D axisType) {
        selectCellAt(y);
    }

    @Override
    public void cursorUp() {
        if (!axis.isStatic() && data[highlightY].isSelected()) {
            axis.selectCellAt(highlightY);
        }
    }

    @Override
    public void cursorDown() {
        axis.cursorDown();
    }

    @Override
    public void cursorLeft() {
        if (highlightY > 0 && data[highlightY].isSelected()) {
            selectCellAt(highlightY - 1);
        } else {
            axis.cursorLeft();
        }
    }

    @Override
    public void cursorRight() {
        if (highlightY < data.length - 1 && data[highlightY].isSelected()) {
            selectCellAt(highlightY + 1);
        } else {
            axis.cursorRight();
        }
    }

    @Override
    public void startHighlight(int x, int y) {
        axis.clearSelection();
        super.startHighlight(x, y);
    }

    @Override
    public void copySelection() {
        Window ancestorWindow = SwingUtilities.getWindowAncestor(this);
        if(null != ancestorWindow) {
            ancestorWindow.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        }
        getEditor().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        super.copySelection();
        copySelection2DWorker = new CopySelection2DWorker(this);
        copySelection2DWorker.execute();
    }

    @Override
    public void copyTable() {
        Window ancestorWindow = SwingUtilities.getWindowAncestor(this);
        if(null != ancestorWindow) {
            ancestorWindow.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        }
        getEditor().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        copyTable2DWorker = new CopyTable2DWorker(editor.getSettings(), this);
        copyTable2DWorker.execute();
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

    @Override
    public void setAxisColor(Color axisColor) {
        axis.setAxisColor(axisColor);
    }

    @Override
    public void validateScaling() {
        super.validateScaling();
        axis.validateScaling();
    }

    @Override
    public void setScaleIndex(int scaleIndex) {
        super.setScaleIndex(scaleIndex);
        axis.setScaleByName(getScale().getName());
    }

    @Override
    public boolean isLiveDataSupported() {
        return !isNullOrEmpty(axis.getLogParam());
    }

    @Override
    public boolean isButtonSelected() {
        return true;
    }

    @Override
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

    @Override
    public void clearLiveDataTrace() {
        for (DataCell cell : data) {
            cell.setLiveDataTrace(false);
            cell.updateDisplayValue();
        }
    }
}

class CopySelection2DWorker extends SwingWorker<Void, Void> {
    Table2D table;
    Table extendedTable;

    public CopySelection2DWorker(Table2D table)
    {
        this.table = table;
    }

    @Override
    protected Void doInBackground() throws Exception {
        table.getAxis().copySelection();
        return null;
    }

    @Override
    public void done() {
        Window ancestorWindow = SwingUtilities.getWindowAncestor(table);
        if(null != ancestorWindow) {
            ancestorWindow.setCursor(null);
        }
        table.setCursor(null);
        table.getEditor().setCursor(null);
    }
}

class CopyTable2DWorker extends SwingWorker<Void, Void> {
    Settings settings;
    Table2D table;

    public CopyTable2DWorker(Settings settings, Table2D table)
    {
        this.settings = settings;
        this.table = table;
    }

    @Override
    protected Void doInBackground() throws Exception {
        String tableHeader = settings.getTable2DHeader();

        // create string
        StringBuffer output = new StringBuffer(tableHeader);
        output.append(table.getAxis().getTableAsString()).append(Table2D.NEW_LINE);
        output.append(table.getTableAsString());
        //copy to clipboard
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(output.toString()), null);
        return null;

    }

    @Override
    public void done() {
        Window ancestorWindow = SwingUtilities.getWindowAncestor(table);
        if(null != ancestorWindow) {
            ancestorWindow.setCursor(null);
        }
        table.setCursor(null);
        table.getEditor().setCursor(null);
    }
}