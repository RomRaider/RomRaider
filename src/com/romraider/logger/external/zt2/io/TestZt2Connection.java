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

package com.romraider.logger.external.zt2.io;

import com.romraider.io.serial.connection.SerialConnection;
import static com.romraider.util.ThreadUtil.sleep;

public final class TestZt2Connection implements SerialConnection {
/*
                ( 0 ), // [0] always 0
                ( 1 ), // [1] always 1
                ( 2 ), // [2] always 2
                ( 147 ), // [3] AFR
                ( 0 ), // [4] EGT Low
                ( 0 ), // [5] EGT High
                ( 0 ), // [6] RPM Low
                ( 0 ), // [7] RPM High
                ( 0 ), // [8] MAP Low
                ( 0 ), // [9] MAP High
                ( 0 ), // [10] TPS
                ( 0 ), // [11] USER1
                ( 0 ), // [12] Config Register1
                ( 0 ), // [13] Config Register2
 */
    private final byte[] data = {(byte) 0x00, 0x01, 0x02, (byte) 0x89, 0x00, 0x04, 0x00, 0x01, 0x00, 0x00, 0x0f, 0x40, 0x00, 0x00};
    private int index;
    private byte[] result = new byte[1];

    public void write(byte[] bytes) {
        throw new UnsupportedOperationException();
    }

    public int available() {
        return 1;
    }

    public void read(byte[] bytes) {
        if (bytes.length != 1) throw new IllegalArgumentException();
        if (index >= data.length) index = 0;
        bytes[0] = data[index++];
        sleep(10);
    }

    public byte[] readAvailable() {
        throw new UnsupportedOperationException();
    }

    public void readStaleData() {
        throw new UnsupportedOperationException();
    }

    public String readLine() {
        throw new UnsupportedOperationException();
    }

    public int read() {
        read(result);
        return result[0];
    }

    public void close() {
        index = 0;
    }

    public void sendBreak(int duration) {
    }
}
