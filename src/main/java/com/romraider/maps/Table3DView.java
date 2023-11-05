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
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.KeyListener;

import java.util.StringTokenizer;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import com.romraider.Settings;
import com.romraider.editor.ecu.ECUEditorManager;
import com.romraider.logger.ecu.ui.swing.vertical.VerticalLabelUI;
import com.romraider.maps.Table1DView.Table1DType;
import com.romraider.util.NumberUtil;

public class Table3DView extends TableView {

    private static final long serialVersionUID = 3103448753263606599L;
   // private static final ResourceBundle rb = new ResourceUtil().getBundle(Table3DView.class.getName());
    private Table3D table;
    private Table1DView xAxis;
    private Table1DView yAxis;
    private JLabel xAxisLabel;
    private JLabel yAxisLabel;

    DataCellView[][] data;

    public Table3DView(Table3D table) {
    	super(table);
    	this.table = table;
    	xAxis = new Table1DView(table.getXAxis(), Table1DType.X_AXIS);
    	yAxis = new Table1DView(table.getYAxis(), Table1DType.Y_AXIS);
    	xAxis.setAxisParent(this);
    	yAxis.setAxisParent(this);
    	
        verticalOverhead += 39;
        horizontalOverhead += 10;
    }
    
    @Override
    public Table3D getTable() {
    	return table;
    }

    public Table1DView getXAxis() {
    	return xAxis;
    }
    
    public Table1DView getYAxis() {
    	return yAxis;
    }
    
    @Override
    public void drawTable() {
    	if(data!=null) {
	        for(DataCellView[] column : data) {
	            for(DataCellView cell : column) {
	                if(null != cell) {
	                    cell.drawCell();
	                }
	            }
	        }
    	}
    	
    	if(xAxis!=null)
    		xAxis.drawTable();
    	
    	if(yAxis!=null)
    		yAxis.drawTable();
    	
    	updateTableLabel();
    }

