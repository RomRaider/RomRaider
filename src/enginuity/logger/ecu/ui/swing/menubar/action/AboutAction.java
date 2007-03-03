package enginuity.logger.ecu.ui.swing.menubar.action;

import enginuity.logger.ecu.EcuLogger;

import static javax.swing.JOptionPane.INFORMATION_MESSAGE;
import static javax.swing.JOptionPane.showMessageDialog;
import java.awt.event.ActionEvent;

public final class AboutAction extends AbstractAction {

    public AboutAction(EcuLogger logger) {
        super(logger);
    }

    public void actionPerformed(ActionEvent actionEvent) {
        showMessageDialog(logger, "Enginuity ECU Logger\nhttp://www.enginuity.org/", "About", INFORMATION_MESSAGE);
    }
}
