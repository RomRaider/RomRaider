package enginuity.logger.ecu.ui.handler.file;

import enginuity.logger.ecu.definition.EcuSwitch;

public interface FileLoggerControllerSwitchMonitor {

    void monitorFileLoggerSwitch(double switchValue);

    EcuSwitch getEcuSwitch();
}
