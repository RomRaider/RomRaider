package enginuity.io.port;

import gnu.io.CommPortIdentifier;
import static gnu.io.CommPortIdentifier.PORT_SERIAL;
import static gnu.io.CommPortIdentifier.getPortIdentifiers;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public final class SerialPortDiscovererImpl implements SerialPortDiscoverer {

    @SuppressWarnings({"unchecked"})
    public List<CommPortIdentifier> listPorts() {
        List<CommPortIdentifier> serialPortIdentifiers = new ArrayList<CommPortIdentifier>();
        Enumeration<CommPortIdentifier> portEnum = getPortIdentifiers();
        while (portEnum.hasMoreElements()) {
            CommPortIdentifier portIdentifier = portEnum.nextElement();
            if (portIdentifier.getPortType() == PORT_SERIAL) {
                serialPortIdentifiers.add(portIdentifier);
            }
        }
        return serialPortIdentifiers;
    }
}
