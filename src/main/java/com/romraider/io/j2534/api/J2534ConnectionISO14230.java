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

import static com.romraider.io.protocol.ncs.iso14230.NCSChecksumCalculator.calculateChecksum;
import static com.romraider.util.HexUtil.asHex;
import static com.romraider.util.ParamChecker.checkNotNull;
import static java.lang.System.arraycopy;
import static org.apache.log4j.Logger.getLogger;

import org.apache.log4j.Logger;

import com.romraider.io.connection.ConnectionManager;
import com.romraider.io.connection.ConnectionProperties;
import com.romraider.io.connection.KwpConnectionProperties;
import com.romraider.io.j2534.api.J2534Impl.Config;
import com.romraider.io.j2534.api.J2534Impl.Flag;
import com.romraider.io.j2534.api.J2534Impl.Protocol;
import com.romraider.io.j2534.api.J2534Impl.TxFlags;
import com.romraider.logger.ecu.comms.manager.PollingState;

public final class J2534ConnectionISO14230 implements ConnectionManager {
    private static final Logger LOGGER = getLogger(J2534ConnectionISO14230.class);
    private J2534 api = null;
    private int LOOPBACK = 0;
    private int channelId;
    private int deviceId;
    private int msgId;
    private byte[] lastResponse;
    private long timeout;
    private boolean commsStarted;
    private PollingState.State currentPollState;
    private ConnectionProperties connectionProperties;
    private String library;
    private byte[] startRequest;
    private byte[] stopRequest;

    public J2534ConnectionISO14230(ConnectionProperties connectionProperties, String library) {
        checkNotNull(connectionProperties, "connectionProperties");
        checkNotNull(library, "library");
        deviceId = -1;
        msgId = -1;
        commsStarted = false;
        this.connectionProperties = connectionProperties;
        timeout = connectionProperties.getConnectTimeout();
        this.library = library;
    }

    @Override
    public void open(byte[] start, byte[] stop) {
        checkNotNull(start, "start");
        checkNotNull(stop, "stop");
        startRequest = start;
        stopRequest = stop;
        initJ2534(connectionProperties, library);
        LOGGER.info("J2534/ISO14230 connection initialised");
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

        try {
            if (pollState.getCurrentState() == PollingState.State.STATE_0) {
                api.writeMsg(channelId, request, timeout, TxFlags.NO_FLAGS);
            }
            api.readMsg(channelId, response, timeout);
        }
        catch (J2534Exception e) {
            LOGGER.error(String.format(
                    "J2534/ISO14230 Send/Receive error: %s", e.getMessage()));
        }

        if (pollState.getCurrentState() == PollingState.State.STATE_1){
            lastResponse = new byte[response.length];
            if ((response[0] + 2) == response.length
                &&  response[response.length - 1] == calculateChecksum(response)) {

                arraycopy(response, 0, lastResponse, 0, response.length);
            }
            else{
                LOGGER.error(String.format(
                        "J2534/ISO14230 Bad Data response: %s", asHex(response)));
                arraycopy(lastResponse, 0, response, 0, response.length);
                pollState.setNewQuery(true);
            }
        }
        currentPollState = pollState.getCurrentState();
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
        boolean repeat = true;
        if (currentPollState == PollingState.State.STATE_0) {
            // Slow poll, no need to send break and clear the line
            return;
        }
        while (repeat) {
            try {
                if (LOGGER.isDebugEnabled())
                    LOGGER.debug("J2534/ISO14230 sending line break");
                int p3_min = getP3Min();
                setP3Min(2);
                api.writeMsg(
                        channelId,
                        new byte[60],
                        0L,
                        TxFlags.WAIT_P3_MIN_ONLY);
                setP3Min(p3_min);
                api.clearBuffers(channelId);
            }
            catch (J2534Exception e) {
                // If get/set fails because the ECU is no longer responding, for
                // a variety of reasons, ignore it and close off the connection
                // cleanly
                LOGGER.error(String.format(
                        "J2534/ISO14230 Error performing get/set before clearing line: %s", e.getMessage()));
                continue;
            }
            boolean empty = false;
            int i = 1;
            do {
                byte[] badBytes = api.readMsg(channelId, 700L);
                if (badBytes.length > 0) {
                    if (LOGGER.isDebugEnabled())
                        LOGGER.debug(String.format(
                            "J2534/ISO14230 clearing line (stale data %d): %s", i, asHex(badBytes)));
                    empty = false;
                    i++;
                }
                else {
                    empty = true;
                    repeat = false;
                }
            } while (!empty && i <= 3);
        }
        try {
            // if we are clearing the line due to a comms error, re-init to continue logging
            fastInit();
        }
        catch (J2534Exception e) {
            // If fastInit fails because the ECU is no longer responding, for
            // a variety of reasons, ignore it and close off the connection
            // cleanly
            LOGGER.error(String.format(
                    "J2534/ISO14230 Error performing fast initialization after clearing line: %s", e.getMessage()));
        }
    }

    @Override
    public void close() {
        try {
            if (commsStarted) stopComms();
            commsStarted = false;
        }
        catch (J2534Exception e) {
            // If the stop command fails because the ECU is no longer responding, for
            // a variety of reasons, ignore it and close off the connection
            // cleanly
            LOGGER.error(String.format(
                    "J2534/ISO14230 Error stopping communications after clearing line: %s", e.getMessage()));
        }
        stopMsgFilter();
        disconnectChannel();
        closeDevice();
    }

