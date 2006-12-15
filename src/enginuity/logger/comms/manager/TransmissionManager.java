package enginuity.logger.comms.manager;

import enginuity.logger.comms.query.RegisteredQuery;

import java.util.Collection;

public interface TransmissionManager {

    void start();

    void sendQueries(Collection<RegisteredQuery> query);

    void stop();

}
