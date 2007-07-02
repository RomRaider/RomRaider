package enginuity.logger.ecu.ui.swing.menubar.action;

import enginuity.logger.ecu.EcuLogger;

import static javax.swing.JOptionPane.OK_OPTION;
import static javax.swing.JOptionPane.WARNING_MESSAGE;
import static javax.swing.JOptionPane.YES_NO_OPTION;
import static javax.swing.JOptionPane.showConfirmDialog;
import static javax.swing.JOptionPane.showMessageDialog;
import java.awt.event.ActionEvent;

public final class ResetEcuAction extends AbstractAction {
    public ResetEcuAction(EcuLogger logger) {
        super(logger);
    }

    public void actionPerformed(ActionEvent actionEvent) {
        if (showConfirmation() == OK_OPTION) {
            //TODO: Finish reset!!
            /*
            -->  80 10 F0 05 B8 00 00 60 40 DD
            <--  80 10 F0 05 B8 00 00 60 40 DD 80 F0 10 02 F8 40 BA
             */
            showMessageDialog(logger, "Not yet implemented!");
        }
    }

    private int showConfirmation() {
        return showConfirmDialog(logger, "Do you want to rest the ECU?", "Reset ECU", YES_NO_OPTION, WARNING_MESSAGE);
    }
}
