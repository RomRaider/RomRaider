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
 *
 * Encoding and decoding routines based on:
 * https://github.com/fenugrec/nissutils/blob/master/cli_utils/
 */

package com.romraider.maps.checksum;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import com.romraider.util.HexUtil;

/**
 * This class contains functions to encode and decoded data as well as
 * calculate its CRC in Nissan fashion.
 */
public final class NcsCoDec {

    public NcsCoDec() {
    }

    /**
     * For the given inBuf, decode the data with the supplied code
     * and return the decoded data
     */
    public final byte[] nisDecode(ByteBuffer inBuf, int code) {
        final ByteBuffer outBuf = ByteBuffer.allocate(inBuf.capacity()).order(ByteOrder.BIG_ENDIAN);
        inBuf.rewind();
        while (inBuf.hasRemaining()) {
            outBuf.putInt(decode(inBuf.getInt(), code));
        }
        return outBuf.array();
    }

    /**
     * For the given inBuf, encode the data with the supplied code
     * and return the encoded data
     */
    public final byte[] nisEncode(ByteBuffer inBuf, int code) {
        final ByteBuffer outBuf = ByteBuffer.allocate(inBuf.capacity()).order(ByteOrder.BIG_ENDIAN);
        inBuf.rewind();
        while (inBuf.hasRemaining()) {
            outBuf.putInt(encode(inBuf.getInt(), code));
        }
        return outBuf.array();
    }

    /**
     * For the given encoded data, decode the data with the supplied code
     * and return decoded data
     */
    // https://github.com/fenugrec/nissutils/blob/master/cli_utils/nislib.c#dec1
    private final int decode(int data, int code) {
        final int dH = data >>> 16;
        final int dL = data & 0xFFFF;
        final int cH = code >>> 16;
        final int cL = code & 0xFFFF;
        final int kL = mess2(dH, dL, cL);
        final int kH = mess1(dL, kL, cH);
        return (kH << 16) | kL;
    }

    /**
     * For the given data, encode the data with the supplied code
     * and return the encoded data
     */
    // https://github.com/fenugrec/nissutils/blob/master/cli_utils/nislib.c#enc1
    private final int encode(int data, int code) {
        final int dH = data >>> 16;
        final int dL = data & 0xFFFF;
        final int cH = code >>> 16;
        final int cL = code & 0xFFFF;
        final int kL = mess1(dH, dL, cH);
        final int kH = mess2(dL, kL, cL);
        return (kH << 16 | kL);
    }

    // https://github.com/fenugrec/nissutils/blob/master/cli_utils/nislib.c#mess1
    private final int mess1(int a, int b, int x) {
        final int var0 = (x + b) & 0xFFFF;
        final int var1 = var0 << 2;
        final int var2 = var1 >>> 16;
        final int var3 = var2 + var0 + var1 - 1;
        return (var3 ^ a) & 0xFFFF;
    }

    // https://github.com/fenugrec/nissutils/blob/master/cli_utils/nislib.c#mess2
    private final int mess2(int a, int b, int x) {
        final int var0 = (x + b) & 0xFFFF;
        final int var1 = var0 << 1;
        final int var2 = ((var1 >>> 16) + var0 + var1 - 1) & 0xFFFF;
        final int var3 = var2 << 4;
        final int var4 = var3 + (var3 >>> 16);
        return (a ^ var4 ^ var2) & 0xFFFF;
    }

    /**
     * For the given data, calculate the 16 bit CRC.
     */
    // sub_15CC in 1ZN67A
    public final short calcCrc(byte[] data) {
        int r6;
        int r5;
        int crc = 0xffff;
        for (int i = 0; i < data.length; i++) {
            r5 = data[i];
            for (int j = 0; j < 8; j++) {
                r6 = crc & 1;
                crc = crc >>> 1;
                if(r6 != (r5 & 1)) {
                    crc = crc ^ 0x8408;
                }
                r5 = r5 >> 1;
            }
        }
        return (short) crc;
    }

    /**
     * Test the NisCoDec functions
     */
    public static void main(String[] args) {
        final NcsCoDec codec = new NcsCoDec();
        String bin_text = "FFFF22E4000115F8000116A000011724000117C80002B9C4E00180F884F820088B1B62F0E7EC9625D91426209623656062637205645C60430274901D7501655C029C760560530624D20D420B75019013D60C029C9011A0040624E4AC644CB18BE5127F204F266EF66DF66CF66BF66AF6000B69F697B481050848035EFFFF22E4";
        byte[] bin = HexUtil.asBytes(bin_text);
        final int scode = 0xC2C0823F;
        ByteBuffer data = ByteBuffer.allocate(bin.length).order(ByteOrder.BIG_ENDIAN).put(bin);
        final byte[] dataEncoded = codec.nisEncode(data, scode);
        short crc = codec.calcCrc(dataEncoded);
        short invert = (short) ~crc;
        short le = (short) (((invert << 8) | ((invert & 0xffff) >>> 8)) & 0xffff);
        final byte[] crcCheck = new byte[dataEncoded.length + 2];
        System.arraycopy(dataEncoded, 0, crcCheck, 0, dataEncoded.length);
        crcCheck[crcCheck.length - 2] = (byte) (invert & 0xff);
        crcCheck[crcCheck.length - 1] = (byte) ((invert & 0xffff) >>> 8);
        short residue = codec.calcCrc(crcCheck);
        System.out.println(String.format(
                "Encode Test:%nbin data: %s%nscode: %08X%nencoded: %s%nCRC (D10D): %04X%n~CRC (2EF2): %04X%n" +
                "little-endian ~CRC (F22E): %04X%nresidue: %04X",
                HexUtil.asHex(bin), scode, HexUtil.asHex(dataEncoded), crc, invert, le,
                residue));
        String text = "34830000003030303030303041060431434D43375150443430310000000000000000005230303030434F4E332B0619022000005CAD04";
        byte[] hex = HexUtil.asBytes(text);
        residue = codec.calcCrc(hex);
        System.out.println(String.format("%s%nCRC: %04X%n~CRC: %04X",
                HexUtil.asHex(hex), residue, (short)~residue));
        text = "348300000030FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFB9BC";
        hex = HexUtil.asBytes(text);
        residue = codec.calcCrc(hex);
        System.out.println(String.format("%s%nCRC: %04X%n~CRC: %04X",
                HexUtil.asHex(hex), residue, (short)~residue));
        data.rewind();
        data = ByteBuffer.allocate(dataEncoded.length).order(ByteOrder.BIG_ENDIAN).put(dataEncoded);
        byte[] dataDecoded = codec.nisDecode(data, scode);
        System.out.println(String.format("Decoded Data: %s",
                HexUtil.asHex(dataDecoded)));
        if(bin_text.equalsIgnoreCase(HexUtil.asHex(dataDecoded))) {
            System.out.println("Decoded data matches input data");
        }
        else {
            System.out.println("DATA DOES NOT MATCH !!!");
        }
    }
}
