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

package com.romraider.io.j2534.api;

import static com.romraider.util.HexUtil.asHex;
import static com.romraider.util.LogManager.initDebugLogging;

import java.util.ArrayList;
import java.util.List;

import com.romraider.io.j2534.api.J2534Impl.Config;
import com.romraider.io.j2534.api.J2534Impl.Flag;
import com.romraider.io.j2534.api.J2534Impl.Protocol;
import com.romraider.io.j2534.api.J2534Impl.TxFlags;

/**
 * This class is used to exercise the J2534 API against a real J2534 device and
 * an active ECU using the ISO9141 protocol.
 */
public class TestJ2534 {
    private static String protocol;   // Dev to choice SSM or DS2 on cmdline for testing
    private static J2534 api;

    public TestJ2534() {
        int deviceId = -1;
        try {
            deviceId = api.open();
            version(deviceId);
            int channelId = -1;
            if (protocol.equalsIgnoreCase("ssm")) {
                channelId = api.connect(
                        deviceId, Flag.ISO9141_NO_CHECKSUM.getValue(), 4800);
            }
            else if (protocol.equalsIgnoreCase("ds2")) {
                channelId = api.connect(
                        deviceId, Flag.ISO9141_NO_CHECKSUM.getValue(), 9600);
            }
            try {
                setConfig(channelId);
                getConfig(channelId);

                int msgId = api.startPassMsgFilter(channelId, (byte) 0x00, (byte) 0x00);
                try {
                    List<byte[]> msgs = new ArrayList<byte[]>();
                    byte[] ecuInit = null;
                    if (protocol.equalsIgnoreCase("ssm")) {
                        ecuInit = new byte[]{
                                (byte) 0x80, (byte) 0x10, (byte) 0xF0,
                                (byte) 0x01, (byte) 0xBF, (byte) 0x40};
                        msgs.add(ecuInit);
                    }
                    else if (protocol.equalsIgnoreCase("ds2")) {
                        ecuInit = new byte[]{
                                (byte) 0x12, (byte) 0x04, (byte) 0x00,
                                (byte) 0x16};
                        byte[] engine = new byte[]{
                                (byte) 0x12, (byte) 0x05, (byte) 0x0B,
                                (byte) 0x03, (byte) 0x1F};
                        byte[] swtch = new byte[]{
                                (byte) 0x12, (byte) 0x05, (byte) 0x0B,
                                (byte) 0x04, (byte) 0x18};
                        byte[] lambda = new byte[]{
                                (byte) 0x12, (byte) 0x05, (byte) 0x0B,
                                (byte) 0x91, (byte) 0x8D};
                        byte[] adapt = new byte[]{
                                (byte) 0x12, (byte) 0x05, (byte) 0x0B,
                                (byte) 0x92, (byte) 0x8E};
                        byte[] corr = new byte[]{
                                (byte) 0x12, (byte) 0x05, (byte) 0x0B,
                                (byte) 0x93, (byte) 0x8F};
                        byte[] cat = new byte[]{
                                (byte) 0x12, (byte) 0x05, (byte) 0x0B,
                                (byte) 0x94, (byte) 0x88};
                        byte[] status = new byte[]{
                                (byte) 0x12, (byte) 0x05, (byte) 0x0B,
                                (byte) 0x95, (byte) 0x89};
                        byte[] close = new byte[]{
                                (byte) 0x12, (byte) 0x05, (byte) 0x0B,
                                (byte) 0xFF, (byte) 0xE3};
                        byte[] _0C = new byte[]{
                                (byte) 0x12, (byte) 0x04, (byte) 0x0C,
                                (byte) 0x1A};
                        byte[] _0D = new byte[]{
                                (byte) 0x12, (byte) 0x04, (byte) 0x0D,
                                (byte) 0x1B};
                        byte[] _25 = new byte[]{
                                (byte) 0x12, (byte) 0x04, (byte) 0x25,
                                (byte) 0x33};
                        byte[] _53 = new byte[]{
                                (byte) 0x12, (byte) 0x04, (byte) 0x53,
                                (byte) 0x45};
                        msgs.add(ecuInit);
                        msgs.add(engine);
                        msgs.add(swtch);
                        msgs.add(lambda);
                        msgs.add(adapt);
                        msgs.add(corr);
                        msgs.add(cat);
                        msgs.add(status);
                        msgs.add(close);
                        msgs.add(_0C);
                        msgs.add(_0D);
                        msgs.add(_25);
                        msgs.add(_53);
                    }

                    for (byte[] msg : msgs) {
                        api.writeMsg(channelId, msg, 55L, TxFlags.NO_FLAGS);
                        System.out.println("Request  = " + asHex(msg));

                        byte[] response = api.readMsg(channelId, 500L);
                        System.out.println("Response = " + asHex(response));
                    }

                } finally {
                    api.stopMsgFilter(channelId, msgId);
                }
            } finally {
                api.disconnect(channelId);
            }
        } finally {
            if (api != null)
                api.close(deviceId);
        }
    }

    private static void version(int deviceId) {
        Version version = api.readVersion(deviceId);
        System.out.printf("Version => Firmware:[%s], DLL:[%s], API:[%s]%n",
                version.firmware, version.dll, version.api);
    }

    private static void setConfig(int channelId) {
        ConfigItem p1Max = new ConfigItem(Config.P1_MAX.getValue(), 2);
        ConfigItem p3Min = new ConfigItem(Config.P3_MIN.getValue(), 0);
        ConfigItem p4Min = new ConfigItem(Config.P4_MIN.getValue(), 0);
        ConfigItem loopback = new ConfigItem(Config.LOOPBACK.getValue(), 1);
        ConfigItem parity = null;
        ConfigItem databits = null;
        if (protocol.equalsIgnoreCase("ssm")) {
            parity = new ConfigItem(Config.PARITY.getValue(), 0);
            databits = new ConfigItem(Config.DATA_BITS.getValue(), 8);
        }
        else if (protocol.equalsIgnoreCase("ds2")) {
            parity = new ConfigItem(Config.PARITY.getValue(), 2);
            databits = new ConfigItem(Config.DATA_BITS.getValue(), 8);
        }
        api.setConfig(channelId, p1Max, p3Min, p4Min, parity, databits, loopback);
    }

    private static void getConfig(int channelId) {
        ConfigItem[] configs = api.getConfig(
                channelId,
                Config.LOOPBACK.getValue(),
                Config.P1_MAX.getValue(),
                Config.P3_MIN.getValue(),
                Config.P4_MIN.getValue(),
                Config.P4_MIN.getValue(),
                Config.DATA_BITS.getValue(),
                Config.LOOPBACK.getValue());
        int i = 1;
        for (ConfigItem item : configs) {
            System.out.printf("Config item %d: Parameter: %d, value:%d%n",
                    i, item.parameter, item.value);
            i++;
        }
    }

    public final static void main(String args[]) throws InterruptedException{
        initDebugLogging();
        if (args.length < 2) {
            System.out.printf("Provide \"library_name\" and \"protocol\" cmdline args.");
            return;
        }
        else {
            protocol = args[1].toLowerCase();   // SSM or DS2
            api = new J2534Impl(  //op20pt32 MONGI432 /usr/local/lib/j2534.so
                    Protocol.ISO9141, args[0]);
        }
        TestJ2534 a = new TestJ2534();
    }
}
