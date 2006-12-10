package enginuity.logger.ui;

import enginuity.logger.comms.controller.LoggerController;
import enginuity.logger.comms.query.LoggerCallback;
import enginuity.logger.definition.EcuData;
import enginuity.logger.ui.handler.DataUpdateHandlerManager;
import static enginuity.util.ParamChecker.checkNotNull;

import java.util.ArrayList;
import static java.util.Collections.synchronizedList;
import java.util.List;

public final class DataRegistrationBrokerImpl implements DataRegistrationBroker {
    private final LoggerController controller;
    private final DataUpdateHandlerManager handlerManager;
    private final String id;
    private final List<EcuData> registeredEcuData = synchronizedList(new ArrayList<EcuData>());
    private long loggerStartTime = 0;

    public DataRegistrationBrokerImpl(LoggerController controller, DataUpdateHandlerManager handlerManager) {
        checkNotNull(controller, handlerManager);
        this.controller = controller;
        this.handlerManager = handlerManager;
        id = System.currentTimeMillis() + "_" + hashCode();
    }

    public synchronized void registerEcuDataForLogging(final EcuData ecuData) {
        if (!registeredEcuData.contains(ecuData)) {
            // register param with handlers
            handlerManager.registerData(ecuData);

            // add logger and setup callback
            controller.addLogger(id, ecuData, new LoggerCallback() {
                public void callback(byte[] value) {
                    // update handlers
                    handlerManager.handleDataUpdate(ecuData, value, System.currentTimeMillis() - loggerStartTime);
                }
            });

            // add to registered parameters list
            registeredEcuData.add(ecuData);
        }
    }

    public synchronized void deregisterEcuDataFromLogging(EcuData ecuData) {
        if (registeredEcuData.contains(ecuData)) {
            // deregister from dependant objects
            deregisterEcuDataFromDependants(ecuData);

            // remove from registered list
            registeredEcuData.remove(ecuData);
        }

    }

    public synchronized void clear() {
        for (EcuData ecuData : registeredEcuData) {
            deregisterEcuDataFromDependants(ecuData);
        }
        registeredEcuData.clear();
    }

    public synchronized void start() {
        loggerStartTime = System.currentTimeMillis();
    }

    public synchronized void stop() {
    }

    private void deregisterEcuDataFromDependants(EcuData ecuData) {
        // remove logger
        controller.removeLogger(id, ecuData);

        // deregister param from handlers
        handlerManager.deregisterData(ecuData);
    }

}
