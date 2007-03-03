package enginuity.logger.ecu.ui.swing.menubar;

import enginuity.logger.ecu.ui.swing.menubar.util.SelectionStateAdaptor;

import javax.swing.Action;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.KeyStroke;

public final class EcuLoggerRadioButtonMenuItem extends JRadioButtonMenuItem {

    public EcuLoggerRadioButtonMenuItem(String text, int mnemonic, KeyStroke accelerator, Action action, boolean selected) {
        super(action);
        initSelectionStateAdaptor(action);
        setText(text);
        setMnemonic(mnemonic);
        setAccelerator(accelerator);
        setSelected(selected);
    }

    private void initSelectionStateAdaptor(Action action) {
        new SelectionStateAdaptor(action, this).configure();
    }

}
