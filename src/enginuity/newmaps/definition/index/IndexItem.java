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

package enginuity.newmaps.definition.index;

import static enginuity.util.MD5Checksum.getMD5Checksum;
import enginuity.util.Nameable;

import java.io.File;
import java.io.Serializable;

public class IndexItem implements Nameable, Serializable {
    
    private String name = "";
    private String base = "";
    private File file = new File("");
    private int idAddress = 0;
    private String idString = "";
    private boolean isAbstract = false;
    private String checksum = "";
    

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBase() {
        return base;
    }

    public void setBase(String base) {
        this.base = base;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
        //System.out.println(file.getAbsolutePath());
        try {
            this.checksum = getMD5Checksum(file.getAbsolutePath());
            //System.out.println(checksum);
        } catch (Exception ex) { }
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

    public boolean isAbstract() {
        return isAbstract;
    }

    public void setAbstract(boolean isAbstract) {
        this.isAbstract = isAbstract;
    }
    
    public String toString() {
        return "Name: " + name +
                "\tBase: " + base + 
                "\tFile: " + file +
                "\tAddress: " + idAddress +
                "\tID: " + idString + 
                "\tAbstract: " + isAbstract +
                "\tChecksum: " + checksum;
    }

    public String getChecksum() {
        return checksum;
    }

    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }
    
}
