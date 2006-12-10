package enginuity.logger.comms.io.serial.protocol;

import enginuity.logger.comms.query.RegisteredQuery;

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
