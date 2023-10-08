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

import static com.fazecast.jSerialComm.SerialPort.FLOW_CONTROL_DISABLED;
import static com.fazecast.jSerialComm.SerialPort.TIMEOUT_READ_SEMI_BLOCKING;
import static com.romraider.util.HexUtil.asHex;
import static com.romraider.util.ParamChecker.checkNotNull;
import static com.romraider.util.ParamChecker.checkNotNullOrEmpty;
import static com.romraider.util.ThreadUtil.sleep;
import static java.lang.System.currentTimeMillis;
import static org.apache.log4j.Logger.getLogger;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.log4j.Logger;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortInvalidPortException;
import com.romraider.io.connection.ConnectionProperties;
import com.romraider.logger.ecu.exception.ConfigurationException;
import com.romraider.logger.ecu.exception.NotConnectedException;
import com.romraider.logger.ecu.exception.SerialCommunicationException;

public class SerialConnectionImpl implements SerialConnection {
    private static final Logger LOGGER = getLogger(SerialConnectionImpl.class);
    private final SerialPort serialPort;
    private final BufferedOutputStream os;
    private final BufferedInputStream is;
    private final BufferedReader reader;

    public SerialConnectionImpl(String portName, ConnectionProperties connectionProperties) {
        checkNotNullOrEmpty(portName, "portName");
        checkNotNull(connectionProperties, "connectionProperties");
        try {
            serialPort = connect(portName, connectionProperties);
            os = new BufferedOutputStream(serialPort.getOutputStream());
            is = new BufferedInputStream(serialPort.getInputStream());
            reader = new BufferedReader(new InputStreamReader(is));
            LOGGER.info("Serial connection initialised: " + connectionProperties);
        } catch (Exception e) {
            close();
            throw new NotConnectedException(e);
        }
    }

    @Override
    public void write(byte[] bytes) {
        try {
            os.write(bytes, 0, bytes.length);
            os.flush();
        } catch (IOException e) {
            throw new SerialCommunicationException("Write bytes: " + e);
        }
    }

    @Override
    public int available() {
        try {
            return is.available();
        } catch (IOException e) {
            throw new SerialCommunicationException("Available: " + e);
        }
    }

    @Override
    public int read() {
        try {
            waitForBytes(1);
            return is.read();
        } catch (IOException e) {
            throw new SerialCommunicationException("Read: " + e);
        }
    }

    @Override
    public void read(byte[] bytes) {
        try {
            waitForBytes(bytes.length);
            is.read(bytes, 0, bytes.length);
        } catch (IOException e) {
            throw new SerialCommunicationException("Read bytes: " + e);
        }
    }

    @Override
    public String readLine() {
        try {
            waitForBytes(1);
            return reader.readLine();
        } catch (IOException e) {
            throw new SerialCommunicationException("Read line: " + e);
        }
    }

    @Override
    public byte[] readAvailable() {
        byte[] response = new byte[available()];
        read(response);
        return response;
    }

    @Override
    public void readStaleData() {
        if (available() <= 0) return;
        final long end = currentTimeMillis() + 100L;
        do {
            byte[] staleBytes = readAvailable();
            if (LOGGER.isDebugEnabled())
                LOGGER.debug("Stale data read: " + asHex(staleBytes));
            sleep(2);
        } while (  (available() > 0)
                && (currentTimeMillis() <= end));
    }

    @Override
    public void close() {
        if (os != null) {
            try {
                os.close();
            } catch (IOException e) {
                LOGGER.error("Error closing output stream", e);
            }
        }
        if (reader != null) {
            try {
                //readStaleData();
                reader.close();
            } catch (IOException e) {
                LOGGER.error("Error closing input stream reader", e);
            }
        }
        if (is != null) {
            try {
                //readStaleData();
                is.close();
            } catch (IOException e) {
                LOGGER.error("Error closing input stream", e);
            }
        }
        if (serialPort != null) {
            if (!serialPort.closePort())
                LOGGER.error("Error closing serial port: " + serialPort.getSystemPortName());
        }
        LOGGER.info("Connection closed.");
    }

    public void sendBreak(int duration) {
        if (!serialPort.setBreak())
            throw new SerialCommunicationException("Send Break");
        sleep((long)duration);
        if (!serialPort.clearBreak())
            throw new SerialCommunicationException("Clear Break");
    }

    private SerialPort connect(String portName, ConnectionProperties connectionProperties) {
        final SerialPort serialPort = openPort(portName);
        serialPort.openPort();
        configSerialPort(serialPort,
        		connectionProperties.getBaudRate(), connectionProperties.getDataBits(),
        		connectionProperties.getStopBits(), connectionProperties.getParity());
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

    private void configSerialPort(SerialPort serialPort, int baudrate, int dataBits, int stopBits, int parity) {
        try {
            if(!serialPort.setFlowControl(FLOW_CONTROL_DISABLED))
            	throw new ConfigurationException("Flow control");
            if (!serialPort.setComPortParameters(baudrate, dataBits, stopBits, parity))
            	throw new ConfigurationException("Connection properties");
            if (!serialPort.setComPortTimeouts(TIMEOUT_READ_SEMI_BLOCKING, 0, 0))
            	throw new ConfigurationException("Timeout values");
            if (!serialPort.clearRTS())
            	throw new ConfigurationException("RTS value");
        } catch (ConfigurationException e) {
            throw new UnsupportedOperationException(e);
        }
    }

    private void waitForBytes(int numBytes) {
        while (available() < numBytes) sleep(2L);
    }
}
