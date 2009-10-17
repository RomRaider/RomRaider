/*
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2009 RomRaider.com
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

package com.romraider.io.connection;

public final class ConnectionPropertiesImpl implements ConnectionProperties {
    private final int baudRate;
    private final int dataBits;
    private final int stopBits;
    private final int parity;
    private final int connectTimeout;
    private final int sendTimeout;


    public ConnectionPropertiesImpl(int baudRate, int dataBits, int stopBits, int parity, int connectTimeout, int sendTimeout) {
        this.baudRate = baudRate;
        this.dataBits = dataBits;
        this.stopBits = stopBits;
        this.parity = parity;
        this.connectTimeout = connectTimeout;
        this.sendTimeout = sendTimeout;
    }

    public int getBaudRate() {
        return baudRate;
    }

    public int getDataBits() {
        return dataBits;
    }

    public int getStopBits() {
        return stopBits;
    }

    public int getParity() {
        return parity;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public int getSendTimeout() {
        return sendTimeout;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(getClass().getSimpleName());
        builder.append("[baudRate=").append(baudRate);
        builder.append(", dataBits=").append(dataBits);
        builder.append(", stopBits=").append(stopBits);
        builder.append(", dataBits=").append(dataBits);
        builder.append(", parity=").append(parity);
        builder.append(", connectTimeout=").append(connectTimeout);
        builder.append(", sendTimeout=").append(sendTimeout).append("]");
        return builder.toString();
    }
}
