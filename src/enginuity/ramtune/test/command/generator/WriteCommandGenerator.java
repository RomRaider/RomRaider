package enginuity.ramtune.test.command.generator;

import enginuity.io.protocol.Protocol;
import static enginuity.util.ParamChecker.checkNotNullOrEmpty;

import static java.util.Arrays.asList;
import java.util.List;

public final class WriteCommandGenerator extends AbstractCommandGenerator {

    public WriteCommandGenerator(Protocol protocol) {
        super(protocol);
    }

    public List<byte[]> createCommands(byte[] data, byte[] address, int length) {
        checkNotNullOrEmpty(address, "address");
        checkNotNullOrEmpty(data, "data");
        return asList(protocol.constructWriteMemoryRequest(address, data));
    }

    public String toString() {
        return "Write";
    }
}
