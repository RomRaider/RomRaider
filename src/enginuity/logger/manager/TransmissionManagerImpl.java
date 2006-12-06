package enginuity.logger.manager;

import enginuity.Settings;
import enginuity.logger.exception.NotConnectedException;
import enginuity.logger.exception.SerialCommunicationException;
import enginuity.logger.io.serial.SerialConnection;
import enginuity.logger.io.serial.SerialConnectionImpl;
import enginuity.logger.io.serial.protocol.Protocol;
import enginuity.logger.io.serial.protocol.ProtocolFactory;
import enginuity.logger.query.LoggerCallback;
import enginuity.logger.query.RegisteredQuery;
import enginuity.util.HexUtil;
import static enginuity.util.ParamChecker.checkNotNull;

import java.util.Collection;

public final class TransmissionManagerImpl implements TransmissionManager {
    private static final int CONNECT_TIMEOUT = 2000;
    private final Settings settings;
    private SerialConnection serialConnection = null;

    public TransmissionManagerImpl(Settings settings) {
        checkNotNull(settings, "settings");
        this.settings = settings;
    }

    public void start() {
        try {
            Protocol protocol = ProtocolFactory.getInstance().getProtocol(settings.getLoggerProtocol());
            serialConnection = new SerialConnectionImpl(protocol, settings.getLoggerPort(), CONNECT_TIMEOUT);
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
            throw new NotConnectedException("TransmissionManager must be started before queries can be sent!");
        }
    }

    public void stop() {
        if (serialConnection != null) {
            serialConnection.close();
        }
        System.out.println("Disconnected.");
    }

    public static void main(String... args) {
        TransmissionManager txManager = new TransmissionManagerImpl(new Settings());
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
