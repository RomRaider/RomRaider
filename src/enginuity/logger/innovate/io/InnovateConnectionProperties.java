package enginuity.logger.innovate.io;

import enginuity.io.connection.ConnectionProperties;

public final class InnovateConnectionProperties implements ConnectionProperties {
    public int getBaudRate() {
        return 19200;
    }

    public int getDataBits() {
        return 8;
    }

    public int getStopBits() {
        return 1;
    }

    public int getParity() {
        return 0;
    }

    public int getConnectTimeout() {
        return 2000;
    }

    public int getSendTimeout() {
        return 82;
    }
}
