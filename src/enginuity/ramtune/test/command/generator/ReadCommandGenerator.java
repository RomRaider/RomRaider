package enginuity.ramtune.test.command.generator;

import enginuity.io.protocol.Protocol;
import static enginuity.util.ParamChecker.checkNotNullOrEmpty;

public final class ReadCommandGenerator extends AbstractCommandGenerator {

    public ReadCommandGenerator(Protocol protocol) {
        super(protocol);
    }

    public byte[] createCommand(byte[] address, byte[] data) {
        checkNotNullOrEmpty(address, "address");
        return protocol.constructReadAddressRequest(new byte[][]{address});
    }

    public String toString() {
        return "Read";
    }
}
