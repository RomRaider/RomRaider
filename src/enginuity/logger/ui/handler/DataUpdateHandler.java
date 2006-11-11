package enginuity.logger.ui.handler;

import enginuity.logger.definition.EcuData;

public interface DataUpdateHandler {

    void registerData(EcuData ecuData);

    void handleDataUpdate(EcuData ecuData, byte[] value, long timestamp);

    void deregisterData(EcuData ecuData);

    void cleanUp();

}
