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

public abstract class Table implements Serializable {
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
       
    protected int storageAddress;
    protected int storageType;
    protected boolean signed;
    protected Settings.Endian endian = Settings.Endian.BIG;
    protected boolean flip;
    
    protected DataLayout dataLayout = DataLayout.DEFAULT;	//DataCell Ordering
    protected DataCell[] data = new DataCell[1];
    
    protected boolean beforeRam = false;
    protected int ramOffset = 0;
    
    protected int userLevel = 0;
    protected boolean locked = false;
    protected String logParam = Settings.BLANK;
	private int bitMask = 0;
	
    protected double minAllowedBin = 0.0;
    protected double maxAllowedBin = 0.0;

    protected double maxBin;
    protected double minBin;

    protected double maxCompare = 0.0;
    protected double minCompare = 0.0;

    protected boolean staticDataTable = false; 
    private Table compareTable = null;
    protected Settings.DataType compareValueType = Settings.DataType.BIN;
    
    public enum DataLayout {
    	DEFAULT,
    	BOSCH_SUBTRACT
    }
    
    protected Table() {
        scales.clear();     
    };

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

    public void setData(DataCell[] data) {
        this.data = data;
    }
    
    
    public int getRamOffset() {
    	return this.ramOffset;
    }
    
    public void populateTable(byte[] input, int romRamOffset) throws ArrayIndexOutOfBoundsException, IndexOutOfBoundsException {
        // temporarily remove lock;
        boolean tempLock = locked;
        locked = false;

        if (!beforeRam) {
            this.ramOffset = romRamOffset;
        }

        for (int i = 0; i < data.length; i++) {          	
        	data[i] = new DataCell(this, i, input);    
        }

        // reset locked status
        locked = tempLock;
        calcCellRanges();
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
            if (scale.getName().equalsIgnoreCase(scaleName)) {
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
            if (scales.get(i).getName().equalsIgnoreCase(scale.getName())) {
                scales.remove(i);
                break;
            }
        }
        scales.add(scale);

        if(null == curScale) {
            this.curScale = scale;
        }

        if(SettingsManager.getSettings().getDefaultScale().equalsIgnoreCase(scale.getName())) {
            this.curScale = scale;
        }

        validateScaling();
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
        calcValueRange();
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

    @Override
    public boolean equals(Object other) {
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

            if(this.data.length != otherTable.data.length)
            {
                return false;
            }

			if (this.bitMask != otherTable.bitMask) {
				return false;
			}
			
            if(this.data.equals(otherTable.data))
            {
                return true;
            }

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

    public double getMaxAllowedBin() {
        return maxAllowedBin;
    }

    public double getMinAllowedBin() {
        return minAllowedBin;
    }

    public double getMaxAllowedReal() {
        return JEPUtil.evaluate(getCurrentScale().getExpression(), getMaxAllowedBin());
    }

    public double getMinAllowedReal() {
        return JEPUtil.evaluate(getCurrentScale().getExpression(), getMinAllowedBin());
    }

    protected void calcValueRange() {   	
        if (getStorageType() != Settings.STORAGE_TYPE_FLOAT) {
            if (isSignedData()) {
                switch (getStorageType()) {
                case 1:
                    minAllowedBin = Byte.MIN_VALUE;
                    maxAllowedBin = Byte.MAX_VALUE;
                    break;
                case 2:
                    minAllowedBin = Short.MIN_VALUE;
                    maxAllowedBin = Short.MAX_VALUE;
                    break;
                case 4:
                    minAllowedBin = Integer.MIN_VALUE;
                    maxAllowedBin = Integer.MAX_VALUE;
                    break;
                case Settings.STORAGE_TYPE_MOVI20:
                    minAllowedBin = Settings.MOVI20_MIN_VALUE;
                    maxAllowedBin = Settings.MOVI20_MAX_VALUE;
                    break;
                case Settings.STORAGE_TYPE_MOVI20S:
                    minAllowedBin = Settings.MOVI20S_MIN_VALUE;
                    maxAllowedBin = Settings.MOVI20S_MAX_VALUE;
                    break;
                }
            }
            else {
            	
            	if(bitMask == 0) {          	
	                maxAllowedBin = (Math.pow(256, getStorageType()) - 1);
            	}
            	else {
            		maxAllowedBin =(int)(Math.pow(2,ByteUtil.lengthOfMask(bitMask)) - 1);
            	}
            	
        		minAllowedBin = 0.0;
            }
        } else {
            maxAllowedBin = Float.MAX_VALUE;

            if(isSignedData()) {
                minAllowedBin = 0.0;
            } else {
                minAllowedBin = -Float.MAX_VALUE;
            }
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
        double minReal = JEPUtil.evaluate(getCurrentScale().getExpression(), getMinBin());
        double maxReal = JEPUtil.evaluate(getCurrentScale().getExpression(), getMaxBin());
        if(minReal > maxReal) {
            return minReal;
        } else {
            return maxReal;
        }
    }

    public double getMinReal() {
        double minReal = JEPUtil.evaluate(getCurrentScale().getExpression(), getMinBin());
        double maxReal = JEPUtil.evaluate(getCurrentScale().getExpression(), getMaxBin());
        if(minReal < maxReal) {
            return minReal;
        } else {
            return maxReal;
        }
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
     
    abstract public byte[] saveFile(byte[] binData);
    
    public void setValues(String name, String value) {
    	if(presetManager == null) presetManager = new PresetManager(this);   	
    	presetManager.setValues(name, value);
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
		int mask = ByteUtil.parseUnsignedInt(stringMask, 16); 		
		setBitMask(mask);
	}
	
	public void setBitMask(int mask) {
		if(mask == 0) return;
		
		//Clamp mask to max size
		bitMask = (int) Math.min(mask, Math.pow(2,getStorageType()*8)-1);
		calcValueRange();
	}

	public int getBitMask() {
		return bitMask;
	}
    
    public void validateScaling() {
        if (getType() != TableType.SWITCH) {

            // make sure a scale is present
            if (scales.isEmpty()) {
                scales.add(new Scale());
            }
            
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

    public void setScaleByName(String scaleName) throws NameNotFoundException {
        for(Scale scale : scales) {
            if(scale.getName().equalsIgnoreCase(scaleName)) {
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
    }

    public void setCompareValueType(Settings.DataType compareValueType) {
        this.compareValueType = compareValueType;
    }

    public Settings.DataType getCompareValueType() {
        return this.compareValueType;
    }

    public void colorCells() {
        calcCellRanges();
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
        X_AXIS(4),
        Y_AXIS(5),
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
}
