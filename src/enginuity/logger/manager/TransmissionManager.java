package enginuity.logger.manager;

import enginuity.logger.query.LoggerCallback;
import enginuity.logger.query.RegisteredQuery;

import java.util.Collection;

public interface TransmissionManager {

    void start();

    void sendEcuInit(LoggerCallback callback);

    void sendQueries(Collection<RegisteredQuery> query);

    void stop();

}
