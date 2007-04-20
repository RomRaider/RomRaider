package enginuity.ramtune.test.command.generator;

import enginuity.io.protocol.Protocol;
import static enginuity.util.ParamChecker.checkNotNull;

public abstract class AbstractCommandGenerator implements CommandGenerator {
    protected final Protocol protocol;

    public AbstractCommandGenerator(Protocol protocol) {
        checkNotNull(protocol, "protocol");
        this.protocol = protocol;
    }

    public abstract String toString();
}
