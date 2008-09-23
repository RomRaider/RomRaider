/*
 *
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
 *
 */

package com.romraider.io.j2534.api;

import com.romraider.io.connection.ConnectionManager;
import com.romraider.io.connection.ConnectionProperties;
import com.romraider.io.j2534.op20.J2534OpenPort20;
import static com.romraider.io.j2534.op20.OpenPort20.CONFIG_P1_MAX;
import static com.romraider.io.j2534.op20.OpenPort20.CONFIG_P3_MIN;
import static com.romraider.io.j2534.op20.OpenPort20.CONFIG_P4_MIN;
import static com.romraider.io.j2534.op20.OpenPort20.FLAG_ISO9141_NO_CHECKSUM;
import static com.romraider.io.j2534.op20.OpenPort20.PROTOCOL_ISO9141;
import static com.romraider.util.ParamChecker.checkNotNull;
import org.apache.log4j.Logger;
import static org.apache.log4j.Logger.getLogger;

public final class J2534ConnectionManager implements ConnectionManager {
    private static final Logger LOGGER = getLogger(J2534ConnectionManager.class);
    private final J2534 api = new J2534OpenPort20(PROTOCOL_ISO9141);
    private int channelId;
    private int deviceId;
    private int msgId;

    public J2534ConnectionManager(ConnectionProperties connectionProperties) {
        checkNotNull(connectionProperties, "connectionProperties");
        initJ2534(connectionProperties.getBaudRate());
    }

    // Send request and wait for response with known length
    public void send(byte[] request, byte[] response, long timeout) {
        checkNotNull(request, "request");
        checkNotNull(request, "response");
        // FIX - should timeout be connectionProperties.getReadTimeout() ??
        api.writeMsg(channelId, request, timeout);
        api.readMsg(channelId, response, timeout);
    }

    // Send request and wait specified time for response with unknown length
    public byte[] send(byte[] bytes, long maxWait) {
        checkNotNull(bytes, "bytes");
        // FIX - should maxWait be connectionProperties.getReadTimeout() ??
        api.writeMsg(channelId, bytes, maxWait);
        return api.readMsg(channelId, maxWait);
    }

    public void close() {
        stopMsgFilter();
        disconnectChannel();
        closeDevice();
    }

    private void initJ2534(int baudRate) {
        this.deviceId = api.open();
        version(deviceId);
        this.channelId = api.connect(deviceId, FLAG_ISO9141_NO_CHECKSUM, baudRate);
        setConfig(channelId);
        this.msgId = api.startPassMsgFilter(channelId, (byte) 0x00, (byte) 0x00);
    }

    private void version(int deviceId) {
        if (!LOGGER.isDebugEnabled()) return;
        Version version = api.readVersion(deviceId);
        LOGGER.debug("J2534 Version => firmware: " + version.firmware + ", dll: " + version.dll + ", api: " + version.api);
    }

    private void setConfig(int channelId) {
        ConfigItem p1Max = new ConfigItem(CONFIG_P1_MAX, 2);
        ConfigItem p3Min = new ConfigItem(CONFIG_P3_MIN, 0);
        ConfigItem p4Min = new ConfigItem(CONFIG_P4_MIN, 0);
        api.setConfig(channelId, p1Max, p3Min, p4Min);
    }

    private void stopMsgFilter() {
        try {
            api.stopMsgFilter(channelId, msgId);
        } catch (Exception e) {
            LOGGER.warn("Error stopping msg filter");
        }
    }

    private void disconnectChannel() {
        try {
            api.disconnect(channelId);
        } catch (Exception e) {
            LOGGER.warn("Error disconnecting channel");
        }
    }

    private void closeDevice() {
        try {
            api.close(deviceId);
        } catch (Exception e) {
            LOGGER.warn("Error closing device");
        }
    }
}