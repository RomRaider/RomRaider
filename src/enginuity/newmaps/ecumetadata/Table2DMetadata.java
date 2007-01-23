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

public class Table2DMetadata extends AxisMetadata implements Serializable {
    
    protected AxisMetadata axis;
    
    public Table2DMetadata(String name) {
        super(name);
    }    

    public AxisMetadata getAxis() {
        return axis;
    }

    public void setAxis(AxisMetadata axis) {
        this.axis = axis;
    }
    
    public int getSize() {
        return axis.getSize();
    }
    
    public String toString() {
        String scaleName = "";
        
        try {
            scaleName = scale.getName();
        } catch (NullPointerException ex) {
            scaleName = "Not found";
        }
        
        String output = "      --- Table: " + name + " ---" +
                "\n      - Description: " + description +
                "\n      - Scale: " + scaleName +
                "\n      - Address: " + address +
                "\n      - Userlevel: " + userLevel +
                "\n" + axis;
                
                
        return output;
    }

}