package enginuity.ramtune.test.command.generator;

import enginuity.io.protocol.Protocol;
import enginuity.ramtune.test.command.Command;
import enginuity.ramtune.test.command.CommandImpl;
import static enginuity.util.ParamChecker.checkNotNullOrEmpty;

public final class WriteCommandGenerator extends AbstractCommandGenerator {

    public WriteCommandGenerator(Protocol protocol) {
        super(protocol);
    }

    public Command createCommand(byte[] address, byte[] data) {
        checkNotNullOrEmpty(address, "address");
        checkNotNullOrEmpty(data, "data");
        return new CommandImpl(protocol.constructWriteMemoryRequest(address, data));
    }

    public String toString() {
        return "Write";
    }
}
