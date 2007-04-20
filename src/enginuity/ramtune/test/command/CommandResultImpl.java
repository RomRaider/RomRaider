package enginuity.ramtune.test.command;

import static enginuity.util.ParamChecker.checkNotNull;

public final class CommandResultImpl implements CommandResult {
    private final byte[] result;

    public CommandResultImpl(byte[] result) {
        checkNotNull(result, "result");
        this.result = result;
    }

    public byte[] getResult() {
        return result;
    }
}
