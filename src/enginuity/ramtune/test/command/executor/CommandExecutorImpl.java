package enginuity.ramtune.test.command.executor;

import enginuity.io.connection.ConnectionProperties;
import enginuity.io.connection.EcuConnection;
import enginuity.io.connection.EcuConnectionImpl;
import static enginuity.util.HexUtil.asBytes;
import static enginuity.util.HexUtil.asHex;
import static enginuity.util.ParamChecker.checkNotNull;
import static enginuity.util.ParamChecker.checkNotNullOrEmpty;

public final class CommandExecutorImpl implements CommandExecutor {
    private final ConnectionProperties connectionProperties;
    private final String port;

    public CommandExecutorImpl(ConnectionProperties connectionProperties, String port) {
        checkNotNull(connectionProperties);
        checkNotNullOrEmpty(port, "port");
        this.connectionProperties = connectionProperties;
        this.port = port;
    }

    public byte[] executeCommand(byte[] command) {
        EcuConnection ecuConnection = new EcuConnectionImpl(connectionProperties, port);
        try {
            byte[] result = ecuConnection.send(command);
            return stripCommandHeader(result, command);
        } finally {
            ecuConnection.close();
        }
    }

    private byte[] stripCommandHeader(byte[] result, byte[] command) {
        String resultHex = asHex(result);
        String commandHex = asHex(command);
        if (resultHex.startsWith(commandHex)) {
            return asBytes(resultHex.replaceFirst(commandHex, ""));
        } else {
            return result;
        }
    }
}
