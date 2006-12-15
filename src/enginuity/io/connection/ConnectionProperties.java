package enginuity.io.connection;

public interface ConnectionProperties {

    int getBaudRate();

    int getDataBits();

    int getStopBits();

    int getParity();

    int getConnectTimeout();

    int getSendTimeout();
}