    @Override
    public void populateTableVisual() {	
    	// fill first empty cell
        centerPanel.add(new JLabel());
        centerLayout.setColumns(table.getSizeX()+1);
        centerLayout.setRows(table.getSizeY()+1);
        
        // temporarily remove lock
        boolean tempLock = table.locked;
        table.locked = false;
        
        xAxis.populateTableVisual();
        yAxis.populateTableVisual();
        for (int x = 0; x < xAxis.getTable().getDataSize(); x++) {
            centerPanel.add(xAxis.getDataCell(x));
        }

        data = new DataCellView[table.getSizeX()][table.getSizeY()];
        
        int iMax = table.getSwapXY() ? xAxis.getTable().getDataSize() : yAxis.getTable().getDataSize();
        int jMax = table.getSwapXY() ? yAxis.getTable().getDataSize() : xAxis.getTable().getDataSize();
        for (int i = 0; i < iMax; i++) {
            for (int j = 0; j < jMax; j++) {

                int x = table.getFlipY() ? jMax - j - 1 : j;
                int y = table.getFlipX() ? iMax - i - 1 : i;
                if (table.getSwapXY()) {
                    int z = x;
                    x = y;
                    y = z;
                }

                // show locked cell
                if (tempLock) {
                    data[x][y].setForeground(Color.GRAY);
                }
                            
                data[x][y] = new DataCellView(table.get3dData()[x][y], this, x,y);
            }
        }

        for (int y = 0; y < yAxis.getTable().getDataSize(); y++) {
            centerPanel.add(yAxis.getDataCell(y));
            for (int x = 0; x < xAxis.getTable().getDataSize(); x++) {
                centerPanel.add(data[x][y]);
            }
        }
        
        // reset locked status
        table.locked = tempLock;

        GridLayout topLayout = new GridLayout(2, 1);
        JPanel topPanel = new JPanel(topLayout);
        this.add(topPanel, BorderLayout.NORTH);
        topPanel.add(new JLabel(getName(), JLabel.CENTER), BorderLayout.NORTH);

        if(null == xAxis.getName() || xAxis.getName().length() < 1 || Settings.BLANK == xAxis.getName()) {
            ;// Do not add label.
        } else if(null == xAxis.getTable().getCurrentScale() || "0x" == xAxis.getTable().getCurrentScale().getUnit()) {
            // static or no scale exists.
            xAxisLabel = new JLabel(xAxis.getName(), JLabel.CENTER);
            topPanel.add(xAxisLabel, BorderLayout.NORTH);
        } else {
            xAxisLabel = new JLabel(xAxis.getName() + " (" + xAxis.getTable().getCurrentScale().getUnit() + ")", JLabel.CENTER);
            topPanel.add(xAxisLabel, BorderLayout.NORTH);
        }

        yAxisLabel = null;
        if(null == yAxis.getName() || yAxis.getName().length() < 1 || Settings.BLANK == yAxis.getName()) {
            ;// Do not add label.
        } else if(null == yAxis.getTable().getCurrentScale() || "0x" == yAxis.getTable().getCurrentScale().getUnit()) {
            // static or no scale exists.
            yAxisLabel = new JLabel(yAxis.getName());
        } else {
            yAxisLabel = new JLabel(yAxis.getName() + " (" + yAxis.getTable().getCurrentScale().getUnit() + ")");
        }
        
        if(yAxisLabel!=null) {
	        yAxisLabel.setUI(new VerticalLabelUI(false));
	        add(yAxisLabel, BorderLayout.WEST);
	        yAxisLabel.setBorder(new EmptyBorder(2, 4, 2, 4));  
        }
        
        tableLabel = new JLabel(table.getCurrentScale().getUnit(), JLabel.CENTER);
        add(tableLabel, BorderLayout.SOUTH);
               
        if(xAxisLabel!=null)
        	xAxisLabel.setBorder(new EmptyBorder(2, 4, 2, 4)); 
        
        if(presetPanel != null) presetPanel.populatePanel();
    }

    @Override
    public void updateTableLabel() {
        if(null == xAxis.getName() || xAxis.getName().length() < 1 || Settings.BLANK == xAxis.getName()) {
            ;// Do not update label.
        } else if(null == xAxis.getTable().getCurrentScale() || "0x" == xAxis.getTable().getCurrentScale().getUnit()) {
            // static or no scale exists.
            xAxisLabel.setText(xAxis.getName());
        } else {
            xAxisLabel.setText(xAxis.getName() + " (" + xAxis.getTable().getCurrentScale().getUnit() + ")");
        }

        if(null == yAxis.getName() || yAxis.getName().length() < 1 || Settings.BLANK == yAxis.getName()) {
            ;// Do not update label.
        } else if(null == yAxis.getTable().getCurrentScale() || "0x" == yAxis.getTable().getCurrentScale().getUnit()) {
            // static or no scale exists.
            yAxisLabel.setText(yAxis.getName());
        } else {
            yAxisLabel.setText(yAxis.getName() + " (" + yAxis.getTable().getCurrentScale().getUnit() + ")");
        }

        tableLabel.setText(table.getCurrentScale().getUnit());
    }

