package com.romraider.logger.plx.plugin;

import com.romraider.logger.ecu.external.ExternalDataItem;

public interface PlxDataItem extends ExternalDataItem {
    void setRaw(int raw);
}
