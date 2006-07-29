package enginuity.logger.manager;

public interface TransmissionManager {

    void start();

    byte[] queryAddress(byte[] query);

    void stop();

}
