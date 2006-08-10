package enginuity.logger;

import enginuity.Settings;
import enginuity.logger.comms.DefaultSerialPortDiscoverer;
import enginuity.logger.comms.SerialPortDiscoverer;
import enginuity.logger.manager.DefaultQueryManager;
import enginuity.logger.manager.DefaultTransmissionManager;
import enginuity.logger.manager.QueryManager;
import enginuity.logger.manager.TransmissionManager;
import enginuity.logger.query.DefaultRegisteredQuery;
import enginuity.logger.query.LoggerCallback;
import static enginuity.util.ParamChecker.checkNotNull;
import gnu.io.CommPortIdentifier;

import java.util.ArrayList;
import java.util.List;

public final class DefaultLoggerController implements LoggerController {
    private final QueryManager queryManager;

    public DefaultLoggerController(Settings settings) {
        TransmissionManager txManager = new DefaultTransmissionManager(settings);
        queryManager = new DefaultQueryManager(txManager);
    }

    public List<String> listSerialPorts() {
        SerialPortDiscoverer serialPortDiscoverer = new DefaultSerialPortDiscoverer();
        List<CommPortIdentifier> portIdentifiers = serialPortDiscoverer.listPorts();
        List<String> portNames = new ArrayList<String>(portIdentifiers.size());
        for (CommPortIdentifier portIdentifier : portIdentifiers) {
            portNames.add(portIdentifier.getName());
        }
        return portNames;
    }

    public void start() {
        new Thread(queryManager).start();
    }

    public void addLogger(String address, LoggerCallback callback) {
        checkNotNull(address, callback);
        queryManager.addQuery(new DefaultRegisteredQuery(address, callback));
    }

    public void removeLogger(String address) {
        checkNotNull(address, "address");
        queryManager.removeQuery(address);
    }

    public void stop() {
        queryManager.stop();
    }
}
