package enginuity.logger.ecu.external;

import java.util.List;

public interface ExternalDataSource {

    String getName();
    
    String getVersion();

    List<ExternalDataItem> getDataItems();
}
