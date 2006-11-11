package enginuity.logger.ui;

import enginuity.logger.definition.EcuData;

public interface DataRegistrationBroker extends ControllerListener {

    void registerEcuDataForLogging(EcuData ecuData);

    void deregisterEcuDataFromLogging(EcuData ecuData);

    void clear();

}
