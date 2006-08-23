package enginuity.logger.ui.handler;

import enginuity.logger.definition.EcuData;

import java.util.ArrayList;
import java.util.List;

public final class DataUpdateHandlerManagerImpl implements DataUpdateHandlerManager {
    private final List<DataUpdateHandler> handlers = new ArrayList<DataUpdateHandler>();

    public synchronized void addHandler(DataUpdateHandler handler) {
        handlers.add(handler);
    }

    public synchronized void registerData(EcuData ecuData) {
        for (DataUpdateHandler handler : handlers) {
            handler.registerData(ecuData);
        }
    }

    public synchronized void handleDataUpdate(EcuData ecuData, byte[] value, long timestamp) {
        for (DataUpdateHandler handler : handlers) {
            handler.handleDataUpdate(ecuData, value, timestamp);
        }
    }

    public synchronized void deregisterData(EcuData ecuData) {
        for (DataUpdateHandler handler : handlers) {
            handler.deregisterData(ecuData);
        }
    }

    public synchronized void cleanUp() {
        for (DataUpdateHandler handler : handlers) {
            handler.cleanUp();
        }
    }
}
