package enginuity.logger.query;

import enginuity.util.HexUtil;
import static enginuity.util.ParamChecker.checkNotNull;
import static enginuity.util.ParamChecker.checkNotNullOrEmpty;

public final class DefaultRegisteredQuery implements RegisteredQuery {
    private final String address;
    private final LoggerCallback callback;
    private final byte[] bytes;

    public DefaultRegisteredQuery(String address, LoggerCallback callback) {
        checkNotNullOrEmpty(address, "address");
        checkNotNull(callback, "callback");
        this.address = address;
        this.callback = callback;
        bytes = HexUtil.asBytes(address);
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
