package com.romraider.logger.ecu.comms.query;

import com.romraider.logger.ecu.definition.LoggerData;

public interface Query {

    LoggerData getLoggerData();

    double getResponse();

}
