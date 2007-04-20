package enginuity.ramtune.test.command.generator;

import enginuity.io.protocol.Protocol;
import static enginuity.util.ParamChecker.checkGreaterThanZero;
import static enginuity.util.ParamChecker.checkNotNullOrEmpty;

public final class ReadCommandGenerator extends AbstractCommandGenerator {

    public ReadCommandGenerator(Protocol protocol) {
        super(protocol);
    }

    public byte[] createCommand(byte[] address, byte[] data, int length) {
        checkNotNullOrEmpty(address, "address");
        checkGreaterThanZero(length, "length");
        return length == 1 ? protocol.constructReadAddressRequest(new byte[][]{address}) : protocol.constructReadMemoryRequest(address, length);
    }

    public String toString() {
        return "Read";
    }
}
