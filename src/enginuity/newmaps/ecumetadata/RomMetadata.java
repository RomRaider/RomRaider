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

package enginuity.newmaps.ecumetadata;

import enginuity.util.Nameable;
import enginuity.util.NamedSet;

import java.io.Serializable;

public class RomMetadata implements Nameable, Serializable {

    private String name;
    private int idAddress;
    private String idString;    
    private String description;    
    private String memmodel;
    private String flashmethod;
    private String caseid;
    private boolean obsolete;
    private boolean isAbstract;
    
    private NamedSet<Scale> scales;
    private NamedSet<TableMetadata> tables;
    private Category categories;
    
    private RomMetadata() { }
    
    public RomMetadata(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getIdAddress() {
        return idAddress;
    }

    public void setIdAddress(int idAddress) {
        this.idAddress = idAddress;
    }

    public String getIdString() {
        return idString;
    }

    public void setIdString(String idString) {
        this.idString = idString;
    }

    public String getMemmodel() {
        return memmodel;
    }

    public void setMemmodel(String memmodel) {
        this.memmodel = memmodel;
    }

    public String getFlashmethod() {
        return flashmethod;
    }

    public void setFlashmethod(String flashmethod) {
        this.flashmethod = flashmethod;
    }

    public String getCaseid() {
        return caseid;
    }

    public void setCaseid(String caseid) {
        this.caseid = caseid;
    }

    public boolean isObsolete() {
        return obsolete;
    }

    public void setObsolete(boolean obsolete) {
        this.obsolete = obsolete;
    }      

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isAbstract() {
        return isAbstract;
    }

    public void setAbstract(boolean isAbstract) {
        this.isAbstract = isAbstract;
    }
    
    public String toString() {
        String output = " --- ROM: " + name + " ---" +
                        "\n - ID Address: " + idAddress +
                        "\n - ID String: " + idString +
                        "\n - Descrption: " + description +
                        "\n - Memmodel: " + memmodel +
                        "\n - Flash Method: " + flashmethod +
                        "\n - CaseID: " + caseid +
                        "\n - Obsolete: " + obsolete +
                        "\n - Abstract: " + isAbstract +
                        "\n   --- SCALES ---\n" + scales +
                        //"\n   --- TABLES ---\n" + tables +
                        "\n   --- CATEGORIES ---\n" + categories;
                
        return output;
    }

    public NamedSet<Scale> getScales() {
        return scales;
    }

    public void setScales(NamedSet<Scale> scales) {
        this.scales = scales;
    }

    public NamedSet<TableMetadata> getTables() {
        return tables;
    }

    public void setTables(NamedSet<TableMetadata> tables) {
        this.tables = tables;
    }
    public Category getCategories() {
        return categories;
    }

    public void setCategories(Category categories) {
        this.categories = categories;
    }

    
}