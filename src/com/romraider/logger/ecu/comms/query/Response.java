package com.romraider.logger.ecu.comms.query;

import com.romraider.logger.ecu.definition.LoggerData;
import java.util.Set;

public interface Response {

    void setDataValue(LoggerData data, double value);

    Set<LoggerData> getData();

    double getDataValue(LoggerData data);

    long getTimestamp();
}
