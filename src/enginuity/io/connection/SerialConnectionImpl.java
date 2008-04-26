/*
 *
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2008 RomRaider.com
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
 *
 */

package enginuity.io.connection;

import enginuity.logger.ecu.exception.NotConnectedException;
import enginuity.logger.ecu.exception.PortNotFoundException;
import enginuity.logger.ecu.exception.SerialCommunicationException;
import enginuity.logger.ecu.exception.UnsupportedPortTypeException;
import static enginuity.util.HexUtil.asHex;
import static enginuity.util.ParamChecker.checkNotNull;
import static enginuity.util.ParamChecker.checkNotNullOrEmpty;
import gnu.io.CommPortIdentifier;
import static gnu.io.CommPortIdentifier.PORT_SERIAL;
import static gnu.io.CommPortIdentifier.getPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import static gnu.io.SerialPort.FLOWCONTROL_NONE;
import gnu.io.UnsupportedCommOperationException;
import org.apache.log4j.Logger;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@SuppressWarnings({"ResultOfMethodCallIgnored"})
public final class SerialConnectionImpl implements SerialConnection {
    private static final Logger LOGGER = Logger.getLogger(SerialConnectionImpl.class);
    private final SerialPort serialPort;
    private final OutputStream os;
    private final InputStream is;

    public SerialConnectionImpl(ConnectionProperties connectionProperties, String portName) {
        checkNotNull(connectionProperties, "connectionProperties");
        checkNotNullOrEmpty(portName, "portName");
        serialPort = connect(connectionProperties, portName);
        os = initOutputStream();
        is = initInputStream();
    }

    public void write(byte[] bytes) throws IOException {
        os.write(bytes, 0, bytes.length);
        os.flush();
    }

    public int available() throws IOException {
        return is.available();
    }

    public void read(byte[] bytes) throws IOException {
        is.read(bytes, 0, bytes.length);
    }

    public byte[] readAvailable() throws IOException {
        byte[] response = new byte[available()];
        read(response);
        return response;
    }

    public void readStaleData() throws IOException {
        if (is.available() > 0) {
            byte[] staleBytes = new byte[is.available()];
            read(staleBytes);
            LOGGER.debug("Stale data read: " + asHex(staleBytes));
        }
    }

    public void close() {
        if (os != null) {
            try {
                os.close();
            } catch (IOException e) {
                LOGGER.error("Error closing output stream", e);
            }
        }
        if (is != null) {
            try {
                is.close();
            } catch (IOException e) {
                LOGGER.error("Error closing input stream", e);
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

    private OutputStream initOutputStream() {
        try {
            return serialPort.getOutputStream();
        } catch (IOException e) {
            close();
            throw new NotConnectedException(e);
        }
    }

    private InputStream initInputStream() {
        try {
            return serialPort.getInputStream();
        } catch (IOException e) {
            close();
            throw new NotConnectedException(e);
        }
    }

    private SerialPort connect(ConnectionProperties connectionProperties, String portName) {
        CommPortIdentifier portIdentifier = resolvePortIdentifier(portName);
        SerialPort serialPort = openPort(portIdentifier, connectionProperties.getConnectTimeout());
        initSerialPort(serialPort, connectionProperties.getBaudRate(), connectionProperties.getDataBits(), connectionProperties.getStopBits(),
                connectionProperties.getParity());
        LOGGER.info("Connected to: " + portName);
        return serialPort;
    }

    private SerialPort openPort(CommPortIdentifier portIdentifier, int connectTimeout) {
        checkIsSerialPort(portIdentifier);
        try {
            return (SerialPort) portIdentifier.open(this.getClass().getName(), connectTimeout);
        } catch (PortInUseException e) {
            throw new SerialCommunicationException("Port is currently in use: " + portIdentifier.getName());
        }
    }

    private void checkIsSerialPort(CommPortIdentifier portIdentifier) {
        if (portIdentifier.getPortType() != PORT_SERIAL) {
            throw new UnsupportedPortTypeException("Port type " + portIdentifier.getPortType() + " not supported - must be serial.");
        }
    }

    private void initSerialPort(SerialPort serialPort, int baudrate, int dataBits, int stopBits, int parity) {
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
