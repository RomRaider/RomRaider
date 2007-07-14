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
            List<Byte> buffer = new ArrayList<Byte>();
            serialConnection.readStaleData();
            long start = System.currentTimeMillis();
            while (System.currentTimeMillis() - start <= sendTimeout) {
                byte[] bytes = serialConnection.readAvailable();
                if (bytes.length > 0) {
                    for (byte b : bytes) {
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
            byte[] badBytes = toArray(buffer);
            LOGGER.warn("AEM UEGO Response [read timeout]: " + asHex(badBytes));
            return badBytes;
        } catch (Exception e) {
            close();
            throw new SerialCommunicationException(e);
        }
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
