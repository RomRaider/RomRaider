/*
 *
 * Enginuity Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006 Enginuity.org
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
 *
 */

package enginuity.util;

@SuppressWarnings({"UnnecessaryBoxing"})
public final class ByteUtil {

    private ByteUtil() {
    }

    public static int asInt(byte[] bytes) {
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

    public static byte[] asBytes(int i) {
        byte[] b = new byte[4];
        for (int j = 0; j < 4; j++) {
            int offset = (b.length - 1 - j) << 3;
            b[j] = (byte) ((i >>> offset) & 0xFF);
        }
        return b;
    }

    public static float asFloat(byte[] bytes) {
        return Float.intBitsToFloat(asInt(bytes));
    }

}
