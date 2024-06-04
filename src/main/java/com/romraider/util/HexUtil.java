/*
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2021 RomRaider.com
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

public final class HexUtil {

    private HexUtil() {
    }

    /**
     * Convert a hexadecimal byte to a character representation.
     * @param b - byte to convert
     * @return string representing the byte value
     */
    public static String asHex(byte b) {
        return asHex(new byte[]{b});
    }

    /**
     * Convert a hexadecimal byte array to a character representation.
     * @param in - byte array to convert
     * @return upper case string representing the byte array value
     */
    public static String asHex(byte[] in) {
        return bytesToHex(in).toUpperCase();
    }

    /**
     * Convert a string representing a hexadecimal value into an array of
     * hexadecimal bytes.
     * @param hex - string to convert, with or without preceding 0x
     * @return array of hexadecimal byte values
     */
    public static byte[] asBytes(String hex) {
        if (null == hex) return new byte[0];
        if (hex.indexOf(' ') >= 0) {
            hex = hex.replaceAll(" ", "");
        }
        if (hex.toLowerCase().indexOf("0x") >= 0) {
            hex = hex.replaceAll("(?i)0x", "");
        }
        return hexToBytes(hex);
    }

    /**
     * Convert a hexadecimal byte array to a character representation given the
     * starting offset and length to convert.
     * @param bs - byte array to convert
     * @param off - starting offset
     * @param length - number of bytes to convert
     * @return string representing the bytes in the array converted
     */
    public static String bytesToHex(byte[] bs, int off, int length) {
        StringBuffer sb = new StringBuffer(length * 2);
        bytesToHexAppend(bs, off, length, sb);
        return sb.toString();
    }

    /**
     * Convert a hexadecimal byte array to a character representation given the
     * starting offset and length to convert and append them to the provided
     * string buffer.
     * @param bs - byte array to convert
     * @param off - starting offset
     * @param length - number of bytes to convert
     * @param sb - string buffer to append the converted bytes to
     */
    public static void bytesToHexAppend(byte[] bs, int off, int length, StringBuffer sb) {
        sb.ensureCapacity(sb.length() + length * 2);
        for (int i = off; (i < (off + length)) && (i < bs.length); i++) {
            sb.append(Character.forDigit((bs[i] >>> 4) & 0xf, 16));
            sb.append(Character.forDigit(bs[i] & 0xf, 16));
        }
    }

    /**
     * Convert a hexadecimal byte array to a character representation.
     * @param bs - byte array to convert
     * @return string representing the byte array value
     */
    public static String bytesToHex(byte[] bs) {
        return bytesToHex(bs, 0, bs.length);
    }

    /**
     * Convert a string representing a hexadecimal value into an array of
     * hexadecimal bytes.
     * @param s - string to convert without preceding 0x
     * @return array of hexadecimal byte values
     */
    public static byte[] hexToBytes(String s) {
        return hexToBytes(s, 0);
    }

    /**
     * Convert a string representing a hexadecimal value into an array of
     * hexadecimal bytes.
     * @param s - string to convert without preceding 0x.  If the string starts
     * with 0x, use the method asBytes(s) to convert the string
     * @param off - starting offset for byte conversion
     * @return array of hexadecimal byte values
     */
    public static byte[] hexToBytes(String s, int off) {
        byte[] bs = new byte[off + (1 + s.length()) / 2];
        hexToBytes(s, bs, off);
        return bs;
    }

    /**
     * Convert a string representing a hexadecimal value into an array of
     * hexadecimal bytes.
     * @param s - string to convert without preceding 0x.  If the string starts
     * with 0x, use the method asBytes(s) to convert the string
     * @param out - the byte array write the converted string into
     * @param off - starting offset for byte conversion
     * @throws NumberFormatException
     * @throws IndexOutOfBoundsException
     */
    public static void hexToBytes(String s, byte[] out, int off) throws NumberFormatException, IndexOutOfBoundsException {
        int slen = s.length();
        if ((slen % 2) != 0) {
            s = '0' + s;
        }
        if (out.length < off + slen / 2) {
            throw new IndexOutOfBoundsException("Output buffer too small for input (" + out.length + "<" + off + slen / 2 + ")");
        }
        // Safe to assume the string is even length
        byte b1, b2;
        for (int i = 0; i < slen; i += 2) {
            b1 = (byte) Character.digit(s.charAt(i), 16);
            b2 = (byte) Character.digit(s.charAt(i + 1), 16);
            if ((b1 < 0) || (b2 < 0)) {
                throw new NumberFormatException();
            }
            out[off + i / 2] = (byte) (b1 << 4 | b2);
        }
    }

    /**
     * Convert a string representing a hexadecimal value into an integer.
     * @param input - string to convert with or without the leading 0x
     * @return integer value
     */
    public static int hexToInt(String input) {
        return Integer.parseInt(input.replace("0x",""), 16);
    }

    /**
     * Convert an integer value to a string representation of the integer
     * value as an unsigned integer in base 16.
     * @param input - integer value to convert
     * @return string representation of the integer starting with 0x
     */
    public static String intToHexString(int input) {
        return "0x" + Integer.toHexString(input).toUpperCase();
    }
}
