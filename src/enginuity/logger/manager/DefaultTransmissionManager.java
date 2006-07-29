package enginuity.logger.manager;

import enginuity.Settings;
import enginuity.logger.comms.DefaultTwoWaySerialComm;
import enginuity.logger.comms.SerialConnection;
import enginuity.logger.comms.TwoWaySerialComm;
import enginuity.logger.exception.NotConnectedException;
import enginuity.logger.protocol.ConnectionProperties;
import enginuity.logger.protocol.Protocol;
import enginuity.logger.query.DefaultQuery;
import static enginuity.util.ParamChecker.checkNotNull;
import static enginuity.util.ParamChecker.checkNotNullOrEmpty;

public final class DefaultTransmissionManager implements TransmissionManager {
    private static final int CONNECT_TIMEOUT = 2000;
    private final Settings settings;
    private final Protocol protocol;
    private final ConnectionProperties connectionProperties;
    private final TwoWaySerialComm twoWaySerialComm = new DefaultTwoWaySerialComm();
    private SerialConnection serialConnection = null;

    public DefaultTransmissionManager(Settings settings, Protocol protocol) {
        checkNotNull(settings, protocol, protocol.getConnectionProperties());
        this.settings = settings;
        this.protocol = protocol;
        this.connectionProperties = protocol.getConnectionProperties();
    }

    public void start() {
        serialConnection = twoWaySerialComm.connect(settings.getLoggerPort(), connectionProperties.getBaudRate(), connectionProperties.getDataBits(),
                connectionProperties.getStopBits(), connectionProperties.getParity(), CONNECT_TIMEOUT);
    }

    public byte[] queryAddress(byte[] address) {
        checkNotNullOrEmpty(address, "address");
        if (serialConnection != null) {
            byte[] query = protocol.constructReadAddressRequest(address);
            return serialConnection.transmit(new DefaultQuery(query));
        } else {
            throw new NotConnectedException("TransmissionManager must be started before a query can be sent!");
        }
    }

    public void stop() {
        twoWaySerialComm.disconnect();
    }

}
