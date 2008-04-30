package com.romraider.logger.ecu.ui.swing.menubar.action;

import com.romraider.Version;
import com.romraider.logger.ecu.EcuLogger;
import static javax.swing.JOptionPane.INFORMATION_MESSAGE;
import static javax.swing.JOptionPane.showMessageDialog;
import java.awt.event.ActionEvent;

public final class AboutAction extends AbstractAction {

    public AboutAction(EcuLogger logger) {
        super(logger);
    }

    public void actionPerformed(ActionEvent actionEvent) {
    	showMessageDialog(logger,
    			Version.PRODUCT_NAME + " RomRaider ECU logger\n" + "Version " + Version.VERSION + "\n" + 
    			"Build ID: " + Version.BUILDNUMBER + "\n" + Version.SUPPORT_URL, 
    			"About " + Version.PRODUCT_NAME, INFORMATION_MESSAGE);
    }
}
