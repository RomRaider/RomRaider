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

public enum MTSSensorInputFunction {
    MTS_FUNC_LAMBDA(0), 
    MTS_FUNC_O2(1),
    MTS_FUNC_INCALIB(2),
    MTS_FUNC_RQCALIB(3),
    MTS_FUNC_WARMUP(4),
    MTS_FUNC_HTRCAL(5),
    MTS_FUNC_ERROR(6),
    MTS_FUNC_FLASHLEV(7),
    MTS_FUNC_SERMODE(8),
    MTS_FUNC_NOTLAMBDA(9),
    MTS_FUNC_INVALID(10);
    
    private final int function;

    private MTSSensorInputFunction(int function) {
        this.function = function;
    }

    /**
     * MTSSensorInputFunction contains the values associated with the various
     * functions of the sensors reported to be in the MTS stream.  Some functions
     * report the state of the sensor, such as, a WBO2 that is in warm-up state (4).
     */
    public int getFunction() {
        return function;
    }

   public static MTSSensorInputFunction valueOf(int function) {
        for (MTSSensorInputFunction type : values()) {
                if (type.getFunction() == function)
                        return type;
        }
        return MTS_FUNC_INVALID;
    }
}
