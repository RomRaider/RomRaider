package com.romraider.logger.innovate.lm1.plugin;

import com.romraider.logger.ecu.external.ExternalDataItem;
import com.romraider.logger.innovate.generic.plugin.DataConvertor;
import com.romraider.logger.innovate.generic.plugin.DataListener;

public final class Lm1DataItem implements ExternalDataItem, DataListener {
    private final DataConvertor convertor = new Lm1DataConvertor();
    private byte[] bytes;

    public String getName() {
        return "Innovate LM-1";
    }

    public String getDescription() {
        return "Innovate LM-1 AFR data";
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
