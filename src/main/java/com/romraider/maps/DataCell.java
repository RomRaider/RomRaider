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
import java.text.ParseException;
import java.util.LinkedList;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import com.romraider.Settings;
import com.romraider.Settings.Endian;
import com.romraider.editor.ecu.ECUEditorManager;
import com.romraider.util.ByteUtil;
import com.romraider.util.JEPUtil;
import com.romraider.util.NumberUtil;
import com.romraider.util.SettingsManager;
import com.romraider.xml.RomAttributeParser;

public class DataCell implements Serializable  {
    private static final long serialVersionUID = 1111479947434817639L;
    private static final Logger LOGGER = Logger.getLogger(DataCell.class);

    //View we need to keep up to date
    private DataCellView view = null;
    private Table table;

    //This sounds like a View property, but the manipulations
    //functions depend on this, so its better to put it here
    private boolean isSelected = false;

    private double binValue = 0.0;
    private double originalValue = 0.0;
    private double compareToValue = 0.0;
    private String liveValue = Settings.BLANK;
    private String staticText = null;
    private Rom rom;

    //Index within table
    private int index;

    public DataCell(Table table, Rom rom) {
        this.table = table;
        this.rom = rom;
    }

    public DataCell(Table table, String staticText, Rom rom) {
        this(table, rom);
        final StringTokenizer st = new StringTokenizer(staticText, DataCellView.ST_DELIMITER);
        if (st.hasMoreTokens()) {
            this.staticText = st.nextToken();
        }
    }

    public DataCell(Table table, int index, Rom rom) {
        this(table, rom);
        this.index = index;

        updateBinValueFromMemory();
        this.originalValue = this.binValue;
        registerDataCell(this);
    }

    public void setTable(Table t) {
        this.table = t;
    }

    public void setRom(Rom rom) {
        this.rom = rom;
    }

    public byte[] getBinary() {
        return rom.getBinary();
    }

    private double getValueFromMemory(int index) {
        double dataValue = 0.0;
        byte[] input = getBinary();
        int storageType = table.getStorageType();
        Endian endian = table.getEndian();
        int ramOffset = table.getRamOffset();
        int storageAddress = table.getStorageAddress();
        boolean signed = table.isSignedData();

        // populate data cells
        if (storageType == Settings.STORAGE_TYPE_FLOAT) { //float storage type
            byte[] byteValue = new byte[4];
            byteValue[0] = input[storageAddress + index * 4 - table.getRamOffset()];
            byteValue[1] = input[storageAddress + index * 4 - table.getRamOffset() + 1];
            byteValue[2] = input[storageAddress + index * 4 - table.getRamOffset() + 2];
            byteValue[3] = input[storageAddress + index * 4 - table.getRamOffset() + 3];
            dataValue = RomAttributeParser.byteToFloat(byteValue, table.getEndian(), table.getMemModelEndian());

        } else if (storageType == Settings.STORAGE_TYPE_MOVI20 ||
                storageType == Settings.STORAGE_TYPE_MOVI20S) { // when data is in MOVI20 instruction
            dataValue = RomAttributeParser.parseByteValue(input,
                    endian,
                    storageAddress + index * 3 - ramOffset,
                    storageType,
                    signed);

        } else { // integer storage type
            if(table.getBitMask() == 0) {
                dataValue = RomAttributeParser.parseByteValue(input,
                        endian, storageAddress + index * storageType - ramOffset,
                        storageType, signed);
                }
                else {
                    dataValue = RomAttributeParser.parseByteValueMasked(input, endian,
                            storageAddress + index * storageType - ramOffset,
                            storageType, signed, table.getBitMask());
                }
        }

        return dataValue;
    }

    private double getValueFromMemory() {
        if (table.getDataLayout() == Table.DataLayout.BOSCH_SUBTRACT) {

            //Bosch Motronic subtract method
             double dataValue = Math.pow(2, 8 * table.getStorageType());

            for (int j = table.data.length - 1; j >= index; j--) {
                dataValue -= getValueFromMemory(j);
            }

            return dataValue;
        }
        else {
            return getValueFromMemory(index);
        }
    }

