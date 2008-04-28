package com.romraider.logger.ecu.comms.query;

import com.romraider.logger.ecu.definition.ExternalData;
import com.romraider.logger.ecu.definition.LoggerData;
import static com.romraider.util.ParamChecker.checkNotNull;

public final class ExternalQueryImpl implements ExternalQuery {
    private final ExternalData externalData;
    private double response;

    public ExternalQueryImpl(ExternalData externalData) {
        checkNotNull(externalData);
        this.externalData = externalData;
    }

    public LoggerData getLoggerData() {
        return externalData;
    }

    public void setResponse(double response) {
        this.response = response;
    }

    public double getResponse() {
        return response;
    }
}
