package enginuity.ramtune.test.command.executor;

import enginuity.io.connection.ConnectionProperties;
import enginuity.io.connection.EcuConnection;
import enginuity.io.connection.EcuConnectionImpl;
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
            return ecuConnection.send(command);
        } finally {
            ecuConnection.close();
        }
    }

}
