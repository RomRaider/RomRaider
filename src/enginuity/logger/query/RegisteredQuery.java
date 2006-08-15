package enginuity.logger.query;

import enginuity.logger.definition.EcuParameter;

public interface RegisteredQuery {

    String[] getAddresses();

    byte[] getBytes();

    String getHex();

    void setResponse(byte[] response);

    EcuParameter getEcuParam();
}
