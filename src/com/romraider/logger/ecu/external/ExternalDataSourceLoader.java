package com.romraider.logger.ecu.external;

import java.util.List;

public interface ExternalDataSourceLoader {

    void loadExternalDataSources();

    List<ExternalDataSource> getExternalDataSources();
}
