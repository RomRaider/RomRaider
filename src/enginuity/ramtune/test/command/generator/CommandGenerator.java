package enginuity.ramtune.test.command.generator;

import enginuity.ramtune.test.command.Command;

public interface CommandGenerator {

    Command createCommand(byte[] address, byte[] data);
}
