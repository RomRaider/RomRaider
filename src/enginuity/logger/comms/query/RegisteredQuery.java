package enginuity.logger.comms.query;

import enginuity.logger.definition.EcuData;

public interface RegisteredQuery {

    String[] getAddresses();

    byte[] getBytes();

    String getHex();

    void setResponse(byte[] response);

    EcuData getEcuData();
}
