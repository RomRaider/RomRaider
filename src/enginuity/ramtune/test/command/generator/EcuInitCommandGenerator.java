package enginuity.ramtune.test.command.generator;

import enginuity.io.protocol.Protocol;
import enginuity.ramtune.test.command.Command;
import enginuity.ramtune.test.command.CommandImpl;
import static enginuity.util.ParamChecker.checkNotNull;

public final class EcuInitCommandGenerator implements CommandGenerator {
    private final Protocol protocol;

    public EcuInitCommandGenerator(Protocol protocol) {
        checkNotNull(protocol, "protocol");
        this.protocol = protocol;
    }

    public Command createCommand(byte[] address, byte[] data) {
        return new CommandImpl(protocol.constructEcuInitRequest());
    }

    public String toString() {
        return "ECU Init";
    }
}
