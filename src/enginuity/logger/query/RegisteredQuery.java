package enginuity.logger.query;

public interface RegisteredQuery {

    String getAddress();

    byte[] getBytes();

    void setResponse(byte[] response);

}
