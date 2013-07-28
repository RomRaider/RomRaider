/*
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2013 RomRaider.com
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

package com.romraider.logger.external.plx.io;

import com.romraider.io.serial.connection.SerialConnection;
import static com.romraider.util.ThreadUtil.sleep;

public final class TestPlxConnection implements SerialConnection {
    // max byte value for address and data is 0x3F
    private final byte[] data = {(byte) 0x80,
                                        0x00, 0x00, 0x00, 0x00, 0x20, // AFR 0
                                        0x00, 0x00, 0x01, 0x00, 0x05, // AFR 1
                                        0x00, 0x01, 0x00, 0x3F, 0x3F, // EGT 0
                                        0x00, 0x02, 0x00, 0x02, 0x30, // Fluid Temp 0
                                        0x40,
                                 (byte) 0x80,
                                        0x00, 0x00, 0x00, 0x00, 0x10, // AFR 0
                                        0x00, 0x00, 0x01, 0x00, 0x07, // AFR 1
                                        0x00, 0x01, 0x00, 0x2F, 0x3F, // EGT 0
                                        0x00, 0x02, 0x00, 0x01, 0x00, // Fluid Temp 0
                                        0x40,
                                 (byte) 0x80,
                                        0x00, 0x00, 0x00, 0x00, 0x08, // AFR 0
                                        0x00, 0x00, 0x01, 0x00, 0x09, // AFR 1
                                        0x00, 0x01, 0x00, 0x1F, 0x3F, // EGT 0
                                        0x00, 0x02, 0x00, 0x00, 0x3F, // Fluid Temp 0
                                        0x40,
                                 (byte) 0x80,
                                        0x00, 0x00, 0x00, 0x01, 0x2f, // AFR 0
                                        0x3f, 0x00, 0x00, 0x00, 0x00, // bogus sensor
                                        0x40};
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
