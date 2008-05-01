package com.romraider.logger.ecu.ui.swing.menubar.action;

import static com.centerkey.utils.BareBonesBrowserLaunch.openURL;
import static com.romraider.Version.LOGGER_DEFS_URL;
import com.romraider.logger.ecu.EcuLogger;
import java.awt.event.ActionEvent;

public final class UpdateLoggerDefAction extends AbstractAction {

    public UpdateLoggerDefAction(EcuLogger logger) {
        super(logger);
    }

    public void actionPerformed(ActionEvent actionEvent) {
        openURL(LOGGER_DEFS_URL);
    }
}