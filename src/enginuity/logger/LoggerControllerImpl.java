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
import enginuity.logger.ui.ControllerListener;
import enginuity.logger.ui.MessageListener;
import static enginuity.util.ParamChecker.checkNotNull;
import gnu.io.CommPortIdentifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public final class LoggerControllerImpl implements LoggerController {
    private final QueryManager queryManager;
    private List<ControllerListener> listeners = Collections.synchronizedList(new ArrayList<ControllerListener>());
    private boolean started = false;

    public LoggerControllerImpl(Settings settings, MessageListener messageListener) {
        TransmissionManager txManager = new TransmissionManagerImpl(settings);
        queryManager = new QueryManagerImpl(this, txManager, messageListener);
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

    public synchronized void addListener(ControllerListener listener) {
        checkNotNull(listener, "listener");
        listeners.add(listener);
    }

    public void addLogger(String callerId, EcuData ecuData, LoggerCallback callback) {
        checkNotNull(ecuData, callback);
        System.out.println("Adding logger:   " + ecuData.getName());
        queryManager.addQuery(callerId, ecuData, callback);
    }

    public void removeLogger(String callerId, EcuData ecuData) {
        checkNotNull(ecuData, "ecuParam");
        System.out.println("Removing logger: " + ecuData.getName());
        queryManager.removeQuery(callerId, ecuData);
    }

    public synchronized void start() {
        if (!started) {
            Thread queryManagerThread = new Thread(queryManager);
            queryManagerThread.setDaemon(true);
            queryManagerThread.start();
            startListeners();
            started = true;
        }
    }

    public synchronized void stop() {
        stopListeners();
        queryManager.stop();
        started = false;
    }

    private void startListeners() {
        for (ControllerListener listener : listeners) {
            listener.start();
        }
    }

    private void stopListeners() {
        for (ControllerListener listener : listeners) {
            listener.stop();
        }
    }

}
