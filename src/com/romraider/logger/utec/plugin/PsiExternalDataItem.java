package com.romraider.logger.utec.plugin;


import com.romraider.logger.ecu.external.ExternalDataItem;
import com.romraider.logger.utec.gui.mapTabs.UtecDataManager;

public class PsiExternalDataItem implements ExternalDataItem {

    public String getName() {
        return "TXS PSI";
    }

    public String getDescription() {
        return "TXS Tuner Boost";
    }

    public String getUnits() {
        return "lb/in^2";
    }

    public double getData() {
        return UtecDataManager.getPsiData();
    }

}
