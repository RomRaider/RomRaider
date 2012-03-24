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

package com.romraider.io.serial.connection;

import static com.romraider.io.protocol.ssm.SSMChecksumCalculator.calculateChecksum;
import com.romraider.io.connection.ConnectionManager;
import com.romraider.io.connection.ConnectionProperties;
import com.romraider.logger.ecu.comms.manager.PollingState;

import static com.romraider.util.HexUtil.asHex;
import static com.romraider.util.ParamChecker.checkNotNull;
import static com.romraider.util.ParamChecker.checkNotNullOrEmpty;
import static com.romraider.util.ThreadUtil.sleep;
import static java.lang.System.arraycopy;
import static java.lang.System.currentTimeMillis;
import org.apache.log4j.Logger;
import static org.apache.log4j.Logger.getLogger;

public final class SerialConnectionManager implements ConnectionManager {
    private static final Logger LOGGER = getLogger(SerialConnectionManager.class);
    private final SerialConnection connection;
    private final ConnectionProperties connectionProperties;
    private byte[] lastResponse;

    public SerialConnectionManager(String portName, ConnectionProperties connectionProperties) {
        checkNotNullOrEmpty(portName, "portName");
        checkNotNull(connectionProperties, "connectionProperties");
        this.connectionProperties = connectionProperties;
        // Use TestSerialConnection for testing!!
        connection = new SerialConnectionImpl(portName, connectionProperties);
//        connection = new TestSerialConnection2(portName, connectionProperties);
        LOGGER.info("Serial connection initialised");
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
           	connection.readStaleData();
            connection.write(request);
        }
        while (connection.available() < response.length) {
            sleep(1);
            timeout -= 1;
            if (timeout <= 0) {
                byte[] badBytes = connection.readAvailable();
                LOGGER.debug("SSM Bad read response (read timeout): " + asHex(badBytes));
                return; // this will reinitialize the connection
            }
        }
        connection.read(response);

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
                LOGGER.error("SSM Bad Data response: " + asHex(response));
	        	arraycopy(lastResponse, 0, response, 0, response.length);
	        	pollState.setNewQuery(true);
	        }
        }
    }

    // Send request and wait specified time for response with unknown length
    public byte[] send(byte[] bytes, long maxWait) {
        checkNotNull(bytes, "bytes");
        connection.readStaleData();
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
            keepLooking = (currentTimeMillis() - lastChange) < maxWait;
        }
        return connection.readAvailable();
    }

    public void clearLine() {
    	LOGGER.debug("SSM sending line break");
        connection.sendBreak( 1 / 
        		(connectionProperties.getBaudRate() *
        		(connectionProperties.getDataBits() +
        		 connectionProperties.getStopBits() +
        		 connectionProperties.getParity() + 1)));
        do {
        	sleep(2);
            byte[] badBytes = connection.readAvailable();
            LOGGER.debug("SSM clearing line (stale data): " + asHex(badBytes));
            sleep(10);
        } while (connection.available() > 0 ); 
    }
    
    public void close() {
        connection.close();
    }
}
