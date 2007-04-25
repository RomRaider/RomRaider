package enginuity.ramtune.test.command.generator;

import enginuity.io.protocol.Protocol;
import static enginuity.util.ParamChecker.checkGreaterThanZero;
import static enginuity.util.ParamChecker.checkNotNullOrEmpty;

import java.math.BigInteger;
import java.util.ArrayList;
import static java.util.Arrays.asList;
import java.util.List;

public final class ReadCommandGenerator extends AbstractCommandGenerator {
    private static final int INCREMENT_SIZE = 32;

    public ReadCommandGenerator(Protocol protocol) {
        super(protocol);
    }

    public List<byte[]> createCommands(byte[] data, byte[] address, int length) {
        checkNotNullOrEmpty(address, "address");
        checkGreaterThanZero(length, "length");
        if (length == 1) {
            return asList(createCommandForAddress(address));
        } else {
            return createCommandsForRange(address, length);
        }
    }

    private byte[] createCommandForAddress(byte[] address) {
        return protocol.constructReadAddressRequest(new byte[][]{address});
    }

    private List<byte[]> createCommandsForRange(byte[] address, int length) {
        List<byte[]> commands = new ArrayList<byte[]>();
        byte[] readAddress = copy(address);
        int i = 0;
        while (i < length) {
            int readLength = (length - i) > INCREMENT_SIZE ? INCREMENT_SIZE : length - i;
            if (readLength == 1) {
                commands.add(createCommandForAddress(readAddress));
            } else {
                commands.add(protocol.constructReadMemoryRequest(readAddress, readLength));
            }
            i += INCREMENT_SIZE;
            System.arraycopy(incrementAddress(readAddress, readLength), 0, readAddress, 0, readAddress.length);
        }
        return commands;
    }

    private byte[] copy(byte[] bytes) {
        byte[] bytes2 = new byte[bytes.length];
        System.arraycopy(bytes, 0, bytes2, 0, bytes2.length);
        return bytes2;
    }

    private byte[] incrementAddress(byte[] address, int increment) {
        return new BigInteger(address).add(new BigInteger(String.valueOf(increment))).toByteArray();
    }

    public String toString() {
        return "Read";
    }
}
