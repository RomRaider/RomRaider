package enginuity.ramtune.test.command.generator;

public interface CommandGenerator {

    byte[] createCommand(byte[] address, byte[] data, int length);
}
