package enginuity.logger.comms.io.serial.port;

import gnu.io.CommPortIdentifier;

import java.util.List;

public interface SerialPortDiscoverer {

    List<CommPortIdentifier> listPorts();

}
