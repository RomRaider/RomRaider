package enginuity.logger.comms.io.serial.protocol;

import enginuity.logger.exception.UnsupportedProtocolException;

public final class ProtocolFactory {
    private static final ProtocolFactory INSTANCE = new ProtocolFactory();

    public static ProtocolFactory getInstance() {
        return INSTANCE;
    }

    private ProtocolFactory() {
    }

    public Protocol getProtocol(String protocolName) {
        try {
            return (Protocol) Class.forName(this.getClass().getPackage().getName() + "." + protocolName + "Protocol").newInstance();
        } catch (Exception e) {
            throw new UnsupportedProtocolException("'" + protocolName + "' is not a supported protocol", e);
        }
    }
}
