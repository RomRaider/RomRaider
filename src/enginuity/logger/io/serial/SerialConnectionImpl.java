package enginuity.logger.io.serial;

import enginuity.logger.exception.NotConnectedException;
import enginuity.logger.exception.PortNotFoundException;
import enginuity.logger.exception.SerialCommunicationException;
import enginuity.logger.exception.UnsupportedPortTypeException;
import enginuity.logger.io.serial.protocol.ConnectionProperties;
import enginuity.logger.io.serial.protocol.Protocol;
import enginuity.logger.query.RegisteredQuery;
import static enginuity.util.HexUtil.asHex;
import static enginuity.util.ParamChecker.checkGreaterThanZero;
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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

@SuppressWarnings({"ResultOfMethodCallIgnored"})
public final class SerialConnectionImpl implements SerialConnection {
    private Protocol protocol;
    private SerialPort serialPort;
    private OutputStream os;
    private InputStream is;

    public SerialConnectionImpl(Protocol protocol, String portName, int connectTimeout) {
        checkNotNull(protocol, "protocol");
        checkNotNullOrEmpty(portName, "portName");
        checkGreaterThanZero(connectTimeout, "connectTimeout");
        this.protocol = protocol;
        ConnectionProperties connectionProperties = protocol.getConnectionProperties();
        serialPort = resolveSerialPort(portName, connectionProperties.getBaudRate(), connectionProperties.getDataBits(),
                connectionProperties.getStopBits(), connectionProperties.getParity(), connectTimeout);
        initStreams();
    }

    public byte[] sendEcuInit() {
        return send(protocol.constructEcuInitRequest());
    }

    public byte[] send(byte[] bytes) {
        try {
            readStaleData();
            serialPort.setRTS(false);
            os.write(bytes, 0, bytes.length);
            os.flush();
            boolean keepLooking = true;
            int available = 0;
            long lastChange = System.currentTimeMillis();
            while (keepLooking) {
                TimeUnit.MILLISECONDS.sleep(5);
                if (is.available() != available) {
                    available = is.available();
                    lastChange = System.currentTimeMillis();
                }
                keepLooking = ((System.currentTimeMillis() - lastChange) < 55);
            }

            byte[] response = new byte[is.available()];
            is.read(response, 0, response.length);
            return response;
        } catch (Exception e) {
            close();
            throw new SerialCommunicationException(e);
        }
    }

    public void sendAddressReads(Collection<RegisteredQuery> queries) {
        try {
            byte[] request = protocol.constructReadAddressRequest(queries);
            byte[] response = protocol.constructReadAddressResponse(queries);

            System.out.println("Raw request        = " + asHex(request));

            readStaleData();
            serialPort.setRTS(false);
            os.write(request, 0, request.length);
            os.flush();
            int timeout = 1000;
            while (is.available() < response.length) {
                TimeUnit.MILLISECONDS.sleep(5);
                timeout -= 5;
                if (timeout <= 0) {
                    byte[] badBytes = new byte[is.available()];
                    is.read(badBytes, 0, badBytes.length);
                    System.out.println("Bad response (read timeout): " + asHex(badBytes));
                    break;
                }
            }
            is.read(response, 0, response.length);

            System.out.println("Raw response       = " + asHex(response));

            byte[] filteredResponse = new byte[response.length - request.length];
            System.arraycopy(response, request.length, filteredResponse, 0, filteredResponse.length);

            System.out.println("Filtered response  = " + asHex(filteredResponse));
            System.out.println();

            protocol.setResponse(queries, filteredResponse);
            //markTime();
        } catch (Exception e) {
            close();
            throw new SerialCommunicationException(e);
        }
    }

    private void readStaleData() throws IOException {
        if (is.available() > 0) {
            byte[] staleBytes = new byte[is.available()];
            is.read(staleBytes, 0, is.available());
            System.out.println("Stale data read: " + asHex(staleBytes));
        }
    }

    public void close() {
        if (os != null) {
            try {
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
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
                e.printStackTrace();
            }
        }
    }

    private void initStreams() {
        try {
            os = serialPort.getOutputStream();
            is = serialPort.getInputStream();
        } catch (IOException e) {
            close();
            throw new NotConnectedException(e);
        }
    }

    private SerialPort resolveSerialPort(String portName, int baudrate, int dataBits, int stopBits, int parity, int connectTimeout) {
        CommPortIdentifier portIdentifier = resolvePortIdentifier(portName);
        SerialPort serialPort = openPort(portIdentifier, connectTimeout);
        initSerialPort(serialPort, baudrate, dataBits, stopBits, parity);
        return serialPort;
    }

    private CommPortIdentifier resolvePortIdentifier(String portName) {
        try {
            return getPortIdentifier(portName);
        } catch (NoSuchPortException e) {
            throw new PortNotFoundException(e);
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
        } catch (UnsupportedCommOperationException e) {
            throw new UnsupportedOperationException(e);
        }
    }

}
