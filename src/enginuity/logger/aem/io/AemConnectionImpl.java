package enginuity.logger.aem.io;

import enginuity.io.connection.ConnectionProperties;
import enginuity.io.connection.SerialConnection;
import enginuity.io.connection.SerialConnectionImpl;
import enginuity.logger.ecu.exception.SerialCommunicationException;
import static enginuity.util.HexUtil.asHex;
import static enginuity.util.ParamChecker.checkNotNull;
import static enginuity.util.ParamChecker.checkNotNullOrEmpty;
import static enginuity.util.ThreadUtil.sleep;
import org.apache.log4j.Logger;
import static java.lang.System.currentTimeMillis;
import java.util.ArrayList;
import java.util.List;

public final class AemConnectionImpl implements AemConnection {
    private static final Logger LOGGER = Logger.getLogger(AemConnectionImpl.class);
    private final long sendTimeout;
    private final SerialConnection serialConnection;

    public AemConnectionImpl(ConnectionProperties connectionProperties, String portName) {
        checkNotNull(connectionProperties, "connectionProperties");
        checkNotNullOrEmpty(portName, "portName");
        this.sendTimeout = connectionProperties.getSendTimeout();
        serialConnection = new SerialConnectionImpl(connectionProperties, portName);
        LOGGER.info("AEM connected");
    }

    //TODO: This a guess!!...untested!!
    public byte[] read() {
        try {
            serialConnection.readStaleData();
            long start = currentTimeMillis();
            while (currentTimeMillis() - start <= sendTimeout) {
                if (serialConnection.available() > 10) {
                    byte[] bytes = serialConnection.readAvailable();
                    LOGGER.trace("AEM UEGO input: " + asHex(bytes));
                    int startIndex = findStart(bytes);
                    LOGGER.trace("AEM UEGO start index: " + startIndex);
                    if (startIndex < 0 || startIndex >= bytes.length) continue;
                    List<Byte> buffer = new ArrayList<Byte>();
                    for (int i = startIndex; i < bytes.length; i++) {
                        byte b = bytes[i];
                        if (b == (byte) 0x0D) {
                            byte[] response = toArray(buffer);
                            LOGGER.trace("AEM UEGO Response: " + asHex(response));
                            return response;
                        } else {
                            buffer.add(b);
                        }
                    }
                }
                sleep(1);
            }
            LOGGER.warn("AEM UEGO Response [read timeout]");
            return new byte[0];
        } catch (Exception e) {
            close();
            throw new SerialCommunicationException(e);
        }
    }

    private int findStart(byte[] bytes) {
        for (int i = 0; i < bytes.length; i++) {
            if (bytes[i] == (byte) 0x0D) return i + 1;
        }
        return -1;
    }

    public void close() {
        serialConnection.close();
        LOGGER.info("AEM disconnected");
    }

    private byte[] toArray(List<Byte> buffer) {
        byte[] result = new byte[buffer.size()];
        for (int j = 0; j < buffer.size(); j++) {
            result[j] = buffer.get(j);
        }
        return result;
    }
}
