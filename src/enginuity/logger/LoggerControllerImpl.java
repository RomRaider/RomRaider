package enginuity.logger;

import enginuity.Settings;
import enginuity.logger.definition.EcuData;
import enginuity.logger.io.serial.SerialPortDiscoverer;
import enginuity.logger.io.serial.SerialPortDiscovererImpl;
import enginuity.logger.manager.QueryManager;
import enginuity.logger.manager.QueryManagerImpl;
import enginuity.logger.manager.TransmissionManager;
import enginuity.logger.manager.TransmissionManagerImpl;
import enginuity.logger.query.LoggerCallback;
import enginuity.logger.ui.MessageListener;
import static enginuity.util.ParamChecker.checkNotNull;
import gnu.io.CommPortIdentifier;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public final class LoggerControllerImpl implements LoggerController {
    private final QueryManager queryManager;
    private boolean started = false;

    public LoggerControllerImpl(Settings settings, MessageListener messageListener) {
        TransmissionManager txManager = new TransmissionManagerImpl(settings);
        queryManager = new QueryManagerImpl(txManager, messageListener);
    }

    public Set<String> listSerialPorts() {
        SerialPortDiscoverer serialPortDiscoverer = new SerialPortDiscovererImpl();
        List<CommPortIdentifier> portIdentifiers = serialPortDiscoverer.listPorts();
        Set<String> portNames = new TreeSet<String>();
        for (CommPortIdentifier portIdentifier : portIdentifiers) {
            String portName = portIdentifier.getName();
            if (!portNames.contains(portName)) {
                portNames.add(portName);
            }
        }
        return portNames;
    }

    public void addLogger(EcuData ecuData, LoggerCallback callback) {
        checkNotNull(ecuData, callback);
        System.out.println("Adding logger:   " + ecuData.getName());
        queryManager.addQuery(ecuData, callback);
    }

    public void removeLogger(EcuData ecuData) {
        checkNotNull(ecuData, "ecuParam");
        System.out.println("Removing logger: " + ecuData.getName());
        queryManager.removeQuery(ecuData);
    }

    public synchronized void start() {
        if (!started) {
            new Thread(queryManager).start();
            started = true;
        }
    }

    public synchronized void stop() {
        queryManager.stop();
        started = false;
    }
}
