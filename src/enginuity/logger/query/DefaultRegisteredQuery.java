package enginuity.logger.query;

import static enginuity.util.HexUtil.asBytes;
import static enginuity.util.ParamChecker.checkNotNull;
import static enginuity.util.ParamChecker.checkNotNullOrEmpty;

//TODO: change address into an EcuParameter object with getAddress() & getLength() methods
//TODO: use the getLength() method to do the response data extraction in SSMProtocol

public final class DefaultRegisteredQuery implements RegisteredQuery {
    private final String address;
    private final LoggerCallback callback;
    private final byte[] bytes;

    public DefaultRegisteredQuery(String address, LoggerCallback callback) {
        checkNotNullOrEmpty(address, "address");
        checkNotNull(callback, "callback");
        this.address = address;
        this.callback = callback;
        bytes = asBytes(address);
    }

    public String getAddress() {
        return address;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public void setResponse(byte[] response) {
        callback.callback(response);
    }
}