    private void initJ2534(ConnectionProperties connectionProperties, String library) {
        api = new J2534Impl(Protocol.ISO14230, library);
        deviceId = api.open();
        try {
            version(deviceId);
            channelId = api.connect(
                    deviceId, Flag.ISO9141_NO_CHECKSUM.getValue(),
                    connectionProperties.getBaudRate());
            setConfig(channelId, (KwpConnectionProperties) connectionProperties);
            msgId = api.startPassMsgFilter(channelId, (byte) 0x00, (byte) 0x00);
            if (LOGGER.isDebugEnabled())
                LOGGER.debug(String.format(
                    "J2534/ISO14230 connection success: deviceId:%d, channelId:%d, msgId:%d, baud:%d",
                    deviceId, channelId, msgId, connectionProperties.getBaudRate()));
            fastInit();
        } catch (Exception e) {
            if (LOGGER.isDebugEnabled())
                LOGGER.debug(String.format(
                    "J2534/ISO14230 exception: deviceId:%d, channelId:%d, msgId:%d",
                    deviceId, channelId, msgId));
            close();
            throw new J2534Exception(String.format(
                    "J2534/ISO14230 Error opening device: %s",e.getMessage()), e);
        }
    }

    private void version(int deviceId) {
        final Version version = api.readVersion(deviceId);
        LOGGER.info(String.format(
                "J2534 Version => firmware: %s, dll: %s, api: %s",
                version.firmware, version.dll, version.api));
    }

    private void setConfig(int channelId, KwpConnectionProperties connectionProperties) {
        final ConfigItem p1Max = new ConfigItem(Config.P1_MAX.getValue(),
                (connectionProperties.getP1Max() * 2));
        final ConfigItem p3Min = new ConfigItem(Config.P3_MIN.getValue(),
                (connectionProperties.getP3Min() * 2));
        final ConfigItem p4Min = new ConfigItem(Config.P4_MIN.getValue(),
                (connectionProperties.getP4Min() * 2));
        final ConfigItem loopback = new ConfigItem(Config.LOOPBACK.getValue(),
                LOOPBACK);
        final ConfigItem dataBits = new ConfigItem(
                Config.DATA_BITS.getValue(),
                (connectionProperties.getDataBits() == 8 ? 0 : 1));
        final ConfigItem parity = new ConfigItem(
                Config.PARITY.getValue(),
                connectionProperties.getParity());
        api.setConfig(channelId, dataBits, parity, p1Max, p3Min, p4Min, loopback);
        if (LOGGER.isDebugEnabled())
            LOGGER.debug(String.format("J2534/ISO14230 connection properties: %s",
                connectionProperties.toString()));
    }

    private void stopMsgFilter() {
        if (msgId == -1) return;
        try {
            api.stopMsgFilter(channelId, msgId);
            if (LOGGER.isDebugEnabled())
                LOGGER.debug(String.format(
                    "J2534/ISO14230 stopped message filter:%s", msgId));
        } catch (Exception e) {
            LOGGER.warn(String.format(
                    "J2534/ISO14230 Error stopping msg filter: %s", e.getMessage()));
        }
    }

    private void disconnectChannel() {
        if (deviceId == -1) return;
        try {
            api.disconnect(channelId);
            if (LOGGER.isDebugEnabled())
                LOGGER.debug(String.format(
                    "J2534/ISO14230 disconnected channel:%d", channelId));
        } catch (Exception e) {
            LOGGER.warn(String.format(
                    "J2534/ISO14230 Error disconnecting channel: %s", e.getMessage()));
        }
    }

    private void closeDevice() {
        try {
            if (deviceId != -1) {
                api.close(deviceId);
                LOGGER.info(String.format(
                        "J2534/ISO14230 closed connection to device:%d", deviceId));
            }
        } catch (Exception e) {
            LOGGER.warn(String.format(
                    "J2534/ISO14230 Error closing device: %s", e.getMessage()));
        }
        finally {
            deviceId = -1;
        }
    }

    private void fastInit() {
        final byte[] timing = api.fastInit(channelId, startRequest);
        if (LOGGER.isDebugEnabled())
            LOGGER.debug(String.format(
                "J2534/ISO14230 Fast Init: deviceId:%d, channelId:%d, timing:%s",
                deviceId, channelId, asHex(timing)));
        if (timing.length < 1) {
            stopComms();
            return;
        }
        commsStarted = true;
    }

    private void stopComms() {
        final byte[] response = send(stopRequest);
        if (LOGGER.isDebugEnabled())
            LOGGER.debug(String.format("Stop comms Response = %s", asHex(response)));
    }

    private void setP3Min(int msec) {
        final ConfigItem p3_min = new ConfigItem(
                Config.P3_MIN.getValue(),
                msec);
        api.setConfig(channelId, p3_min);
        if (LOGGER.isTraceEnabled())
            LOGGER.trace(String.format("Config set P3_MIN value  = %d msec", msec / 2));
    }

    private int getP3Min() {
        final ConfigItem[] configs = api.getConfig(
                channelId,
                Config.P3_MIN.getValue());
        int i = 10;
        for (ConfigItem item : configs) {
            if (Config.get(item.parameter) == Config.P3_MIN) {
                i = item.value;
            }
        }
        if (LOGGER.isTraceEnabled())
            LOGGER.trace(String.format("Config get P3_MIN value  = %d msec", i / 2));
        return i;
    }
}
