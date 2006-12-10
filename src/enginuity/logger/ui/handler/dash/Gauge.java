package enginuity.logger.ui.handler.dash;

import javax.swing.*;

public abstract class Gauge extends JPanel {

    public abstract void refreshTitle();

    public abstract void updateValue(byte[] value);

    public abstract void resetValue();

}
