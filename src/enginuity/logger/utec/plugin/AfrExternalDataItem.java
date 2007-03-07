package enginuity.logger.utec.plugin;

import enginuity.logger.ecu.external.ExternalDataItem;
import enginuity.logger.utec.gui.mapTabs.UtecDataManager;

public class AfrExternalDataItem implements ExternalDataItem{

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
