package enginuity.logger.aem.plugin;

import enginuity.logger.ecu.external.ExternalDataItem;

public final class AemDataItem implements ExternalDataItem, DataListener {
    private final AemConvertor convertor = new AemConvertorImpl();
    private byte[] bytes;

    public String getName() {
        return "AEM UEGO";
    }

    public String getDescription() {
        return "AEM UEGO AFR data";
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
