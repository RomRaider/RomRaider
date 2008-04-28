package com.romraider.logger.ecu.ui.handler.file;

import com.romraider.logger.ecu.definition.EcuSwitch;
import static com.romraider.util.ParamChecker.checkNotNull;

public final class FileLoggerControllerSwitchMonitorImpl implements FileLoggerControllerSwitchMonitor {
    private final EcuSwitch fileLoggingSwitch;
    private final FileLoggerControllerSwitchHandler handler;

    public FileLoggerControllerSwitchMonitorImpl(EcuSwitch fileLoggingSwitch, FileLoggerControllerSwitchHandler handler) {
        checkNotNull(fileLoggingSwitch, handler);
        this.fileLoggingSwitch = fileLoggingSwitch;
        this.handler = handler;
    }

    public void monitorFileLoggerSwitch(double switchValue) {
        handler.handleSwitch(switchValue);
    }

    public EcuSwitch getEcuSwitch() {
        return fileLoggingSwitch;
    }
}
