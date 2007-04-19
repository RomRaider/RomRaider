package enginuity.ramtune.test.command;

public interface Command {

    byte[] constructCommandRequest(byte[] address, byte[] data);
}
