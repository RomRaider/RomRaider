package enginuity.logger.ui;

import enginuity.logger.definition.EcuData;

import java.util.List;

public interface ParameterRegistrationBroker {

    void registerEcuParameterForLogging(EcuData ecuData);

    void deregisterEcuParameterFromLogging(EcuData ecuData);

    List<String> listSerialPorts();

    void start();

    void stop();

}
