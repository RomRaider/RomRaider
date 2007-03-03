package enginuity.io.connection;

public final class ConnectionPropertiesImpl implements ConnectionProperties {
    private final int baudRate;
    private final int dataBits;
    private final int stopBits;
    private final int parity;
    private final int connectTimeout;
    private final int sendTimeout;


    public ConnectionPropertiesImpl(int baudRate, int dataBits, int stopBits, int parity, int connectTimeout, int sendTimeout) {
        this.baudRate = baudRate;
        this.dataBits = dataBits;
        this.stopBits = stopBits;
        this.parity = parity;
        this.connectTimeout = connectTimeout;
        this.sendTimeout = sendTimeout;
    }

    public int getBaudRate() {
        return baudRate;
    }

    public int getDataBits() {
        return dataBits;
    }

    public int getStopBits() {
        return stopBits;
    }

    public int getParity() {
        return parity;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public int getSendTimeout() {
        return sendTimeout;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(getClass().getSimpleName());
        builder.append("[baudRate=").append(baudRate);
        builder.append(", dataBits=").append(dataBits);
        builder.append(", stopBits=").append(stopBits);
        builder.append(", dataBits=").append(dataBits);
        builder.append(", parity=").append(parity);
        builder.append(", connectTimeout=").append(connectTimeout);
        builder.append(", sendTimeout=").append(sendTimeout).append("]");
        return builder.toString();
    }
}
