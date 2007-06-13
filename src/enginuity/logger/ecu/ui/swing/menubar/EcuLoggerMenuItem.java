package enginuity.logger.ecu.ui.swing.menubar;

import javax.swing.Action;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

public final class EcuLoggerMenuItem extends JMenuItem {

    public EcuLoggerMenuItem(String text, Action action) {
        super(action);
        setText(text);
    }

    public EcuLoggerMenuItem(String text, Action action, int mnemonic) {
        super(action);
        setText(text);
        setMnemonic(mnemonic);
    }

    public EcuLoggerMenuItem(String text, Action action, int mnemonic, KeyStroke accelerator) {
        this(text, action, mnemonic);
        setAccelerator(accelerator);
    }

}
