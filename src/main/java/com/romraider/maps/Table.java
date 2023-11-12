/*
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2022 RomRaider.com
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

import java.io.Serializable;
import java.util.Vector;

import javax.naming.NameNotFoundException;

import org.apache.log4j.Logger;

import com.romraider.Settings;
import com.romraider.swing.TableFrame;
import com.romraider.util.ByteUtil;
import com.romraider.util.JEPUtil;
import com.romraider.util.NumberUtil;
import com.romraider.util.SettingsManager;

public abstract class Table implements Serializable, Comparable<Table> {
    private static final long serialVersionUID = 6559256489995552645L;
    protected static final Logger LOGGER = Logger.getLogger(Table.class);
    protected static final String ST_DELIMITER = "\t\n\r\f";
    protected static Settings.Endian memModelEndian;

    protected TableView tableView;
    protected TableFrame tableFrame;

    protected String name;
    protected String category = "Other";
    protected String description = Settings.BLANK;
    protected Vector<Scale> scales = new Vector<Scale>();
    protected Scale curScale;
    protected PresetManager presetManager;

    protected int tableBitMask;
    protected int storageAddress;
    protected int storageType;
    protected boolean signed;
    protected Settings.Endian endian = Settings.Endian.BIG;
    protected boolean flip;

    protected DataLayout dataLayout = DataLayout.DEFAULT;   //DataCell Ordering
    protected DataCell[] data = new DataCell[1];

    protected boolean beforeRam = false;
    protected int ramOffset = 0;

    protected int userLevel = 0;
    protected boolean locked = false;
    protected String logParam = Settings.BLANK;

    protected double maxBin;
    protected double minBin;

    protected double maxCompare = 0.0;
    protected double minCompare = 0.0;

    protected Rom rom;
    protected boolean staticDataTable = false;
    private Table compareTable = null;
    protected Settings.DataType compareValueType = Settings.DataType.BIN;

    public enum DataLayout {
        DEFAULT,
        BOSCH_SUBTRACT
    }

    public void setTableView(TableView v) {
        this.tableView = v;
    }

    public TableView getTableView() {
        return this.tableView;
    }

    public void setTableFrame(TableFrame v) {
        this.tableFrame = v;
    }

    public TableFrame getTableFrame() {
        return this.tableFrame;
    }

    public DataCell[] getData() {
        return data;
    }

    public void addStaticDataCell(String s) {
        setStaticDataTable(true);
        DataCell c = new DataCell(this, s, null);

        for(int i = 0; i < data.length; i++) {
            if(data[i] == null) {
                data[i] = c;
                break;
            }
        }
    }

    //Cleans up all references to avoid data leaks
    public void clearData() {
        if(data != null) {
            for(int i=0;i<getDataSize();i++) {
                if(data[i]!=null) {
                    data[i].setTable(null);
                    data[i].setRom(null);
                    data[i] = null;
                }
            }

            data = null;
        }
        rom = null;
    }

    public void setData(DataCell[] data) {
        this.data = data;
    }

    public int getRamOffset() {
        return this.ramOffset;
    }

    public Rom getRom() {
    	return rom;
    }

    public void setRom(Rom rom) {
    	this.rom = rom;
    }

    public void populateTable(Rom rom) throws ArrayIndexOutOfBoundsException, IndexOutOfBoundsException {
    	if(isStaticDataTable()) return;
        validateScaling();

        // temporarily remove lock;
        boolean tempLock = locked;
        locked = false;

        if (!beforeRam) {
            this.ramOffset = rom.getRomID().getRamOffset();
        }

        for (int i = 0; i < data.length; i++) {
            data[i] = new DataCell(this, i, rom);
        }

        // reset locked status
        locked = tempLock;
        calcCellRanges();

        //Add Raw Scale
        addScale(new Scale());
    }

    public abstract TableType getType();

    public DataCell getDataCell(int location) {
        return data[location];
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        category = category.trim().replace(" //", "//").replace("// ", "//");
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


    //Gets called by toolbar
    public void updateIncrementDecrementValues(double fineInc, double courseInc) {
        this.curScale.setCoarseIncrement(courseInc);
        this.curScale.setFineIncrement(fineInc);
    }

    public Scale getCurrentScale() {
        return this.curScale;
    }

    public Scale getScale(String scaleName) throws NameNotFoundException {
        for (Scale scale : scales) {
            if (scale.getCategory().equalsIgnoreCase(scaleName)) {
                return scale;
            }
        }
        return new Scale();
    }

    public Vector<Scale> getScales() {
        return scales;
    }

    public void addScale(Scale scale) {
        // look for scale, replace or add new
        for (int i = 0; i < scales.size(); i++) {
            if (scales.get(i).getCategory().equalsIgnoreCase(scale.getCategory())) {
                scales.remove(i);
                break;
            }
        }
        scales.add(scale);

        if(null == curScale) {
            this.curScale = scale;
        }

        if(SettingsManager.getSettings().getDefaultScale().equalsIgnoreCase(scale.getCategory())) {
            this.curScale = scale;
        }
        else if("Default".equalsIgnoreCase(scale.getCategory())) {
            this.curScale = scale;
        }
    }

    public int getStorageAddress() {
        return storageAddress;
    }

    public void setStorageAddress(int storageAddress) {
        this.storageAddress = storageAddress;
    }

    public int getStorageType() {
        return storageType;
    }

    public void setStorageType(int storageType) {
        this.storageType = storageType;
    }

    public boolean isSignedData() {
        return signed;
    }

    public void setSignedData(boolean signed) {
        this.signed = signed;
    }

    public Settings.Endian getEndian() {
        return endian;
    }

    public void setEndian(Settings.Endian endian) {
        this.endian = endian;
    }

    public void setDataSize(int size) {
        data = new DataCell[size];
    }

    public int getDataSize() {
        return data.length;
    }

    public boolean getFlip() {
        return flip;
    }

    public void setFlip(boolean flip) {
        this.flip = flip;
    }

    public void setLogParam(String logParam) {
        this.logParam = logParam;
    }

    public String getLogParam() {
        return logParam;
    }

    public String getLogParamString() {
        return getName()+ ":" + getLogParam();
    }

    @Override
    public String toString() {
        if(null == name || name.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            sb.append(Settings.DEFAULT_TABLE_NAME);

            if(0 != this.getStorageAddress()) {
                sb.append(" ("+this.getStorageAddress() + ")");
            }

            if(null != this.getLogParam() && !this.getLogParam().isEmpty()) {
                sb.append(" - " + this.getLogParam());
            }

            return sb.toString();
        }
        return name;
    }

    public void setName(String n) {
        this.name = n;
    }

    public String getName() {
        return name;
    }

    public StringBuffer getTableAsString() {
        StringBuffer output = new StringBuffer(Settings.BLANK);
        for (int i = 0; i < data.length; i++) {

            if(data[i]!= null)
                output.append(NumberUtil.stringValue(data[i].getRealValue()));

            if (i < data.length - 1) {
                output.append(Settings.TAB);
            }
        }
        return output;
    }

    //Faster version of equals where data doesnt matter (yet)
    public boolean equalsWithoutData(Object other) {
        try {
            if(null == other) {
                return false;
            }

            if(other == this) {
                return true;
            }

            if(!(other instanceof Table)) {
                return false;
            }

            Table otherTable = (Table)other;

            if(storageAddress != otherTable.storageAddress) {
                return false;
            }

            if(!this.name.equals(otherTable.name)) {
                return false;
            }

            if(this.data.length != otherTable.data.length)
            {
                return false;
            }

            return true;
        } catch(Exception ex) {
            // TODO: Log Exception.
            return false;
        }
    }

    @Override
    public boolean equals(Object other) {
        try {
            boolean withoutData = equalsWithoutData(other);
            if(!withoutData) return false;

            Table otherTable = (Table)other;

            // Compare Bin Values
            for(int i=0 ; i < this.data.length ; i++) {
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

    public void calcCellRanges() {
        if(data.length > 0) {
            double binMax = data[0].getBinValue();
            double binMin = data[0].getBinValue();

            double compareMax = data[0].getCompareValue();
            double compareMin = data[0].getCompareValue();

            for(DataCell cell : data) {

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
            setMaxBin(binMax);
            setMinBin(binMin);
            setMaxCompare(compareMax);
            setMinCompare(compareMin);
        }
    }

    public double getMaxBin() {
        return this.maxBin;
    }

    public double getMinBin() {
        return this.minBin;
    }

    public double getMaxReal() {
    	return JEPUtil.evaluate(getCurrentScale().getExpression(), getMaxBin());
    }

    public double getMinReal() {
    	return JEPUtil.evaluate(getCurrentScale().getExpression(), getMinBin());
    }

    public void setMaxBin(double maxBin) {
        this.maxBin = maxBin;
    }

    public void setMinBin(double minBin) {
        this.minBin = minBin;
    }

    public double getMaxCompare() {
        return this.maxCompare;
    }

    public void setMaxCompare(double maxCompare) {
        this.maxCompare = maxCompare;
    }

    public double getMinCompare() {
        return this.minCompare;
    }

    public void setMinCompare(double minCompare) {
        this.minCompare = minCompare;
    }

    public void setRevertPoint() {
        for (DataCell cell : data) {
            cell.setRevertPoint();
        }
    }

    public void undoAll() throws UserLevelException {
        for (DataCell cell : data) {
            cell.undo();
        }
    }

    //Don't check for duplicate names, just add
    public void setPresetValues(String name, String value) {
    	if(presetManager == null) presetManager = new PresetManager(this);
    	presetManager.setPresetValues(name, value, 0, false);
    }

    //Don't check for duplicate names, just add
    public void setPresetValues(String name, String value, int dataCellOffset) {
    	if(presetManager == null) presetManager = new PresetManager(this);
    	presetManager.setPresetValues(name, value, dataCellOffset, true);
    }

    //Check for duplicate names, then replace if exist or add otherwise
    public void addPresetValue(String name, String value) {
    	if(presetManager == null) presetManager = new PresetManager(this);
    	presetManager.addPresetValue(name, value, 0, false);
    }

    //Check for duplicate names, then replace if exist or add otherwise
    public void addPresetValue(String name, String value, int dataCellOffset) {
    	if(presetManager == null) presetManager = new PresetManager(this);
    	presetManager.addPresetValue(name, value, dataCellOffset, true);
    }

    public boolean isBeforeRam() {
        return beforeRam;
    }

    public void setBeforeRam(boolean beforeRam) {
        this.beforeRam = beforeRam;
    }

    public void setDataLayout(String s) {
        if(s.trim().equalsIgnoreCase("bosch_subtract")) {
            setDataLayout(DataLayout.BOSCH_SUBTRACT);
        }
        else {
            setDataLayout(DataLayout.DEFAULT);
        }
    }

    public void setDataLayout(DataLayout m) {
        this.dataLayout = m;
    }

    public DataLayout getDataLayout() {
        return this.dataLayout;
    }

    public void setStringMask(String stringMask) {
    	if(!stringMask.isEmpty()) {
	        int mask = ByteUtil.parseUnsignedInt(stringMask, 16);
	        setBitMask(mask);
        }
    }

    public void setBitMask(int mask) {
    	//We dont update the DataCells here!
    	//Clamp to max size
    	tableBitMask = (int) Math.min(mask, Math.pow(2,getStorageType()*8)-1);
    }

    public int getBitMask() {
    	return tableBitMask;
    }

    public void validateScaling() {
        if (getType() != TableType.SWITCH) {
            for(Scale scale : scales) {
                if (!scale.validate()) {
                    TableView.showBadScalePopup(this, scale);
                }
            }
        }
    }

    public void populateCompareValues(Table otherTable) {
        if(null == otherTable) {
            return;
        }

        DataCell[] compareData = otherTable.getData();
        if(data.length != compareData.length) {
            return;
        }


        int i = 0;
        for(DataCell cell : data) {
            cell.setCompareValue(compareData[i]);
            i++;
        }

        calcCellRanges();
        if(tableView != null) tableView.drawTable();
    }

    public void clearSelection() {
        if(data!=null) {
            for (DataCell cell : data) {
                cell.setSelected(false);
            }
        }
    }

    public void selectCellAt(int y) {
        if(y >= 0 && y < data.length) {
            clearSelection();
            data[y].setSelected(true);
            if(tableView!=null) tableView.highlightBeginY = y;
        }
    }

    public void selectCellAtWithoutClear(int y) {
        if(y >= 0 && y < data.length) {
            data[y].setSelected(true);
            if(tableView!=null) tableView.highlightBeginY = y;
        }
    }

    public double linearInterpolation(double x, double x1, double x2, double y1, double y2) {
        return (x1 == x2) ? y1 : (y1 + (x - x1) * (y2 - y1) / (x2 - x1));
    }
    
    public void verticalInterpolate() throws UserLevelException{
        horizontalInterpolate();
    }

    public void horizontalInterpolate() throws UserLevelException {
        int[] coords = { getDataSize(), 0};
        DataCell[] tableData = getData();

        for (int i = 0; i < getDataSize(); ++i) {
            if (tableData[i].isSelected()) {
                if (i < coords[0])
                    coords[0] = i;
                if (i > coords[1])
                    coords[1] = i;
            }
        }

        if (coords[1] - coords[0] > 1) {
            double y1, y2;
            y1 = tableData[coords[0]].getBinValue();
            y2 = tableData[coords[1]].getBinValue();
            for (int i = coords[0] + 1; i < coords[1]; ++i) {
                float p = (float)((i - coords[0]))/(coords[1] - coords[0]);
                data[i].setBinValue((y2*p)+(y1 *(1-p)));
            }
        }
    }
    
    public abstract double queryTable(Double input_x, Double input_y);

    public void interpolate() throws UserLevelException {
        horizontalInterpolate();
    }

    public void increment(double increment) throws UserLevelException {
        for (DataCell cell : data) {
            if (cell.isSelected()) {
                cell.increment(increment);
            }
        }
    }

    public void multiply(double factor) throws UserLevelException{
        for (DataCell cell : data) {
            if (cell.isSelected()) {
                cell.multiply(factor);
            }
         }
    }

    public void setRealValue(String realValue) throws UserLevelException {
        for(DataCell cell : data) {
            if (cell.isSelected()) {
                cell.setRealValue(realValue);
            }
        }
    }

    public abstract boolean isLiveDataSupported();
   
    
    public int getUserLevel() {
        return userLevel;
    }

    public void setUserLevel(int userLevel) {
        this.userLevel = userLevel;
        if (userLevel > 5) {
            userLevel = 5;
        } else if (userLevel < 1) {
            userLevel = 1;
        }
    }

    public void setScaleByCategory(String scaleName) throws NameNotFoundException {
        for(Scale scale : scales) {
            if(scale.getCategory().equalsIgnoreCase(scaleName)) {
                Scale currentScale = getCurrentScale();
                if(currentScale == null || !currentScale.equals(scale)) {
                    this.setCurrentScale(scale);
                }
                return;
            }
        }

        throw new NameNotFoundException();
    }

    public void setCurrentScale(Scale curScale) {
        this.curScale = curScale;

        if(tableView!=null) {
            tableView.drawTable();
        }
    }

    public Settings getSettings()
    {
        return SettingsManager.getSettings();
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public abstract boolean isButtonSelected();

    public Table getCompareTable() {
        return compareTable;
    }

    public void setCompareTable(Table compareTable) {
        this.compareTable = compareTable;

        if(tableView!= null) tableView.drawTable();
    }

    public void setCompareValueType(Settings.DataType compareValueType) {
        this.compareValueType = compareValueType;

        if(tableView!= null) tableView.drawTable();
    }

    public Settings.DataType getCompareValueType() {
        return this.compareValueType;
    }

    public void colorCells() {
        calcCellRanges();

        if(tableView!=null) tableView.drawTable();
    }

    public void refreshCompare() {
        populateCompareValues(getCompareTable());
    }

    public boolean isStaticDataTable() {
        return staticDataTable;
    }

    public void setStaticDataTable(boolean staticDataTable) {
        this.staticDataTable = staticDataTable;
    }

    public void setMemModelEndian(Settings.Endian endian) {
        memModelEndian = endian;
    }

    public Settings.Endian getMemModelEndian() {
        return memModelEndian;
    }

    public enum TableType {
        TABLE_1D(1),
        TABLE_2D(2),
        TABLE_3D(3),
       // X_AXIS(4),
       // Y_AXIS(5),
        SWITCH(6);

        private final int marshallingCode;

        TableType(int marshallingCode) {
            this.marshallingCode = marshallingCode;
        }

        public int getDimension() {
            switch (this) {
                case TABLE_1D:
                    return 1;
                case TABLE_2D:
                    return 2;
                case TABLE_3D:
                    return 3;
                default:
                    return -1;
            }
        }

        public String getMarshallingString() {
            return String.valueOf(marshallingCode);
        }
    }

    @Override
    public int compareTo(Table otherTable) {
        return this.getName().compareTo(otherTable.getName());
    }
}
