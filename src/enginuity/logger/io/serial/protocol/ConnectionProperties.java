package enginuity.logger.io.serial.protocol;

public interface ConnectionProperties {

    int getBaudRate();

    int getDataBits();

    int getStopBits();

    int getParity();

}
