package enginuity.logger.comms.manager;

import enginuity.logger.comms.query.LoggerCallback;
import enginuity.logger.comms.query.RegisteredQuery;

import java.util.Collection;

public interface TransmissionManager {

    void start();

    void sendEcuInit(LoggerCallback callback);

    void sendQueries(Collection<RegisteredQuery> query);

    void stop();

}
