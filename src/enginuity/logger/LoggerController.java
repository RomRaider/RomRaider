package enginuity.logger;

import enginuity.logger.definition.EcuParameter;
import enginuity.logger.query.LoggerCallback;

import java.util.List;

public interface LoggerController {

    List<String> listSerialPorts();

    void addLogger(EcuParameter ecuParam, LoggerCallback callback);

    void removeLogger(EcuParameter ecuParam);

    void start();

    void stop();

}
