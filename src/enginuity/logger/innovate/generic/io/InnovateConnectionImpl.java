package enginuity.logger.innovate.generic.io;

import enginuity.io.connection.ConnectionProperties;
import enginuity.io.connection.SerialConnection;
import enginuity.io.connection.SerialConnectionImpl;
import enginuity.logger.ecu.exception.SerialCommunicationException;
import static enginuity.util.ByteUtil.matchOnes;
import static enginuity.util.ByteUtil.matchZeroes;
import static enginuity.util.HexUtil.asBytes;
import static enginuity.util.ParamChecker.checkGreaterThanZero;
import static enginuity.util.ParamChecker.checkNotNull;
import static enginuity.util.ParamChecker.checkNotNullOrEmpty;
import static enginuity.util.ThreadUtil.sleep;
import org.apache.log4j.Logger;
import java.io.IOException;
import static java.lang.Math.min;
import static java.lang.System.arraycopy;
import static java.lang.System.currentTimeMillis;

public final class InnovateConnectionImpl implements InnovateConnection {
    private static final Logger LOGGER = Logger.getLogger(InnovateConnectionImpl.class);
    private static final byte[] INNOVATE_HEADER = asBytes("0xB280");
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

    // FIX - YIKES!!
    public byte[] read() {
        try {
            byte[] response = new byte[responseLength];
            int bufferLength = responseLength + INNOVATE_HEADER.length - 1;
            long start = currentTimeMillis();
            while (currentTimeMillis() - start <= sendTimeout) {
                sleep(1);
                int available = serialConnection.available();
                if (available < bufferLength) continue;
                byte[] buffer = new byte[bufferLength];
                serialConnection.read(buffer);
                int responseBeginIndex = 0;
                int bufferBeginIndex = findHeader(buffer);
                if (bufferBeginIndex < 0) {
                    bufferBeginIndex = findLm1(buffer);
                    if (bufferBeginIndex < 0) continue;
                    arraycopy(INNOVATE_HEADER, 0, response, 0, INNOVATE_HEADER.length);
                    responseBeginIndex = INNOVATE_HEADER.length;
                }
                int tailLength = responseLength - responseBeginIndex;
                arraycopy(buffer, bufferBeginIndex, response, responseBeginIndex, min(tailLength, (buffer.length - bufferBeginIndex)));
                int remainderLength = tailLength - (buffer.length - bufferBeginIndex);
                if (remainderLength > 0) {
                    byte[] remainder = remainder(remainderLength, start);
                    if (remainder.length == 0) continue;
                    arraycopy(remainder, 0, response, responseLength - remainderLength, remainderLength);
                }
                return response;
            }
            LOGGER.warn(device + " Response [read timeout]");
            return new byte[0];
        } catch (Exception e) {
            close();
            throw new SerialCommunicationException(e);
        }
    }

    private byte[] remainder(int remainderLength, long start) throws IOException {
        while (currentTimeMillis() - start <= sendTimeout) {
            sleep(1);
            int available = serialConnection.available();
            if (available >= remainderLength) {
                byte[] remainder = new byte[remainderLength];
                serialConnection.read(remainder);
                return remainder;
            }
        }
        return new byte[0];
    }

    public void close() {
        serialConnection.close();
        LOGGER.info(device + " disconnected");
    }

    private int findHeader(byte[] bytes) {
        for (int i = 0; i < bytes.length - 1; i++) {
            if (matchOnes(bytes[i], 178) && matchOnes(bytes[i + 1], 128)) return i;
        }
        return -1;
    }

    private int findLm1(byte[] bytes) {
        for (int i = 0; i < bytes.length; i++) {
            if (matchOnes(bytes[i], 128) && matchZeroes(bytes[i], 34)) return i;
        }
        return -1;
    }
}
