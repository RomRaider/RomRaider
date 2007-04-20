package enginuity.ramtune.test.command.generator;

import enginuity.io.protocol.Protocol;
import static enginuity.util.ParamChecker.checkNotNullOrEmpty;

public final class WriteCommandGenerator extends AbstractCommandGenerator {

    public WriteCommandGenerator(Protocol protocol) {
        super(protocol);
    }

    public byte[] createCommand(byte[] address, byte[] data) {
        checkNotNullOrEmpty(address, "address");
        checkNotNullOrEmpty(data, "data");
        return protocol.constructWriteMemoryRequest(address, data);
    }

    public String toString() {
        return "Write";
    }
}
