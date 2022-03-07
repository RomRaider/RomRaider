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

package com.romraider.io.serial.connection;

import static com.romraider.io.protocol.ssm.iso9141.SSMChecksumCalculator.calculateChecksum;
import static com.romraider.util.HexUtil.asHex;
import static com.romraider.util.ParamChecker.checkNotNull;
import static com.romraider.util.ParamChecker.checkNotNullOrEmpty;
import static com.romraider.util.ThreadUtil.sleep;
import static java.lang.System.arraycopy;
import static java.lang.System.currentTimeMillis;
import static org.apache.log4j.Logger.getLogger;

import org.apache.log4j.Logger;

import com.romraider.io.connection.ConnectionManager;
import com.romraider.io.connection.ConnectionProperties;
import com.romraider.logger.ecu.comms.manager.PollingState;

public final class SerialConnectionManager implements ConnectionManager {
    private static final Logger LOGGER = getLogger(SerialConnectionManager.class);
    private final SerialConnection connection;
    private final ConnectionProperties connectionProperties;
    private byte[] lastResponse;
    private final long timeout;
    private long readTimeout;

    public SerialConnectionManager(String portName, ConnectionProperties connectionProperties) {
        checkNotNullOrEmpty(portName, "portName");
        checkNotNull(connectionProperties, "connectionProperties");
        this.connectionProperties = connectionProperties;
        timeout = connectionProperties.getConnectTimeout();
        readTimeout = timeout;
        // Use TestSerialConnection for testing!!
        connection = new SerialConnectionImpl(portName, connectionProperties);
        //connection = new TestSerialConnection2(portName, connectionProperties);
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
            connection.readStaleData();
            connection.write(request);
        }
        while (connection.available() < response.length) {
            sleep(1);
            readTimeout -= 1;
            if (readTimeout <= 0) {
                byte[] badBytes = connection.readAvailable();
                if (LOGGER.isDebugEnabled())
                    LOGGER.debug("Serial Bad Read response (read timeout): " + asHex(badBytes));
                return; // this will reinitialize the connection
            }
        }
        readTimeout = timeout;
        connection.read(response);

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
                LOGGER.error("Serial Bad Data response: " + asHex(response));
                arraycopy(lastResponse, 0, response, 0, response.length);
                pollState.setNewQuery(true);
            }
        }
    }

    // Send request and wait specified time for response with unknown length
    @Override
    public byte[] send(byte[] bytes) {
        checkNotNull(bytes, "bytes");
        if (LOGGER.isTraceEnabled())
            LOGGER.trace("Reading stale data");
        connection.readStaleData();
        if (LOGGER.isTraceEnabled())
            LOGGER.trace("Writing bytes");
        connection.write(bytes);
        int available = 0;
        boolean keepLooking = true;
        long lastChange = currentTimeMillis();
        while (keepLooking) {
            sleep(2);
            if (connection.available() != available) {
                available = connection.available();
                lastChange = currentTimeMillis();
            }
            keepLooking = (currentTimeMillis() - lastChange) < timeout;
        }
        return connection.readAvailable();
    }

    @Override
    public void clearLine() {
        int duration = (10000 / connectionProperties.getBaudRate()) *
                        (connectionProperties.getDataBits() +
                         connectionProperties.getStopBits() +
                         connectionProperties.getParity() + 1);
        if (LOGGER.isDebugEnabled())
            LOGGER.debug("Serial sending line break of duration: " + duration + " msec");
        connection.sendBreak(duration);
        do {
            sleep(2);
            byte[] badBytes = connection.readAvailable();
            if (LOGGER.isDebugEnabled())
                LOGGER.debug("Serial clearing line (stale data): " + asHex(badBytes));
            sleep(10);
        } while (connection.available() > 0 );
    }

    @Override
    public void close() {
        connection.close();
    }
}
