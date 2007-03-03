package enginuity.logger.ecu.ui.swing.menubar.action;

import enginuity.logger.ecu.EcuLogger;

import java.awt.event.ActionEvent;

public final class LogFileControllerSwitchAction extends AbstractAction {

    public LogFileControllerSwitchAction(EcuLogger logger) {
        super(logger);
    }

    public void actionPerformed(ActionEvent actionEvent) {
        try {
            logger.getSettings().setFileLoggingControllerSwitchActive((Boolean) getValue(SELECTED_KEY));
        } catch (Exception e) {
            logger.reportError(e);
        }
    }
}
