package enginuity.logger.ui;

import enginuity.Settings;
import enginuity.logger.LoggerController;
import enginuity.logger.LoggerControllerImpl;
import enginuity.logger.definition.EcuParameter;
import enginuity.logger.query.LoggerCallback;
import enginuity.logger.ui.handler.ParameterUpdateHandlerManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ParameterRegistrationBrokerImpl implements ParameterRegistrationBroker {
    private final LoggerController controller;
    private final ParameterUpdateHandlerManager handlerManager;
    private final List<EcuParameter> registeredEcuParameters = Collections.synchronizedList(new ArrayList<EcuParameter>());
    private long loggerStartTime = 0;

    public ParameterRegistrationBrokerImpl(ParameterUpdateHandlerManager handlerManager, Settings settings) {
        this.handlerManager = handlerManager;
        this.controller = new LoggerControllerImpl(settings);
    }

    public synchronized void registerEcuParameterForLogging(final EcuParameter ecuParam) {
        if (!registeredEcuParameters.contains(ecuParam)) {
            // register param with handlers
            handlerManager.registerParam(ecuParam);

            // add logger and setup callback
            controller.addLogger(ecuParam, new LoggerCallback() {
                public void callback(byte[] value) {
                    // update handlers
                    handlerManager.handleParamUpdate(ecuParam, value, System.currentTimeMillis() - loggerStartTime);
                }
            });

            // add to registered parameters list
            registeredEcuParameters.add(ecuParam);
        }
    }

    public synchronized void deregisterEcuParameterFromLogging(EcuParameter ecuParam) {
        if (registeredEcuParameters.contains(ecuParam)) {
            // remove logger
            controller.removeLogger(ecuParam);

            // deregister param from handlers
            handlerManager.deregisterParam(ecuParam);

            // remove from registered list
            registeredEcuParameters.remove(ecuParam);
        }

    }

    public List<String> listSerialPorts() {
        return controller.listSerialPorts();
    }


    public synchronized void start() {
        loggerStartTime = System.currentTimeMillis();
        controller.start();
    }

    public synchronized void stop() {
        controller.stop();
    }

}
