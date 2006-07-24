package enginuity.logger.manager;

import enginuity.Settings;
import enginuity.logger.comms.DefaultTwoWaySerialComm;
import enginuity.logger.comms.SerialConnection;
import enginuity.logger.comms.TwoWaySerialComm;
import enginuity.logger.exception.NotConnectedException;
import enginuity.logger.query.DefaultQuery;
import static gnu.io.SerialPort.DATABITS_8;
import static gnu.io.SerialPort.PARITY_NONE;
import static gnu.io.SerialPort.STOPBITS_1;

public final class DefaultTransmissionManager implements TransmissionManager {
    private static final int BAUDRATE = 10400;
    private static final int CONNECT_TIMEOUT = 2000;
    private final TwoWaySerialComm twoWaySerialComm = new DefaultTwoWaySerialComm();
    private SerialConnection serialConnection = null;

    public void start(Settings settings) {
        serialConnection = twoWaySerialComm.connect(settings.getLoggerPort(), BAUDRATE, DATABITS_8, STOPBITS_1, PARITY_NONE, CONNECT_TIMEOUT);
    }

    public byte[] queryAddress(String address) {
        if (serialConnection != null) {
            byte[] request = buildRequest(address);
            return serialConnection.transmit(new DefaultQuery(request));
        } else {
            throw new NotConnectedException("TransmissionManager must be started before an address is queried!");
        }
    }

    public void stop() {
        twoWaySerialComm.disconnect();
    }

    private byte[] buildRequest(String address) {
        // TODO Implement this!! What is the correct message format??
        return address.getBytes();
    }

}
