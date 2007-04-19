package enginuity.ramtune.test.command;

import enginuity.io.protocol.Protocol;
import enginuity.io.protocol.SSMProtocol;

public final class ReadCommand implements Command {
    private final Protocol protocol = new SSMProtocol();

    public byte[] constructCommandRequest(byte[] address, byte[] data) {
        return protocol.constructReadAddressRequest(new byte[][] {address});
    }

    public String toString() {
        return "Read";
    }
}
