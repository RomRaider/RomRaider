package enginuity.ramtune.test.command;

import static enginuity.util.ParamChecker.checkNotNull;

public final class CommandImpl implements Command {
    private final byte[] bytes;

    public CommandImpl(byte[] bytes) {
        checkNotNull(bytes, "bytes");
        this.bytes = bytes;
    }

    public byte[] getBytes() {
        return bytes;
    }
}
