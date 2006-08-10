package enginuity.logger.comms;

import enginuity.logger.query.RegisteredQuery;

import java.util.Collection;

public interface SerialConnection {

    byte[] sendEcuInit();

    byte[] send(byte[] bytes);

    void sendAddressReads(Collection<RegisteredQuery> queries);

    void close();

}
