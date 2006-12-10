package enginuity.logger.comms.io.serial.protocol;

public interface ConnectionProperties {

    int getBaudRate();

    int getDataBits();

    int getStopBits();

    int getParity();

}
