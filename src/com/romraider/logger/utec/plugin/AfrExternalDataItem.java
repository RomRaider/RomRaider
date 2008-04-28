package com.romraider.logger.utec.plugin;

import com.romraider.logger.ecu.external.ExternalDataItem;
import com.romraider.logger.utec.gui.mapTabs.UtecDataManager;

public class AfrExternalDataItem implements ExternalDataItem {

    public String getName() {
        return "TXS WB AFR";
    }

    public String getDescription() {
        return "TXS Tuner WB AFR";
    }

    public String getUnits() {
        return "lambda";
    }

    public double getData() {
        return UtecDataManager.getAfrData();
    }

}
