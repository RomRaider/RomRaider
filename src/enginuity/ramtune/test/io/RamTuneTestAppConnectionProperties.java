package enginuity.ramtune.test.io;

import enginuity.io.connection.ConnectionProperties;

public final class RamTuneTestAppConnectionProperties implements ConnectionProperties {
    private final ConnectionProperties defaultConnectionProperties;
    private final int sendTimeout;

    public RamTuneTestAppConnectionProperties(ConnectionProperties defaultConnectionProperties, int sendTimeout) {
        this.defaultConnectionProperties = defaultConnectionProperties;
        this.sendTimeout = sendTimeout;
    }

    public int getBaudRate() {
        return defaultConnectionProperties.getBaudRate();
    }

    public int getDataBits() {
        return defaultConnectionProperties.getDataBits();
    }

    public int getStopBits() {
        return defaultConnectionProperties.getStopBits();
    }

    public int getParity() {
        return defaultConnectionProperties.getParity();
    }

    public int getConnectTimeout() {
        return defaultConnectionProperties.getConnectTimeout();
    }

    public int getSendTimeout() {
        return sendTimeout > 0 ? sendTimeout : defaultConnectionProperties.getSendTimeout();
    }
}
