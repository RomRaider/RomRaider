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
import com.romraider.util.SettingsManager;

public class Table2D extends Table {
    private static final long serialVersionUID = -7684570967109324784L;
    private Table1D axis = new Table1D();

    @Override
    public TableType getType() {
        return TableType.TABLE_2D;
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
    public void clearData() {
        super.clearData();
        axis.clearData();
        axis=null;
    }

    @Override
    public void populateCompareValues(Table otherTable) {
        if(null == otherTable || !(otherTable instanceof Table2D)) {
            return;
        }

        Table2D compareTable2D = (Table2D) otherTable;
        if(data.length != compareTable2D.data.length ||
                axis.data.length != compareTable2D.axis.data.length) {
            return;
        }

        super.populateCompareValues(otherTable);
        axis.populateCompareValues(compareTable2D.getAxis());
    }

    @Override
    public void refreshCompare() {
        populateCompareValues(getCompareTable());
        axis.refreshCompare();
    }

    @Override
    public void populateTable(Rom rom) throws ArrayIndexOutOfBoundsException, IndexOutOfBoundsException {
            axis.populateTable(rom);
            super.populateTable(rom);
    }

    @Override
    public void setRevertPoint() {
        super.setRevertPoint();
        axis.setRevertPoint();
    }

    @Override
    public void undoAll() throws UserLevelException {
        super.undoAll();
        axis.undoAll();
    }

    @Override
    public String getLogParamString() {
        StringBuilder sb = new StringBuilder();
        sb.append(axis.getLogParamString()+ ", ");
        sb.append(getName()+ ":" + getLogParam());
        return sb.toString();
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
    public void setCompareValueType(Settings.DataType compareValueType) {
        super.setCompareValueType(compareValueType);
        axis.setCompareValueType(compareValueType);
    }

    @Override
    public void setCurrentScale(Scale curScale) {
        if(SettingsManager.getSettings().isScaleHeadersAndData() && !axis.isStaticDataTable()) {
            try {
                this.axis.setScaleByCategory(curScale.getCategory());
            } catch (NameNotFoundException e) {
                try {
                    this.axis.setScaleByCategory(SettingsManager.getSettings().getDefaultScale());
                } catch (NameNotFoundException e1) {
                    try {
                        this.axis.setScaleByCategory("Default");
                    } catch (NameNotFoundException e2) {
                        e2.printStackTrace();
                    }
                }
            }
        }
        this.curScale = curScale;

        if(tableView != null) tableView.drawTable();
    }

    @Override
    public void clearSelection() {
        if(axis!=null)
            axis.clearSelection();

        super.clearSelection();
    }

    @Override
    public void setRealValue(String realValue) throws UserLevelException {
        super.setRealValue(realValue);
        axis.setRealValue(realValue);
    }

    @Override
    public void increment(double increment) throws UserLevelException {
        super.increment(increment);
        axis.increment(increment);
    }

    @Override
    public void multiply(double factor) throws UserLevelException{
        super.multiply(factor);
        axis.multiply(factor);
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
        int[] coords = { getDataSize(), 0};
        DataCell[] tableData = getData();
        DataCell[] axisData = getAxis().getData();

        for (int i = 0; i < getDataSize(); ++i) {
            if (tableData[i].isSelected()) {
                if (i < coords[0])
                    coords[0] = i;
                if (i > coords[1])
                    coords[1] = i;
            }
        }
        if (coords[1] - coords[0] > 1) {
            double x, x1, x2, y1, y2;
            x1 = axisData[coords[0]].getBinValue();
            y1 = tableData[coords[0]].getBinValue();
            x2 = axisData[coords[1]].getBinValue();
            y2 = tableData[coords[1]].getBinValue();
            for (int i = coords[0] + 1; i < coords[1]; ++i) {
                x = axisData[i].getBinValue();
                data[i].setBinValue(linearInterpolation(x, x1, x2, y1, y2));
            }
        }
        // Interpolate x axis in case the x axis in selected.
        this.getAxis().horizontalInterpolate();
    }
    
    @Override
	public double queryTable(Double input_x, Double input_y) {
		double input = input_x == null ? input_y : input_x;
		DataCell[] tableData = getData();
		DataCell[] axisData = getAxis().getData();

		int start = 0;
		int end = tableData.length - 1;
		boolean foundEnd = false;

		for (int i = 0; i < tableData.length && i < axisData.length; i++) {
			DataCell c = axisData[i];
			if (c.getRealValue() <= input) {
				start = i;
			}
			if (c.getRealValue() >= input && !foundEnd) {
				end = i;
				foundEnd = true;
			}
		}
		
		return linearInterpolation(input, axisData[start].getRealValue(), axisData[end].getRealValue(),
				tableData[start].getRealValue(), tableData[end].getRealValue());
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

            if( (null == this.getName() && null == otherTable.getName())
                    || (this.getName().isEmpty() && otherTable.getName().isEmpty()) ) {
                ;// Skip name compare if name is null or empty.
            } else if (!this.getName().equalsIgnoreCase(otherTable.getName())) {
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
}
