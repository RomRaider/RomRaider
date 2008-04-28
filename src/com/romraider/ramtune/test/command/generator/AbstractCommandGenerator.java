package com.romraider.ramtune.test.command.generator;

import com.romraider.io.protocol.Protocol;
import static com.romraider.util.ParamChecker.checkNotNull;

public abstract class AbstractCommandGenerator implements CommandGenerator {
    protected final Protocol protocol;

    public AbstractCommandGenerator(Protocol protocol) {
        checkNotNull(protocol, "protocol");
        this.protocol = protocol;
    }

    public abstract String toString();
}
