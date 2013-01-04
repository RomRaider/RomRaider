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

package com.romraider.logger.external.innovate.generic.serial.io;

import com.romraider.io.serial.connection.SerialConnection;
import static com.romraider.util.HexUtil.asBytes;

public final class TestInnovateConnection implements SerialConnection {
    private final byte[] source;
    private int i;

    public TestInnovateConnection(String hex) {
        this.source = asBytes(hex);
    }

    public void write(byte[] bytes) {
        throw new UnsupportedOperationException();
    }

    public int available() {
        return source.length;
    }

    public void read(byte[] bytes) {
        for (int j = 0; j < bytes.length; j++) {
            bytes[j] = source[(i + j) % source.length];
        }
        i = (i + bytes.length);
        if (i >= source.length) i %= source.length;
    }

    public byte[] readAvailable() {
        byte[] result = new byte[available()];
        read(result);
        return result;
    }

    public void readStaleData() {
        throw new UnsupportedOperationException();
    }

    public void close() {
    }

    public String readLine() {
        throw new UnsupportedOperationException();
    }

    public int read() {
        byte[] result = new byte[1];
        read(result);
        return result[0];
    }

    public void sendBreak(int duration) {
    }
}
