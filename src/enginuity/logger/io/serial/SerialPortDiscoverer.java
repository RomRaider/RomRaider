package enginuity.logger.io.serial;

import gnu.io.CommPortIdentifier;

import java.util.List;

public interface SerialPortDiscoverer {

    List<CommPortIdentifier> listPorts();

}
