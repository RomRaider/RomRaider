/*
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2021 RomRaider.com
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
import javax.swing.border.EmptyBorder;

import com.romraider.Settings;
import com.romraider.editor.ecu.ECUEditorManager;

public class Table2DView extends TableView {
	private static final long serialVersionUID = -7684570967109324784L;
    private JLabel axisLabel;
    private Table1DView axis;

    private CopyTable2DWorker copyTable2DWorker;
    private CopySelection2DWorker copySelection2DWorker;
   
    public Table2DView(Table2D table) {
		super(table);
		axis = new Table1DView(table.getAxis());
        verticalOverhead += 18;
        populateTableVisual();
	}
      
	public Table1DView getAxis() {
    	return axis;
    }
    
    public JLabel getAxisLabel() {
    	return axisLabel;
    }
    
    public void setAxisLabel(JLabel label) {
    	axisLabel = label;
    }
    
    @Override
    public String toString() {
        return super.toString() + " (2D)";// + axis;
    }

    @Override
    public Dimension getFrameSize() {
        int height = verticalOverhead + cellHeight * 2;
        int width = horizontalOverhead + data.length * cellWidth;
        if (height < minHeight) {
            height = minHeight;
        }
        int minWidth = table.isLiveDataSupported() ? minWidthOverlay : minWidthNoOverlay;
        if (width < minWidth) {
            width = minWidth;
        }
        return new Dimension(width, height);
    }

    @Override
    public void populateTableVisual(){
        super.populateTableVisual();
        
    	centerLayout.setRows(2);
        centerLayout.setColumns(table.getDataSize());

        // add to table
        for (int i = 0; i < axis.getTable().getDataSize(); i++) {
            centerPanel.add(axis.getDataCell(i));
        }
        
        if (table.flip) {
            for (int i = table.getDataSize() - 1; i >= 0; i--) {
                centerPanel.add(this.getDataCell(i));
            }
        } else {
            for (int i = 0; i < table.getDataSize(); i++) {
                centerPanel.add(this.getDataCell(i));
            }
        }

        if(null == axis.getName() || axis.getName().isEmpty() || Settings.BLANK == axis.getName()) {
            ;// Do not add label.
        } else if(null == axis.getTable().getCurrentScale() || "0x" == axis.getTable().getCurrentScale().getUnit()) {
            // static or no scale exists.
            axisLabel = new JLabel(axis.getName(), JLabel.CENTER);
            add(axisLabel, BorderLayout.NORTH);
        } else {
            axisLabel = new JLabel(axis.getName() + " (" + axis.getTable().getCurrentScale().getUnit() + ")", JLabel.CENTER);
            add(axisLabel, BorderLayout.NORTH);
        }

        tableLabel = new JLabel(table.getCurrentScale().getUnit(), JLabel.CENTER);
        add(tableLabel, BorderLayout.SOUTH);
        
        if(axisLabel != null)
        	axisLabel.setBorder(new EmptyBorder(2, 4, 2, 4));   
        
        if(presetPanel != null) presetPanel.populatePanel();
        repaint();
    }

    @Override
    public void updateTableLabel() {
        if(null == axis.getName() || axis.getName().length() < 1 || Settings.BLANK == axis.getName()) {
            ;// Do not update label.
        } else if(null == axis.getTable().getCurrentScale() || "0x" == axis.getTable().getCurrentScale().getUnit()) {
            // static or no scale exists.
            axisLabel.setText(axis.getName());
        } else {
            axisLabel.setText(axis.getName() + " (" + axis.getTable().getCurrentScale().getUnit() + ")");
        }

        tableLabel.setText(table.getCurrentScale().getUnit());
    }

    @Override
    public void interpolate() throws UserLevelException {
        super.interpolate();
        this.getAxis().interpolate();
    }

    @Override
    public void verticalInterpolate() throws UserLevelException {
        super.verticalInterpolate();
        this.getAxis().verticalInterpolate();
    }

    @Override
    public void horizontalInterpolate() throws UserLevelException {
        int[] coords = { table.getDataSize(), 0};
        DataCellView[] tableData = getData();
        DataCellView[] axisData = getAxis().getData();

        for (int i = 0; i < table.getDataSize(); ++i) {
            if (tableData[i].isSelected()) {
                if (i < coords[0])
                    coords[0] = i;
                if (i > coords[1])
                    coords[1] = i;
            }
        }
        if (coords[1] - coords[0] > 1) {
            double x, x1, x2, y1, y2;
            x1 = axisData[coords[0]].getDataCell().getBinValue();
            y1 = tableData[coords[0]].getDataCell().getBinValue();
            x2 = axisData[coords[1]].getDataCell().getBinValue();
            y2 = tableData[coords[1]].getDataCell().getBinValue();
            for (int i = coords[0] + 1; i < coords[1]; ++i) {
                x = axisData[i].getDataCell().getBinValue();
                data[i].getDataCell().setBinValue(linearInterpolation(x, x1, x2, y1, y2));
            }
        }
        // Interpolate x axis in case the x axis in selected.
        this.getAxis().horizontalInterpolate();
    }
    
    @Override
    public void clearSelection() {
        axis.clearSelection();
        super.clearSelection();
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
        
        if(axis !=null)
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
	public void shiftCursorUp() {
        if (data[highlightY].isSelected()) {
        	data[highlightY].setSelected(false);
        }
        axis.selectCellAt(highlightY);
	}

	@Override
	public void shiftCursorDown() {
        axis.cursorDown();
	}

	@Override
	public void shiftCursorLeft() {
        if (highlightY > 0 && data[highlightY].isSelected()) {
        	selectCellAtWithoutClear(highlightY - 1);
        } else {
        	axis.shiftCursorLeft();
        }
	}

	@Override
	public void shiftCursorRight() {
        if (highlightY < data.length - 1 && data[highlightY].isSelected()) {
        	selectCellAtWithoutClear(highlightY + 1);
        } else {
        	axis.shiftCursorRight();
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
    public void paste() throws UserLevelException {
        StringTokenizer st = new StringTokenizer(Settings.BLANK);
        String input = Settings.BLANK;
        try {
            input = (String) Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null).getTransferData(DataFlavor.stringFlavor);
            st = new StringTokenizer(input, table.ST_DELIMITER);
        } catch (UnsupportedFlavorException ex) { /* wrong paste type -- do nothing */
        } catch (IOException ex) {
        }

        String pasteType = st.nextToken();

        if (pasteType.equalsIgnoreCase("[Table2D]")) { // Paste table
            String currentToken = st.nextToken(Settings.NEW_LINE);
            if (currentToken.endsWith("\t")) {
                currentToken = st.nextToken(Settings.NEW_LINE);
            }

            String axisValues = "[Table1D]" + Settings.NEW_LINE + currentToken;
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
    public void clearLiveDataTrace() {
        super.clearLiveDataTrace();
        axis.clearLiveDataTrace();
    }

    @Override
    public void updateLiveDataHighlight() {
        if (getOverlayLog()) {
            data[axis.getPreviousLiveDataIndex()].setPreviousLiveDataTrace(true);
            data[axis.getLiveDataIndex()].setPreviousLiveDataTrace(false);
            data[axis.getLiveDataIndex()].setLiveDataTrace(true);
        }
    }


    @Override
    public void setOverlayLog(boolean overlayLog) {
        super.setOverlayLog(overlayLog);
        axis.setOverlayLog(overlayLog);
    }

    @Override
    public void setCompareDisplay(Settings.CompareDisplay compareDisplay) {
        super.setCompareDisplay(compareDisplay);
        axis.setCompareDisplay(compareDisplay);
    }

    @Override
    public boolean equals(Object other) {
        return table.equals(other);
    }

    @Override
    public void repaint() {
        super.repaint();
        if(null != axis) {
            axis.repaint();
        }
    } 
}

class CopySelection2DWorker extends SwingWorker<Void, Void> {
	Table2DView table;
    TableView extendedTable;

    public CopySelection2DWorker(Table2DView table)
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
    Table2DView table;

    public CopyTable2DWorker(Table2DView table)
    {
        this.table = table;
    }

    @Override
    protected Void doInBackground() throws Exception {
        String tableHeader = table.getSettings().getTable2DHeader();
        StringBuffer output = new StringBuffer(tableHeader);
        output.append(table.getTable().getTableAsString());

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
