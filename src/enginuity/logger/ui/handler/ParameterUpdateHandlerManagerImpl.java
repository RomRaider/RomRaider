package enginuity.logger.ui.handler;

import enginuity.logger.definition.EcuParameter;

import java.util.ArrayList;
import java.util.List;

public final class ParameterUpdateHandlerManagerImpl implements ParameterUpdateHandlerManager {
    private final List<ParameterUpdateHandler> handlers = new ArrayList<ParameterUpdateHandler>();

    public synchronized void addHandler(ParameterUpdateHandler handler) {
        handlers.add(handler);
    }

    public synchronized void registerParam(EcuParameter ecuParam) {
        for (ParameterUpdateHandler handler : handlers) {
            handler.registerParam(ecuParam);
        }
    }

    public synchronized void handleParamUpdate(EcuParameter ecuParam, byte[] value, long timestamp) {
        for (ParameterUpdateHandler handler : handlers) {
            handler.handleParamUpdate(ecuParam, value, timestamp);
        }
    }

    public synchronized void deregisterParam(EcuParameter ecuParam) {
        for (ParameterUpdateHandler handler : handlers) {
            handler.deregisterParam(ecuParam);
        }
    }
}
