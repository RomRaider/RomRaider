package enginuity.logger.query;

import enginuity.util.ParamChecker;

public final class DefaultRegisteredQuery implements RegisteredQuery {
    private final String address;
    private final LoggerCallback callback;

    public DefaultRegisteredQuery(String address, LoggerCallback callback) {
        ParamChecker.checkNotNull(address, callback);
        this.address = address;
        this.callback = callback;
    }

    public String getAddress() {
        return address;
    }

    public void setResponse(byte[] response) {
        callback.callback(response);
    }
}
