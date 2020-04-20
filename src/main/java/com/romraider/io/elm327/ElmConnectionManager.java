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
    private ElmConnection connection;
    
    private static final Logger LOGGER = getLogger(ElmConnectionManager.class);
    private static int currentBaudrate = 38400;
    private final int baudrates[] = {currentBaudrate, 9600};    

    private int elmMode = 0;
    private String portName;

    //private final long timeout;
    //private long readTimeout;

    public ElmConnectionManager(String portName, ConnectionProperties connectionProperties) {
        checkNotNullOrEmpty(portName, "portName");
        checkNotNull(connectionProperties, "connectionProperties");
       
        this.portName = portName;
    	this.connection = new ElmConnection(this.portName, currentBaudrate);
       // this.connectionProperties = connectionProperties;
       // timeout = connectionProperties.getConnectTimeout();
       // readTimeout = timeout;
    }
    
    private int parseProtocolType(String protocol) {
    	String s = protocol.toLowerCase().trim();
    	
    	if(s.equals("automatic"))
	    		return 0;
    	else if(s.equals("saej1850-pwm"))
	    		return 1;
    	else if(s.equals("sae1850-vpw"))
	    		return 2;
	    else if(s.equals("iso9141-2"))
	    		return 3;
	    else if(s.equals("iso4230-4kwp-5"))
	    		return 4;
	    else if(s.equals("iso14230-4kwp-fast"))
	    		return 5;
	    else if(s.equals("iso15765") || s.equals("iso15765-4can11-500"))
	    		return 6;
	    else if(s.equals("iso15765-4can29-500"))
	    		return 7;
	    else if(s.equals("iso15765-4can11-250"))
	    		return 8;
	    else if(s.equals("iso15765-4can29-250"))
	    		return 9;
	    else if(s.equals("isoj1939-250"))
	    		return 10;	    			
	    else
	    	return -1;
    }
    
 
    public String getCurrentProtcol() {
        String result = "";
        
        result = sendAndWaitForNewLine("AT DP", 2500);               
        return result;
    }
    
    public byte[] getSupportedPids(byte[] responseBytes) {
    	 String result = "";
       
         //Check if ELM is even there          
         result = sendAndWaitForNewLine("01 00", 2500);    
         
         while(result.contains("SEARCHING")) {
        	 result = WaitForNewLine(2500);
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
    
    public boolean resetAndInit(String transportProtcol, String moduleString, String testerString) {

        elmMode = parseProtocolType(transportProtcol);
    	
        if(elmMode == -1) {
        	LOGGER.error("Unknown ELM Protocol, check xml for"
        			+ " available modes! Supplied mode: " + transportProtcol);
        	return false;
        }
        
        for(int baudrate: baudrates) {
            try {
            	if(this.connection != null) this.connection.close();
	        	this.connection = new ElmConnection(this.portName, baudrate);
	        	
	            String result = "";
	              
	            clearLine(); //Clear input buffer
	            send("");	//Clear buffer of ELM incase there is some data
	            
	            //Check if ELM is even there
	            result = sendAndWaitForResponse("ATZ", 2500, 15);
	
	            if(!result.contains("ELM327")) {
	            	LOGGER.error("Tried settings: " +  this.portName +
	            			", Baudrate: " + baudrate);
                	continue;
	            }
	            
	            result = result.replace(">", "");
	            LOGGER.info(result.trim()  + " found with Baudrate: " + baudrate + "!");
	            
	            //Set the correct protocol
	            result = sendAndWaitForNewLine("ATSP " + elmMode, 2500); 
	            
	            if(!result.contains("OK")) {
	            	LOGGER.error("ELM327 rejected Protocol Init!");
                	continue;
	            }
	            else
	            	LOGGER.debug("ELM327 accepted Protocol Init!");
	            
	            
	            LOGGER.info("Current Protocol: " + getCurrentProtcol().replace(">", "").trim());
	                       
	            String reqATSH = "";
	            
	            //If CAN Modes
	            if((elmMode >= 6 && elmMode <= 9) || elmMode > 10) {
	            	reqATSH = "ATSH " + testerString;
	            }
	            else {
	            	//Also set init address for K-Line
	                result = sendAndWaitForNewLine("ATIIA " + moduleString, 2500);
	                
	                if(!result.contains("OK")) {
	                	LOGGER.error("ELM327 rejected ATIIA Init (Only K-Line)!");
	                	continue;
	                }
	                else
	                	Log.debug("ELM327 accepted ATIIA Init!");
	                
	            	
	                reqATSH = "ATSH 82" +  moduleString + " " +  testerString; 
	            }
	            
	            
	            result = sendAndWaitForNewLine(reqATSH, 2500);
	            
	            if(!result.contains("OK")) {
	            	LOGGER.error("ELM327 rejected ATSH Init!");
                	continue;
	            }
	            else
	            	Log.debug("ELM327 accepted ATSH Init!");
	            
	            
	            currentBaudrate = baudrate;
	            return true;
            
	        } catch (Exception e) {
	            throw new SerialCommunicationException(e);
	        }
        }
        
        return false;
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
		throw new java.lang.UnsupportedOperationException("Send does not work"
				+ " with bytes on ELM Connection");
		
	}

	@Override
	public byte[] send(byte[] bytes) {
		throw new java.lang.UnsupportedOperationException("Send does not work"
				+ " with bytes on ELM Connection");
	}
}
