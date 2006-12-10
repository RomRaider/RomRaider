package enginuity.logger.comms.io.serial;

import enginuity.logger.comms.query.RegisteredQuery;

import java.util.Collection;

public interface SerialConnection {

    byte[] sendEcuInit();

    byte[] send(byte[] bytes);

    void sendAddressReads(Collection<RegisteredQuery> queries);

    void close();

}
