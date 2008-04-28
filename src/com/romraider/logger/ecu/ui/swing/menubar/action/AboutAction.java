package com.romraider.logger.ecu.ui.swing.menubar.action;

import com.romraider.logger.ecu.EcuLogger;
import static javax.swing.JOptionPane.INFORMATION_MESSAGE;
import static javax.swing.JOptionPane.showMessageDialog;
import java.awt.event.ActionEvent;

public final class AboutAction extends AbstractAction {

    public AboutAction(EcuLogger logger) {
        super(logger);
    }

    public void actionPerformed(ActionEvent actionEvent) {
        showMessageDialog(logger, "RomRaider ECU Logger\nhttp://www.romraider.com/", "About", INFORMATION_MESSAGE);
    }
}
