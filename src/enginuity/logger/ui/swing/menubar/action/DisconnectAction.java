package enginuity.logger.ui.swing.menubar.action;

import enginuity.logger.EcuLogger;

import java.awt.event.ActionEvent;

public final class DisconnectAction extends AbstractAction {

    public DisconnectAction(EcuLogger logger) {
        super(logger);
    }

    public void actionPerformed(ActionEvent actionEvent) {
        try {
            logger.stopLogging();
        } catch (Exception e) {
            logger.reportError(e);
        }
    }
}
