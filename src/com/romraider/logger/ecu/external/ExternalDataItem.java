package com.romraider.logger.ecu.external;

public interface ExternalDataItem {

    String getName();

    String getDescription();

    String getUnits();

    double getData();
}
