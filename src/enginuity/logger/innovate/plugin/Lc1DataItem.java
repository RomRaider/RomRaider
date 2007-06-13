package enginuity.logger.innovate.plugin;

import enginuity.logger.ecu.external.ExternalDataItem;

public final class Lc1DataItem implements ExternalDataItem, DataListener {
    private final Lc1Convertor convertor = new Lc1ConvertorImpl();
    private byte[] bytes;

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
        if (bytes != null) {
            return convertor.convert(bytes);
        } else {
            return 0.0;
        }
    }

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }
}
