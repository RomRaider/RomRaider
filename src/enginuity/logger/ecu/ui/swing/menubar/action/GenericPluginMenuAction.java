package enginuity.logger.ecu.ui.swing.menubar.action;

import enginuity.io.port.SerialPortDiscoverer;
import enginuity.io.port.SerialPortDiscovererImpl;
import enginuity.logger.ecu.EcuLogger;
import enginuity.logger.ecu.external.ExternalDataSource;
import gnu.io.CommPortIdentifier;

import static javax.swing.JOptionPane.QUESTION_MESSAGE;
import static javax.swing.JOptionPane.showInputDialog;
import java.awt.event.ActionEvent;
import java.util.List;

public final class GenericPluginMenuAction extends AbstractAction {
    private final SerialPortDiscoverer portDiscoverer = new SerialPortDiscovererImpl();
    private final ExternalDataSource dataSource;

    public GenericPluginMenuAction(EcuLogger logger, ExternalDataSource dataSource) {
        super(logger);
        this.dataSource = dataSource;
    }

    public void actionPerformed(ActionEvent actionEvent) {
        String port = (String) showInputDialog(logger, "Select COM port:", dataSource.getName() + " Plugin Settings", QUESTION_MESSAGE, null,
                getPorts(), dataSource.getPort());
        dataSource.setPort(port);
    }

    private String[] getPorts() {
        List<CommPortIdentifier> portIdentifiers = portDiscoverer.listPorts();
        String[] ports = new String[portIdentifiers.size()];
        for (int i = 0; i < portIdentifiers.size(); i++) {
            CommPortIdentifier identifier = portIdentifiers.get(i);
            ports[i] = identifier.getName();
        }
        return ports;
    }
}
