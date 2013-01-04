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

import com4j.DISPID;
import com4j.IID;

@IID("{4A8AA6AC-E180-433E-8871-A2F8D2413F03}")
public interface MTSEvents {
    /**
     * Triggered to indicate MTS connection result
     */
    @DISPID(1)
    void connectionEvent(int result);

    /**
     * Triggered when an error occurs on the MTS data stream
     */
    @DISPID(2)
    void connectionError();

    /**
     * Triggered when new sample data is available
     */
    @DISPID(3)
    void newData();
}
