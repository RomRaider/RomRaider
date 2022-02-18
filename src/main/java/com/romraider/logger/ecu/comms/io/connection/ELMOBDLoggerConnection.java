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

package com.romraider.logger.ecu.comms.io.connection;

import static com.romraider.util.ParamChecker.checkNotNull;

import static org.apache.log4j.Logger.getLogger;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import com.romraider.Settings;
import com.romraider.util.SettingsManager;
import com.romraider.io.elm327.ElmConnectionManager;
import com.romraider.io.elm327.ElmConnectionManager.ERROR_TYPE;
import com.romraider.io.protocol.ProtocolFactory;
import com.romraider.logger.ecu.comms.io.protocol.LoggerProtocolOBD;
import com.romraider.logger.ecu.comms.manager.PollingState;
import com.romraider.logger.ecu.comms.query.EcuInitCallback;
import com.romraider.logger.ecu.comms.query.EcuQuery;
import com.romraider.logger.ecu.definition.Module;
import com.romraider.logger.ecu.exception.SerialCommunicationException;

public final class ELMOBDLoggerConnection implements LoggerConnection {
    private static final Logger LOGGER = getLogger(OBDLoggerConnection.class);
    private final LoggerProtocolOBD protocol;
    private final ElmConnectionManager manager;
    private Collection<EcuQuery> obdQueries = new ArrayList<EcuQuery>();
    final Settings settings = SettingsManager.getSettings();

    public ELMOBDLoggerConnection(ElmConnectionManager manager) {
        checkNotNull(manager, "manager");
        this.manager = manager;
        this.protocol = (LoggerProtocolOBD) ProtocolFactory.getProtocol("OBD", "iso15765");
    }

    @Override
    public void open(Module module) {
    }

    @Override
    // TODO:
    public void ecuReset(Module module, int resetCode) {
    }


    @Override
    public void ecuInit(EcuInitCallback callback, Module module) {
    	String moduleStr =  concatBytes(module.getAddress());
    	String testerStr =  concatBytes(module.getTester());

    	ERROR_TYPE result = manager.resetAndInit(settings.getTransportProtocol(),
    			moduleStr, testerStr);

    	if(result == ERROR_TYPE.ELM_NOT_FOUND) {
    		throw new SerialCommunicationException("ELM327 was not found!");
    	}
    	else  if (result == ERROR_TYPE.ECU_NOT_FOUND) {
    		throw new SerialCommunicationException("ELM327 was found,"
    				+ " but no response from ECU!");
    	}
    	else if(result == ERROR_TYPE.UNKNOWN_PROTOCOL) {
    		throw new SerialCommunicationException("Unknown ELM Protocol, check xml for"
    				+ " available modes!");
    	}
    	else if(result == ERROR_TYPE.ELM_REJECTED_REQUEST) {
    		throw new SerialCommunicationException("ELM rejected request!");
    	}

    }

    private String concatBytes(final byte[] bytes) {
        final StringBuilder sb = new StringBuilder();
        boolean foundData = false;

        for (byte b : bytes) {
        	if(b!= 0) foundData = true;

        	if(foundData)
        		sb.append(String.format("%02X", b & 0xFF)); //Unsigned
        }

        String finalS = sb.toString();
        if(finalS.startsWith("0")) finalS = finalS.replaceFirst("0", "");

        return finalS;
    }


    @Override
    //TODO: CAN supports requesting multiple PIDs at once
    //This still has to be implemented
    public final void sendAddressReads(Collection<EcuQuery> queries, Module module,
    		PollingState pollState) {

        final int obdQueryListLength = queries.size();
        for (int i = 0; i < obdQueryListLength; i++) {

        	EcuQuery query = ((ArrayList<EcuQuery>) queries).get(i);
            obdQueries.add(query);

            final byte[] request = protocol.constructReadAddressRequest(module, obdQueries);
            String reqStr = String.format("%02X %02X", (int)request[4], (int)request[5]);
            if (LOGGER.isTraceEnabled())
                LOGGER.trace("Request: " + reqStr);
            String result = manager.sendAndWaitForChar(reqStr, 2500, ">");
            if (LOGGER.isTraceEnabled())
                LOGGER.trace("ELM: " + result);

           	if(result.contains("BUS INIT"))
           	{
           		LOGGER.warn("ELM 327 still initializing bus while querying!");
           		continue;
           	}
           	else if(result.contains("STOPPED")) {
            	LOGGER.warn("ELM327 stopped trying to connect to the ECU!");
            	continue;
            }
           	else if(result.contains("NO DATA")) {
            	LOGGER.warn("ELM327 received no response from ECU!");
            	continue;
            }

           	boolean found = false;

           	String[] resultSplit = result.split("\r");

           	for(String s : resultSplit) {
		        String[] bytesSplit = s.split(" ");

		        for(int j = 0; j < bytesSplit.length; j++) {

		        	if(bytesSplit[j].equals(String.format("%02X",(int)request[5]))) {

			            byte[] response = new byte[bytesSplit.length - j - 1];

			            for(int k = 0; k < response.length; k++) {
			            		response[k] = (byte) Integer.parseInt(bytesSplit[k + j + 1], 16);
			            }

			            query.setResponse(response);
			            found = true;
			            break;
		            }
		        }

		        if(found) break;
           	}

            obdQueries.clear();
        }
    }

    @Override
    public void clearLine() {
        manager.clearLine();
    }

    @Override
    public void close() {
        manager.close();
    }

    @Override
    public void sendAddressWrites(Map<EcuQuery, byte[]> writeQueries, Module module) {
        throw new UnsupportedOperationException();
    }
}
