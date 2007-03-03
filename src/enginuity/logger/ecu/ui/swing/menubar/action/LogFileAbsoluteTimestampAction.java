package enginuity.logger.ecu.ui.swing.menubar.action;

import enginuity.logger.ecu.EcuLogger;

import java.awt.event.ActionEvent;

public final class LogFileAbsoluteTimestampAction extends AbstractAction {

    public LogFileAbsoluteTimestampAction(EcuLogger logger) {
        super(logger);
    }

    public void actionPerformed(ActionEvent actionEvent) {
        try {
            logger.getSettings().setFileLoggingAbsoluteTimestamp((Boolean) getValue(SELECTED_KEY));
        } catch (Exception e) {
            logger.reportError(e);
        }
    }
}
