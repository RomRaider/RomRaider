package enginuity.logger.ecu.ui.swing.menubar.action;

import enginuity.logger.ecu.EcuLogger;

import javax.swing.JToggleButton;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public final class ToggleButtonAction extends AbstractAction {
    private final JToggleButton button;

    public ToggleButtonAction(EcuLogger logger, JToggleButton button) {
        super(logger);
        this.button = button;
    }

    public void actionPerformed(ActionEvent actionEvent) {
        button.setSelected(!button.isSelected());
        ActionListener[] actionListeners = button.getActionListeners();
        for (ActionListener listener : actionListeners) {
            listener.actionPerformed(actionEvent);
        }
    }
}
