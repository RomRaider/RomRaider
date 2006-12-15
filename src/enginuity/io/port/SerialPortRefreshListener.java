package enginuity.io.port;

import java.util.Set;

public interface SerialPortRefreshListener {

    void refreshPortList(Set<String> ports);

}
