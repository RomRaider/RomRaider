package enginuity.logger.ecu.ui.swing.menubar;

import javax.swing.Action;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

public final class EcuLoggerMenuItem extends JMenuItem {

    public EcuLoggerMenuItem(String text, int mnemonic, Action action) {
        super(action);
        setText(text);
        setMnemonic(mnemonic);
    }

    public EcuLoggerMenuItem(String text, int mnemonic, KeyStroke accelerator, Action action) {
        this(text, mnemonic, action);
        setAccelerator(accelerator);
    }

}
