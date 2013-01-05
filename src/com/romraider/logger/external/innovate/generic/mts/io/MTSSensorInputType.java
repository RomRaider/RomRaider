/*
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2012 RomRaider.com
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

package com.romraider.logger.external.innovate.generic.mts.io;

public enum MTSSensorInputType {
    MTS_TYPE_LAMBDA(0),
    MTS_TYPE_AFR(1),
    MTS_TYPE_VDC(2);

    private final int inputType;

    private MTSSensorInputType(int inputType) {
        this.inputType = inputType;
    }

    /**
     * MTSSensorInputType contains the values associated with the various
     * types of sensors reported to be in the MTS stream.
     */
    public int getType() {
        return inputType;
    }

   public static MTSSensorInputType valueOf(int inputType) {
        for (MTSSensorInputType type : values()) {
                if (type.getType() == inputType)
                        return type;
        }
        return null;
    }
}
