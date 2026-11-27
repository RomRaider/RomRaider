/*
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2022 RomRaider.com
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

package com.romraider.io.j2534.api;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.romraider.io.j2534.api.J2534Impl.Protocol;
import com.romraider.io.j2534.api.J2534Impl.TxFlags;
import com.romraider.util.HexUtil;
import com.romraider.util.LogManager;

/**
 * This class is used to exercise the J2534 API against a real J2534 device and
 * an active ECU using the ISO11898 (CAN bus) protocol.
 */
public final class TestJ2534Can {
    private static final Logger LOGGER = Logger.getLogger(TestJ2534Can.class);
    private static J2534 api ;
    private static final StringBuilder sb = new StringBuilder();


    public TestJ2534Can() throws InterruptedException {
        LOGGER.setLevel(Level.DEBUG);
        final int deviceId = api.open();
        sb.delete(0, sb.capacity());
        try {
            version(deviceId);
            final int channelId = api.connect(deviceId, 0, 500000);

            final double vBatt = api.getVbattery(deviceId);
            sb.append(String.format(
                    "J2534 Interface Pin 16: %sVDC%n", vBatt));

            // accept everything
            final byte[] mask1 = {
                    (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00};
            final byte[] match1 = {
                    (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00};
            // accept 0x0180 and 0x0182
            final byte[] mask2 = {
                    (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xfd};
            final byte[] match2 = {
                    (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x80};
            // accept 0x01f9
            final byte[] mask3 = {
                    (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff};
            final byte[] match3 = {
                    (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0xf9};

//            final int msgId = api.startPassMsgFilter(
//                    channelId, mask1, match1, TxFlags.ISO15765_FRAME_PAD);
            final int msgId1 = api.startPassMsgFilter(
                    channelId, mask2, match2, TxFlags.ISO15765_FRAME_PAD);
            final int msgId2 = api.startPassMsgFilter(
                    channelId, mask3, match3, TxFlags.ISO15765_FRAME_PAD);

            try {
                byte[] response = null;
                for (int i = 0; i < 10; i++) {
                    response = api.readMsg(channelId, 1, 1000L);
                    LOGGER.info("Response = " +
                            HexUtil.asHex(response));
                }
            }
            catch (Exception e) {
                LOGGER.error(e);
            }
            finally {
                //api.stopMsgFilter(channelId, msgId);
                api.stopMsgFilter(channelId, msgId1);
                api.stopMsgFilter(channelId, msgId2);
                api.clearBuffers(channelId);
                api.disconnect(channelId);
            }
        } finally {
            api.close(deviceId);
        }
    }

    private final static void version(final int deviceId) {
        final Version version = api.readVersion(deviceId);
        sb.append(String.format(
                "J2534 Firmware:[%s], DLL:[%s], API:[%s]%n",
                version.firmware, version.dll, version.api));
    }

    @Override
    public final String toString() {
        return sb.toString();
    }

    public final static void main(String args[]) throws InterruptedException{
        LogManager.initDebugLogging();
        if (args.length < 1) {
            System.out.printf("Provide \"library_name\" cmdline arg.");
            return;
        }
        else {
            //op20pt32, MONGI432, /usr/local/lib/j2534.so, System.getProperty("java.library.path")\\windows\\j2534.dll
            api = new J2534Impl(
                    Protocol.CAN, args[0]);
        }

        final TestJ2534Can test1 = new TestJ2534Can();
        System.out.print(test1.toString());
    }
}
