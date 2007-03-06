package enginuity.logger.ecu.comms.query;

import enginuity.logger.ecu.definition.ExternalData;
import static enginuity.util.ParamChecker.checkNotNull;

public final class ExternalQueryImpl implements ExternalQuery {
    private final ExternalData externalData;
    private final LoggerCallback callback;

    public ExternalQueryImpl(ExternalData externalData, LoggerCallback callback) {
        checkNotNull(externalData, callback);
        this.externalData = externalData;
        this.callback = callback;
    }

    public ExternalData getExternalData() {
        return externalData;
    }

    public void setResponse(double response) {
        callback.callback(response);
    }
}
