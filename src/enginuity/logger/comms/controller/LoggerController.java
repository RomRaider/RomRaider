package enginuity.logger.comms.controller;

import enginuity.logger.comms.query.LoggerCallback;
import enginuity.logger.definition.EcuData;
import enginuity.logger.ui.ControllerListener;

import java.util.Set;

public interface LoggerController {

    Set<String> listSerialPorts();

    void addLogger(String callerId, EcuData ecuData, LoggerCallback callback);

    void removeLogger(String callerId, EcuData ecuData);

    void start();

    void stop();

    void addListener(ControllerListener listener);
}