    public void saveBinValueInFile() {
        if (table.getName().contains("Checksum Fix")) return;

        byte[] binData = getBinary();
        int userLevel = table.getUserLevel();
        int storageType = table.getStorageType();
        Endian endian = table.getEndian();
        int ramOffset = table.getRamOffset();
        int storageAddress = table.getStorageAddress();
        boolean isBoschSubtract = table.getDataLayout() == Table.DataLayout.BOSCH_SUBTRACT;

        double crossedValue = 0;

        //Do reverse cross referencing in for Bosch Subtract Axis array
        if(isBoschSubtract) {
            for (int i = table.data.length - 1; i >=index ; i--) {
                if(i == index)
                    crossedValue -= table.data[i].getBinValue();
                else if(i == table.data.length - 1)
                    crossedValue = Math.pow(2, 8 * storageType) - getValueFromMemory(i);
                else {
                    crossedValue -= getValueFromMemory(i);
                }
            }
        }

        if (userLevel <= getSettings().getUserLevel() && (userLevel < 5 || getSettings().isSaveDebugTables()) ) {
                // determine output byte values
                byte[] output;
                int mask = table.getBitMask();

                if (storageType != Settings.STORAGE_TYPE_FLOAT) {
                    int finalValue = 0;

                    // convert byte values
                    if(table.isStaticDataTable() && storageType > 0) {
                        LOGGER.warn("Static data table: " + table.toString() + ", storageType: "+storageType);

                        try {
                            finalValue = Integer.parseInt(getStaticText());
                        } catch (NumberFormatException ex) {
                            LOGGER.error("Error parsing static data table value: " + getStaticText(), ex);
                            LOGGER.error("Validate the table definition storageType and data value.");
                            return;
                        }
                    } else if(table.isStaticDataTable() && storageType < 1) {
                        // Do not save the value.
                        //if (LOGGER.isDebugEnabled())
                        //    LOGGER.debug("The static data table value will not be saved.");
                        return;
                    }  else {
                        finalValue = (int) (isBoschSubtract ? crossedValue : getBinValue());
                    }

                    if(mask != 0) {
                        // Shift left again
                        finalValue = finalValue << ByteUtil.firstOneOfMask(mask);
                    }

                    output = RomAttributeParser.parseIntegerValue(finalValue, endian, storageType);

                    int byteLength = storageType;
                    if (storageType == Settings.STORAGE_TYPE_MOVI20 ||
                            storageType == Settings.STORAGE_TYPE_MOVI20S) { // when data is in MOVI20 instruction
                        byteLength = 3;
                    }

                    //If mask enabled, only change bits within the mask
                    if(mask != 0) {
                        int tempBitMask = 0;

                        for (int z = 0; z < byteLength; z++) { // insert into file

                            tempBitMask = mask;

                            //Trim mask depending on byte, from left to right
                            tempBitMask = (tempBitMask & (0xFF << 8 * (byteLength - 1 - z))) >> 8*(byteLength - 1 - z);

                            // Delete old bits
                            binData[index * byteLength + z + storageAddress - ramOffset] &= ~tempBitMask;

                            // Overwrite
                            binData[index * byteLength + z + storageAddress - ramOffset] |= output[z];
                        }
                    }
                    //No Masking
                    else {
                        for (int z = 0; z < byteLength; z++) { // insert into file
                            binData[index * byteLength + z + storageAddress - ramOffset] = output[z];
                        }
                    }

                } else { // float
                    // convert byte values
                    output = RomAttributeParser.floatToByte((float) getBinValue(), endian, table.getMemModelEndian());

                    for (int z = 0; z < 4; z++) { // insert in to file
                        binData[index * 4 + z + storageAddress - ramOffset] = output[z];
                    }
                }
        }

        //On the Bosch substract model, we need to update all previous cells, because they depend on our value
        if(isBoschSubtract && index > 0) table.data[index-1].saveBinValueInFile();

        checkForDataUpdates();
    }

