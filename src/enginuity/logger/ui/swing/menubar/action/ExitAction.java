package enginuity.logger.ui.swing.menubar.action;

import enginuity.logger.EcuLogger;

import java.awt.event.ActionEvent;

public final class ExitAction extends AbstractAction {

    public ExitAction(EcuLogger logger) {
        super(logger);
    }

    public void actionPerformed(ActionEvent actionEvent) {
        logger.handleExit();
        logger.dispose();
        logger.setVisible(false);
    }

}
