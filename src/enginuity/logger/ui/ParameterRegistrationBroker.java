package enginuity.logger.ui;

import enginuity.logger.definition.EcuData;

public interface ParameterRegistrationBroker {

    void registerEcuParameterForLogging(EcuData ecuData);

    void deregisterEcuParameterFromLogging(EcuData ecuData);

    void start();

    void stop();

}
