/*
 *
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2008 RomRaider.com
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

package com.romraider.NewGUI.etable;

import org.apache.log4j.Logger;

public class ETableSaveState {
    private static final Logger LOGGER = Logger.getLogger(ETableSaveState.class);
    private Object[][] internalData;

    //private String name;
    public ETableSaveState(Object[][] data) {
        //this.name = name;
        int width = data.length;
        int height = data[0].length;

        //LOGGER.debug("Dimensions:  w:"+ width+"   h:"+height);
        this.internalData = new Object[width][height];

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                Object tempData = data[i][j];
                this.internalData[i][j] = tempData;
            }
        }

        LOGGER.debug("Sample: " + this.internalData[0][0]);
    }

    public Object[][] getData() {
        return this.internalData;
    }

    /*
     public String getName(){
         return name;
     }
     */
}
