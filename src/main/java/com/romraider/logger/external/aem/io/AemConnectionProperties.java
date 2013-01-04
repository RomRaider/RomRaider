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

package com.romraider.logger.external.aem.io;

import com.romraider.io.connection.ConnectionProperties;

public final class AemConnectionProperties implements ConnectionProperties {
    public int getBaudRate() {
        return 9600;
    }

    public void setBaudRate(int b) {

    }

    public int getDataBits() {
        return 8;
    }

    public int getStopBits() {
        return 1;
    }

    public int getParity() {
        return 0;
    }

    public int getConnectTimeout() {
        return 2000;
    }

    public int getSendTimeout() {
        return 500;
    }

    public String toString() {
        String properties = String.format(
                "%s[baudRate=%d, dataBits=%d, stopBits=%d, parity=%d, " + 
                "connectTimeout=%d, sendTimeout=%d]",
            getClass().getSimpleName(),
            getBaudRate(),
            getDataBits(),
            getStopBits(),
            getParity(),
            getConnectTimeout(),
            getSendTimeout()
        );
        return properties;
    }
}
