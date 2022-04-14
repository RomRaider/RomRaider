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

//ECU version definition

package com.romraider.maps;

import static com.romraider.util.HexUtil.asBytes;

import java.io.Serializable;
import java.util.Arrays;

import org.apache.log4j.Logger;

public class RomID implements Serializable {
    private static final Logger LOGGER = Logger.getLogger(RomID.class);
    private static final long serialVersionUID = 7272741253665400643L;
	private String version;					//Version e.g. V0.45
	private String author;					//Author name
    private String xmlid;               	//ID stored in XML
    private int    internalIdAddress;   	//address of ECU version in image
    private String internalIdString = "";    //ID stored in image
    private String caseId;              	//ECU hardware version
    private String ecuId = "";
    private String make;                	//manufacturer
    private String market;
    private String model;
    private String subModel;            	//trim, ie WRX
    private String transmission;
    private String year;
    private String flashMethod;         	//flash method string used for ecuflash
    private String memModel;            	//model used for reflashing with ecuflash
    private String editStamp;           	//YYYY-MM-DD and v, the save count for this ROM
    private int fileSize;
    private int ramOffset;

    private boolean noRamOffset;
    private boolean obsolete;           	// whether a more recent revision exists
    private String checksum;            	// checksum method used to validate ROM contents


    public boolean checkMatch(byte[] file) {
        try {
        	if(internalIdString == null || internalIdString.length() == 0) return false;

        	//If both fields are set to force, use this definition no matter what
        	if(internalIdAddress == -1 && internalIdString.equalsIgnoreCase("force")) return true;

            // romid is hex string
            if (internalIdString.length() > 2
                    && internalIdString.substring(0, 2).equalsIgnoreCase("0x")) {

                // put romid in to byte array to check for match without "0x"
                byte[] romIDBytes = asBytes(internalIdString.substring(2));

                //If file is smaller than the address we are looking for, it can't be it
                if(file.length < getInternalIdAddress() + romIDBytes.length) return false;

                //Extract bytes at specified location in ROM
                byte[] romBytes = Arrays.copyOfRange(file,
                		getInternalIdAddress(), getInternalIdAddress() + romIDBytes.length);

                //Check if bytes match
                return Arrays.equals(romIDBytes, romBytes);
            }
            else {
            	if(file.length < getInternalIdAddress() + getInternalIdString().length()) return false;

                String ecuID = new String(file, getInternalIdAddress(),
                        getInternalIdString().length());
                return ecuID.equalsIgnoreCase(getInternalIdString());
            }

        } catch (Exception ex) {
            // if any exception is encountered, names do not match or code is buggy :)
            LOGGER.warn("Error finding match", ex);
            return false;
       }
    }

    @Override
    public String toString() {
        return String.format(
                "%n   ---- RomID %s ----" +
                "%n   Version: %s" +
                "%n   Internal ID Address: %s" +
                "%n   Internal ID String: %s" +
                "%n   Case ID: %s" +
                "%n   ECU ID: %s" +
                "%n   Make: %s" +
                "%n   Market: %s" +
                "%n   Model: %s" +
                "%n   Submodel: %s" +
                "%n   Transmission: %s" +
                "%n   Year: %s" +
                "%n   Flash Method: %s" +
                "%n   Memory Model: %s" +
                "%n   ---- End RomID %s ----",
                xmlid,
                version,
                internalIdAddress,
                internalIdString,
                caseId,
                ecuId,
                make,
                market,
                model,
                subModel,
                transmission,
                year,
                flashMethod,
                memModel,
                xmlid);
    }

    public String getXmlid() {
        return xmlid;
    }

    public void setXmlid(String xmlid) {
        this.xmlid = xmlid;
    }

    public int getInternalIdAddress() {
        return internalIdAddress;
    }

    public void setInternalIdAddress(int internalIdAddress) {
        this.internalIdAddress = internalIdAddress;
    }

    public String getInternalIdString() {
        return internalIdString;
    }

    public void setInternalIdString(String internalIdString) {
        this.internalIdString = internalIdString;
    }

    public String getCaseId() {
        return caseId;
    }

    public void setCaseId(String caseId) {
        this.caseId = caseId;
    }

    public String getEcuId() {
        return ecuId;
    }

    public void setEcuId(String ecuId) {
        this.ecuId = ecuId;
    }

    public String getMake() {
        return make;
    }

    public void setMake(String make) {
        this.make = make;
    }

    public String getMarket() {
        return market;
    }

    public void setMarket(String market) {
        this.market = market;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getSubModel() {
        return subModel;
    }

    public void setSubModel(String subModel) {
        this.subModel = subModel;
    }

    public String getTransmission() {
        return transmission;
    }

    public void setTransmission(String transmission) {
        this.transmission = transmission;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getFlashMethod() {
        return flashMethod;
    }

    public void setFlashMethod(String flashMethod) {
        this.flashMethod = flashMethod;
    }

    public String getMemModel() {
        return memModel;
    }

    public void setMemModel(String memModel) {
        this.memModel = memModel;
    }

    public void setOffset(int offset) {
    	this.ramOffset = -offset;
    	noRamOffset=true;
    }

    public void disableRamOffset() {
        noRamOffset = true;
        ramOffset = 0;
    }

    public int getRamOffset() {
        return ramOffset;
    }

    public void setRamOffset(int ramOffset) {
    	if(noRamOffset) return;

        this.ramOffset = ramOffset;
    }

    public int getFileSize() {
        return fileSize;
    }

    public void setFileSize(int fileSize) {
        this.fileSize = fileSize;
    }

    public boolean isObsolete() {
        return obsolete;
    }

    public void setObsolete(boolean obsolete) {
        this.obsolete = obsolete;
    }

    public String getEditStamp() {
        return editStamp;
    }

    public void setEditStamp(String editStamp) {
        this.editStamp = editStamp;
    }

    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }

    public String getChecksum() {
        return checksum;
    }

	public void setVersion(String version) {
		this.version = version;	
	}
	
	public String getVersion() {
		return version;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}
}