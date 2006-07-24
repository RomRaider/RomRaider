package enginuity.logger.comms;

import gnu.io.CommPortIdentifier;

import java.util.List;

public interface SerialPortDiscoverer {

    List<CommPortIdentifier> listPorts();

}
