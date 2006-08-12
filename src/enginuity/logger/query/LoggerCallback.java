package enginuity.logger.query;

import enginuity.logger.definition.convertor.EcuParameterConvertor;

public interface LoggerCallback {

    void callback(byte[] value, EcuParameterConvertor convertor);

}
