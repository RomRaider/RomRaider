package enginuity.logger.protocol;

import enginuity.logger.query.RegisteredQuery;

import java.util.Collection;

public interface Protocol {

    byte[] constructEcuInitRequest();

    byte[] constructReadMemoryRequest(RegisteredQuery query, int numBytes);

    byte[] constructReadAddressRequest(Collection<RegisteredQuery> queries);

    byte[] constructReadAddressResponse(Collection<RegisteredQuery> queries);

    void setResponse(Collection<RegisteredQuery> queries, byte[] response);

    byte[] extractResponseData(byte[] response);

    ConnectionProperties getConnectionProperties();

}
