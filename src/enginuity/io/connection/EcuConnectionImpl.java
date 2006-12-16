package enginuity.io.connection;

import enginuity.logger.exception.SerialCommunicationException;
import static enginuity.util.ParamChecker.checkNotNull;
import static enginuity.util.ParamChecker.checkNotNullOrEmpty;
import static enginuity.util.ThreadUtil.sleep;

public final class EcuConnectionImpl implements EcuConnection {
    private final long sendTimeout;
    private final SerialConnection serialConnection;

    public EcuConnectionImpl(ConnectionProperties connectionProperties, String portName) {
        checkNotNull(connectionProperties, "connectionProperties");
        checkNotNullOrEmpty(portName, "portName");
        serialConnection = new SerialConnectionImpl(connectionProperties, portName);
        this.sendTimeout = connectionProperties.getSendTimeout();
    }

    public byte[] send(byte[] bytes) {
        checkNotNull(bytes, "bytes");
        try {
            serialConnection.readStaleData();
            serialConnection.write(bytes);
            boolean keepLooking = true;
            int available = 0;
            long lastChange = System.currentTimeMillis();
            while (keepLooking) {
                sleep(5);
                if (serialConnection.available() != available) {
                    available = serialConnection.available();
                    lastChange = System.currentTimeMillis();
                }
                keepLooking = ((System.currentTimeMillis() - lastChange) < sendTimeout);
            }
            return serialConnection.readAvailable();
        } catch (Exception e) {
            close();
            throw new SerialCommunicationException(e);
        }
    }

    public void close() {
        serialConnection.close();
    }
}
