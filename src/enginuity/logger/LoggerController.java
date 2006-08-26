package enginuity.logger;

import enginuity.logger.definition.EcuData;
import enginuity.logger.query.LoggerCallback;

import java.util.Set;

public interface LoggerController {

    Set<String> listSerialPorts();

    void addLogger(EcuData ecuData, LoggerCallback callback);

    void removeLogger(EcuData ecuData);

    void start();

    void stop();

}
