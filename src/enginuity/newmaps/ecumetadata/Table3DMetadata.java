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

import java.io.Serializable;

public class Table3DMetadata extends TableMetadata implements Serializable {
    
    protected AxisMetadata xaxis;
    protected AxisMetadata yaxis;
    
    
    public Table3DMetadata(String name) {
        super(name);
    }

    public AxisMetadata getXaxis() {
        return xaxis;
    }

    public void setXaxis(AxisMetadata xaxis) {
        this.xaxis = xaxis;
    }

    public AxisMetadata getYaxis() {
        return yaxis;
    }
    
    public int getSize() {
        return xaxis.getSize() * yaxis.getSize();
    }
    
    public int getSizeX() {
        return xaxis.getSize();
    }
    
    public int getSizeY() {
        return yaxis.getSize();
    }

    public void setYaxis(AxisMetadata yaxis) {
        this.yaxis = yaxis;
    }
    
    public String toString() {
        return super.toString() + 
                "\n" + xaxis + 
                "\n" + yaxis;
    }
    
}
