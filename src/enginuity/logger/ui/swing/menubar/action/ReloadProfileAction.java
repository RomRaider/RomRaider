package enginuity.logger.ui.swing.menubar.action;

import enginuity.logger.EcuLogger;

import java.awt.event.ActionEvent;

public final class ReloadProfileAction extends AbstractAction {

    public ReloadProfileAction(EcuLogger logger) {
        super(logger);
    }

    public void actionPerformed(ActionEvent actionEvent) {
        try {
            logger.loadUserProfile(logger.getSettings().getLoggerProfileFilePath());
        } catch (Exception e) {
            logger.reportError(e);
        }
    }
}
