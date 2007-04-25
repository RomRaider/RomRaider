package enginuity.ramtune.test.command.generator;

import enginuity.io.protocol.Protocol;

import static java.util.Arrays.asList;
import java.util.List;

public final class EcuInitCommandGenerator extends AbstractCommandGenerator {
    
    public EcuInitCommandGenerator(Protocol protocol) {
        super(protocol);
    }

    public List<byte[]> createCommands(byte[] data, byte[] address, int length) {
        return asList(protocol.constructEcuInitRequest());
    }

    public String toString() {
        return "Init";
    }
}
