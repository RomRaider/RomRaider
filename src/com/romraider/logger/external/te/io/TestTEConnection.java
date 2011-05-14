/*
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2010 RomRaider.com
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

package com.romraider.logger.external.te.io;

import com.romraider.io.serial.connection.SerialConnection;
import static com.romraider.util.ThreadUtil.sleep;

public final class TestTEConnection implements SerialConnection {
/*
	2.0 Data Frame Format:
	Byte - Description
	1 - Frame Header byte 1 (0x5A)
	2 - Frame Header byte 2 (0xA5)
	3 - Frame Sequence counter
	4 - Tick [high] (1 tick = 1/100 Second)
	5 - Tick [low] byte
	6 - l-16 or Ipx(0), (or ADC) [high] byte
	7 - l-16 or Ipx(0), (or ADC) [low] byte
	8 - Ipx(1) [high] (8192=F/A, 4096=Ipx[0])
	9 - Ipx(1) [low] byte
	10 - User 1 ADC [high] (V1 input)
	11 - User 1 ADC [low] byte
	12 - User 2 ADC [high] (V2 input)
	13 - User 2 ADC [low] byte
	14 - User 3 ADC [high] (V3 input)
	15 - User 3 ADC [low] byte
	16 - Thermocouple 1 ADC [high] (T1 Input)
	17 - Thermocouple 1 ADC [low]
	18 - Thermocouple 2 ADC [high] (T2 Input)
	19 - Thermocouple 2 ADC [low]
	20 - Thermocouple 3 ADC [high] (T3 Input)
	21 - Thermocouple 3 ADC [low]
	22 - Thermistor ADC or Vss count [high]
	23 - Thermistor ADC or Vss count [low]
	24 - RPM count [high] byte
	25 - RPM count [low] bye
	26 - Status/Error [high] byte
	27 - Status/Error [low] byte
	28 - CRC (1's comp. sum of above)
 */
    private final byte[] data = {
    		(byte) 0x5a,
    		(byte) 0xa5,
    		(byte) 0x02,	// frame count
    		(byte) 0x89,
    		(byte) 0x00,
    		(byte) 0x1f,	// lambda [high]
    		(byte) 0xf4,	// lambda [low]
    		(byte) 0x01,	// Ipx(1) [high]
    		(byte) 0x00,	// Ipx(1) [low]
    		(byte) 0x1f,	// user1 [high]
    		(byte) 0xf8,	// user1 [low]
    		(byte) 0x0f,
    		(byte) 0xfc,
    		(byte) 0x00,
    		(byte) 0x21,
    		(byte) 0x03,	// tc1 [high]
    		(byte) 0xf8,	// tc1 [low]
    		(byte) 0x01,
    		(byte) 0xfc,
    		(byte) 0x00,
    		(byte) 0x20,
    		(byte) 0x09,
    		(byte) 0x00,
    		(byte) 0x03,	// RPM count [high]
    		(byte) 0xec,	// RPM count [low]
    		(byte) 0x0b,
    		(byte) 0x00,
    		(byte) 0x00
    		};
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
        data[3] = (byte) index;
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
}
