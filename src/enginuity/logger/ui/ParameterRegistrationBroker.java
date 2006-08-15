package enginuity.logger.ui;

import enginuity.logger.definition.EcuParameter;

import java.util.List;

public interface ParameterRegistrationBroker {
    void registerEcuParameterForLogging(EcuParameter ecuParam);

    void deregisterEcuParameterFromLogging(EcuParameter ecuParam);

    List<String> listSerialPorts();

    void start();

    void stop();

}
