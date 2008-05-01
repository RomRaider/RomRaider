package com.romraider.logger.ecu.ui.swing.menubar.action;

import static com.romraider.Version.BUILDNUMBER;
import static com.romraider.Version.PRODUCT_NAME;
import static com.romraider.Version.SUPPORT_URL;
import static com.romraider.Version.VERSION;
import com.romraider.logger.ecu.EcuLogger;
import static javax.swing.JOptionPane.INFORMATION_MESSAGE;
import static javax.swing.JOptionPane.showMessageDialog;
import java.awt.event.ActionEvent;

public final class AboutAction extends AbstractAction {

    public AboutAction(EcuLogger logger) {
        super(logger);
    }

    public void actionPerformed(ActionEvent actionEvent) {
        String message = PRODUCT_NAME + " ECU Logger\n"
                + "Version: " + VERSION + " [build #" + BUILDNUMBER + "]\n"
                + SUPPORT_URL;
        String title = "About " + PRODUCT_NAME;
        showMessageDialog(logger, message, title, INFORMATION_MESSAGE);
    }
}
