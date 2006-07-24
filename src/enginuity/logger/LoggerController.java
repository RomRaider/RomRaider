package enginuity.logger;

import enginuity.logger.query.LoggerCallback;

import java.util.List;

public interface LoggerController {

    List<String> listSerialPorts();

    void start();

    void addLogger(String address, LoggerCallback callback);

    void removeLogger(String address);

    void stop();

}
