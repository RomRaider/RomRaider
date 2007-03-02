package enginuity.logger.ui.handler.file;

import enginuity.logger.ui.StatusChangeListener;
import enginuity.logger.ui.handler.DataUpdateHandler;

public interface FileUpdateHandler extends DataUpdateHandler {

    void addListener(StatusChangeListener listener);

    void start();

    void stop();
}
