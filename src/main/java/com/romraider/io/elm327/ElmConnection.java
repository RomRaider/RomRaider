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

import static com.fazecast.jSerialComm.SerialPort.FLOW_CONTROL_DISABLED;
import static com.fazecast.jSerialComm.SerialPort.TIMEOUT_READ_SEMI_BLOCKING;
import static com.romraider.util.ParamChecker.checkNotNullOrEmpty;
import static com.romraider.util.ThreadUtil.sleep;
import static org.apache.log4j.Logger.getLogger;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.PrintWriter;

import org.apache.log4j.Logger;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortInvalidPortException;
import com.romraider.io.serial.connection.SerialConnectionImpl;
import com.romraider.logger.ecu.exception.ConfigurationException;
import com.romraider.logger.ecu.exception.NotConnectedException;
import com.romraider.logger.ecu.exception.SerialCommunicationException;


public final class ElmConnection {
    private static final Logger LOGGER = getLogger(SerialConnectionImpl.class);
    private SerialPort serialPort;
    private PrintWriter os;
    private BufferedInputStream is;

    public ElmConnection(String portName, int baudrate) {
        checkNotNullOrEmpty(portName, "portName");

        try {
            serialPort = connect(portName, baudrate);
            os = new PrintWriter(serialPort.getOutputStream());
            is = new BufferedInputStream(serialPort.getInputStream());

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

        response += read(available());
        return response;
    }

    public void readStaleData() {
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
            if (!serialPort.closePort())
                LOGGER.error("Error closing serial port: " + serialPort.getSystemPortName());
        }
        LOGGER.info("Connection closed.");
        try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    public void sendBreak(long duration) {
        if (!serialPort.setBreak())
            throw new SerialCommunicationException("Send Break");
        sleep(duration);
        if (!serialPort.clearBreak())
            throw new SerialCommunicationException("Clear Break");
    }

    private SerialPort connect(String portName, int baudrate) {
        final SerialPort serialPort = openPort(portName);
        serialPort.openPort();
        configSerialPort(serialPort, baudrate, 8, 1, 0);
        LOGGER.info("Connected to: " + portName);
        return serialPort;
    }

    private SerialPort openPort(String portName) {
    	SerialPort serialPort;
        try {
        	serialPort = SerialPort.getCommPort(portName);
        	if (!serialPort.openPort())
        		throw new SerialCommunicationException("Failed to open port: " + portName);
        } catch (SerialPortInvalidPortException e) {
            throw new SerialCommunicationException("Port is unavailable: " + portName);
        }
		return serialPort;
    }

    private void configSerialPort(
    		SerialPort serialPort, int baudrate, int dataBits, int stopBits, int parity) {
        try {
            if(!serialPort.setFlowControl(FLOW_CONTROL_DISABLED))
            	throw new ConfigurationException("Flow control");
            if (!serialPort.setComPortParameters(baudrate, dataBits, stopBits, parity))
            	throw new ConfigurationException("Connection properties");
            if (!serialPort.setComPortTimeouts(TIMEOUT_READ_SEMI_BLOCKING, 0, 0))
            	throw new ConfigurationException("Timeout values");
            if (!serialPort.setRTS())
            	throw new ConfigurationException("RTS value");
        } catch (ConfigurationException e) {
            throw new UnsupportedOperationException(e);
        }
    }
}
