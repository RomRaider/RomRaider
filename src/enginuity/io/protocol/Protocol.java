package enginuity.io.protocol;

import enginuity.io.connection.ConnectionProperties;

public interface Protocol {

    byte[] constructEcuInitRequest();

    byte[] constructReadMemoryRequest(byte[] address, int numBytes);

    byte[] constructReadAddressRequest(byte[][] addresses);

    byte calculateChecksum(byte[] bytes);

    ConnectionProperties getConnectionProperties();

}