    public void registerDataCell(DataCell cell) {

        int memoryIndex = getMemoryStartAddress(cell);

        if (rom.byteCellMapping.containsKey(memoryIndex))
            {
            rom.byteCellMapping.get(memoryIndex).add(cell);
            }
        else {
            LinkedList<DataCell> l = new LinkedList<DataCell>();
            l.add(cell);
            rom.byteCellMapping.put(memoryIndex, l);
        }
    }

    public void checkForDataUpdates() {
        int memoryIndex = getMemoryStartAddress(this);

        if (rom.byteCellMapping.containsKey(memoryIndex)){
            for(DataCell c : rom.byteCellMapping.get(memoryIndex)) {
                c.updateBinValueFromMemory();
            }
        }
    }

    public static int getMemoryStartAddress(DataCell cell) {
        Table t = cell.getTable();
        return t.getStorageAddress() + cell.getIndexInTable() * t.getStorageType() - t.getRamOffset();
    }

    public Settings getSettings()
    {
        return SettingsManager.getSettings();
    }

    public void setSelected(boolean selected) {
        if(!table.isStaticDataTable() && this.isSelected != selected) {
            this.isSelected = selected;

            if(view!=null) {
                ECUEditorManager.getECUEditor().getTableToolBar().updateTableToolBar(table);
                view.drawCell();
            }
        }
    }

    public boolean isSelected() {
        return isSelected;
    }
    public void updateBinValueFromMemory() {
        this.binValue = getValueFromMemory();
        updateView();
    }

    public void setDataView(DataCellView v) {
        view = v;
    }

    public int getIndexInTable() {
        return index;
    }

    private void updateView() {
        if(view != null)
            view.drawCell();
    }

    public Table getTable() {
        return this.table;
    }

    public String getStaticText() {
        return staticText;
    }

    public String getLiveValue() {
        return this.liveValue;
    }

    public void setLiveDataTraceValue(String liveValue) {
        if(this.liveValue != liveValue) {
            this.liveValue = liveValue;
            updateView();
        }
    }

    public double getBinValue() {
        return binValue;
    }

    public double getOriginalValue() {
        return originalValue;
    }

    public double getCompareToValue() {
        return compareToValue;
    }

    public double getRealValue() {
        if(table.getCurrentScale() == null) return binValue;

        return JEPUtil.evaluate(table.getCurrentScale().getExpression(), binValue);
    }

    public void setRealValue(String input) throws UserLevelException {
        // create parser
        input = input.replaceAll(DataCellView.REPLACE_TEXT, Settings.BLANK);
        try {
            double result = 0.0;
            if (!"x".equalsIgnoreCase(input)) {

                if(table.getCurrentScale().getByteExpression() == null) {
                	result = table.getCurrentScale().approximateToByteFunction(NumberUtil.doubleValue(input), table.getStorageType(), table.isSignedData());
                }
                else {
                	result = JEPUtil.evaluate(table.getCurrentScale().getByteExpression(), NumberUtil.doubleValue(input));
                }

                if (table.getStorageType() != Settings.STORAGE_TYPE_FLOAT) {
                    result = (int) Math.round(result);
                }

                if(binValue != result) {
                    this.setBinValue(result);
                }
            }
        } catch (ParseException e) {
            // Do nothing.  input is null or not a valid number.
        }
    }

    public double getCompareValue() {
        return binValue - compareToValue;
    }

    public double getRealCompareValue() {
        return JEPUtil.evaluate(table.getCurrentScale().getExpression(), binValue) - JEPUtil.evaluate(table.getCurrentScale().getExpression(), compareToValue);
    }

