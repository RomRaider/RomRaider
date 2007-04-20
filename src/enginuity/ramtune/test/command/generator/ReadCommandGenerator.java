package enginuity.ramtune.test.command.generator;

import enginuity.io.protocol.Protocol;
import enginuity.ramtune.test.command.Command;
import enginuity.ramtune.test.command.CommandImpl;
import static enginuity.util.ParamChecker.checkNotNullOrEmpty;

public final class ReadCommandGenerator extends AbstractCommandGenerator {

    public ReadCommandGenerator(Protocol protocol) {
        super(protocol);
    }

    public Command createCommand(byte[] address, byte[] data) {
        checkNotNullOrEmpty(address, "address");
        return new CommandImpl(protocol.constructReadAddressRequest(new byte[][] {address}));
    }

    public String toString() {
        return "Read";
    }
}
