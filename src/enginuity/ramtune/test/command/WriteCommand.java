package enginuity.ramtune.test.command;

import enginuity.io.protocol.Protocol;
import enginuity.io.protocol.SSMProtocol;

public final class WriteCommand implements Command {
    private final Protocol protocol = new SSMProtocol();

    public byte[] constructCommandRequest(byte[] address, byte[] data) {
        return protocol.constructWriteMemoryRequest(address, data);
    }

    public String toString() {
        return "Write";
    }
}
