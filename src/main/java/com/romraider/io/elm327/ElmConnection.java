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

import com.romraider.io.serial.connection.SerialConnectionImpl;
import com.romraider.logger.ecu.exception.NotConnectedException;
import com.romraider.logger.ecu.exception.PortNotFoundException;
import com.romraider.logger.ecu.exception.SerialCommunicationException;
import com.romraider.logger.ecu.exception.UnsupportedPortTypeException;
import static com.romraider.util.ParamChecker.checkNotNullOrEmpty;
import gnu.io.CommPortIdentifier;
import static gnu.io.CommPortIdentifier.PORT_SERIAL;
import static gnu.io.CommPortIdentifier.getPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import static gnu.io.SerialPort.FLOWCONTROL_NONE;
import gnu.io.UnsupportedCommOperationException;
import org.apache.log4j.Logger;
import static org.apache.log4j.Logger.getLogger;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.PrintWriter;


public final class ElmConnection {
    private static final Logger LOGGER = getLogger(SerialConnectionImpl.class);
    private final SerialPort serialPort;
    private final PrintWriter os;
    private final BufferedInputStream is;
    
    public ElmConnection(String portName, int baudrate) {
        checkNotNullOrEmpty(portName, "portName");

        try {      	
            serialPort = connect(portName, baudrate);
            os = new PrintWriter(serialPort.getOutputStream());
            is = new BufferedInputStream (serialPort.getInputStream());
        } catch (Exception e) {
            close();
            throw new NotConnectedException(e);
        }
    }
    
    public void flush() {
        try {
            os.flush();
        } catch (Exception e) {
            throw new SerialCommunicationException(e);
        }
    }

       
    public void write(String command) {
        try {
            os.println(command);
            os.flush();
        } catch (Exception e) {
            throw new SerialCommunicationException(e);
        }
    }

    public int available() {
        try {
			return is.available();
		} catch (IOException e) {
			e.printStackTrace();
		}
        
        return 0;
    }

    public String read() {
        try {
			return "" + (char)is.read();
		} catch (IOException e) {
			e.printStackTrace();
		}
        
        return "";
    }

    //Reads up to numChars
    public String read(int numChars) {
        StringBuilder response = new StringBuilder();

        try {
            for(int i = 0; i < numChars && available() > 0; i++) {
                response.append((char)is.read());
            }           

        } catch (Exception e) {
            throw new SerialCommunicationException(e);
        }
        
        return response.toString();
    }

    //Reads everything that is available
    public String readAvailable() {
        String response = "";
        response = read(available());
        return response;
    }

    public void readStaleData() {
        if (available() <= 0) return;
        
    	while(available() > 0)
			try {
				is.read();
			} catch (IOException e) {
				e.printStackTrace();
			}
    }

    public void close() {
        if (os != null) {
            try {
                os.close();
            } catch (Exception e) {
                LOGGER.error("Error closing output stream", e);
            }
        }
        if (is != null) {
            try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
        }
        if (serialPort != null) {
            try {
                serialPort.close();
            } catch (Exception e) {
                LOGGER.error("Error closing serial port", e);
            }
        }
        LOGGER.info("Connection closed.");
    }

    public void sendBreak(int duration) {
        try {
            serialPort.sendBreak(duration);
        } catch (Exception e) {
            throw new SerialCommunicationException(e);
        }
    }
    
    private SerialPort connect(String portName, int baudrate) {
        CommPortIdentifier portIdentifier = resolvePortIdentifier(portName);
        SerialPort serialPort = openPort(portIdentifier, 3000);
        
        initSerialPort(serialPort, baudrate, 8, 1 ,0);
        LOGGER.info("Connected to: " + portName);      
        return serialPort;
    }

    private SerialPort openPort(CommPortIdentifier portIdentifier, int connectTimeout) {
        checkIsSerialPort(portIdentifier);
        try {
            return (SerialPort) portIdentifier.open(this.getClass().getName(), connectTimeout);
        } catch (PortInUseException e) {
            throw new SerialCommunicationException("Port is currently in use: " 
        + portIdentifier.getName());
        }
    }

    private void checkIsSerialPort(CommPortIdentifier portIdentifier) {
        if (portIdentifier.getPortType() != PORT_SERIAL) {
            throw new UnsupportedPortTypeException("Port type "
        + portIdentifier.getPortType() + " not supported - must be serial.");
        }
    }

    private void initSerialPort(SerialPort serialPort, int baudrate, int dataBits,
    		int stopBits, int parity) {
        try {
            serialPort.setFlowControlMode(FLOWCONTROL_NONE);
            serialPort.setSerialPortParams(baudrate, dataBits, stopBits, parity);
            serialPort.disableReceiveTimeout();
            serialPort.setRTS(false);
        } catch (UnsupportedCommOperationException e) {
            throw new UnsupportedOperationException(e);
        }
    }

    private CommPortIdentifier resolvePortIdentifier(String portName) {
        try {
            return getPortIdentifier(portName);
        } catch (NoSuchPortException e) {
            throw new PortNotFoundException("Unable to resolve port: " + portName);
        }
    }
}
