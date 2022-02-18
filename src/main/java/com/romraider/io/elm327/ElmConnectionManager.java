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

package com.romraider.io.elm327;

import static com.romraider.util.ParamChecker.checkNotNull;
import static com.romraider.util.ParamChecker.checkNotNullOrEmpty;
import static com.romraider.util.ThreadUtil.sleep;

import static java.lang.System.currentTimeMillis;
import static org.apache.log4j.Logger.getLogger;

import org.apache.log4j.Logger;

import com.romraider.io.connection.ConnectionManager;
import com.romraider.io.connection.ConnectionProperties;

import com.romraider.logger.ecu.comms.manager.PollingState;
import com.romraider.logger.ecu.exception.SerialCommunicationException;

public final class ElmConnectionManager implements ConnectionManager {
    private ElmConnection connection;

    private static final Logger LOGGER = getLogger(ElmConnectionManager.class);
    private static int baudrate = 9600;

    private int elmMode = 0;
    private String portName;

    public static enum ERROR_TYPE{NO_ERROR, UNKNOWN_PROTOCOL, ELM_NOT_FOUND,
    	ELM_REJECTED_REQUEST, ECU_NOT_FOUND}

    //private final long timeout;
    //private long readTimeout;

    public ElmConnectionManager(String portName, ConnectionProperties connectionProperties) {
        checkNotNullOrEmpty(portName, "portName");
        checkNotNull(connectionProperties, "connectionProperties");

        this.portName = portName;
    	this.connection = new ElmConnection(this.portName, baudrate);
       // this.connectionProperties = connectionProperties;
       // timeout = connectionProperties.getConnectTimeout();
       // readTimeout = timeout;
    }

    @Override
    public void open(byte[] start, byte[] stop) {
    }

    public int getCurrentProtocolMode() {
    	return elmMode;
    }

    private int parseProtocolType(String protocol) {
    	String s = protocol.toLowerCase().trim();

    	if(s.equals("automatic"))
	    		return 0;
    	else if(s.equals("saej1850-pwm"))
	    		return 1;
    	else if(s.equals("sae1850-vpw"))
	    		return 2;
	    else if(s.equals("iso19141-2"))
	    		return 3;
	    else if(s.equals("iso14230-4kwp-5"))
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

        result = sendAndWaitForChar("AT DP", 2500, ">").trim();
        return result;
    }

    public byte[] extractResponseBytes(String input) {
    	byte responseBytes[] = {0};

    	input = input.trim();

        String[] splitBytes = input.split(" ");

        for(int i = 0; i < splitBytes.length; i++) {
        	responseBytes[i] = (byte) Integer.parseInt(splitBytes[i],16);
         }

         return responseBytes;
    }

