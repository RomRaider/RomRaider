/*
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2008 RomRaider.com
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

import com.romraider.io.j2534.op20.J2534OpenPort20;
import static com.romraider.io.j2534.op20.OpenPort20.CONFIG_P1_MAX;
import static com.romraider.io.j2534.op20.OpenPort20.CONFIG_P3_MIN;
import static com.romraider.io.j2534.op20.OpenPort20.CONFIG_P4_MIN;
import static com.romraider.io.j2534.op20.OpenPort20.FLAG_ISO9141_NO_CHECKSUM;
import static com.romraider.io.j2534.op20.OpenPort20.PROTOCOL_ISO9141;
import static com.romraider.util.HexUtil.asHex;
import static com.romraider.util.LogManager.initDebugLogging;

public final class TestJ2534 {
    private static final J2534 api = new J2534OpenPort20(PROTOCOL_ISO9141);

    public static void main(String[] args) {
        initDebugLogging();
        if (api.isSupported()) ecuInit();
    }

    private static void ecuInit() {
        int deviceId = api.open();
        try {
            version(deviceId);
            int channelId = api.connect(deviceId, FLAG_ISO9141_NO_CHECKSUM, 4800);
            try {
                setConfig(channelId);
                int msgId = api.startPassMsgFilter(channelId, (byte) 0x00, (byte) 0x00);
                try {

                    byte[] ecuInit = {(byte) 0x80, (byte) 0x10, (byte) 0xF0, (byte) 0x01, (byte) 0xBF, (byte) 0x40};

                    api.writeMsg(channelId, ecuInit, 55L);
                    System.out.println("Request  = " + asHex(ecuInit));

                    byte[] response = api.readMsg(channelId, 2000L);
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
        System.out.println("Version => firmware: " + version.firmware + ", dll: " + version.dll + ", api: " + version.api);
    }

    private static void setConfig(int channelId) {
        ConfigItem p1Max = new ConfigItem(CONFIG_P1_MAX, 2);
        ConfigItem p3Min = new ConfigItem(CONFIG_P3_MIN, 0);
        ConfigItem p4Min = new ConfigItem(CONFIG_P4_MIN, 0);
        api.setConfig(channelId, p1Max, p3Min, p4Min);
    }
}
