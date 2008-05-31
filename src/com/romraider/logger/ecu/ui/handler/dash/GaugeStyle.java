package com.romraider.logger.ecu.ui.handler.dash;

import javax.swing.JPanel;

public interface GaugeStyle extends GaugeUpdateListener {
    void apply(JPanel panel);
}
