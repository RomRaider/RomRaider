package enginuity.logger.io.serial;

import java.util.Set;

public interface SerialPortRefreshListener {

    void refreshPortList(Set<String> ports);

}
