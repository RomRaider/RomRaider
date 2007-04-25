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

import enginuity.newmaps.ecumetadata.Scale;

import java.nio.ByteBuffer;

@SuppressWarnings({"UnnecessaryBoxing"})
public final class ByteUtil {

    private ByteUtil() {
        throw new UnsupportedOperationException();
    }

    public static int asUnsignedInt(byte[] bytes, int endian) {

        if (endian == Scale.ENDIAN_LITTLE) bytes = reverseEndian(bytes);

        int i = 0;
        for (int j = 0; j < bytes.length; j++) {
            if (j > 0) {
                i <<= 8;
            }
            i |= bytes[j] & 0xFF;
        }
        return i;
    }

    public static int asUnsignedInt(byte[] bytes) {
        return asUnsignedInt(bytes, Scale.ENDIAN_BIG);
    }

    public static int asSignedInt(byte[] bytes, int endian) {

        if (endian == Scale.ENDIAN_LITTLE) bytes = reverseEndian(bytes);

        int i = 0;
        for (int j = 0; j < bytes.length; j++) {
            if (j > 0) {
                i <<= 8;
            }
            i |= bytes[j];
        }
        return i;
    }

    public static int asSignedInt(byte[] bytes) {
        return asSignedInt(bytes, Scale.ENDIAN_BIG);
    }

    public static int getLengthInBytes(int type) {

        if (type == Scale.STORAGE_TYPE_CHAR) return 1;
        else if (type == Scale.STORAGE_TYPE_FLOAT) return 4;
        else if (type == Scale.STORAGE_TYPE_HEX) return 1;
        else if (type == Scale.STORAGE_TYPE_INT16) return 2;
        else if (type == Scale.STORAGE_TYPE_INT8) return 1;
        else if (type == Scale.STORAGE_TYPE_UINT16) return 2;
        else if (type == Scale.STORAGE_TYPE_UINT8) return 1;
        else if (type == Scale.STORAGE_TYPE_UINT32) return 4;
        else if (type == Scale.STORAGE_TYPE_INT32) return 4;

        else return 0;
    }

    public static byte asByte(int i) {
        return Integer.valueOf(i).byteValue();
    }

    public static int asInt(byte b) {
        return Byte.valueOf(b).intValue();
    }

    public static byte[] asUnsignedBytes(int i, int endian) {
        byte[] b = new byte[4];
        for (int j = 0; j < 4; j++) {
            int offset = (b.length - 1 - j) << 3;
            b[j] = (byte) ((i >>> offset) & 0xFF);
        }

        if (endian == Scale.ENDIAN_LITTLE) b = reverseEndian(b);
        return b;
    }

    public static byte[] reverseEndian(byte[] data) {
        byte[] newData = new byte[data.length];
        for (int i = 0; i < data.length; i++) {
            newData[data.length - 1 - i] = data[i];
        }
        return newData;
    }

    public static byte[] asSignedBytes(int i, int endian) {
        byte[] b = new byte[4];
        for (int j = 0; j < 4; j++) {
            int offset = (b.length - 1 - j) << 3;
            b[j] = (byte) (i >>> offset);
        }
        if (endian == Scale.ENDIAN_LITTLE) b = reverseEndian(b);
        return b;
    }

    public static float asFloat(byte[] bytes, int endian) {
        byte[] currentCell = { bytes[0],bytes[1],bytes[2],bytes[3] };
        return Float.intBitsToFloat(asUnsignedInt(currentCell, endian));
    }

    public static byte[] asBytes(float f) {
        byte[] output = new byte[4];
        ByteBuffer bb = ByteBuffer.wrap(output, 0, 4);
        bb.putFloat(f);
        return bb.array();
    }

}
