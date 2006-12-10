package enginuity.logger.comms.io.serial.port;

import java.util.Set;

public interface SerialPortRefreshListener {

    void refreshPortList(Set<String> ports);

}
