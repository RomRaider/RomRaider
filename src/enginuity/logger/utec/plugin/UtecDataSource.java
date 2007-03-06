package enginuity.logger.utec.plugin;

import enginuity.logger.ecu.external.ExternalDataItem;
import enginuity.logger.ecu.external.ExternalDataSource;

import java.util.ArrayList;
import java.util.List;

public final class UtecDataSource implements ExternalDataSource {

    public String getName() {
        return "UTEC Datasource";
    }

    public String getVersion() {
        return "0.01";
    }

    public List<ExternalDataItem> getDataItems() {
        return new ArrayList<ExternalDataItem>();
    }
}
