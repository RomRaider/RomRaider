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

import static com.romraider.util.ParamChecker.isNullOrEmpty;

import javax.naming.NameNotFoundException;
import com.romraider.Settings;
import com.romraider.util.NumberUtil;
import com.romraider.util.SettingsManager;

public class Table3D extends Table {

    private static final long serialVersionUID = 3103448753263606599L;

    private Table1D xAxis = new Table1D(TableType.X_AXIS);
    private Table1D yAxis= new Table1D(TableType.Y_AXIS);;

    DataCell[][] data = new DataCell[1][1];
    private boolean swapXY = false;
    private boolean flipX = false;
    private boolean flipY = false;

    @Override
    public TableType getType() {
        return Table.TableType.TABLE_3D;
    }
    
    public Table3DView getTableView() {
    	return (Table3DView) tableView;
    }
    
    public Table1D getXAxis() {
        return xAxis;
    }

    public void setXAxis(Table1D xAxis) {
        this.xAxis = xAxis;
        xAxis.setAxisParent(this);
    }

    public Table1D getYAxis() {
        return yAxis;
    }

    public void setYAxis(Table1D yAxis) {
        this.yAxis = yAxis;
        yAxis.setAxisParent(this);
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
    }

    public int getSizeX() {
        return data.length;
    }

    public void setSizeY(int size) {
        data = new DataCell[data.length][size];
    }

    public int getSizeY() {
        return data[0].length;
    }
    
    @Override
    public void clearData() {
        for(DataCell[] column : data) {
            for(DataCell cell : column) {
	    		cell.setTable(null);
	    		cell.setRom(null);
            }
        }
    	
    	xAxis.clearData();
    	yAxis.clearData();
    	
    	data = null;
    	xAxis=null;
    	yAxis=null;
    }
    
