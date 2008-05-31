package com.romraider.logger.ecu.ui.handler.dash;

public interface GaugeUpdateListener {
    void refreshTitle();

    public void updateValue(double value);

    public void resetValue();
}
