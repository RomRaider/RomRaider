package enginuity.ramtune.test.command.generator;

import enginuity.io.protocol.Protocol;
import enginuity.ramtune.test.command.Command;
import enginuity.ramtune.test.command.CommandImpl;
import static enginuity.util.ParamChecker.checkNotNull;
import static enginuity.util.ParamChecker.checkNotNullOrEmpty;

public final class ReadCommandGenerator implements CommandGenerator {
    private final Protocol protocol;

    public ReadCommandGenerator(Protocol protocol) {
        checkNotNull(protocol, "protocol");
        this.protocol = protocol;
    }

    public Command createCommand(byte[] address, byte[] data) {
        checkNotNullOrEmpty(address, "address");
        return new CommandImpl(protocol.constructReadAddressRequest(new byte[][] {address}));
    }

    public String toString() {
        return "Read";
    }
}
