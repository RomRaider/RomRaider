package enginuity.logger.comms;

import enginuity.logger.query.Query;

public interface SerialConnection {

    byte[] transmit(Query query);

    void close();

}
