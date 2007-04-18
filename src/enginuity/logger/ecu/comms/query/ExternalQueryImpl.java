package enginuity.logger.ecu.comms.query;

import enginuity.logger.ecu.definition.ExternalData;
import enginuity.logger.ecu.definition.LoggerData;
import static enginuity.util.ParamChecker.checkNotNull;

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
