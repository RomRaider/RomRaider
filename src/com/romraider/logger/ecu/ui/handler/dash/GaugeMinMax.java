package com.romraider.logger.ecu.ui.handler.dash;

public final class GaugeMinMax {
    public final double min;
    public final double max;
    public final double step;

    public GaugeMinMax(double min, double max, double step) {
        this.min = min;
        this.max = max;
        this.step = step;
    }
}