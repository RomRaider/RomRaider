package enginuity.logger.aem.io;

import enginuity.io.connection.ConnectionProperties;

public final class AemConnectionProperties implements ConnectionProperties {
    public int getBaudRate() {
        return 9600;
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
        //TODO: What should this be??
        return 200;
    }
}
