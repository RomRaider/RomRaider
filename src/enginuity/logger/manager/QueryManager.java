package enginuity.logger.manager;

import enginuity.logger.query.RegisteredQuery;

public interface QueryManager extends Runnable {

    void addQuery(RegisteredQuery registeredQuery);

    void removeQuery(String address);

    void stop();

}
