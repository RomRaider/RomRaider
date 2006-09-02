package enginuity.logger.ui;

import enginuity.logger.definition.EcuData;

public interface ParameterRegistrationBroker extends ControllerListener {

    void registerEcuParameterForLogging(EcuData ecuData);

    void deregisterEcuParameterFromLogging(EcuData ecuData);

}
