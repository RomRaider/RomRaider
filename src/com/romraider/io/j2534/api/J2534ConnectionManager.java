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

import com.romraider.io.connection.ConnectionManager;
import com.romraider.io.connection.ConnectionProperties;
import com.romraider.io.j2534.op20.Old_J2534OpenPort20;
import com.romraider.logger.ecu.comms.manager.PollingState;

import static com.romraider.io.j2534.op20.OpenPort20.CONFIG_LOOPBACK;
import static com.romraider.io.j2534.op20.OpenPort20.CONFIG_P1_MAX;
import static com.romraider.io.j2534.op20.OpenPort20.CONFIG_P3_MIN;
import static com.romraider.io.j2534.op20.OpenPort20.CONFIG_P4_MIN;
import static com.romraider.io.j2534.op20.OpenPort20.FLAG_ISO9141_NO_CHECKSUM;
import static com.romraider.io.j2534.op20.OpenPort20.PROTOCOL_ISO9141;
import static com.romraider.io.protocol.ssm.SSMChecksumCalculator.calculateChecksum;
import static com.romraider.util.HexUtil.asHex;
import static com.romraider.util.ParamChecker.checkNotNull;

import org.apache.log4j.Logger;

import static java.lang.System.arraycopy;
import static org.apache.log4j.Logger.getLogger;

public final class J2534ConnectionManager implements ConnectionManager {
    private static final Logger LOGGER = getLogger(J2534ConnectionManager.class);
    private final J2534 api = new Old_J2534OpenPort20(PROTOCOL_ISO9141);
    //        private final J2534 api = new J2534OpenPort20(PROTOCOL_ISO9141);
    //    private final J2534 api = proxy(new Old_J2534OpenPort20(PROTOCOL_ISO9141), TimerWrapper.class);
    //    private final J2534 api = proxy(new J2534OpenPort20(PROTOCOL_ISO9141), TimerWrapper.class);
    private int channelId;
    private int deviceId;
    private int msgId;
    private byte[] lastResponse;

    public J2534ConnectionManager(ConnectionProperties connectionProperties) {
        checkNotNull(connectionProperties, "connectionProperties");
        initJ2534(connectionProperties.getBaudRate());
        LOGGER.info("J2534 connection initialised");
    }

    // Send request and wait for response with known length
    public void send(byte[] request, byte[] response, long timeout, PollingState pollState) {
        checkNotNull(request, "request");
        checkNotNull(response, "response");
        checkNotNull(pollState, "pollState");

        if (pollState.getCurrentState() == 0 && pollState.getLastState() == 1) {
        	clearLine();
        }

        if (pollState.getCurrentState() == 0) {
	        // FIX - should timeout be connectionProperties.getReadTimeout() ??
	        api.writeMsg(channelId, request, timeout);
        }
        api.readMsg(channelId, response, timeout);

        if (pollState.getCurrentState() == 1){
	        if (    response[0] == (byte) 0x80
	        	&&  response[1] == (byte) 0xF0
	        	&& (response[2] == (byte) 0x10 || response[2] == (byte) 0x18)
	        	&&  response[3] == (response.length - 5)
	        	&&  response[response.length - 1] == calculateChecksum(response)) {
	
	        	lastResponse = new byte[response.length];
	        	arraycopy(response, 0, lastResponse, 0, response.length);
	        }
	        else{
                LOGGER.error("J2534 Bad Data response: " + asHex(response));
	        	arraycopy(lastResponse, 0, response, 0, response.length);
	        	pollState.setNewQuery(true);
	        }
        }
    }

    // Send request and wait specified time for response with unknown length
    public byte[] send(byte[] request, long maxWait) {
        checkNotNull(request, "request");
        // FIX - should maxWait be connectionProperties.getReadTimeout() ??
        api.writeMsg(channelId, request, maxWait);
        return api.readMsg(channelId, maxWait);
    }

	public void clearLine() {
    	LOGGER.debug("J2534 sending line break");
    	api.writeMsg(channelId, new byte[] {0,0,0,0,0,0,0,0,0,0}, 100L);
    	boolean empty = false;
        do {
            byte[] badBytes = api.readMsg(channelId, 100L);
            if (badBytes.length > 0) {
            	LOGGER.debug("J2534 clearing line (stale data): " + asHex(badBytes));
            	empty = false;
            }
            else {
            	empty = true;
            }
        } while (!empty ); 
	}

	public void close() {
        stopMsgFilter();
        disconnectChannel();
        closeDevice();
        resetHandles();
        LOGGER.info("J2534 connection closed");
    }

    private void initJ2534(int baudRate) {
        deviceId = api.open();
        try {
            version(deviceId);
            channelId = api.connect(deviceId, FLAG_ISO9141_NO_CHECKSUM, baudRate);
            setConfig(channelId);
            msgId = api.startPassMsgFilter(channelId, (byte) 0x00, (byte) 0x00);
        } catch (Exception e) {
            close();
            throw new J2534Exception("J2534 Error opening device: " + e.getMessage(), e);
        }
    }

    private void version(int deviceId) {
        if (!LOGGER.isDebugEnabled()) return;
        Version version = api.readVersion(deviceId);
        LOGGER.info("J2534 Version => firmware: " + version.firmware + ", dll: " + version.dll + ", api: " + version.api);
    }

    private void setConfig(int channelId) {
        ConfigItem p1Max = new ConfigItem(CONFIG_P1_MAX, 2);
        ConfigItem p3Min = new ConfigItem(CONFIG_P3_MIN, 0);
        ConfigItem p4Min = new ConfigItem(CONFIG_P4_MIN, 0);
        ConfigItem loopback = new ConfigItem(CONFIG_LOOPBACK, 1);
        api.setConfig(channelId, p1Max, p3Min, p4Min, loopback);
    }

    private void stopMsgFilter() {
        try {
            if (channelId > 0 && msgId > 0) api.stopMsgFilter(channelId, msgId);
        } catch (Exception e) {
            LOGGER.warn("J2534 Error stopping msg filter: " + e.getMessage());
        }
    }

    private void disconnectChannel() {
        try {
            if (channelId > 0) api.disconnect(channelId);
        } catch (Exception e) {
            LOGGER.warn("J2534 Error disconnecting channel: " + e.getMessage());
        }
    }

    private void closeDevice() {
        try {
            if (deviceId > 0) api.close(deviceId);
        } catch (Exception e) {
            LOGGER.warn("J2534 Error closing device: " + e.getMessage());
        }
    }

    private void resetHandles() {
        channelId = 0;
        deviceId = 0;
        msgId = 0;
    }
}