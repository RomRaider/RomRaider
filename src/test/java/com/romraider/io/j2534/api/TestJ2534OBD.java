/*
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2018 RomRaider.com
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

import com.romraider.io.j2534.api.J2534Impl.Config;
import com.romraider.io.j2534.api.J2534Impl.Protocol;
import com.romraider.io.j2534.api.J2534Impl.TxFlags;
import com.romraider.util.HexUtil;
import com.romraider.util.LogManager;

/**
 * This class is used to exercise the J2534 API against a real J2534 device and
 * an active ECU using the ISO14230 protocol.
 */
public final class TestJ2534OBD {
    private static J2534 api;
    private static final int LOOPBACK = 0;

    public TestJ2534OBD() throws InterruptedException {
        final int deviceId = api.open();
        try {
            version(deviceId);
            final int channelId = api.connect(deviceId, 0, 10400);
            
            final double vBatt = api.getVbattery(channelId);
            System.out.println("Pin 16 Volts = " + vBatt);

            final int msgId = api.startPassMsgFilter(
                    channelId, (byte) 0x00, (byte) 0x00);

            try {
                final byte[] startReq = {
                        (byte) 0xC1, (byte) 0x33, (byte) 0xF1,
                        (byte) 0x81};

                byte[] response =
                        api.fastInit(channelId, startReq);
                System.out.println("Start Response = " + HexUtil.asHex(response));

                if (response.length > 0
                    &&    response[4] == (byte) 0xE9
                    &&    response[5] == (byte) 0x8F) {
                    System.out.println("Standard timing = " +
                            HexUtil.asHex(response));
                }
                setConfig(channelId);
                getConfig(channelId);
                
                Thread.sleep(10L);
                final byte[] timingReq = {
                        (byte) 0xC2, (byte) 0x33, (byte) 0xF1,
                        (byte) 0x83, (byte) 0x01};

                api.writeMsg(channelId, timingReq, 55L, TxFlags.NO_FLAGS);
                System.out.println("Timing Request  = " + HexUtil.asHex(timingReq));

                response = api.readMsg(channelId, 1, 2000L);
                System.out.println("Timing Response = " + HexUtil.asHex(response));

                Thread.sleep(20L);
                final byte[] mode09pid01 = {
                        (byte) 0xC2, (byte) 0x33, (byte) 0xF1,
                        (byte) 0x09, (byte) 0x01};

                api.writeMsg(channelId, mode09pid01, 55L, TxFlags.NO_FLAGS);
                System.out.println("PID0901 Request  = " + HexUtil.asHex(mode09pid01));

                response = api.readMsg(channelId, 1, 2000L);
                System.out.println("PID0901 Response = " + HexUtil.asHex(response));

                int numMsg = 0;
                switch (LOOPBACK) {
                    case 0:
                        numMsg = response[5];
                        break;
                    case 1:
                        numMsg = response[10];
                }
                
                Thread.sleep(10L);
                final byte[] mode09pid02 = {
                        (byte) 0xC2, (byte) 0x33, (byte) 0xF1,
                        (byte) 0x09, (byte) 0x02};

                api.writeMsg(channelId, mode09pid02, 55L, TxFlags.NO_FLAGS);
                System.out.println("PID0902 Request  = " + HexUtil.asHex(mode09pid02));

                response = api.readMsg(channelId, numMsg, 2000L);
                System.out.println("PID0902 Response = " + HexUtil.asHex(response));

                Thread.sleep(10L);
                final byte[] stopReq = {
                        (byte) 0xC1, (byte) 0x33, (byte) 0xF1,
                        (byte) 0x82};

                api.writeMsg(channelId, stopReq, 55L, TxFlags.NO_FLAGS);
                System.out.println("Stop Request  = " + HexUtil.asHex(stopReq));

                response = api.readMsg(channelId, 1, 2000L);
                System.out.println("Stop Response = " + HexUtil.asHex(response));

            } finally {
                api.stopMsgFilter(channelId, msgId);
                api.disconnect(channelId);
            }
        } finally {
            api.close(deviceId);
        }
    }

    private static void version(final int deviceId) {
        Version version = api.readVersion(deviceId);
        System.out.printf("Version => Firmware:[%s], DLL:[%s], API:[%s]%n",
                version.firmware, version.dll, version.api);
    }

    private static void setConfig(final int channelId) {
        final ConfigItem p1Max = new ConfigItem(
                Config.P1_MAX.getValue(),
                40);
        final ConfigItem p3Min = new ConfigItem(
                Config.P3_MIN.getValue(),
                110);
        final ConfigItem p4Min = new ConfigItem(
                Config.P4_MIN.getValue(),
                10);
        final ConfigItem loopback = new ConfigItem(
                Config.LOOPBACK.getValue(),
                LOOPBACK);
        api.setConfig(channelId, p1Max, p3Min, p4Min, loopback);
    }

    private static void getConfig(int channelId) {
        final ConfigItem[] configs = api.getConfig(
                channelId,
                Config.LOOPBACK.getValue(),
                //Config.P1_MIN.getValue(),
                Config.P1_MAX.getValue(),
                //Config.P2_MIN.getValue(),
                //Config.P2_MAX.getValue(),
                Config.P3_MIN.getValue(),
                //Config.P3_MAX.getValue(),
                Config.P4_MIN.getValue()
                //Config.P4_MAX.getValue()
                );
        int i = 1;
        for (ConfigItem item : configs) {
            System.out.printf("Config item %d: Parameter: %s, value:%d%n",
                    i, Config.get(item.parameter), item.value);
            i++;
        }
    }

    public static void main(String args[]) throws InterruptedException{
        LogManager.initDebugLogging();
        if (args.length < 1) {
            System.out.printf("Provide \"library_name\" cmdline arg.");
            return;
        }
        else {
            api = new J2534Impl(  //op20pt32 MONGI432 /usr/local/lib/j2534.so
                    Protocol.ISO14230, args[0]);
        }
        TestJ2534OBD test1 = new TestJ2534OBD();
    }
}
