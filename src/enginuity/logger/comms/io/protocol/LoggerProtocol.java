package enginuity.logger.comms.io.protocol;

import enginuity.io.connection.ConnectionProperties;
import enginuity.logger.comms.query.RegisteredQuery;

import java.util.Collection;

public interface LoggerProtocol {

    byte[] constructReadAddressRequest(Collection<RegisteredQuery> queries);

    byte[] constructReadAddressResponse(Collection<RegisteredQuery> queries);

    void processReadAddressResponses(Collection<RegisteredQuery> queries, byte[] response);

    ConnectionProperties getConnectionProperties();

}
