/*
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2012 RomRaider.com
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

import com.romraider.io.j2534.api.J2534Impl.Config;
import com.romraider.io.j2534.api.J2534Impl.Flag;
import com.romraider.io.j2534.api.J2534Impl.Protocol;

/**
 * This class is used to exercise the J2534 API against a real J2534 device and
 * an active ECU using the ISO9141 protocol.
 */
public class TestJ2534 {
    private static final J2534 api = new J2534Impl(Protocol.ISO9141, "op20pt32");

    public TestJ2534() {
        int deviceId = api.open();
        try {
            version(deviceId);
            int channelId = api.connect(
                    deviceId, Flag.ISO9141_NO_CHECKSUM.getValue(), 4800);
            try {
                setConfig(channelId);
                getConfig(channelId);
                
                int msgId = api.startPassMsgFilter(channelId, (byte) 0x00, (byte) 0x00);
                try {

                    byte[] ecuInit = {
                            (byte) 0x80, (byte) 0x10, (byte) 0xF0,
                            (byte) 0x01, (byte) 0xBF, (byte) 0x40};

                    api.writeMsg(channelId, ecuInit, 55L);
                    System.out.println("Request  = " + asHex(ecuInit));

                    byte[] response = api.readMsg(channelId, 1, 2000L);
                    System.out.println("Response = " + asHex(response));

                } finally {
                    api.stopMsgFilter(channelId, msgId);
                }
            } finally {
                api.disconnect(channelId);
            }
        } finally {
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
        api.setConfig(channelId, p1Max, p3Min, p4Min, loopback);
    }

    private static void getConfig(int channelId) {
        ConfigItem[] configs = api.getConfig(
                channelId,
                Config.LOOPBACK.getValue(),
                Config.P1_MAX.getValue(),
                Config.P3_MIN.getValue(),
                Config.P4_MIN.getValue());
        int i = 1;
        for (ConfigItem item : configs) {
            System.out.printf("Config item %d: Parameter: %d, value:%d%n",
                    i, item.parameter, item.value);
            i++;
        }
    }

    public static void main(String args[]){
        initDebugLogging();
        TestJ2534 a = new TestJ2534();
    }
}
