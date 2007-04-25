package enginuity.ramtune.test.command.generator;

import java.util.List;

public interface CommandGenerator {

    List<byte[]> createCommands(byte[] data, byte[] address, int length);

}
