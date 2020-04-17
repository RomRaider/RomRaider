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

import static com.romraider.util.ParamChecker.checkNotNull;

import static org.apache.log4j.Logger.getLogger;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import com.romraider.Settings;
import com.romraider.util.SettingsManager;
import com.romraider.io.elm327.ElmConnectionManager;
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
    public void ecuReset(Module module, int resetCode) {
    	/*
        byte[] request = protocol.constructEcuResetRequest(module, resetCode);
        LOGGER.debug(String.format("%s Reset Request  ---> %s", module, asHex(request)));
        byte[] response = manager.send(request);
        byte[] processedResponse = protocol.preprocessResponse( request, response, new PollingStateImpl());
        LOGGER.debug(String.format("%s Reset Response <--- %s",module, asHex(processedResponse)));
        protocol.processEcuResetResponse(processedResponse);*/
    }

    @Override
    public void ecuInit(EcuInitCallback callback, Module module) {
    	String moduleStr =  concatBytes(module.getAddress());   
    	String testerStr =  concatBytes(module.getTester());   
    	
    	boolean result = manager.resetAndInit(settings.getTransportProtocol(), moduleStr, testerStr);
    	
    	if(!result) {
    		throw new SerialCommunicationException("ELM327 was not found!");
    	}
    	
    }
    
    private String concatBytes(final byte[] bytes) {
        final StringBuilder sb = new StringBuilder();
        boolean foundData = false;
        
        for (byte b : bytes) {
        	if(b!= 0) foundData = true;
        	
        	if(foundData)
        		sb.append(String.format("%02X", (int)b & 0xFF)); //Unsigned
        }
        
        String finalS = sb.toString();
        if(finalS.startsWith("0")) finalS = finalS.replaceFirst("0", "");
        
        return finalS;
    }
    

    @Override
    public final void sendAddressReads(Collection<EcuQuery> queries, Module module, PollingState pollState) {

        final int obdQueryListLength = queries.size();
        for (int i = 0; i < obdQueryListLength; i++) {
            
        	EcuQuery query = ((ArrayList<EcuQuery>) queries).get(i);
            obdQueries.add(query);
   	
            final byte[] request = protocol.constructReadAddressRequest(module, obdQueries);
            String reqStr = String.format("%02X %02X", (int)request[4], (int)request[5]);
            String result = manager.sendAndWaitForNewLine(reqStr, 2500);
           	result = result.trim();
           	
           	String[] resultSplit = result.split("\r");
           	String moduleStr = concatBytes(module.getAddress());         	
           	         	
           	for(String s : resultSplit) {
           		
           		if(s.startsWith(moduleStr)) {
		            String[] bytesSplit = s.split(" ");
		            
		            //Skip header (3 Bytes plus Request PID)
		            if(bytesSplit.length > 4) {           	            	            
			            byte[] response = new byte[bytesSplit.length - 4];
			         
			            for(int j = 4; j < bytesSplit.length; j++) {
			            		response[j - 4] = (byte) Integer.parseInt(bytesSplit[j], 16);	
			            }
			            
			            LOGGER.debug("Response for Module " + moduleStr +  " " + response);
			            query.setResponse(response);           	
		            }
           		}
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
