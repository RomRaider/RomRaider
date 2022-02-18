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

import static com.romraider.io.protocol.ssm.iso9141.SSMChecksumCalculator.calculateChecksum;
import static com.romraider.util.HexUtil.asHex;
import static com.romraider.util.ParamChecker.checkNotNull;
import static java.lang.System.arraycopy;
import static org.apache.log4j.Logger.getLogger;

import org.apache.log4j.Logger;

import com.romraider.io.connection.ConnectionManager;
import com.romraider.io.connection.ConnectionProperties;
import com.romraider.io.j2534.api.J2534Impl.Config;
import com.romraider.io.j2534.api.J2534Impl.Flag;
import com.romraider.io.j2534.api.J2534Impl.Protocol;
import com.romraider.io.j2534.api.J2534Impl.TxFlags;
import com.romraider.logger.ecu.comms.manager.PollingState;

public final class J2534ConnectionISO9141 implements ConnectionManager {
    private static final Logger LOGGER = getLogger(J2534ConnectionISO9141.class);
    private J2534 api = null;
    private int channelId;
    private int deviceId;
    private int msgId;
    private byte[] lastResponse;
    private long timeout;

    public J2534ConnectionISO9141(ConnectionProperties connectionProperties, String library) {
        checkNotNull(connectionProperties, "connectionProperties");
        deviceId = -1;
        msgId = -1;
        timeout = connectionProperties.getConnectTimeout();
        initJ2534(connectionProperties, library);
        LOGGER.info("J2534/ISO9141 connection initialised");
    }

    @Override
    public void open(byte[] start, byte[] stop) {
    }

    // Send request and wait for response with known length
    @Override
    public void send(byte[] request, byte[] response, PollingState pollState) {
        checkNotNull(request, "request");
        checkNotNull(response, "response");
        checkNotNull(pollState, "pollState");

        if (pollState.getCurrentState() == PollingState.State.STATE_0 &&
                pollState.getLastState() == PollingState.State.STATE_1) {
            clearLine();
        }

        if (pollState.getCurrentState() == PollingState.State.STATE_0) {
            api.writeMsg(channelId, request, timeout, TxFlags.NO_FLAGS);
        }
        api.readMsg(channelId, response, timeout);

        if (pollState.getCurrentState() == PollingState.State.STATE_1){
            if (    response[0] == (byte) 0x80
                &&  response[1] == (byte) 0xF0
                && (response[2] == (byte) 0x10 || response[2] == (byte) 0x18)
                &&  response[3] == (response.length - 5)
                &&  response[response.length - 1] == calculateChecksum(response)) {

                lastResponse = new byte[response.length];
                arraycopy(response, 0, lastResponse, 0, response.length);
            }
            else{
                LOGGER.error("J2534/ISO9141 Bad Data response: " + asHex(response));
                arraycopy(lastResponse, 0, response, 0, response.length);
                pollState.setNewQuery(true);
            }
        }
    }

    // Send request and wait specified time for response with unknown length
    @Override
    public byte[] send(byte[] request) {
        checkNotNull(request, "request");
        api.writeMsg(channelId, request, timeout, TxFlags.NO_FLAGS);
        return api.readMsg(channelId, 1, timeout);
    }

    @Override
    public void clearLine() {
        if (LOGGER.isDebugEnabled())
            LOGGER.debug("J2534/ISO9141 sending line break");
        api.writeMsg(
                channelId,
                new byte[20],
                100L,
                TxFlags.NO_FLAGS);
        boolean empty = false;
        do {
            byte[] badBytes = api.readMsg(channelId, 100L);
            if (badBytes.length > 0) {
                if (LOGGER.isDebugEnabled())
                    LOGGER.debug("J2534/ISO9141 clearing line (stale data): " + asHex(badBytes));
                empty = false;
            }
            else {
                empty = true;
            }
        } while (!empty );
    }

    @Override
    public void close() {
        stopMsgFilter();
        disconnectChannel();
        closeDevice();
    }

    private void initJ2534(ConnectionProperties connectionProperties, String library) {
        api = new J2534Impl(Protocol.ISO9141, library);
        deviceId = api.open();
        try {
            version(deviceId);
            channelId = api.connect(
                    deviceId, Flag.ISO9141_NO_CHECKSUM.getValue(),
                    connectionProperties.getBaudRate());
            setConfig(channelId, connectionProperties);
            msgId = api.startPassMsgFilter(channelId, (byte) 0x00, (byte) 0x00);
            if (LOGGER.isDebugEnabled())
                LOGGER.debug(String.format(
                    "J2534/ISO9141 success: deviceId:%d, channelId:%d, msgId:%d",
                    deviceId, channelId, msgId));
        } catch (Exception e) {
            if (LOGGER.isDebugEnabled())
                LOGGER.debug(String.format(
                    "J2534/ISO9141 exception: deviceId:%d, channelId:%d, msgId:%d",
                    deviceId, channelId, msgId));
            close();
            throw new J2534Exception("J2534/ISO9141 Error opening device: " + e.getMessage(), e);
        }
    }

    private void version(int deviceId) {
        final Version version = api.readVersion(deviceId);
        LOGGER.info("J2534 Version => firmware: " + version.firmware + ", dll: " + version.dll + ", api: " + version.api);
    }

    private void setConfig(int channelId, ConnectionProperties connectionProperties) {
        final ConfigItem p1Max = new ConfigItem(Config.P1_MAX.getValue(), 1);
        final ConfigItem p3Min = new ConfigItem(Config.P3_MIN.getValue(), 1);
        final ConfigItem p4Min = new ConfigItem(Config.P4_MIN.getValue(), 0);
        final ConfigItem loopback = new ConfigItem(Config.LOOPBACK.getValue(), 1);
        final ConfigItem dataBits = new ConfigItem(
                Config.DATA_BITS.getValue(),
                (connectionProperties.getDataBits() == 8 ? 0 : 1));
        final ConfigItem parity = new ConfigItem(
                Config.PARITY.getValue(),
                connectionProperties.getParity());
        api.setConfig(channelId, p1Max, p3Min, p4Min, loopback, dataBits, parity);
        if (LOGGER.isDebugEnabled())
            LOGGER.debug("J2534/ISO9141 set connection properties: bits=" +
                connectionProperties.getDataBits() + ", parity=" +
                connectionProperties.getParity());
    }

    private void stopMsgFilter() {
        if (msgId == -1) return;
        try {
            api.stopMsgFilter(channelId, msgId);
            if (LOGGER.isDebugEnabled())
                LOGGER.debug("J2534/ISO9141 stopped message filter:" + msgId);
        } catch (Exception e) {
            LOGGER.warn("J2534/ISO9141 Error stopping msg filter: " + e.getMessage());
        }
    }

    private void disconnectChannel() {
        if (deviceId == -1) return;
        try {
            api.disconnect(channelId);
            if (LOGGER.isDebugEnabled())
                LOGGER.debug("J2534/ISO9141 disconnected channel:" + channelId);
        } catch (Exception e) {
            LOGGER.warn("J2534/ISO9141 Error disconnecting channel: " + e.getMessage());
        }
    }

    private void closeDevice() {
        try {
            if (deviceId != -1) {
                api.close(deviceId);
                LOGGER.info("J2534/ISO9141 closed connection to device:" + deviceId);
            }
        } catch (Exception e) {
            LOGGER.warn("J2534/ISO9141 Error closing device: " + e.getMessage());
        }
        finally {
            deviceId = -1;
        }
    }
}
