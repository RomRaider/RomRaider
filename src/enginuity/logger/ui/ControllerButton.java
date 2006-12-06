package enginuity.logger.ui;

import javax.swing.*;

public final class ControllerButton extends JButton implements ControllerListener {
    private final boolean enabledOnInit;

    public ControllerButton(String string, boolean enabledOnInit) {
        super(string);
        this.enabledOnInit = enabledOnInit;
        setEnabled(enabledOnInit);
    }

    public void start() {
        setEnabled(!enabledOnInit);
    }

    public void stop() {
        setEnabled(enabledOnInit);
    }

}
