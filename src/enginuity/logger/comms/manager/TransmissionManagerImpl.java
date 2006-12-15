package enginuity.logger.comms.manager;

import enginuity.Settings;
import enginuity.logger.comms.io.connection.LoggerConnection;
import enginuity.logger.comms.io.connection.LoggerConnectionFactory;
import enginuity.logger.comms.query.RegisteredQuery;
import enginuity.logger.exception.NotConnectedException;
import enginuity.logger.exception.SerialCommunicationException;
import static enginuity.util.ParamChecker.checkNotNull;

import java.util.Collection;

public final class TransmissionManagerImpl implements TransmissionManager {
    private final Settings settings;
    private LoggerConnection connection = null;

    public TransmissionManagerImpl(Settings settings) {
        checkNotNull(settings, "settings");
        this.settings = settings;
    }

    public void start() {
        try {
            connection = LoggerConnectionFactory.getInstance().getLoggerConnection(settings.getLoggerProtocol(), settings.getLoggerPort());
            System.out.println("Connected to: " + settings.getLoggerPort() + "; using protocol: " + settings.getLoggerProtocol());
        } catch (Throwable e) {
            stop();
            throw new SerialCommunicationException("Unable to connect to port: " + settings.getLoggerPort() + ", with protocol: "
                    + settings.getLoggerProtocol(), e);
        }
    }

    public void sendQueries(Collection<RegisteredQuery> queries) {
        checkNotNull(queries, "queries");
        if (connection != null) {
            connection.sendAddressReads(queries);
        } else {
            throw new NotConnectedException("TransmissionManager must be started before queries can be sent!");
        }
    }

    public void stop() {
        if (connection != null) {
            connection.close();
        }
        System.out.println("Disconnected.");
    }

}
