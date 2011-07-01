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

package com.romraider.util;

import java.util.List;

public final class ByteUtil {

    private ByteUtil() {
        throw new UnsupportedOperationException();
    }

    public static int asUnsignedInt(byte[] bytes) {
        int i = 0;
        for (int j = 0; j < bytes.length; j++) {
            if (j > 0) {
                i <<= 8;
            }
            i |= bytes[j] & 0xFF;
        }
        return i;
    }

    public static byte asByte(int i) {
        return Integer.valueOf(i).byteValue();
    }

    public static int asInt(byte b) {
        return Byte.valueOf(b).intValue();
    }

    public static boolean matchOnes(byte b, int mask) {
        return (b & mask) == mask;
    }

    public static boolean matchZeroes(byte b, int mask) {
        return (b & mask) == 0;
    }

	public static void byteListToBytes(List<Byte> buffer, byte[] response) {
		for (int i = 0; i < buffer.size(); i++) {
			response[i] = buffer.get(i);
		}
	}
}
