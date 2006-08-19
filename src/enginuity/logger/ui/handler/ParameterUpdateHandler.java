package enginuity.logger.ui.handler;

import enginuity.logger.definition.EcuParameter;

public interface ParameterUpdateHandler {

    void registerParam(EcuParameter ecuParam);

    void handleParamUpdate(EcuParameter ecuParam, byte[] value, long timestamp);

    void deregisterParam(EcuParameter ecuParam);

}
