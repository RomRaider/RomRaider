package com.romraider.logger.ecu.ui.handler.file;

import com.romraider.logger.ecu.definition.EcuSwitch;

public interface FileLoggerControllerSwitchMonitor {

    void monitorFileLoggerSwitch(double switchValue);

    EcuSwitch getEcuSwitch();
}
