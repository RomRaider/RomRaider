package enginuity.logger.innovate.generic.io;

import enginuity.io.connection.ConnectionProperties;
import enginuity.io.connection.SerialConnection;
import enginuity.io.connection.SerialConnectionImpl;
import enginuity.logger.ecu.exception.SerialCommunicationException;
import static enginuity.util.HexUtil.asHex;
import static enginuity.util.ParamChecker.checkGreaterThanZero;
import static enginuity.util.ParamChecker.checkNotNull;
import static enginuity.util.ParamChecker.checkNotNullOrEmpty;
import static enginuity.util.ThreadUtil.sleep;
import org.apache.log4j.Logger;

public final class InnovateConnectionImpl implements InnovateConnection {
    private static final Logger LOGGER = Logger.getLogger(InnovateConnectionImpl.class);
    private final String device;
    private final long sendTimeout;
    private final SerialConnection serialConnection;
    private final int responseLength;

    public InnovateConnectionImpl(String device, ConnectionProperties connectionProperties, String portName, int responseLength) {
        checkNotNullOrEmpty(device, "device");
        checkNotNull(connectionProperties, "connectionProperties");
        checkNotNullOrEmpty(portName, "portName");
        checkGreaterThanZero(responseLength, "responseLength");
        this.device = device;
        this.sendTimeout = connectionProperties.getSendTimeout();
        this.responseLength = responseLength;
        serialConnection = new SerialConnectionImpl(connectionProperties, portName);
        LOGGER.info(device + " connected");
    }

    public byte[] read() {
        try {
            byte[] response = new byte[responseLength];
            serialConnection.readStaleData();
            long start = System.currentTimeMillis();
            while (serialConnection.available() < response.length) {
                if (System.currentTimeMillis() - start > sendTimeout) {
                    byte[] badBytes = serialConnection.readAvailable();
                    LOGGER.warn(device + " Response [read timeout]: " + asHex(badBytes));
                    return badBytes;
                }
                sleep(5);
            }
            serialConnection.read(response);
            LOGGER.trace(device + " Response: " + asHex(response));
            return response;
        } catch (Exception e) {
            close();
            throw new SerialCommunicationException(e);
        }
    }

    public void close() {
        serialConnection.close();
        LOGGER.info(device + " disconnected");
    }
}
