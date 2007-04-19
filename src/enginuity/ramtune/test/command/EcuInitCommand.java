package enginuity.ramtune.test.command;

import enginuity.io.protocol.Protocol;
import enginuity.io.protocol.SSMProtocol;

public final class EcuInitCommand implements Command {
    private final Protocol protocol = new SSMProtocol();

    public byte[] constructCommandRequest(byte[] address, byte[] data) {
        return protocol.constructEcuInitRequest();
    }

    public String toString() {
        return "ECU Init";
    }
}
