package enginuity.logger.ecu.ui.handler.file;

import enginuity.logger.ecu.ui.StatusChangeListener;
import enginuity.logger.ecu.ui.handler.DataUpdateHandler;

public interface FileUpdateHandler extends DataUpdateHandler {

    void addListener(StatusChangeListener listener);

    void start();

    void stop();
}
