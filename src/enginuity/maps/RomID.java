/*
 *
 * Enginuity Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006 Enginuity.org
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
 *
 */

//ECU version definition

package enginuity.maps;

import java.io.Serializable;

public class RomID implements Serializable {

    private String xmlid = "";//ID stored in XML
    private int internalIdAddress = 0;//address of ECU version in image
    private String internalIdString = "";//ID stored in image
    private String caseId = "";//ECU hardware version
    private String ecuId = "";
    private String make = "";//manufacturer
    private String market = "";
    private String model = "";
    private String subModel = "";//trim, ie WRX
    private String transmission = "";
    private String year = "Unknown";
    private String flashMethod = "";//flash method string used for ecuflash
    private String memModel = "";//model used for reflashing with ecuflash
    private int fileSize = 0;
    private int ramOffset = 0;
    private boolean obsolete = false; // whether a more recent revision exists

    public String toString() {
        return "\n   ---- RomID " + xmlid + " ----" +
                "\n   Internal ID Address: " + internalIdAddress +
                "\n   Internal ID String: " + internalIdString +
                "\n   Case ID: " + caseId +
                "\n   ECU ID: " + ecuId +
                "\n   Make: " + make +
                "\n   Market: " + market +
                "\n   Model: " + model +
                "\n   Submodel: " + subModel +
                "\n   Transmission: " + transmission +
                "\n   Year: " + year +
                "\n   Flash Method: " + flashMethod +
                "\n   Memory Model: " + memModel +
                "\n   ---- End RomID " + xmlid + " ----";
    }

    public RomID() {
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

    public int getRamOffset() {
        return ramOffset;
    }

    public void setRamOffset(int ramOffset) {
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
}