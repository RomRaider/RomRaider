package enginuity.logger.utec.plugin;


import enginuity.logger.ecu.external.ExternalDataItem;
import enginuity.logger.utec.gui.mapTabs.UtecDataManager;

public class PsiExternalDataItem implements ExternalDataItem{

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
