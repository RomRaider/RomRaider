package enginuity.logger.comms.io.connection;

import enginuity.logger.comms.query.RegisteredQuery;

import java.util.Collection;

public interface LoggerConnection {

    void sendAddressReads(Collection<RegisteredQuery> queries);

    void close();

}
