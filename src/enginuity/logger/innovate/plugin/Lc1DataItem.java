package enginuity.logger.innovate.plugin;

import enginuity.logger.ecu.external.ExternalDataItem;

public final class Lc1DataItem implements ExternalDataItem {
    private final double data;

    public Lc1DataItem(double data) {
        this.data = data;
    }

    public String getName() {
        return "LC-1";
    }

    public String getDescription() {
        return "Innovate LC-1 AFR data";
    }

    public String getUnits() {
        return "AFR";
    }

    public double getData() {
        return data;
    }
}