    public double getRealCompareChangeValue() {
        double realBinValue = JEPUtil.evaluate(table.getCurrentScale().getExpression(), binValue);
        double realCompareValue = JEPUtil.evaluate(table.getCurrentScale().getExpression(), compareToValue);

        if(realCompareValue != 0.0) {
            // Compare change formula ((V2 - V1) / |V1|).
            return ((realBinValue - realCompareValue) / Math.abs(realCompareValue));
        } else {
            // Use this to avoid divide by 0 or infinite increase.
            return realBinValue - realCompareValue;
        }
    }

    public void setBinValue(double newBinValue) throws UserLevelException {
        if(binValue == newBinValue || table.locked || table.getName().contains("Checksum Fix")) {
            return;
        }

        if (table.userLevel > getSettings().getUserLevel())
            throw new UserLevelException(table.userLevel);

        double checkedValue = newBinValue;

        // make sure it's in range
        if(checkedValue < table.getMinAllowedBin()) {
            checkedValue = table.getMinAllowedBin();
        }

        if(checkedValue > table.getMaxAllowedBin()) {
            checkedValue = table.getMaxAllowedBin();
        }

        if(binValue == checkedValue) {
            return;
        }

        // set bin.
        binValue = checkedValue;
        saveBinValueInFile();
        updateView();
    }

    public void increment(double increment) throws UserLevelException {
        double oldValue = getRealValue();

        if (table.getCurrentScale().getCoarseIncrement() < 0.0) {
            increment = 0.0 - increment;
        }

        double incResult = 0;
        if(table.getCurrentScale().getByteExpression() == null) {
        	incResult = table.getCurrentScale().approximateToByteFunction(oldValue + increment, table.getStorageType(), table.isSignedData());
        }
        else {
        	incResult = JEPUtil.evaluate(table.getCurrentScale().getByteExpression(), (oldValue + increment));
        }

        if (table.getStorageType() == Settings.STORAGE_TYPE_FLOAT) {
            if(binValue != incResult) {
                this.setBinValue(incResult);
            }
        } else {
            int roundResult = (int) Math.round(incResult);
            if(binValue != roundResult) {
                this.setBinValue(roundResult);
            }
        }

        // make sure table is incremented if change isn't great enough
        int maxValue = (int) Math.pow(8, table.getStorageType());

        if (table.getStorageType() != Settings.STORAGE_TYPE_FLOAT &&
                oldValue == getRealValue() &&
                binValue > 0.0 &&
                binValue < maxValue) {
            if (LOGGER.isDebugEnabled())
                LOGGER.debug(maxValue + " " + binValue);
            increment(increment * 2);
        }
    }

    public void undo() throws UserLevelException {
        this.setBinValue(originalValue);
    }

    public void setRevertPoint() {
        this.setOriginalValue(binValue);
        updateView();
    }

    public void setOriginalValue(double originalValue) {
        this.originalValue = originalValue;
    }

    public void setCompareValue(DataCell compareCell) {
        if(Settings.DataType.BIN == table.getCompareValueType())
        {
            if(this.compareToValue == compareCell.binValue) {
                return;
            }

            this.compareToValue = compareCell.binValue;
        } else {
            if(this.compareToValue == compareCell.originalValue) {
                return;
            }

            this.compareToValue = compareCell.originalValue;
        }
    }

    public void multiply(double factor) throws UserLevelException {
        if(table.getCurrentScale().getCategory().equals("Raw Value"))
            setBinValue(binValue * factor);
        else {
            String newValue = (getRealValue() * factor) + "";

            //We need to convert from dot to comma, in the case of EU Format.
            // This is because getRealValue to String has dot notation.
            if(NumberUtil.getSeperator() == ',') newValue = newValue.replace('.', ',');

            setRealValue(newValue);
        }
    }

    @Override
    public boolean equals(Object other) {
        if(other == null) {
            return false;
        }

        if(!(other instanceof DataCell)) {
            return false;
        }

        DataCell otherCell = (DataCell) other;

        if(this.table.isStaticDataTable() != otherCell.table.isStaticDataTable()) {
            return false;
        }

        return binValue == otherCell.binValue;
    }
}
