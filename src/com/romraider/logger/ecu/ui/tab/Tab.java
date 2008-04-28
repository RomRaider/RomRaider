package com.romraider.logger.ecu.ui.tab;

import javax.swing.JPanel;

public interface Tab {
    JPanel getPanel();

    boolean isValidMafvChange(double value);
}
