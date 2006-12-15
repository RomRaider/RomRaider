package enginuity.logger.comms.io.connection;

import enginuity.logger.exception.UnsupportedProtocolException;

public final class LoggerConnectionFactory {
    private static final LoggerConnectionFactory INSTANCE = new LoggerConnectionFactory();

    public static LoggerConnectionFactory getInstance() {
        return INSTANCE;
    }

    private LoggerConnectionFactory() {
    }

    public LoggerConnection getLoggerConnection(String protocolName, String portName) {
        try {
            Class<?> cls = Class.forName(this.getClass().getPackage().getName() + "." + protocolName + "LoggerConnection");
            return (LoggerConnection) cls.getConstructor(String.class).newInstance(portName);
        } catch (Exception e) {
            throw new UnsupportedProtocolException("'" + protocolName + "' is not a supported protocol", e);
        }
    }
}
