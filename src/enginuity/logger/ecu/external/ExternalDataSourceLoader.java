package enginuity.logger.ecu.external;

import enginuity.logger.ecu.definition.ExternalData;

import java.util.List;

public interface ExternalDataSourceLoader {

    void loadFromExternalDataSources();

    List<ExternalData> getExternalDatas();
}
