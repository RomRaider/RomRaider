package enginuity.logger.query;

import enginuity.logger.definition.EcuParameter;
import static enginuity.util.HexUtil.asBytes;
import static enginuity.util.ParamChecker.checkNotNull;

//TODO: change address into an EcuParameter object with getAddress() & getLength() methods
//TODO: use the getLength() method to do the response data extraction in SSMProtocol

@SuppressWarnings({"FieldCanBeLocal"})
public final class RegisteredQueryImpl implements RegisteredQuery {
    private final EcuParameter ecuParam;
    private final LoggerCallback callback;
    private final byte[] bytes;

    public RegisteredQueryImpl(EcuParameter ecuParam, LoggerCallback callback) {
        checkNotNull(ecuParam, callback);
        this.ecuParam = ecuParam;
        this.callback = callback;
        bytes = asBytes(ecuParam.getAddress());
    }

    public String getAddress() {
        return ecuParam.getAddress();
    }

    public byte[] getBytes() {
        return bytes;
    }

    public void setResponse(byte[] response) {
        callback.callback(response, ecuParam.getConvertor());
    }
}
