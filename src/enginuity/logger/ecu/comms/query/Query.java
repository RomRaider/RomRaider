package enginuity.logger.ecu.comms.query;

import enginuity.logger.ecu.definition.LoggerData;

public interface Query {

    LoggerData getLoggerData();

    double getResponse();
    
}
