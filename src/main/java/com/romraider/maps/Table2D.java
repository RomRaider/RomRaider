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
import com.romraider.editor.ecu.ECUEditorManager;
import com.romraider.util.AxisRange;

public class Table2D extends Table {
    private static final long serialVersionUID = -7684570967109324784L;
    private Table1D axis = new Table1D();

    private CopyTable2DWorker copyTable2DWorker;
    private CopySelection2DWorker copySelection2DWorker;

    public Table2D() {
        super();
        verticalOverhead += 18;
    }

    public Table1D getAxis() {
        return axis;
    }

    public void setAxis(Table1D axis) {
        this.axis = axis;
        axis.setAxisParent(this);
    }

    @Override
    public String toString() {
        return super.toString() + " (2D)";// + axis;
    }

    @Override
    public void refreshDataBounds(){
        super.refreshDataBounds();
        axis.refreshDataBounds();
    }

    @Override
    public void refreshCompares() {
        super.refreshCompares();
        axis.refreshCompares();
    }

    @Override
    public void populateCompareValues(Table otherTable) {
        loaded = false;
        if(null == otherTable || !(otherTable instanceof Table2D)) {
            loaded = true;
            return;
        }

        Table2D compareTable2D = (Table2D) otherTable;
        if(data.length != compareTable2D.data.length ||
                axis.data.length != compareTable2D.axis.data.length) {
            loaded = true;
            return;
        }
        loaded = true;
        refreshDataBounds();
        drawTable();

        super.populateCompareValues(otherTable);
        axis.populateCompareValues(compareTable2D.getAxis());
    }

    @Override
    public StringBuffer getTableAsString() {
        StringBuffer output = new StringBuffer(Settings.BLANK);
        output.append(axis.getTableAsString());
        output.append(Settings.NEW_LINE);
        output.append(super.getTableAsString());
        return output;
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
    public void populateTable(byte[] input, int ramOffset) throws ArrayIndexOutOfBoundsException {
        centerLayout.setRows(2);
        centerLayout.setColumns(this.getDataSize());

        try {
            axis.populateTable(input, ramOffset);
            super.populateTable(input, ramOffset);
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

        if(null == axis.getName() || axis.getName().length() < 1 || "" == axis.getName()) {
            ;// Do not add label.
        } else if("0x" == axis.getScale().getUnit()) {
            // static or no scale exists.
            add(new JLabel(axis.getName(), JLabel.CENTER), BorderLayout.NORTH);
        } else {
            add(new JLabel(axis.getName() + " (" + axis.getScale().getUnit() + ")", JLabel.CENTER), BorderLayout.NORTH);
        }
        add(new JLabel(getScale().getUnit(), JLabel.CENTER), BorderLayout.SOUTH);
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
        axis.clearSelectedData();
        clearSelectedData();
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

    @Override
    public void cursorUp() {
        if (data[highlightY].isSelected()) {
            axis.selectCellAt(highlightY);
        }
    }

    @Override
    public void drawTable() {
        super.drawTable();
        axis.drawTable();
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
        axis.clearSelectedData();
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
        ECUEditorManager.getECUEditor().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        copyTable2DWorker = new CopyTable2DWorker(this);
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
            String axisValues = "[Table1D]" + Settings.NEW_LINE + st.nextToken(Settings.NEW_LINE);
            String dataValues = "[Table1D]" + Settings.NEW_LINE + st.nextToken(Settings.NEW_LINE);

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
    public void highlightLiveData(String liveValue) {
        if (overlayLog) {
            AxisRange range = getLiveDataRangeForAxis(axis);
            clearSelection();
            boolean first = true;
            for (int i = range.getStartIndex(); i <= range.getEndIndex(); i++) {
                int x = 0;
                int y = i;
                if (axis.getType() == Settings.TABLE_X_AXIS) {
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
                cell.setLiveDataTraceValue(liveValue);
            }
            stopHighlight();
            getToolbar().setLiveDataValue(liveValue);
        }
    }

    @Override
    public void clearLiveDataTrace() {
        for (DataCell cell : data) {
            cell.setLiveDataTrace(false);
        }
    }

    @Override
    public void setCompareDisplay(int compareDisplay) {
        super.setCompareDisplay(compareDisplay);
        axis.setCompareDisplay(compareDisplay);
    }

    @Override
    public void setCompareValueType(int compareValueType) {
        super.setCompareValueType(compareValueType);
        axis.setCompareValueType(compareValueType);
    }

    @Override
    public void setDisplayValueType(int displayValueType) {
        super.setDisplayValueType(displayValueType);
        axis.setDisplayValueType(displayValueType);
    }

    @Override
    public void addComparedToTable(Table table) {
        if(!(table instanceof Table2D)) {
            return;
        }

        Table2D table2D = (Table2D) table;

        super.addComparedToTable(table2D);
        axis.addComparedToTable(table2D.axis);
    }

    @Override
    public void removeComparedToTable(Table table) {
        if(!(table instanceof Table2D)) {
            return;
        }

        Table2D table2D = (Table2D) table;

        super.removeComparedToTable(table2D);
        axis.removeComparedToTable(table2D.axis);
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

            if(!(other instanceof Table2D)) {
                return false;
            }

            Table2D otherTable = (Table2D)other;

            if(!this.getName().equalsIgnoreCase(otherTable.getName())) {
                return false;
            }

            if(!this.axis.equals(otherTable.axis)) {
                return false;
            }

            if(this.data.length != otherTable.data.length)
            {
                return false;
            }

            if(this.data.equals(otherTable.data))
            {
                return true;
            }

            // Compare Bin Values
            for(int i = 0 ; i < this.data.length ; i++) {
                if(! this.data[i].equals(otherTable.data[i])) {
                    return false;
                }
            }

            return true;
        } catch(Exception ex) {
            // TODO: Log Exception.
            return false;
        }
    }

    @Override
    public void repaint() {
        super.repaint();
        if(null != axis) {
            axis.repaint();
        }
    }

    @Override
    public void setComparing(boolean comparing) {
        super.setComparing(comparing);
        axis.setComparing(comparing);
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
        ECUEditorManager.getECUEditor().setCursor(null);
    }
}

class CopyTable2DWorker extends SwingWorker<Void, Void> {
    Table2D table;

    public CopyTable2DWorker(Table2D table)
    {
        this.table = table;
    }

    @Override
    protected Void doInBackground() throws Exception {
        String tableHeader = table.getSettings().getTable2DHeader();
        StringBuffer output = new StringBuffer(tableHeader);
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
        ECUEditorManager.getECUEditor().setCursor(null);
    }
}