    @Override
    public Dimension getFrameSize() {
        int height = verticalOverhead + cellHeight * data[0].length;
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
    public void highlight(int xCoord, int yCoord) {
        if (highlight) {
            for (int x = 0; x < table.getSizeX(); x++) {
                for (int y = 0; y < table.getSizeY(); y++) {
                    if (((y >= highlightBeginY && y <= yCoord) ||
                            (y <= highlightBeginY && y >= yCoord)) &&
                            ((x >= highlightBeginX && x <= xCoord) ||
                                    (x <= highlightBeginX && x >= xCoord))) {
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
        for (int x = 0; x < table.getSizeX(); x++) {
            for (int y = 0; y < table.getSizeY(); y++) {
                if (data[x][y].isHighlighted()) {
                    data[x][y].getDataCell().setSelected(true);
                    data[x][y].setHighlighted(false);
                }
            }
        }
    }

    @Override
    public void undoSelected() throws UserLevelException {
        clearLiveDataTrace();
        for (int x = 0; x < table.getSizeX(); x++) {
            for (int y = 0; y < table.getSizeY(); y++) {
                if (data[x][y].isSelected()) {
                    data[x][y].getDataCell().undo();
                }
            }
        }
    }

    @Override
    public void addKeyListener(KeyListener listener) {
        xAxis.addKeyListener(listener);
        yAxis.addKeyListener(listener);
        for (int x = 0; x < table.getSizeX(); x++) {
            for (int y = 0; y < table.getSizeY(); y++) {
                data[x][y].addKeyListener(listener);
            }
        }
    }

    public void selectCellAt(int y, Table1DView axisType) {
        if (axisType.getType() == Table1DType.Y_AXIS) {
            table.selectCellAt(0, y);
        } else { // y axis
            table.selectCellAt(y, 0);
        }
        ECUEditorManager.getECUEditor().getTableToolBar().updateTableToolBar(table);
    }

    @Override
    public void cursorUp() {
        if (highlightBeginY > 0 && data[highlightBeginX][highlightBeginY].isSelected()) {
            table.selectCellAt(highlightBeginX, highlightBeginY - 1);
        } else if (data[highlightBeginX][highlightBeginY].isSelected()) {
            xAxis.getTable().selectCellAt(highlightBeginX);
        } else {
            xAxis.cursorUp();
            yAxis.cursorUp();
        }
    }

    @Override
    public void cursorDown() {
        if (highlightBeginY < table.getSizeY() - 1 && data[highlightBeginX][highlightBeginY].isSelected()) {
            table.selectCellAt(highlightBeginX, highlightBeginY + 1);
        } else {
            xAxis.cursorDown();
            yAxis.cursorDown();
        }
    }

    @Override
    public void cursorLeft() {
        if (highlightBeginX > 0 && data[highlightBeginX][highlightBeginY].isSelected()) {
            table.selectCellAt(highlightBeginX - 1, highlightBeginY);
        } else if (data[highlightBeginX][highlightBeginY].isSelected()) {
            yAxis.getTable().selectCellAt(highlightBeginY);
        } else {
            xAxis.cursorLeft();
            yAxis.cursorLeft();
        }
    }

    @Override
    public void cursorRight() {
        if (highlightBeginX < table.getSizeX() - 1 && data[highlightBeginX][highlightBeginY].isSelected()) {
            table.selectCellAt(highlightBeginX + 1, highlightBeginY);
        } else {
            xAxis.cursorRight();
            yAxis.cursorRight();
        }
    }

	@Override
	public void shiftCursorUp() {
        if (highlightBeginY > 0 && data[highlightBeginX][highlightBeginY].isSelected()) {
        	table.selectCellAtWithoutClear(highlightBeginX, highlightBeginY - 1);
        } else if (data[highlightBeginX][highlightBeginY].isSelected()) {
        	data[highlightBeginX][highlightBeginY].getDataCell().setSelected(false);
        	xAxis.getTable().selectCellAt(highlightBeginX);
        } else {
        	xAxis.cursorUp();
        	yAxis.shiftCursorUp();
        }
	}

	@Override
	public void shiftCursorDown() {
        if (highlightBeginY < table.getSizeY() - 1 && data[highlightBeginX][highlightBeginY].isSelected()) {
        	table.selectCellAtWithoutClear(highlightBeginX, highlightBeginY + 1);
        } else {
            xAxis.shiftCursorDown();
            yAxis.shiftCursorDown();
        }
	}

	@Override
	public void shiftCursorLeft() {
        if (highlightBeginX > 0 && data[highlightBeginX][highlightBeginY].isSelected()) {
        	table.selectCellAtWithoutClear(highlightBeginX - 1, highlightBeginY);
        } else if (data[highlightBeginX][highlightBeginY].isSelected()) {
            yAxis.getTable().selectCellAt(highlightBeginY);
        } else {
            xAxis.shiftCursorLeft();
            yAxis.shiftCursorLeft();
        }
	}

	@Override
	public void shiftCursorRight() {
        if (highlightBeginX < table.getSizeX() - 1 && data[highlightBeginX][highlightBeginY].isSelected()) {
        	table.selectCellAtWithoutClear(highlightBeginX + 1, highlightBeginY);
        } else {
            xAxis.shiftCursorRight();
            yAxis.shiftCursorRight();
        }
	}

    @Override
    public void startHighlight(int x, int y) {
        xAxis.getTable().clearSelection();
        yAxis.getTable().clearSelection();
        super.startHighlight(x, y);
    }

    @Override
    public void copySelection() {
    	// find bounds of selection
        // coords[0] = x min, y min, x max, y max
        boolean copy = false;
        int[] coords = new int[4];
        coords[0] = getTable().getSizeX();
        coords[1] = getTable().getSizeY();

        for (int x = 0; x < getTable().getSizeX(); x++) {
            for (int y = 0; y < getTable().getSizeY(); y++) {
                if (get3dData()[x][y].isSelected()) {
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
            StringBuffer output = new StringBuffer("[Selection3D]" + Settings.NEW_LINE);
            for (int y = coords[1]; y <= coords[3]; y++) {
                for (int x = coords[0]; x <= coords[2]; x++) {
                    if (get3dData()[x][y].isSelected()) {
                        output.append(NumberUtil.stringValue(get3dData()[x][y].getDataCell().getRealValue()));
                    } else {
                        output.append("x"); // x represents non-selected cell
                    }
                    if (x < coords[2]) {
                        output.append("\t");
                    }
                }
                if (y < coords[3]) {
                    output.append(Settings.NEW_LINE);
                }
                
                setClipboard(String.valueOf(output));
            }
        } else {
            getTable().getXAxis().getTableView().copySelection();
            getTable().getYAxis().getTableView().copySelection();
        }
    }

    @Override
    public void copyTable() {
        String tableHeader = TableView.getSettings().getTable3DHeader();
        StringBuffer output = new StringBuffer(tableHeader);
        output.append(getTable().getTableAsString());
        
        setClipboard(String.valueOf(output));
    }

    @Override
    public void paste(String s) throws UserLevelException {        
    	StringTokenizer st = new StringTokenizer(s, Table.ST_DELIMITER);
        String pasteType = st.nextToken();

        if ("[Table3D]".equalsIgnoreCase(pasteType)) { // Paste table
            String currentToken = st.nextToken(Settings.NEW_LINE);
            if (currentToken.endsWith("\t")) {
                currentToken = st.nextToken(Settings.NEW_LINE);
            }
            String xAxisValues = "[Table1D]" + Settings.NEW_LINE + currentToken;

            // build y axis and data values
            StringBuffer yAxisValues = new StringBuffer("[Table1D]" + Settings.NEW_LINE + st.nextToken("\t"));
            StringBuffer dataValues = new StringBuffer("[Table3D]" + Settings.NEW_LINE + st.nextToken("\t") + st.nextToken(Settings.NEW_LINE));
            while (st.hasMoreTokens()) {
                yAxisValues.append("\t").append(st.nextToken("\t"));
                dataValues.append(Settings.NEW_LINE).append(st.nextToken("\t")).append(st.nextToken(Settings.NEW_LINE));
            }

            xAxis.paste(xAxisValues);
            yAxis.paste(String.valueOf(yAxisValues));           
            pasteValues(String.valueOf(dataValues));
            
        } else if ("[Selection3D]".equalsIgnoreCase(pasteType)) { // paste selection
            pasteValues(s);
        } else if ("[Selection1D]".equalsIgnoreCase(pasteType)) { // paste selection
            xAxis.paste();
            yAxis.paste();
        }
    }

    public void pasteValues(String s) throws UserLevelException {
        StringTokenizer st = new StringTokenizer(s, Table.ST_DELIMITER);
        String pasteType = st.nextToken();

        // figure paste start cell
        int startX = 0;
        int startY = 0;
        
        // if pasting a table, startX and Y at 0, else find highlight
        if ("[Selection3D]".equalsIgnoreCase(pasteType)) {
        	boolean somethingSelected = false;
        	
            for (int x = getTable().getSizeX() - 1; x >=0 ; x--) {
                for (int y = 0; y < getTable().getSizeY(); y++) {
                	if(data[x][y].isSelected()) {
                		startX = x;
                		startY = y;
                		somethingSelected = true;
                		break;
                	}
                }
            }
            
            if(!somethingSelected) return;
        }
        
        // set values
        for (int y = startY; st.hasMoreTokens() && y < table.getSizeY(); y++) {
            String checkToken = st.nextToken(Settings.NEW_LINE);
            if (y==startY && checkToken.endsWith("\t")) {
                checkToken = st.nextToken(Settings.NEW_LINE);
            }
            StringTokenizer currentLine = new StringTokenizer(checkToken, Table.ST_DELIMITER);
            for (int x = startX; currentLine.hasMoreTokens() && x < table.getSizeX(); x++) {
                String currentToken = currentLine.nextToken();

                try {
                    if (!data[x][y].getText().equalsIgnoreCase(currentToken)) {
                        data[x][y].getDataCell().setRealValue(currentToken);
                    }
                } catch (ArrayIndexOutOfBoundsException ex) { /* copied table is larger than current table*/ }
            }
        }
    }

    @Override
    public void highlightLiveData(String liveValue) {
        if (getOverlayLog()) {
            int x = xAxis.getLiveDataIndex();
            int y = yAxis.getLiveDataIndex();
            DataCellView cell = data[x][y];
            cell.setLiveDataTrace(true);
            cell.getDataCell().setLiveDataTraceValue(liveValue);
            getToolbar().setLiveDataValue(liveValue);
        }
    }

    @Override
    public void updateLiveDataHighlight() {
        if (getOverlayLog()) {
            int x = xAxis.getLiveDataIndex();
            int y = yAxis.getLiveDataIndex();
            int xp = xAxis.getPreviousLiveDataIndex();
            int yp = yAxis.getPreviousLiveDataIndex();
            data[xp][yp].setPreviousLiveDataTrace(true);
            data[x][y].setPreviousLiveDataTrace(false);
            data[x][y].setLiveDataTrace(true);
        }
    }

    @Override
    public void clearLiveDataTrace() {
        xAxis.clearLiveDataTrace();
        yAxis.clearLiveDataTrace();
        for (int x = 0; x < table.getSizeX(); x++) {
            for (int y = 0; y < table.getSizeY(); y++) {
                data[x][y].setLiveDataTrace(false);
                data[x][y].setPreviousLiveDataTrace(false);
            }
        }
    }

    public DataCellView[][] get3dData() {
        return data;
    }

    @Override
    public void setCompareDisplay(Settings.CompareDisplay compareDisplay) {
        super.setCompareDisplay(compareDisplay);
        xAxis.setCompareDisplay(compareDisplay);
        yAxis.setCompareDisplay(compareDisplay);
    }

    @Override
    public void setOverlayLog(boolean overlayLog) {
        super.setOverlayLog(overlayLog);
        xAxis.setOverlayLog(overlayLog);
        yAxis.setOverlayLog(overlayLog);
    }

    @Override
    public boolean equals(Object other) {
       return table.equals(other);
    }

    @Override
    public void repaint() {
        super.repaint();

        if(null != xAxis) {
            xAxis.repaint();
        }

        if(null != yAxis) {
            yAxis.repaint();
        }
    }
}
