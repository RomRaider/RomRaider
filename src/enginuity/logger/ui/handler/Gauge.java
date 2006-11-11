package enginuity.logger.ui.handler;

import javax.swing.*;

public abstract class Gauge extends JPanel {

    public abstract void refreshTitle();

    public abstract void updateValue(byte[] value);

    public abstract void resetValue();

}