    //TODO: Test this for more protocols and different ELM versions
    public ERROR_TYPE resetAndInit(String transportProtocol, String moduleString,
    		String testerString) {
    	moduleString = moduleString.trim();
    	testerString = testerString.trim();

    	//Its possible to set a custom baudrate for iso14230 protocols with :96/:48 at the end
    	String splitTransport[] = transportProtocol.trim().split(":");
    	transportProtocol = splitTransport[0];

        elmMode = parseProtocolType(transportProtocol);

        if(elmMode == -1) {
        	return ERROR_TYPE.UNKNOWN_PROTOCOL;
        }

    	try {
	            String result = "";
	            send("");
	            result = sendAndWaitForChar("AT PC", 2000, ">");

	            clearLine();
	            result = sendAndWaitForChar("AT Z", 3500, ">");

	            //ATZ could be sent if echo mode is on
	            if(!result.contains("ELM327")) {
	            	LOGGER.error("Tried settings: " +  this.portName +
	            			", Baudrate: " + baudrate);
                	return ERROR_TYPE.ELM_NOT_FOUND;
	            }

	            clearLine();
	            LOGGER.info("Found "  + result);

	            //Turn off echo
	            result = sendAndWaitForChar("AT E0", 1000, ">");

	            if(!result.contains("OK")) {
	            	LOGGER.error("ELM327 rejected echo off!");
                	return ERROR_TYPE.ELM_REJECTED_REQUEST;
	            }

	            //Set custom baudrate with :96 or :48 at the end for iso14230 protocols
	            if(splitTransport.length > 1) {
		            result = sendAndWaitForChar("ATIB " + splitTransport[1], 2500, ">");

		            if(!result.contains("OK")) {
		            	LOGGER.error("ELM rejected ATIB " + splitTransport[1] + "!");
		            	return ERROR_TYPE.ELM_REJECTED_REQUEST;
		            }
		            else
		            {
		                if (LOGGER.isDebugEnabled())
		                    LOGGER.debug("ELM accepted ATIB " + splitTransport[1] + "!");
		            }
	            }

	            String reqATSH = "";

	            //If K-Line
	            if(elmMode == 4 || elmMode == 5) {

	            	//Also set init address for K-Line
	            	//Not 100% sure if this is needed
	                result = sendAndWaitForNewLine("ATIIA " + moduleString, 2500);

	                if(!result.contains("OK")) {
	                	LOGGER.error("ELM327 rejected ATIIA Init (Only K-Line)!");
	                	return ERROR_TYPE.ELM_REJECTED_REQUEST;
	                }
	                else {
	                	LOGGER.info("ELM327 accepted ATIIA Init!");
	                }

	                reqATSH = "ATSH 82" +  moduleString +  testerString;
	            }
	            else {
	            	reqATSH = "ATSH " + testerString;
	            }

	            clearLine();
	            result = sendAndWaitForChar(reqATSH, 2500, ">");

	            if(!result.contains("OK")) {
	            	LOGGER.error("ELM327 rejected ATSH Init!");
	            	return ERROR_TYPE.ELM_REJECTED_REQUEST;
	            }
	            else
	                if (LOGGER.isDebugEnabled())
                        LOGGER.debug("ELM327 accepted ATSH Init!");

	            if (LOGGER.isDebugEnabled())
                    LOGGER.debug("ELM Mode: " + elmMode);

	            //Set the correct protocol
	            result = sendAndWaitForChar("ATSP " + elmMode, 2500, ">");

	            if(!result.contains("OK")) {
	            	LOGGER.error("ELM327 rejected Protocol Init!");
	            	return ERROR_TYPE.ELM_REJECTED_REQUEST;
	            }
	            else
	                if (LOGGER.isDebugEnabled())
                        LOGGER.debug("ELM327 accepted Protocol Init!");

	            LOGGER.info("Current Protocol: " + getCurrentProtcol());

	            result = sendAndWaitForChar("0100", 5000, ">").trim();
	            if (LOGGER.isDebugEnabled())
                    LOGGER.debug("ECU Init Response: " + result);

	            //TODO: Check the actual Pids that are supported,
	            //this is more of an did-the-ecu-respond check.
	            //Might contain "SEARCHING..."
	            if(result.contains("NO DATA") ||  result.split(" ").length <= 4) {
	            	return ERROR_TYPE.ECU_NOT_FOUND;
	            }

	            //byte[] byteResponse = extractResponseBytes(result);

	            return ERROR_TYPE.NO_ERROR;

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

    // Send request and wait specified time for response with exact length
    public String sendAndWaitForNewLine(String command, int timeout) {
    	return sendAndWaitForChar(command, timeout, "\n");
    }

    // Send request and wait specified time for response with exact length
    public String sendAndWaitForChar(String command, int timeout, String charac) {
        connection.readStaleData();
        connection.write(command);
        long lastChange = currentTimeMillis();

        String response = "";
        while(!response.contains(charac)) {
            response += connection.readAvailable();
        	if(currentTimeMillis() - lastChange > timeout) break;
        	sleep(1);
        }

        return response.replace(charac, "").trim();
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