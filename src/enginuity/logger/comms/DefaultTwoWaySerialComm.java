package enginuity.logger.comms;

import enginuity.logger.exception.PortNotFoundException;
import enginuity.logger.exception.SerialCommunicationException;
import enginuity.logger.exception.UnsupportedPortTypeException;
import static enginuity.util.ParamChecker.checkGreaterThanZero;
import static enginuity.util.ParamChecker.checkNotNull;
import gnu.io.CommPortIdentifier;
import static gnu.io.CommPortIdentifier.PORT_SERIAL;
import static gnu.io.CommPortIdentifier.getPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import static gnu.io.SerialPort.FLOWCONTROL_NONE;
import gnu.io.UnsupportedCommOperationException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public final class DefaultTwoWaySerialComm implements TwoWaySerialComm {
    private SerialPort serialPort;
    private OutputStream os;
    private InputStream is;
    private SerialConnection serialConnection;

    public SerialConnection connect(String portName, int baudrate, int dataBits, int stopBits, int parity, int connectTimeout) {
        checkConnectParams(portName, baudrate, dataBits, stopBits, connectTimeout);
        try {
            serialPort = resolveSerialPort(portName, connectTimeout);
            initSerialPort(serialPort, baudrate, dataBits, stopBits, parity);
            resolveIoStreams(serialPort);
            serialConnection = doConnect(serialPort);
            System.out.println("Connected to " + portName + ".");
            return serialConnection;
        } catch (Throwable e) {
            disconnect();
            throw new SerialCommunicationException("Unable to connect to port " + portName + " with following config - [baudrate: "
                    + baudrate + "bps, dataBits: " + dataBits + ", stopBits: " + stopBits + ", parity: " + parity + ", connectTimeout: "
                    + connectTimeout + "ms]", e);
        }
    }

    public void disconnect() {
        if (serialConnection != null) {
            serialConnection.close();
        }
        if (serialPort != null) {
            cleanupIoStreams();
            serialPort.close();
        }
        System.out.println("Disconnected.");
    }

    private void checkConnectParams(String portName, int baudrate, int dataBits, int stopBits, int connectTimeout) {
        checkNotNull(portName, "portName");
        checkGreaterThanZero(baudrate, "baudrate");
        checkGreaterThanZero(dataBits, "dataBits");
        checkGreaterThanZero(stopBits, "stopBits");
        checkGreaterThanZero(connectTimeout, "connectTimeout");
    }

    private SerialPort resolveSerialPort(String portName, int connectTimeout) {
        CommPortIdentifier portIdentifier = resolvePortIdentifier(portName);
        return openPort(portIdentifier, connectTimeout);
    }

    private void initSerialPort(SerialPort serialPort, int baudrate, int dataBits, int stopBits, int parity) {
        try {
            serialPort.setSerialPortParams(baudrate, dataBits, stopBits, parity);
            serialPort.setFlowControlMode(FLOWCONTROL_NONE);
        } catch (UnsupportedCommOperationException e) {
            throw new UnsupportedOperationException(e);
        }
    }

    private void resolveIoStreams(SerialPort serialPort) {
        try {
            os = serialPort.getOutputStream();
            is = serialPort.getInputStream();
        } catch (IOException e) {
            cleanupIoStreams();
            throw new SerialCommunicationException(e);
        }
    }

    private void cleanupIoStreams() {
        try {
            try {
                if (os != null) {
                    os.close();
                }
            } finally {
                if (is != null) {
                    is.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private SerialConnection doConnect(SerialPort serialPort) {
        SerialReader serialReader = new DefaultSerialReader(serialPort);
        SerialWriter serialWriter = new DefaultSerialWriter(serialPort);
        (new Thread(serialReader)).start();
        (new Thread(serialWriter)).start();
        return new DefaultSerialConnection(serialWriter, serialReader);
    }

    private CommPortIdentifier resolvePortIdentifier(String portName) {
        try {
            return getPortIdentifier(portName);
        } catch (NoSuchPortException e) {
            throw new PortNotFoundException(e);
        }
    }

    private void checkIsSerialPort(CommPortIdentifier portIdentifier) {
        if (portIdentifier.getPortType() != PORT_SERIAL) {
            throw new UnsupportedPortTypeException("Port type " + portIdentifier.getPortType() + " not supported - must be serial.");
        }
    }

    private SerialPort openPort(CommPortIdentifier portIdentifier, int connectTimeout) {
        checkIsSerialPort(portIdentifier);
        try {
            return (SerialPort) portIdentifier.open(this.getClass().getName(), connectTimeout);
        } catch (PortInUseException e) {
            throw new SerialCommunicationException("Port is currently in use: " + portIdentifier.getName());
        }
    }

}
