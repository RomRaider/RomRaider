/*
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2014 RomRaider.com
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

import static com.romraider.util.ParamChecker.checkNotNull;
import static org.apache.log4j.Logger.getLogger;

import org.apache.log4j.Logger;

import com.romraider.io.connection.ConnectionManager;
import com.romraider.io.connection.ConnectionProperties;
import com.romraider.io.j2534.api.J2534Impl.Config;
import com.romraider.io.j2534.api.J2534Impl.Protocol;
import com.romraider.io.j2534.api.J2534Impl.TxFlags;
import com.romraider.logger.ecu.comms.manager.PollingState;
import com.romraider.util.SettingsManager;

public final class J2534ConnectionISO15765 implements ConnectionManager {
    private static final Logger LOGGER = getLogger(J2534ConnectionISO15765.class);
    private J2534 api;
    private int channelId;
    private int deviceId;
    private int msgId;
    private final long timeout;

    public J2534ConnectionISO15765(
            ConnectionProperties connectionProperties,
            String library) {

        api = null;
        timeout = 2000;
        initJ2534(500000, library);
        LOGGER.info("J2534/ISO15765 connection initialized");
    }

    // Send request and wait for response with known length
    @Override
    public void send(byte[] request, byte[] response, PollingState pollState) {
        checkNotNull(request, "request");
        checkNotNull(response, "response");
        checkNotNull(pollState, "pollState");
        pollState.setFastPoll(false);
        pollState.setCurrentState(0);
        api.writeMsg(channelId, request, timeout, TxFlags.ISO15765_FRAME_PAD);
        final byte[] readMsg = api.readMsg(channelId, 1, timeout);
        System.arraycopy(readMsg, 0, response, 0, readMsg.length) ;
    }

    // Send request and wait specified time for one response with unknown length
    @Override
    public byte[] send(byte[] request) {
        checkNotNull(request, "request");
        api.writeMsg(channelId, request, timeout, TxFlags.ISO15765_FRAME_PAD);
        return api.readMsg(channelId, 1, timeout);
    }

    @Override
    public void clearLine() {
        //        LOGGER.debug("J2534/ISO15765 clearing buffers");
        //        api.clearBuffers(channelId);
    }

    @Override
    public void close() {
        stopFcFilter();
        disconnectChannel();
        closeDevice();
    }

    private void initJ2534(int baudRate, String library) {
        api = new J2534Impl(Protocol.ISO15765, library);
        deviceId = api.open();
        try {
            version(deviceId);
            channelId = api.connect(deviceId, 0, baudRate);
            setConfig(channelId);

            final byte[] mask = {
                    (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff};
            final byte[] pattern =
                    SettingsManager.getSettings().getDestinationTarget().getAddress();
            final byte[] flowCntrl =
                    SettingsManager.getSettings().getDestinationTarget().getTester();

            msgId = api.startFlowCntrlFilter(
                    channelId, mask, pattern,
                    flowCntrl, TxFlags.ISO15765_FRAME_PAD);

            LOGGER.debug(String.format(
                    "J2534/ISO15765 success: deviceId:%d, channelId:%d, msgId:%d",
                    deviceId, channelId, msgId));
        }
        catch (Exception e) {
            LOGGER.debug(String.format(
                    "J2534/ISO15765 exception: deviceId:%d, channelId:%d, msgId:%d",
                    deviceId, channelId, msgId));
            close();
            throw new J2534Exception("J2534/ISO15765 Error opening device: " +
                    e.getMessage(), e);
        }
    }

    private void version(int deviceId) {
        if (!LOGGER.isDebugEnabled()) return;
        final Version version = api.readVersion(deviceId);
        LOGGER.info("J2534 Version => firmware: " + version.firmware +
                ", dll: " + version.dll + ", api: " + version.api);
    }

    private void setConfig(int channelId) {
        final ConfigItem loopback = new ConfigItem(Config.LOOPBACK.getValue(), 0);
        final ConfigItem bs = new ConfigItem(Config.ISO15765_BS.getValue(), 0);
        final ConfigItem stMin = new ConfigItem(Config.ISO15765_STMIN.getValue(), 0);
        final ConfigItem bs_tx = new ConfigItem(Config.BS_TX.getValue(), 0xffff);
        final ConfigItem st_tx = new ConfigItem(Config.STMIN_TX.getValue(), 0xffff);
        final ConfigItem wMax = new ConfigItem(Config.ISO15765_WFT_MAX.getValue(), 0);
        api.setConfig(channelId, loopback, bs, stMin, bs_tx, st_tx, wMax);
    }

    private void stopFcFilter() {
        try {
            api.stopMsgFilter(channelId, msgId);
            LOGGER.debug("J2534/ISO15765 stopped message filter:" + msgId);
        } catch (Exception e) {
            LOGGER.warn("J2534/ISO15765 Error stopping msg filter: " +
                    e.getMessage());
        }
    }

    private void disconnectChannel() {
        try {
            api.disconnect(channelId);
            LOGGER.debug("J2534/ISO15765 disconnected channel:" + channelId);
        } catch (Exception e) {
            LOGGER.warn("J2534/ISO15765 Error disconnecting channel: " +
                    e.getMessage());
        }
    }

    private void closeDevice() {
        try {
            api.close(deviceId);
            LOGGER.info("J2534/ISO15765 closed connection to device:" + deviceId);
        } catch (Exception e) {
            LOGGER.warn("J2534/ISO15765 Error closing device: " + e.getMessage());
        }
    }
}
