package enginuity.logger.ecu.comms.query;

import enginuity.logger.ecu.definition.ExternalData;

public interface ExternalQuery extends Query {

    ExternalData getExternalData();

    void setResponse(double response);
}
