package com.romraider.logger.ecu.ui.handler.file;

import com.romraider.logger.ecu.ui.StatusChangeListener;
import com.romraider.logger.ecu.ui.handler.DataUpdateHandler;

public interface FileUpdateHandler extends DataUpdateHandler {

    void addListener(StatusChangeListener listener);

    void start();

    void stop();
}
