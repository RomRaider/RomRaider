/*
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2020 RomRaider.com
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

import static com.romraider.util.HexUtil.asHex;
import static com.romraider.util.ParamChecker.checkNotNull;
import static org.apache.log4j.Logger.getLogger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.apache.log4j.Logger;

import com.romraider.Settings;
import com.romraider.util.SettingsManager;
import com.romraider.io.elm327.ElmConnectionManager;
import com.romraider.io.protocol.ProtocolFactory;
import com.romraider.logger.ecu.comms.io.protocol.LoggerProtocolOBD;
import com.romraider.logger.ecu.comms.manager.PollingState;
import com.romraider.logger.ecu.comms.manager.PollingStateImpl;
import com.romraider.logger.ecu.comms.query.EcuInitCallback;
import com.romraider.logger.ecu.comms.query.EcuQuery;
import com.romraider.logger.ecu.definition.Module;

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
    public void ecuReset(Module module, int resetCode) {
        byte[] request = protocol.constructEcuResetRequest(module, resetCode);
        LOGGER.debug(String.format("%s Reset Request  ---> %s",
                module, asHex(request)));
        byte[] response = manager.send(request);
        byte[] processedResponse = protocol.preprocessResponse(
                request, response, new PollingStateImpl());
        LOGGER.debug(String.format("%s Reset Response <--- %s",
                module, asHex(processedResponse)));
        protocol.processEcuResetResponse(processedResponse);
    }

    @Override
    public void ecuInit(EcuInitCallback callback, Module module) {
    	manager.resetAndInit(settings.getTransportProtocol(), ""+(int)module.getAddress()[0], ""+(int)module.getTester()[0]);
    	//manager.getSupportedPids(processedResponse);//protocol.preprocessResponse(request, tmp, new PollingStateImpl());
    }

    @Override
    public final void sendAddressReads(Collection<EcuQuery> queries,Module module, PollingState pollState) {

        final int obdQueryListLength = queries.size();
        for (int i = 0; i < obdQueryListLength; i++) {
            
        	EcuQuery query = ((ArrayList<EcuQuery>) queries).get(i);
            obdQueries.add(query);
   	
            final byte[] request = protocol.constructReadAddressRequest(module, obdQueries);
                                   
            String result = manager.sendAndWaitForNewLine(String.format("%02X %02X", request[4], request[5]), 2500);
            
            if(result.length() > 4) {           
            	
            	result = result.substring(0, result.length() - 3); //remove \r\r>
	            String[] resultSplit = result.split(" ");
	            
	            byte[] response = new byte[resultSplit.length - 4];
	            
	            //Skip header (4 Bytes)
	            for(int j = 4; j < resultSplit.length; j++) {
	            	try {
	            		response[j - 4] = (byte) Integer.parseInt(resultSplit[j], 16);
	            	}
	            	catch(NumberFormatException e) {
	            		throw e;
	            	}
	            }
	            	            
	            query.setResponse(response);           	
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
