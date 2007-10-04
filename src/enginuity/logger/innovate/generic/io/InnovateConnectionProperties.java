package enginuity.logger.innovate.generic.io;

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
        // innovate specifies 82 but this isn't enough...
        return 200;
    }
}
