package enginuity.logger.query;

import enginuity.logger.definition.EcuParameter;
import static enginuity.util.HexUtil.asBytes;
import static enginuity.util.HexUtil.asHex;
import static enginuity.util.ParamChecker.checkNotNull;

public final class RegisteredQueryImpl implements RegisteredQuery {
    private final EcuParameter ecuParam;
    private final LoggerCallback callback;
    private final byte[] bytes;

    public RegisteredQueryImpl(EcuParameter ecuParam, LoggerCallback callback) {
        checkNotNull(ecuParam, callback);
        this.ecuParam = ecuParam;
        this.callback = callback;
        bytes = getAddressBytes(ecuParam);
    }

    public EcuParameter getEcuParam() {
        return ecuParam;
    }

    public String[] getAddresses() {
        return ecuParam.getAddresses();
    }

    public byte[] getBytes() {
        return bytes;
    }

    public String getHex() {
        return asHex(bytes);
    }

    public void setResponse(byte[] response) {
        callback.callback(response);
    }

    private byte[] getAddressBytes(EcuParameter ecuParam) {
        String[] addresses = ecuParam.getAddresses();
        byte[] bytes = new byte[0];
        for (String address : addresses) {
            byte[] tmp1 = asBytes(address);
            byte[] tmp2 = new byte[bytes.length + tmp1.length];
            System.arraycopy(bytes, 0, tmp2, 0, bytes.length);
            System.arraycopy(tmp1, 0, tmp2, bytes.length, tmp1.length);
            bytes = tmp2;
        }
        return bytes;
    }
}
