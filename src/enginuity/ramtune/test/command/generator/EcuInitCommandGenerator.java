package enginuity.ramtune.test.command.generator;

import enginuity.io.protocol.Protocol;

public final class EcuInitCommandGenerator extends AbstractCommandGenerator {
    
    public EcuInitCommandGenerator(Protocol protocol) {
        super(protocol);
    }

    public byte[] createCommand(byte[] address, byte[] data, int length) {
        return protocol.constructEcuInitRequest();
    }

    public String toString() {
        return "Init";
    }
}
