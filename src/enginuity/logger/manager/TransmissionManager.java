package enginuity.logger.manager;

import enginuity.Settings;

public interface TransmissionManager {

    void start(Settings settings);

    byte[] queryAddress(String address);

    void stop();

}
