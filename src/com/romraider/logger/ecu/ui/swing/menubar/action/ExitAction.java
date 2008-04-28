package com.romraider.logger.ecu.ui.swing.menubar.action;

import com.romraider.logger.ecu.EcuLogger;
import java.awt.event.ActionEvent;

public final class ExitAction extends AbstractAction {

    public ExitAction(EcuLogger logger) {
        super(logger);
    }

    public void actionPerformed(ActionEvent actionEvent) {
        logger.setVisible(false);
        logger.handleExit();
        logger.dispose();
    }

}