    @Override
    public StringBuffer getTableAsString() {
        StringBuffer output = new StringBuffer(Settings.BLANK);

        output.append(xAxis.getTableAsString());
        output.append(Settings.NEW_LINE);

        for (int y = 0; y < getSizeY(); y++) {
            output.append(NumberUtil.stringValue(yAxis.data[y].getRealValue()));
            output.append(Settings.TAB);

            for (int x = 0; x < getSizeX(); x++) {

            	output.append(NumberUtil.stringValue(data[x][y].getRealValue()));
                
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
    public void populateTable(Rom rom) throws NullPointerException, ArrayIndexOutOfBoundsException, IndexOutOfBoundsException {
    	validateScaling();
    	
    	// fill first empty cell
        if (!beforeRam) {
            this.ramOffset = rom.getRomID().getRamOffset();
        }

        // temporarily remove lock
        boolean tempLock = locked;
        locked = false;

        // populate axes
        try {
            xAxis.populateTable(rom);
            yAxis.populateTable(rom);
        } catch (ArrayIndexOutOfBoundsException ex) {
            throw new ArrayIndexOutOfBoundsException();
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
                DataCell c = new DataCell(this, offset, rom);
                data[x][y] = c;
                offset++;
            }
        }

        // reset locked status
        locked = tempLock;      
        calcCellRanges();
    }

    @Override
    public void calcCellRanges() {
        double binMax = data[0][0].getBinValue();
        double binMin = data[0][0].getBinValue();

        double compareMax = data[0][0].getCompareValue();
        double compareMin = data[0][0].getCompareValue();

        for(DataCell[] column : data) {
            for(DataCell cell : column) {
                // Calc bin
                if(binMax < cell.getBinValue()) {
                    binMax = cell.getBinValue();
                }
                if(binMin > cell.getBinValue()) {
                    binMin = cell.getBinValue();
                }

                // Calc compare
                double compareValue = cell.getCompareValue();
                if(compareMax < compareValue) {
                    compareMax = compareValue;
                }
                if(compareMin > compareValue) {
                    compareMin = compareValue;
                }
            }
        }
        setMaxBin(binMax);
        setMinBin(binMin);
        setMaxCompare(compareMax);
        setMinCompare(compareMin);
    }

    @Override
    public void populateCompareValues(Table otherTable) {
        if(null == otherTable || !(otherTable instanceof Table3D)) {
            return;
        }

        Table3D compareTable3D = (Table3D) otherTable;
        if(data.length != compareTable3D.data.length ||
                data[0].length != compareTable3D.data[0].length ||
                xAxis.getDataSize() != compareTable3D.xAxis.getDataSize() ||
                yAxis.getDataSize() != compareTable3D.yAxis.getDataSize()) {
            return;
        }

        int x=0;
        for (DataCell[] column : data) {
            int y = 0;
            for(DataCell cell : column) {
                cell.setCompareValue(compareTable3D.data[x][y]);
                y++;
            }
            x++;
        }

        xAxis.populateCompareValues(compareTable3D.getXAxis());
        yAxis.populateCompareValues(compareTable3D.getYAxis());

        calcCellRanges();
    }

    @Override
    public void refreshCompare() {
        populateCompareValues(getCompareTable());
        xAxis.refreshCompare();
        yAxis.refreshCompare();
    }


    @Override
    public String toString() {
        return super.toString() + " (3D)";
    }

    

    @Override
    public void setRevertPoint() {
        for (int x = 0; x < this.getSizeX(); x++) {
            for (int y = 0; y < this.getSizeY(); y++) {
                data[x][y].setRevertPoint();
            }
        }
        yAxis.setRevertPoint();
        xAxis.setRevertPoint();
    }

    @Override
    public void undoAll() throws UserLevelException {
        for (int x = 0; x < this.getSizeX(); x++) {
            for (int y = 0; y < this.getSizeY(); y++) {
                data[x][y].undo();
            }
        }
        yAxis.undoAll();
        xAxis.undoAll();
    }

    @Override
    public byte[] saveFile(byte[] binData) {
    	return binData;
    }

    @Override
    public boolean isLiveDataSupported() {
        return !isNullOrEmpty(xAxis.getLogParam()) && !isNullOrEmpty(yAxis.getLogParam());
    }

    @Override
    public boolean isButtonSelected() {
        return true;
    }

    public DataCell[][] get3dData() {
        return data;
    }

    @Override
    public void setCompareValueType(Settings.DataType compareValueType) {
        super.setCompareValueType(compareValueType);
        xAxis.setCompareValueType(compareValueType);
        yAxis.setCompareValueType(compareValueType);
    }

    @Override
    public void setCurrentScale(Scale curScale) {
        if(SettingsManager.getSettings().isScaleHeadersAndData()) {
            if(!xAxis.isStaticDataTable()) {
                try {
                    this.xAxis.setScaleByName(curScale.getName());
                } catch (NameNotFoundException e) {
                    try {
                        this.xAxis.setScaleByName(SettingsManager.getSettings().getDefaultScale());
                    } catch (NameNotFoundException e1) {
                    	try {
                            this.xAxis.setScaleByName("Default");
                        } catch (NameNotFoundException e2) {
                        	e2.printStackTrace();
                        }
                    }
                }
            }
            if(!yAxis.isStaticDataTable()) {
                try {
                    this.yAxis.setScaleByName(curScale.getName());
                } catch (NameNotFoundException e) {
                    try {
                        this.yAxis.setScaleByName(SettingsManager.getSettings().getDefaultScale());
                    } catch (NameNotFoundException e1) {
                    	try {
                            this.yAxis.setScaleByName("Default");
                        } catch (NameNotFoundException e2) {
                        	e2.printStackTrace();
                        }
                    }
                }
            }
        }
        
        this.curScale = curScale;     
        if(tableView!=null) tableView.drawTable();
    }
    
    private void setHighlightXY(int x, int y) {
        if(tableView!=null) {
        	tableView.highlightBeginX = x;
        	tableView.highlightBeginY = y;
        }
    }
    
    public void deSelectCellAt(int x, int y) {
        clearSelection();
        data[x][y].setSelected(false);
        setHighlightXY(x,y);
    }
    
    public void selectCellAt(int x, int y) {
        clearSelection();
        data[x][y].setSelected(true);
        setHighlightXY(x,y);
    }
    
    public void selectCellAtWithoutClear(int x, int y) {
        data[x][y].setSelected(true);
        setHighlightXY(x,y);
    }
    
    @Override
    public void clearSelection() {
    	if(xAxis!=null)
    		xAxis.clearSelection();
    	
    	if(yAxis!=null)
    		yAxis.clearSelection();
        
    	if(data!=null) {
	        for (int x = 0; x < getSizeX(); x++) {
	            for (int y = 0; y < getSizeY(); y++) {
	                data[x][y].setSelected(false);
	            }
	        }
    	}
    }
    
    @Override
    public void increment(double increment) throws UserLevelException {
            for (int x = 0; x < getSizeX(); x++) {
                for (int y = 0; y < getSizeY(); y++) {
                    if (data[x][y].isSelected()) {
                        data[x][y].increment(increment);
                    }
                }
            }
    }

    @Override
    public void multiply(double factor) throws UserLevelException {
            for (int x = 0; x < getSizeX(); x++) {
                for (int y = 0; y < getSizeY(); y++) {
                    if (data[x][y].isSelected()) {                    
                    		data[x][y].multiply(factor);                            
                    }
                }
            }        
    }
    
    @Override
    public void setRealValue(String realValue) throws UserLevelException {
        for(DataCell[] column : data) {
            for(DataCell cell : column) {
                if(cell.isSelected()) {
                    cell.setRealValue(realValue);
                }
            }
        }
        xAxis.setRealValue(realValue);
        yAxis.setRealValue(realValue);
    }
    
    @Override
    public void verticalInterpolate() throws UserLevelException {
        int[] coords = { getSizeX(), getSizeY(), 0, 0};
        DataCell[][] tableData = get3dData();
        DataCell[] axisData = getYAxis().getData();
        int i, j;
        for (i = 0; i < getSizeX(); ++i) {
            for (j = 0; j < getSizeY(); ++j) {
                if (tableData[i][j].isSelected()) {
                    if (i < coords[0])
                        coords[0] = i;
                    if (i > coords[2])
                        coords[2] = i;
                    if (j < coords[1])
                        coords[1] = j;
                    if (j > coords[3])
                        coords[3] = j;
                }
            }
        }
        if (coords[3] - coords[1] > 1) {
            double x, x1, x2, y1, y2;
            x1 = axisData[coords[1]].getBinValue();
            x2 = axisData[coords[3]].getBinValue();
            for (i = coords[0]; i <= coords[2]; ++i) {
                y1 = tableData[i][coords[1]].getBinValue();
                y2 = tableData[i][coords[3]].getBinValue();
                for (j = coords[1] + 1; j < coords[3]; ++j) {
                    x = axisData[j].getBinValue();
                    tableData[i][j].setBinValue(linearInterpolation(x, x1, x2, y1, y2));
                }
            }
        }
        // Interpolate y axis in case the y axis in selected.
        getYAxis().verticalInterpolate();
    }

    @Override
    public void horizontalInterpolate() throws UserLevelException {
        int[] coords = { getSizeX(), getSizeY(), 0, 0 };
        DataCell[][] tableData = get3dData();
        DataCell[] axisData = getXAxis().getData();
        int i, j;
        for (i = 0; i < getSizeX(); ++i) {
            for (j = 0; j < getSizeY(); ++j) {
                if (tableData[i][j].isSelected()) {
                    if (i < coords[0])
                        coords[0] = i;
                    if (i > coords[2])
                        coords[2] = i;
                    if (j < coords[1])
                        coords[1] = j;
                    if (j > coords[3])
                        coords[3] = j;
                }
            }
        }
        if (coords[2] - coords[0] > 1) {
            double x, x1, x2, y1, y2;
            x1 = axisData[coords[0]].getBinValue();
            x2 = axisData[coords[2]].getBinValue();
            for (i = coords[1]; i <= coords[3]; ++i) {
                y1 = tableData[coords[0]][i].getBinValue();
                y2 = tableData[coords[2]][i].getBinValue();
                for (j = coords[0] + 1; j < coords[2]; ++j) {
                    x = axisData[j].getBinValue();
                    tableData[j][i].setBinValue(linearInterpolation(x, x1, x2, y1, y2));
                }
            }
        }
        // Interpolate x axis in case the x axis in selected.
        getXAxis().horizontalInterpolate();
    }

    @Override
    public void interpolate() throws UserLevelException {
        verticalInterpolate();
        horizontalInterpolate();
    }
    
    @Override
    public String getLogParamString() {
        StringBuilder sb = new StringBuilder();
        sb.append(xAxis.getLogParamString()+", ");
        sb.append(yAxis.getLogParamString()+", ");
        sb.append(getName()+ ":" + getLogParam());
        return sb.toString();
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

            if( (null == this.getName() && null == otherTable.getName())
                    || (this.getName().isEmpty() && otherTable.getName().isEmpty()) ) {
                ;// Skip name compare if name is null or empty.
            } else if(!this.getName().equalsIgnoreCase(otherTable.getName())) {
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
                    if(! this.data[i][j].equals(otherTable.data[i][j]) ) {
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


