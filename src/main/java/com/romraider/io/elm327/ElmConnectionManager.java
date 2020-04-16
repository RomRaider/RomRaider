/*
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2016 RomRaider.com
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

package com.romraider.io.elm327;

import static com.romraider.util.ParamChecker.checkNotNull;
import static com.romraider.util.ParamChecker.checkNotNullOrEmpty;
import static com.romraider.util.ThreadUtil.sleep;

import static java.lang.System.currentTimeMillis;
import static org.apache.log4j.Logger.getLogger;

import org.apache.log4j.Logger;
import org.jfree.util.Log;

import com.romraider.io.connection.ConnectionManager;
import com.romraider.io.connection.ConnectionProperties;

import com.romraider.logger.ecu.comms.manager.PollingState;
import com.romraider.logger.ecu.exception.SerialCommunicationException;

public final class ElmConnectionManager implements ConnectionManager {
    private static final Logger LOGGER = getLogger(ElmConnectionManager.class);
    private final ElmConnection connection;
    private final ConnectionProperties connectionProperties;
    private final long timeout;
    private long readTimeout;

    public ElmConnectionManager(String portName, ConnectionProperties connectionProperties) {
        checkNotNullOrEmpty(portName, "portName");
        checkNotNull(connectionProperties, "connectionProperties");
       
        this.connectionProperties = connectionProperties;
        timeout = connectionProperties.getConnectTimeout();
        readTimeout = timeout;
        // Use TestSerialConnection for testing!!
        connection = new ElmConnection(portName, connectionProperties);
        //connection = new TestSerialConnection2(portName, connectionProperties);
    }


    
    public String getCurrentProtcol() {
        String result = "";
        
        result = sendAndWaitForResponse("AT DP", 3500, 10);               
        return result;
    }
    
    public byte[] getSupportedPids(byte[] responseBytes) {
    	 String result = "";
       
         //Check if ELM is even there          
         result = sendAndWaitForNewLine("01 00", 2500);               
         while(result.contains("SEARCHING")) {
        	 result = WaitForNewLine(2500);
        	 sleep(500);
         }
         
         LOGGER.info("Supported Pids:" +  result);
         
         String[] splitLines = result.split("\r");
         
         for(String s : splitLines) {
        	 if(s.length() < 2) continue; 
        	 
             String[] splitBytes = s.split(" ");
             for(int i = 0; i < splitBytes.length; i++) {
            	 try {
            		 responseBytes[i] = (byte) Integer.parseInt(splitBytes[i],16);
            	 }
            	 catch(NumberFormatException e) {
            		 throw e;
            	 }
	         }
         }
         
         return responseBytes;
    }
    
    public boolean resetAndInit(String transportProtcol, String module, String tester) {
        try {

            String result = "";
            
            //Check if ELM is even there          
            result = sendAndWaitForResponse("ATZ", 2500, 15);               
            if(!result.contains("ELM327")) {
                return false;
            }
            
            LOGGER.info("ELM327 found: " + result);
            
            //Set the correct protocol
            result = sendAndWaitForResponse("ATSP 6", 2500,2); 
            
            if(!result.contains("OK")) {
            	Log.info("ELM327 rejected Protocol Init!");
            	return false;
            }
            else
            	Log.info("ELM327 accepted Protocol Init!");
            
            
            //Set the correct module and tester IDs
            //String req = "ATSH 68 " + module + " " + tester;
            /*
            String req = "ATSH 68 7EA F1";
            sendAndWaitForResponse(req, 2500);
            result = connection.readAvailable();
            
            if(!result.contains("OK")) {
            	Log.error("ELM327 rejected ATSH Init!");
            	return false;
            }
            else
            	Log.info("ELM327 accepted ATSH Init!");*/
            
            return true;
            
        } catch (Exception e) {
            throw new SerialCommunicationException(e);
        }
    }
    
    // Send request 
    public void send(String command) {
        checkNotNull(command, "bytes");
        connection.readStaleData();
        connection.write(command);   
    }
    
    // Send request and wait specified time for response with unknown length
    public String sendAndWaitForResponse(String command, int timeout) {
    	return sendAndWaitForResponse(command,timeout,1);
    }
    
    // Send request and wait specified time for response with exact length
    public String sendAndWaitForNewLine(String command, int timeout) {
        connection.readStaleData();
        connection.write(command);
        long lastChange = currentTimeMillis();
        
        String response = "";
        while(!response.contains("\n") && !response.contains("\r")) {
        	sleep(2);
            response += connection.readAvailable();
        	if(currentTimeMillis() - lastChange > timeout) break;    	
        }
        
        return response;
    }
    
    // Send request and wait specified time for response with exakt length
    public String sendAndWaitForResponse(String command, int timeout, int length) {
        connection.readStaleData();
        connection.write(command);
        long lastChange = currentTimeMillis();
        
        String response = "";
        while(response.length() < length) {
        	sleep(2);
            response += connection.readAvailable();
        	if(currentTimeMillis() - lastChange > timeout) break;    	
        }
        
        return response;
    }
    
    // Send request and wait specified time for response with exakt length
    public String WaitForResponse(int timeout, int length) {

        long lastChange = currentTimeMillis();
        
        String response = "";
        while(response.length() < length) {
        	sleep(2);
            response += connection.readAvailable();
        	if(currentTimeMillis() - lastChange > timeout) break;    	
        }
        
        return response;
    }
    
    // Send request and wait specified time for response with exact length
    public String WaitForNewLine(int timeout) {

        long lastChange = currentTimeMillis();
        
        String response = "";
        while(!response.contains("\n") && !response.contains("\r")) {
        	sleep(2);
            response += connection.readAvailable();
        	if(currentTimeMillis() - lastChange > timeout) break;    	
        }
        
        return response;
    }

    @Override
    public void clearLine() {
    	connection.readStaleData();
    }

    @Override
    public void close() {
        connection.close();
    }

	@Override
	public void send(byte[] request, byte[] response, PollingState pollState) {
		throw new java.lang.UnsupportedOperationException("Send does not work with bytes on ELM Connection");
		
	}

	@Override
	public byte[] send(byte[] bytes) {
		throw new java.lang.UnsupportedOperationException("Send does not work with bytes on ELM Connection");
	}
}
