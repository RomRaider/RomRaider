package enginuity.logger.manager;

import enginuity.Settings;
import enginuity.logger.comms.DefaultSerialConnection;
import enginuity.logger.comms.SerialConnection;
import enginuity.logger.exception.NotConnectedException;
import enginuity.logger.exception.SerialCommunicationException;
import enginuity.logger.protocol.Protocol;
import enginuity.logger.protocol.ProtocolFactory;
import enginuity.logger.query.LoggerCallback;
import enginuity.logger.query.RegisteredQuery;
import enginuity.util.HexUtil;
import static enginuity.util.ParamChecker.checkNotNull;

import java.util.Collection;

//TODO: Make a ssm protocol package containing specific commands. SSM Protocol then returns instances of the command interfaces.
//TODO: Break protocol up into commands which are passed to serial connection. each command has buildQuery() and responseSize() methods which construct the request and indicate the expected response size.
//TODO: Let the serial connection build and execute the commands. the response should be set back on the command.

public final class DefaultTransmissionManager implements TransmissionManager {
    private static final int CONNECT_TIMEOUT = 2000;
    private final Settings settings;
    private SerialConnection serialConnection = null;

    public DefaultTransmissionManager(Settings settings) {
        checkNotNull(settings, "settings");
        this.settings = settings;
    }

    public void start() {
        try {
            Protocol protocol = ProtocolFactory.getInstance().getProtocol(settings.getLoggerProtocol());
            serialConnection = new DefaultSerialConnection(protocol, settings.getLoggerPort(), CONNECT_TIMEOUT);
            System.out.println("Connected to: " + settings.getLoggerPort() + "; using protocol: " + settings.getLoggerProtocol());
        } catch (Throwable e) {
            stop();
            throw new SerialCommunicationException("Unable to connect to port: " + settings.getLoggerPort() + ", with protocol: "
                    + settings.getLoggerProtocol() + ", and connection timeout: " + CONNECT_TIMEOUT + "ms", e);
        }
    }

    public void sendEcuInit(LoggerCallback callback) {
        if (serialConnection != null) {
            callback.callback(serialConnection.sendEcuInit());
        } else {
            throw new NotConnectedException("TransmissionManager must be started before a query can be sent!");
        }
    }

    public void sendQueries(Collection<RegisteredQuery> queries) {
        checkNotNull(queries, "queries");
        if (serialConnection != null) {
            serialConnection.sendAddressReads(queries);
        } else {
            throw new NotConnectedException("TransmissionManager must be started before a queries can be sent!");
        }
    }

    public void stop() {
        if (serialConnection != null) {
            serialConnection.close();
        }
        System.out.println("Disconnected.");
    }

    public static void main(String... args) {
        TransmissionManager txManager = new DefaultTransmissionManager(new Settings());
        try {
            txManager.start();
            txManager.sendEcuInit(new LoggerCallback() {
                public void callback(byte[] value) {
                    System.out.println("ECU Init Response = " + HexUtil.asHex(value));
                }
            });
        } finally {
            txManager.stop();
        }
    }

}
