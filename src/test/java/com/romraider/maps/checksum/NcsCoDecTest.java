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

package com.romraider.maps.checksum;

import static org.junit.Assert.assertEquals;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.junit.Test;

import com.romraider.util.HexUtil;

public class NcsCoDecTest {
    final NcsCoDec codec = new NcsCoDec();
    final String text = "FFFF22E4000115F8000116A000011724000117C80002B9C4E00180F884F820088B1B62F0E7EC9625D91426209623656062637205645C60430274901D7501655C029C760560530624D20D420B75019013D60C029C9011A0040624E4AC644CB18BE5127F204F266EF66DF66CF66BF66AF6000B69F697B481050848035EFFFF22E4";
    final byte[] bin = HexUtil.asBytes(text);
    final int scode = 0xC2C0823F;
    final ByteBuffer data = ByteBuffer.allocate(bin.length).order(ByteOrder.BIG_ENDIAN).put(bin);
    final String encoded = "118283C9B4953B9B63A23EE3EE224177089344AB4C466E96F606B2997EDEE9129E7B377498905B95D0C75576E88B5EBCBB0065BB03DFCB5279839C252FADBD8A8C1119449FBD8C25107BC5FB5961EB1E7FA50CC2A0BF7DC5B2414339FD0B213B321AAC721891B7AB6F0A837B0D088F7B185ADF86263BC56DBAB2D6D0118283C9";

    @Test
    public final void testEncodeData() {
        final byte[] dataEncoded = codec.nisEncode(data, scode);
        assertEquals(encoded, HexUtil.asHex(dataEncoded));
    }

    @Test
    public final void testEncodedDataCrc() {
        final byte[] dataEncoded = codec.nisEncode(data, scode);
        assertEquals((short) 0xD10D, (short) codec.calcCrc(dataEncoded));
    }

    @Test
    public final void testBitwiseComplementCrc() {
        final byte[] dataEncoded = codec.nisEncode(data, scode);
        assertEquals((short) 0x2EF2, (short) ~codec.calcCrc(dataEncoded));
    }
    
    @Test
    public final void testBitwiseComplementCrcLitteEndian() {
        final byte[] dataEncoded = codec.nisEncode(data, scode);
        short bcCrc = (short) ~codec.calcCrc(dataEncoded);
        short le = (short) (((bcCrc << 8) | ((bcCrc & 0xffff) >>> 8)) & 0xffff);
        assertEquals((short) 0xF22E, le);
    }

    @Test
    public final void testResidue() {
        final byte[] dataEncoded = codec.nisEncode(data, scode);
        short bcCrc = (short) ~codec.calcCrc(dataEncoded);
        final byte[] crcCheck = new byte[dataEncoded.length + 2];
        System.arraycopy(dataEncoded, 0, crcCheck, 0, dataEncoded.length);
        crcCheck[crcCheck.length - 2] = (byte) (bcCrc & 0xff);
        crcCheck[crcCheck.length - 1] = (byte) ((bcCrc & 0xffff) >>> 8);
        assertEquals((short) 0xF0B8, (short) codec.calcCrc(crcCheck));
    }

    @Test
    public final void testDecodeData() {
        final byte[] dataEncoded = codec.nisEncode(data, scode);
        final ByteBuffer bb = ByteBuffer.allocate(dataEncoded.length)
                .order(ByteOrder.BIG_ENDIAN).put(dataEncoded);
        final byte[] data = codec.nisDecode(bb, scode);
        assertEquals(text, HexUtil.asHex(data));
    }
}
