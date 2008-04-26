package enginuity.logger.utec.plugin;

import enginuity.logger.ecu.external.ExternalDataItem;
import enginuity.logger.utec.gui.mapTabs.UtecDataManager;

public class LoadExternalDataItem implements ExternalDataItem {

    public String getName() {
        return "TXS LOAD";
    }

    public String getDescription() {
        return "TXS Utec Load";
    }

    public String getUnits() {
        return "n/a";
    }

    public double getData() {
        return UtecDataManager.getLoadData();
    }

}