package enginuity.ramtune.test.command.generator;

import enginuity.io.protocol.Protocol;
import enginuity.ramtune.test.command.Command;
import enginuity.ramtune.test.command.CommandImpl;

public final class EcuInitCommandGenerator extends AbstractCommandGenerator {
    
    public EcuInitCommandGenerator(Protocol protocol) {
        super(protocol);
    }

    public Command createCommand(byte[] address, byte[] data) {
        return new CommandImpl(protocol.constructEcuInitRequest());
    }

    public String toString() {
        return "ECU Init";
    }
}
