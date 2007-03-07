package enginuity.logger.utec.plugin;

import enginuity.logger.ecu.external.ExternalDataItem;
import enginuity.logger.ecu.external.ExternalDataSource;

import java.util.ArrayList;
import java.util.List;

//NOTE: This class is instantiated via a no-args constructor.
public final class UtecDataSource implements ExternalDataSource {
	private ArrayList<ExternalDataItem> externalDataItems = new ArrayList<ExternalDataItem>();
	
	public UtecDataSource(){
		externalDataItems.add(new AfrExternalDataItem());
		externalDataItems.add(new PsiExternalDataItem());
		externalDataItems.add(new KnockExternalDataItem());
	}
	
    public String getName() {
        return "UTEC Datasource";
    }

    public String getVersion() {
        return "0.01";
    }

    public List<ExternalDataItem> getDataItems() {
        System.out.println("External data items requested.");
        
        return new ArrayList<ExternalDataItem>();
    }
}
