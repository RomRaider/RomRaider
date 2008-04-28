package com.romraider.logger.utec.plugin;


import com.romraider.logger.ecu.external.ExternalDataItem;
import com.romraider.logger.utec.gui.mapTabs.UtecDataManager;

public class KnockExternalDataItem implements ExternalDataItem {

    public String getName() {
        return "TXS Knock";
    }

    public String getDescription() {
        return "TXS Tuner Knock Count";
    }

    public String getUnits() {
        return "n/a";
    }

    public double getData() {
        return UtecDataManager.getKnockData();
    }

}
