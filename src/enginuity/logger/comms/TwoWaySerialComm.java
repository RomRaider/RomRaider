package enginuity.logger.comms;

public interface TwoWaySerialComm {

    SerialConnection connect(String portName, int baudrate, int dataBits, int stopBits, int parity, int connectTimeout);

    void disconnect();

}
