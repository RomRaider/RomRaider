package enginuity.logger.protocol;

public interface Protocol {

    byte[] constructReadMemoryRequest(byte[] fromAddress, int numBytes);

    byte[] constructReadAddressRequest(byte[]... addresses);

    byte[] constructEcuInitRequest();

    byte[] extractResponseData(byte[] response);

}
