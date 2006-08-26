package enginuity.logger.ui;

import enginuity.Settings;
import enginuity.logger.io.serial.SerialPortRefreshListener;
import static enginuity.util.ParamChecker.checkNotNull;

import javax.swing.*;
import java.util.Set;
import java.util.TreeSet;

public final class SerialPortComboBox extends JComboBox implements SerialPortRefreshListener {
    private final Settings settings;

    public SerialPortComboBox(Settings settings) {
        checkNotNull(settings);
        this.settings = settings;
    }

    public void refreshPortList(Set<String> ports) {
        checkNotNull(ports);
        boolean changeDetected = ports.isEmpty() || ports.size() != getItemCount();
        if (!changeDetected) {
            for (int i = 0; i < getItemCount(); i++) {
                String port = (String) getItemAt(i);
                if (!ports.contains(port)) {
                    changeDetected = true;
                    break;
                }
            }
            if (!changeDetected) {
                Set<String> comboPorts = new TreeSet<String>();
                for (int i = 0; i < getItemCount(); i++) {
                    comboPorts.add((String) getItemAt(i));
                }
                for (String port : ports) {
                    if (!comboPorts.contains(port)) {
                        changeDetected = true;
                        break;
                    }
                }
            }
        }
        if (changeDetected) {
            String selectedPort = (String) getSelectedItem();
            removeAllItems();
            if (!ports.isEmpty()) {
                for (String port : ports) {
                    addItem(port);
                }
                if (selectedPort != null) {
                    if (ports.contains(selectedPort)) {
                        setSelectedItem(selectedPort);
                    }
                    settings.setLoggerPort(selectedPort);
                } else {
                    setSelectedIndex(0);
                    settings.setLoggerPort((String) getItemAt(0));
                }
            }
        }
    }
}
